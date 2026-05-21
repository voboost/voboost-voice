package ru.voboost.voice.engine.sherpa

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean // Sherpa-ONNX импорты
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import ru.voboost.voice.core.BaseSpeechSynthesis

/**
 * Реализация синтеза речи через Sherpa-ONNX TTS
 * Поддерживает офлайн-синтез с моделями VITS, Piper, Kokoro
 *
 * Документация: https://k2-fsa.github.io/sherpa/onnx/tts/
 *
 * Поддерживает загрузку моделей с SD-карты (внешнего хранилища)
 */
class SherpaSpeechSynthesis(private val modelPath: String, private val speakerId: Int) :
        BaseSpeechSynthesis() {

    companion object {
        const val TAG = "SherpaSynthesis"
        private const val DEFAULT_SAMPLE_RATE = 24000
    }

    @Volatile private var isInitialized = false

    @Volatile private var rate = 1.0f

    @Volatile private var pitch = 1.0f

    @Volatile private var sampleRate = DEFAULT_SAMPLE_RATE
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isPlaying = AtomicBoolean(false)
    private var currentJob: Job? = null
    private var offlineTts: OfflineTts? = null

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Initializing Sherpa-ONNX TTS...")
                Log.i(TAG, "Model path: $modelPath")
                Log.i(TAG, "Speaker ID: $speakerId")

                val modelFile = File(modelPath)

                if (!modelFile.exists()) {
                    throw IllegalStateException("Sherpa TTS model not found at: ${modelPath}\n" + "Please copy the model using copy-sherpa-models.bat script")
                }

                // Инициализация Sherpa-ONNX TTS
                offlineTts = createTts(modelPath)
                sampleRate = offlineTts?.sampleRate ?: DEFAULT_SAMPLE_RATE

                isInitialized = true
                Log.i(TAG, "Sherpa-ONNX TTS initialized successfully! (sample rate: $sampleRate)")

            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Sherpa-ONNX TTS", e)
                isInitialized = false
                throw e
            }
        }
    }

    /**
     * Создать конфигурацию для OfflineTts
     */
    private fun createTts(modelPath: String): OfflineTts {
        val modelFile = File(modelPath)

        // Если это файл .onnx
        val modelPathStr = if (modelFile.isFile && modelFile.extension == "onnx") {
            modelFile.absolutePath
        }
        else { // Если это директория, ищем модель
            val onnxFile = modelFile.listFiles { f -> f.extension == "onnx" }?.firstOrNull()
            onnxFile?.absolutePath ?: modelFile.absolutePath
        }

        Log.d(TAG, "TTS model path: $modelPathStr")

        // Для Sherpa-ONNX Piper моделей (ru_RU-ruslan-medium) используем tokens.txt + espeak
        // modelPath = /data/.../sherpa/tts-ru-model
        val modelDir = File(modelPath)
        val tokensFile = File(modelDir, "tokens.txt")
        val espeakDir = File(modelDir, "espeak-ng-data")

        Log.d(TAG, "Model dir: ${modelDir.absolutePath}")
        Log.d(TAG, "Tokens file: ${tokensFile.absolutePath}, exists: ${tokensFile.exists()}")
        Log.d(TAG, "eSpeak dir: ${espeakDir.absolutePath}, exists: ${espeakDir.exists()}")

        // Создаём конфигурацию VITS модели для Sherpa-ONNX Piper
        // Передаём data_dir для eSpeak-ng (требуется для phonemization)
        val vitsModelConfig = OfflineTtsVitsModelConfig.Builder()
            .setModel(modelPathStr)
            .setTokens(if (tokensFile.exists()) tokensFile.absolutePath else "")
            .setDataDir(if (espeakDir.exists()) espeakDir.absolutePath else "")
            .setNoiseScale(0.667f)
            .setNoiseScaleW(0.8f)
            .setLengthScale(1.0f)
            .build()

        // Создаём обёртку модели
        val ttsModelConfig = OfflineTtsModelConfig.Builder()
            .setVits(vitsModelConfig)
            .setNumThreads(2)
            .setProvider("cpu")  // < CPU вместо NNAPI для совместимости
            .setDebug(true)
            .build()

        // Создаём основную конфигурацию TTS
        val ttsConfig = OfflineTtsConfig.Builder().setModel(ttsModelConfig).build()

        return OfflineTts(ttsConfig)
    }

    override fun isReady(): Boolean = isInitialized

    override fun speak(text: String) {
        if (text.isBlank()) {
            Log.w(TAG, "Empty text, skipping TTS")
            onSpeechFinish()
            return
        }

        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized")
            onSpeechFinish()
            return
        }

        Log.d(TAG, "Speaking: $text")

        // Запустить воспроизведение если не играет
        if (!isPlaying.get()) {
            currentJob = scope.launch {
                playSpeech(text)
            }
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

    override fun isAvailable(): Boolean = isInitialized && offlineTts != null

    override fun shutdown() {
        Log.i(TAG, "Shutting down...")
        stop()
        scope.cancel()
        offlineTts?.release()
        offlineTts = null
        isInitialized = false
        Log.i(TAG, "Shutdown complete")
    }

    private suspend fun playSpeech(text: String) {
        withContext(Dispatchers.IO) {
            try {
                val ttsInstance = offlineTts
                                  ?: throw IllegalStateException("TTS not initialized") // Сгенерировать аудио через Sherpa-ONNX
                // API: generate(text: String, speakerId: Int, speed: Float): GeneratedAudio
                val audio = ttsInstance.generate(text, speakerId, rate)

                if (audio != null && audio.samples.isNotEmpty()) {
                    Log.d(TAG,
                          "Audio generated: ${audio.samples.size} samples, sample rate: ${audio.sampleRate}")
                    playAudioData(audio.samples)
                }
                else {
                    Log.w(TAG, "Generated empty audio for: $text")
                }
            }
            catch (e: Exception) {
                Log.e(TAG, "Error playing speech", e)
            }
            finally {
                onSpeechFinish()
            }
        }
    }

    @SuppressLint("MissingPermission") private suspend fun playAudioData(audio: FloatArray) {
        withContext(Dispatchers.IO) {
            val bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                                                         AudioFormat.CHANNEL_OUT_MONO,
                                                         AudioFormat.ENCODING_PCM_FLOAT)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .build()

            val audioTrack = AudioTrack(audioAttributes,
                                        audioFormat,
                                        bufferSize,
                                        AudioTrack.MODE_STREAM,
                                        AudioManager.AUDIO_SESSION_ID_GENERATE)

            try {
                audioTrack.play()

                val chunkSize = bufferSize / 4  // Float = 4 байта
                var offset = 0

                // ? Используем coroutineContext.isActive для проверки отмены
                while (offset < audio.size && coroutineContext.isActive) {
                    val remaining = audio.size - offset
                    val size = minOf(chunkSize, remaining)

                    audioTrack.write(audio, offset, size, AudioTrack.WRITE_BLOCKING)
                    offset += size
                }

                audioTrack.stop()
                audioTrack.flush()
                Log.d(TAG, "Audio playback completed")

            }
            finally {
                audioTrack.release()
                onSpeechFinish()
            }
        }
    }
}


