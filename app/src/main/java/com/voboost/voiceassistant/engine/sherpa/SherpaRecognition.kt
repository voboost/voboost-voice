package com.voboost.voiceassistant.engine.sherpa

import android.content.Context
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.voboost.voiceassistant.core.SpeechRecognition
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine // Sherpa-ONNX импорты
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineStream
import com.k2fsa.sherpa.onnx.OfflineRecognizerResult

/**
 * Реализация распознавания речи через Sherpa-ONNX
 * Поддерживает офлайн-распознавание с ключевыми фразами
 *
 * Документация: https://k2-fsa.github.io/sherpa/onnx/
 */
class SherpaRecognition(private val context: Context,
                        private val modelPath: String,
                        private var keywords: MutableList<String> = mutableListOf()) :
        SpeechRecognition {

    companion object {
        private const val TAG = "SherpaRecognition"
        private const val SAMPLE_RATE = 16000
        private const val BUFFER_SIZE = 4096
        private const val KEYWORD_TIMEOUT_MS = 30000L
        private const val COMMAND_TIMEOUT_MS = 5000L
    }

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isRunning = false

    @Volatile
    private var isListeningForKeyword = false

    @Volatile
    private var isProcessingCommand = false

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recordJob: Job? = null

    // Sherpa-ONNX recognizer
    private var recognizer: OfflineRecognizer? = null
    private var stream: OfflineStream? = null

    private val audioQueue = ConcurrentLinkedQueue<ShortArray>()
    private var activationKeywords: List<String> = emptyList()

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Initializing Sherpa-ONNX recognition...")
                Log.i(TAG, "Model path: $modelPath")
                Log.i(TAG, "Keywords: $keywords")

                // Копирование модели из assets если не существует
                val modelDir = File(modelPath)
                if (!modelDir.exists()) {
                    Log.i(TAG, "Model not found, copying from assets...")
                    copyAssetFolder(context.assets, "sherpa/asr-ru-model", modelDir)
                }
                
                if (!modelDir.exists()) {
                    throw IllegalStateException("ASR model not found: $modelPath")
                }

                // Инициализация Sherpa-ONNX OnlineRecognizer
                recognizer = createOfflineRecognizer(modelPath)

                isInitialized = true
                Log.i(TAG, "Sherpa-ONNX recognition initialized successfully!")

            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Sherpa-ONNX", e)
                isInitialized = false
                throw e
            }
        }
    }

    /**
     * Копировать папку из assets во внутреннюю память
     */
    private fun copyAssetFolder(assetManager: android.content.res.AssetManager, assetPath: String, targetDir: File) {
        try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val assets = assetManager.list(assetPath)
            if (assets.isNullOrEmpty()) {
                // Это файл, копируем
                copyAssetFile(assetManager, assetPath, targetDir.parentFile!!.absolutePath + "/" + targetDir.name)
            } else {
                // Это папка, рекурсивно копируем содержимое
                for (filename in assets) {
                    val fullAssetPath = if (assetPath.isEmpty()) filename else "$assetPath/$filename"
                    val targetFile = File(targetDir, filename)
                    copyAssetFolder(assetManager, fullAssetPath, targetFile)
                }
            }
            Log.i(TAG, "Copied $assetPath to $targetDir")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy asset: $assetPath", e)
            throw e
        }
    }

    /**
     * Копировать файл из assets
     */
    private fun copyAssetFile(assetManager: android.content.res.AssetManager, assetPath: String, targetPath: String) {
        val targetFile = File(targetPath)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        
        assetManager.open(assetPath).use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Создать конфигурацию для OnlineRecognizer
     */
    private fun createOfflineRecognizer(modelPath: String): OfflineRecognizer { // Путь к файлам модели
        val encoderPath = File(modelPath, "encoder.onnx").absolutePath
        val decoderPath = File(modelPath, "decoder.onnx").absolutePath
        val joinerPath = File(modelPath, "joiner.onnx").absolutePath
        val tokensPath = File(modelPath, "tokens.txt").absolutePath

        Log.d(TAG, "Encoder: $encoderPath")
        Log.d(TAG, "Decoder: $decoderPath")
        Log.d(TAG, "Joiner: $joinerPath")
        Log.d(TAG, "Tokens: $tokensPath")

        // Создаём конфигурацию транседера
        val transducerConfig =
            OfflineTransducerModelConfig.Builder()
                .setEncoder(encoderPath)
                .setDecoder(decoderPath)
                .setJoiner(joinerPath)
                .build()

        // Создаём конфигурацию модели
        val modelConfig =
            OfflineModelConfig.Builder().setTransducer(transducerConfig).setTokens(tokensPath)
                .setNumThreads(2)
                .setProvider("cpu")  // ← CPU вместо NNAPI для совместимости
                .setDebug(true)
                .build()

        // Создаём конфигурацию фичей
        val featureConfig =
            FeatureConfig.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setFeatureDim(80)
                .build()

        // Создаём основную конфигурацию распознавания
        val recognizerConfig = OfflineRecognizerConfig.Builder().setFeatureConfig(featureConfig)
            .setOfflineModelConfig(modelConfig).setMaxActivePaths(4) //.setEnableEndpoint(false)
            .build()

        return OfflineRecognizer(recognizerConfig)
    }

    override fun isReady(): Boolean = isInitialized

    override suspend fun startKeywordSpotting(onKeywordDetected: () -> Unit,
                                              onError: suspend (String) -> Unit) {
        if (!isInitialized) {
            Log.e(TAG, "Not initialized, call initialize() first")
            return
        }

        if (isRunning) {
            Log.w(TAG, "Already running, skipping")
            return
        }

        Log.d(TAG, "Starting keyword spotting...")
        isListeningForKeyword = true
        isProcessingCommand = false
        isRunning = true

        // Создать новый стрим для распознавания
        stream = recognizer?.createStream()

        recordJob = scope.launch {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
                Log.i(TAG, "Keyword spotting started")

                continuousRecording(onKeywordDetected = onKeywordDetected,
                                    onTimeout = { /* timeout не нужен для keyword spotting */ },
                                    timeoutMs = KEYWORD_TIMEOUT_MS)
            }
            catch (e: Exception) {
                Log.e(TAG, "Keyword spotting error", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    override fun startCommandListening(onCommandReceived: (String) -> Unit,
                                       onError: (String) -> Unit,
                                       onTimeout: suspend () -> Unit) {
        if (!isInitialized) {
            Log.e(TAG, "Not initialized")
            return
        }

        Log.d(TAG, "Starting command listening...")
        isListeningForKeyword = false
        isProcessingCommand = true
        isRunning = true

        // Создать новый стрим
        stream = recognizer?.createStream()

        scope.launch {
            try {
                Log.i(TAG, "Listening for command...")
                continuousRecording(onCommandReceived = onCommandReceived,
                                    onError = onError,
                                    onTimeout = onTimeout,
                                    timeoutMs = COMMAND_TIMEOUT_MS)
            }
            catch (e: Exception) {
                Log.e(TAG, "Command listening error", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    override suspend fun listenForCommand(timeout: Long): String {
        Log.d(TAG, "listenForCommand called, timeout: ${timeout}ms")

        return withTimeoutOrNull(timeout) {
            suspendCoroutine<String> { continuation ->
                var resultReceived = false

                scope.launch {
                    try {
                        isListeningForKeyword = false
                        isProcessingCommand = true
                        isRunning = true

                        // Создать новый стрим
                        stream = recognizer?.createStream()

                        continuousRecording(onCommandReceived = { text ->
                            if (!resultReceived) {
                                resultReceived = true
                                Log.i(TAG, "listenForCommand: received '$text'")
                                continuation.resume(text)
                            }
                        }, onError = { error ->
                            if (!resultReceived) {
                                resultReceived = true
                                Log.w(TAG, "listenForCommand: error '$error'")
                                continuation.resume("")
                            }
                        }, onTimeout = {
                            if (!resultReceived) {
                                resultReceived = true
                                Log.d(TAG, "listenForCommand: timeout")
                                continuation.resume("")
                            }
                        }, timeoutMs = timeout)
                    }
                    catch (e: Exception) {
                        if (!resultReceived) {
                            resultReceived = true
                            Log.e(TAG, "listenForCommand: exception", e)
                            continuation.resume("")
                        }
                    }
                }
            }
        } ?: run {
            Log.w(TAG, "listenForCommand: timeout")
            ""
        }
    }

    override fun setActivationKeywords(keywords: List<String>) {
        this.activationKeywords = keywords
        Log.d(TAG, "Activation keywords updated: $keywords")
    }

    override fun stop() {
        Log.d(TAG, "Stopping recognition...")
        isRunning = false
        isListeningForKeyword = false
        isProcessingCommand = false
        recordJob?.cancel()
        stream = null
    }

    override fun shutdown() {
        Log.i(TAG, "Shutting down...")
        stop()
        scope.cancel() // Очистка очереди
        while (audioQueue.poll() != null) { /* discard */
        }
        recognizer?.release()
        recognizer = null
        isInitialized = false
        Log.i(TAG, "Shutdown complete")
    }

    // ==================== ВНУТРЕННИЕ МЕТОДЫ ====================

    private suspend fun continuousRecording(onKeywordDetected: (() -> Unit)? = null,
                                            onCommandReceived: ((String) -> Unit)? = null,
                                            onError: (suspend (String) -> Unit)? = null,
                                            onTimeout: suspend () -> Unit,
                                            timeoutMs: Long) {
        val audioRecord = createAudioRecord()
        val recognizer = this.recognizer ?: throw IllegalStateException("Recognizer is null")

        try {
            audioRecord.startRecording()
            Log.d(TAG, "AudioRecord started")

            val buffer = ByteArray(BUFFER_SIZE)
            var startTime = System.currentTimeMillis()
            var silenceDuration = 0L
            var lastSoundTime = System.currentTimeMillis()
            var lastResult = ""

            while (isRunning) { // Проверка таймаута
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    if (isListeningForKeyword) {
                        Log.i(TAG, "Keyword timeout - restarting...")
                        startTime = System.currentTimeMillis() // Пересоздать стрим
                        stream = recognizer.createStream()
                        continue
                    }

                    Log.d(TAG, "Timeout reached")
                    if (isProcessingCommand) {
                        onTimeout()
                    }
                    break
                }

                // Чтение аудио данных
                val read = audioRecord.read(buffer, 0, BUFFER_SIZE)

                if (read > 0) {
                    lastSoundTime = System.currentTimeMillis()
                    silenceDuration = 0

                    // Преобразовать в ShortArray для Sherpa
                    val samples = ShortArray(read / 2)
                    for (i in samples.indices) {
                        samples[i] =
                            ((buffer[i * 2].toInt() and 0xFF) or (buffer[i * 2 + 1].toInt() shl 8)).toShort()
                    }

                    // Передать данные в Sherpa-ONNX
                    val currentStream =
                        stream ?: continue // acceptWaveform принимает FloatArray и Int sampleRate
                    val floatSamples =
                        FloatArray(samples.size) { samples[it].toFloat() / Short.MAX_VALUE }
                    currentStream.acceptWaveform(floatSamples, SAMPLE_RATE)

                    // Проверить есть ли результат
                    //                    while (recognizer.isReady(currentStream)) {
                    //                        recognizer.decode(currentStream)
                    //                    }

                    recognizer.decode(currentStream)

                    // Получить текущий результат
                    val result: OfflineRecognizerResult = recognizer.getResult(currentStream)
                    val resultText = result.text
                    if (resultText.isNotEmpty() && resultText != lastResult) {
                        lastResult = resultText
                        Log.d(TAG, "Partial result: $resultText")

                        // Проверка ключевых фраз
                        if (isListeningForKeyword && onKeywordDetected != null) {
                            if (activationKeywords.any {
                                        resultText.contains(it,
                                                            ignoreCase = true)
                                    }) {
                                Log.i(TAG, "🎯 KEYWORD DETECTED: $resultText")
                                withContext(Dispatchers.Main) {
                                    onKeywordDetected()
                                }
                                break
                            }
                        } // Проверка команды
                        else if (isProcessingCommand && onCommandReceived != null) {
                            Log.i(TAG, "📝 COMMAND RECEIVED: $resultText")
                            withContext(Dispatchers.Main) {
                                onCommandReceived(resultText)
                            }
                            break
                        }
                    }
                }
                else if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "AudioRecord read error: $read")
                    throw IllegalStateException("AudioRecord read failed: $read")
                }

                // Детекция тишины
                if (System.currentTimeMillis() - lastSoundTime > 3000) {
                    silenceDuration += 100
                    if (silenceDuration > 5000 && isListeningForKeyword) {
                        Log.d(TAG, "Silence detected, resetting")
                        stream = recognizer.createStream()
                        silenceDuration = 0
                        lastSoundTime = System.currentTimeMillis()
                    }
                }
            }

            // Финальный результат
            if (isProcessingCommand && onCommandReceived != null) {
                val currentStream = stream ?: return
                val finalResult = recognizer.getResult(currentStream)
                if (finalResult.text.isNotEmpty()) {
                    Log.i(TAG, "Final command: ${finalResult.text}")
                    withContext(Dispatchers.Main) {
                        onCommandReceived(finalResult.text)
                    }
                }
            }

        }
        finally {
            try {
                audioRecord.stop()
                audioRecord.release()
                Log.d(TAG, "AudioRecord stopped and released")
            }
            catch (e: Exception) {
                Log.w(TAG, "Error stopping AudioRecord", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecord(): AudioRecord {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                                                      AudioFormat.CHANNEL_IN_MONO,
                                                      AudioFormat.ENCODING_PCM_16BIT)

        return AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                           SAMPLE_RATE,
                           AudioFormat.CHANNEL_IN_MONO,
                           AudioFormat.ENCODING_PCM_16BIT,
                           maxOf(BUFFER_SIZE, bufferSize * 2))
    }
}
