package ru.voboost.voiceassistant.engine.sherpa

import android.content.Context
import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
// Sherpa-ONNX импорты
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig

/**
 * Реализация синтеза речи через Sherpa-ONNX TTS
 * Поддерживает офлайн-синтез с моделями VITS, Piper, Kokoro
 *
 * Документация: https://k2-fsa.github.io/sherpa/onnx/tts/
 * 
 * Поддерживает загрузку моделей с SD-карты (внешнего хранилища)
 */
class SherpaSynthesis(
    private val context: Context,
    modelPath: String? = null,  // null = автоопределение (SD-карта → внутренняя память → assets)
    private val speakerId: Int = 0
) : ISpeechSynthesis {

    companion object {
        const val TAG = "SherpaSynthesis"
        private const val DEFAULT_MODEL_PATH = "models/sherpa/tts-ru-model"
        private const val DEFAULT_SAMPLE_RATE = 24000
    }

    @Volatile
    private var isInitialized = false
    @Volatile
    private var rate = 1.0f
    @Volatile
    private var pitch = 1.0f
    @Volatile
    private var sampleRate = DEFAULT_SAMPLE_RATE

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val speechQueue = ConcurrentLinkedQueue<SpeechItem>()
    private val isPlaying = AtomicBoolean(false)
    private var currentJob: Job? = null

    // Sherpa-ONNX TTS generator
    private var tts: OfflineTts? = null
    
    // Путь к модели (определяется при инициализации)
    private val modelPath: String

    init {
        // Определяем путь к модели
        this.modelPath = resolveModelPath(modelPath)
        Log.i(TAG, "Sherpa TTS model path: ${this.modelPath}")
    }
    
    /**
     * Определить путь к модели
     * Используем внутреннюю память для надёжности (системные приложения)
     */
    private fun resolveModelPath(providedPath: String?): String {
        // Если путь передан явно - используем его
        if (providedPath != null) {
            val providedFile = File(providedPath)
            if (providedFile.exists() && isTtsModelComplete(providedFile)) {
                Log.i(TAG, "Using provided TTS model path: $providedPath")
                return providedPath
            }
        }

        // Путь во внутренней памяти (надёжно для системных приложений)
        val internalModelDir = File(context.filesDir, DEFAULT_MODEL_PATH)
        if (internalModelDir.exists() && isTtsModelComplete(internalModelDir)) {
            Log.i(TAG, "Using TTS model from internal storage: ${internalModelDir.absolutePath}")
            return internalModelDir.absolutePath
        }

        // Модели нет - выбрасываем исключение
        throw IllegalStateException(
            "Sherpa TTS model not found at: ${internalModelDir.absolutePath}\n" +
            "Please copy the model using copy-vosk-to-internal.bat script"
        )
    }
    
    /**
     * Проверить что TTS модель полная
     */
    private fun isTtsModelComplete(modelDir: File): Boolean {
        if (!modelDir.isDirectory) return false
        // Проверяем наличие ключевых файлов Piper TTS модели
        val requiredFiles = listOf("ru_RU-ruslan-medium.onnx", "tokens.txt")
        return requiredFiles.all { File(modelDir, it).exists() }
    }

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Initializing Sherpa-ONNX TTS...")
                Log.i(TAG, "Model path: $modelPath")
                Log.i(TAG, "Speaker ID: $speakerId")

                // Копирование модели из assets если не существует
                val modelFile = File(modelPath)
                if (!modelFile.exists()) {
                    Log.i(TAG, "TTS model not found, copying from assets...")
                    copyTtsModelFromAssets()
                }
                
                if (!modelFile.exists()) {
                    throw IllegalStateException("TTS model not found: $modelPath")
                }

                // Инициализация Sherpa-ONNX TTS
                tts = createOfflineTts(modelPath)
                sampleRate = tts?.sampleRate ?: DEFAULT_SAMPLE_RATE

                isInitialized = true
                Log.i(TAG, "Sherpa-ONNX TTS initialized successfully! (sample rate: $sampleRate)")

                // Обработать очередь если есть
                processQueue()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Sherpa-ONNX TTS", e)
                isInitialized = false
                throw e
            }
        }
    }

    /**
     * Копировать TTS модель из assets
     */
    private suspend fun copyTtsModelFromAssets() = withContext(Dispatchers.IO) {
        try {
            val targetDir = File(modelPath).parentFile ?: return@withContext
            
            // Создать директорию если не существует
            if (!targetDir.exists()) {
                targetDir.mkdirs()
                Log.i(TAG, "Created directory: ${targetDir.absolutePath}")
            }
            
            var archiveFile = File(targetDir, "tts-ru-model.tar.gz.bin")
            
            // Проверка, существует ли уже распакованная модель
            if (File(modelPath, "ru_RU-ruslan-medium.onnx").exists()) {
                Log.i(TAG, "TTS model already exists, skipping copy")
                return@withContext
            }
            
            Log.i(TAG, "Copying TTS model archive from assets...")
            
            // Копировать архив из assets
            context.assets.open("sherpa/tts-ru-model.tar.gz.bin").use { input ->
                archiveFile.outputStream().use { output ->
                    input.copyTo(output)
                    Log.i(TAG, "Archive copied: ${archiveFile.length()} bytes")
                }
            }
            
            // Переименовать .bin в .tar.gz
            val tarGzFile = File(targetDir, "tts-ru-model.tar.gz")
            archiveFile.renameTo(tarGzFile)
            archiveFile = tarGzFile
            
            // Распаковать архив
            Log.i(TAG, "Extracting archive...")
            val process = Runtime.getRuntime().exec(arrayOf("tar", "-xzf", archiveFile.absolutePath, "-C", targetDir.absolutePath))
            process.waitFor()
            
            // Удалить архив
            archiveFile.delete()
            Log.i(TAG, "TTS model extracted to $modelPath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy/extract TTS model", e)
            throw e
        }
    }

    /**
     * Создать конфигурацию для OfflineTts
     */
    private fun createOfflineTts(modelPath: String): OfflineTts {
        val modelFile = File(modelPath)

        // Если это файл .onnx
        val modelPathStr = if (modelFile.isFile && modelFile.extension == "onnx") {
            modelFile.absolutePath
        } else {
            // Если это директория, ищем модель
            val onnxFile = modelFile.listFiles { f -> f.extension == "onnx" }?.firstOrNull()
            onnxFile?.absolutePath ?: modelFile.absolutePath
        }

        Log.d(TAG, "TTS model path: $modelPathStr")

        // Для Sherpa-ONNX Piper моделей (ru_RU-ruslan-medium) используем tokens.txt
        // modelPath = /data/.../sherpa/tts-ru-model
        val modelDir = File(modelPath)
        val tokensFile = File(modelDir, "tokens.txt")
        val espeakDir = File(modelDir, "espeak-ng-data")
        
        Log.d(TAG, "Model dir: ${modelDir.absolutePath}")
        Log.d(TAG, "Tokens file: ${tokensFile.absolutePath}, exists: ${tokensFile.exists()}")
        Log.d(TAG, "eSpeak dir: ${espeakDir.absolutePath}, exists: ${espeakDir.exists()}")
        
        // Создаём конфигурацию VITS модели для Sherpa-ONNX Piper
        val vitsModelConfig = OfflineTtsVitsModelConfig.Builder()
            .setModel(modelPathStr)
            .setTokens(if (tokensFile.exists()) tokensFile.absolutePath else "")
            .setDataDir(if (espeakDir.exists()) espeakDir.absolutePath else "")
            .setNoiseScale(0.667f)
            .setNoiseScaleW(0.8f)
            .setLengthScale(1.0f)
            .build()

        // Создаём обёртку модели
        val ttsModelConfig = com.k2fsa.sherpa.onnx.OfflineTtsModelConfig.Builder()
            .setVits(vitsModelConfig)
            .setNumThreads(2)
            .setProvider("cpu")  // ← CPU вместо NNAPI для совместимости
            .setDebug(true)
            .build()

        // Создаём основную конфигурацию TTS
        val ttsConfig = OfflineTtsConfig.Builder()
            .setModel(ttsModelConfig)
            .build()

        return OfflineTts(ttsConfig)
    }

    override fun isReady(): Boolean = isInitialized

    override fun speak(text: String, onCompletion: (() -> Unit)?) {
        if (text.isBlank()) {
            Log.w(TAG, "Empty text, skipping TTS")
            onCompletion?.invoke()
            return
        }

        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, adding to queue")
            speechQueue.add(SpeechItem(text, onCompletion))
            return
        }

        Log.d(TAG, "Speaking: $text")

        // Добавить в очередь
        speechQueue.add(SpeechItem(text, onCompletion))

        // Запустить воспроизведение если не играет
        if (!isPlaying.get()) {
            processQueue()
        }
    }

    override fun stop() {
        Log.d(TAG, "Stopping TTS playback")
        currentJob?.cancel()
        isPlaying.set(false)
    }

    override fun setRate(rate: Float) {
        this.rate = rate.coerceIn(0.5f, 2.0f)
        Log.d(TAG, "Speech rate set to: $rate")
    }

    override fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.5f, 2.0f)
        Log.d(TAG, "Speech pitch set to: $pitch")
    }

    override fun isAvailable(): Boolean = isInitialized && tts != null

    override fun clearQueue() {
        Log.d(TAG, "Clearing speech queue")
        speechQueue.clear()
        stop()
    }

    override fun shutdown() {
        Log.i(TAG, "Shutting down...")
        stop()
        clearQueue()
        scope.cancel()
        tts?.release()
        tts = null
        isInitialized = false
        Log.i(TAG, "Shutdown complete")
    }

    // ==================== ВНУТРЕННИЕ МЕТОДЫ ====================

    private fun processQueue() {
        if (isPlaying.getAndSet(true)) return  // Уже играет

        currentJob = scope.launch {
            try {
                while (speechQueue.isNotEmpty() && isActive) {
                    val item = speechQueue.poll() ?: break
                    playSpeech(item.text, item.onCompletion)
                }
            } finally {
                isPlaying.set(false)
            }
        }
    }

    private suspend fun playSpeech(text: String, onCompletion: (() -> Unit)?) {
        withContext(Dispatchers.IO) {
            try {
                val ttsInstance = tts ?: throw IllegalStateException("TTS not initialized")

                Log.d(TAG, "Generating speech for: $text")

                // Сгенерировать аудио через Sherpa-ONNX
                // API: generate(text: String, speakerId: Int, speed: Float): GeneratedAudio
                val audio = ttsInstance.generate(text, speakerId, rate)

                if (audio != null && audio.samples.isNotEmpty()) {
                    Log.d(TAG, "Audio generated: ${audio.samples.size} samples, sample rate: ${audio.sampleRate}")
                    playAudioData(audio.samples)
                } else {
                    Log.w(TAG, "Generated empty audio for: $text")
                }

                onCompletion?.invoke()

            } catch (e: Exception) {
                Log.e(TAG, "Error playing speech", e)
                onCompletion?.invoke()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun playAudioData(audio: FloatArray) {
        withContext(Dispatchers.IO) {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
            )

            // ✅ Используем USAGE_ASSISTANT как в оригинальном QGTtsService
            // Это отдельный аудиоканал для голосового помощника
            // Громкость не приглушает музыку
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)  // ✅ Голосовой канал
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .build()

            val audioTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            try {
                audioTrack.play()

                // Воспроизвести данные
                val chunkSize = bufferSize / 4  // Float = 4 байта
                var offset = 0

                while (offset < audio.size && isActive) {
                    val remaining = audio.size - offset
                    val size = minOf(chunkSize, remaining.toInt())

                    audioTrack.write(audio, offset, size,
                                     AudioTrack.WRITE_BLOCKING)
                    offset += size
                }

                // Дождаться окончания воспроизведения
                audioTrack.stop()
                audioTrack.flush()

                Log.d(TAG, "Audio playback completed")

            } finally {
                audioTrack.release()
            }
        }
    }

    private data class SpeechItem(
        val text: String,
        val onCompletion: (() -> Unit)?
    )
}
