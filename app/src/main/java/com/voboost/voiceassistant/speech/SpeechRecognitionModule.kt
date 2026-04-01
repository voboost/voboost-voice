package com.voboost.voiceassistant.speech

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat
import com.voboost.voiceassistant.config.ConfigManager
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

/**
 * Модуль распознавания речи
 * Поддерживает постоянное прослушивание с детекцией ключевой фразы
 * 
 * Поддерживает загрузку моделей с SD-карты (внешнего хранилища)
 */
class SpeechRecognitionModule(
    private val context: Context,
    private val modelPath: String? = null  // Путь к модели (null = assets)
) {
    companion object {
        private const val TAG = "SpeechRecognition"
        private const val SAMPLE_RATE = 16000
        private const val BUFFER_SIZE = 4096
        private const val KEYWORD_TIMEOUT_MS = 30000L  // Максимум 30 сек ожидания ключевой фразы
        private const val COMMAND_TIMEOUT_MS = 5000L   // 5 сек на команду после активации
    }

    private val configManager = ConfigManager.getInstance(context)
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null

    @Volatile
    private var isRunning = false
    @Volatile
    private var isListeningForKeyword = false
    @Volatile
    private var isProcessingCommand = false

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recordJob: Job? = null

    init {
        if (hasRecordPermission()) {  // ← Проверка перед вызовом
            try {
                initializeVosk()
            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to initialize", e)
            }
        }
        else {
            Log.w(TAG, "No RECORD_AUDIO permission, initialization skipped")
        }
    }

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================

    private fun initializeVosk() {
        val config = configManager.getConfig()
        val modelName = config.speech.offline.model

        // Используем переданный путь (модель должна быть на SD-карте)
        val modelDir = if (modelPath != null) {
            File(modelPath)
        } else {
            context.filesDir.resolve("vosk/$modelName")
        }

        Log.i(TAG, "Initializing Vosk with model: $modelName, path: ${modelDir.absolutePath}")

        // Проверяем наличие модели
        if (!modelDir.exists()) {
            throw IllegalStateException(
                "Vosk model not found at: ${modelDir.absolutePath}\n" +
                "Please copy models to SD card using copy-models-to-sdcard.bat script"
            )
        }

        val model = Model(modelDir.absolutePath)
        recognizer = Recognizer(model, SAMPLE_RATE.toFloat())
        Log.i(TAG, "Vosk initialized successfully!")
    }


    //@RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @SuppressLint("MissingPermission")
    private fun ensureAudioRecordInitialized() {

        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) return  // Уже инициализирован

        if (!hasRecordPermission()) {
            Log.e(TAG, "RECORD_AUDIO permission not granted!")
            throw SecurityException("RECORD_AUDIO permission required")
        }

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                                                      AudioFormat.CHANNEL_IN_MONO,
                                                      AudioFormat.ENCODING_PCM_16BIT)

        audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                                  SAMPLE_RATE,
                                  AudioFormat.CHANNEL_IN_MONO,
                                  AudioFormat.ENCODING_PCM_16BIT,
                                  maxOf(BUFFER_SIZE,
                                        bufferSize * 2)) // Увеличенный буфер для стабильности

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord failed to initialize, state: ${audioRecord?.state}")
            audioRecord?.release()
            audioRecord = null
            throw IllegalStateException("AudioRecord initialization failed")
        }

        Log.i(TAG, "AudioRecord initialized successfully!")
    }

    // ==================== ПЕРЕКЛЮЧЕНИЕ РЕЖИМОВ ====================

    /**
     * Запустить постоянное прослушивание ключевой фразы
     */
    suspend fun startKeywordSpotting(onKeywordDetected: () -> Unit,
                                     onError: suspend (String) -> Unit) {
        ensureAudioRecordInitialized()

//        if (isRunning) {
//            Log.w(TAG, "Already running")
//            return
//        }
        Log.d(TAG, "startKeywordSpotting called")
        Log.d(TAG, "  isRunning=$isRunning")
        Log.d(TAG, "  isListeningForKeyword=$isListeningForKeyword")
        Log.d(TAG, "  isProcessingCommand=$isProcessingCommand")

        isListeningForKeyword = true
        isProcessingCommand = false
        isRunning = true

        recordJob = scope.launch {
            try { // Устанавливаем приоритет для аудио-потока (как в Baidu примере)
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                Log.i(TAG, "Starting keyword spotting...")
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

    /**
     * После детекции ключевой фразы - слушаем команду
     */
    fun startCommandListening(onCommandReceived: (String) -> Unit,
                              onError: (String) -> Unit,
                              onTimeout: suspend () -> Unit) {

        isListeningForKeyword = false
        isProcessingCommand = true
        isRunning = true

        scope.launch {
            try {
                Log.i(TAG, "Listening for command...")
                continuousRecording(onCommandReceived = onCommandReceived,
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

    // ==================== ОСНОВНОЙ ЦИКЛ ЗАПИСИ ====================
    // Непрерывная запись с анализом в реальном времени

    private suspend fun continuousRecording(onKeywordDetected: (() -> Unit)? = null,
                                            onCommandReceived: ((String) -> Unit)? = null,
                                            onTimeout: suspend () -> Unit,
                                            timeoutMs: Long) {
        val recorder = audioRecord ?: throw IllegalStateException("AudioRecord is null")
        val recognizer = this.recognizer ?: throw IllegalStateException("Recognizer is null")
        recognizer.reset()

        try {
            recorder.startRecording()
            Log.d(TAG, "AudioRecord started")

            val buffer = ByteArray(BUFFER_SIZE)
            var startTime = System.currentTimeMillis()
            var silenceDuration = 0L
            var lastSoundTime = System.currentTimeMillis()

            while (isRunning) { // Проверка таймаута

                if (System.currentTimeMillis() - startTime > timeoutMs) {

                    if (isListeningForKeyword) {
                        Log.i(TAG, "Keyword timeout - restarting...")
                        startTime = System.currentTimeMillis()  // Сброс таймера
                        recognizer.reset()
                        continue  // Продолжить слушать
                    }

                    Log.d(TAG, "Timeout reached")
                    if (isProcessingCommand) {
                        Log.d(TAG, "Timeout onTimeout")
                        onTimeout()
                    }
                    break
                }

                // Чтение аудио данных (блокирующее, но в IO потоке)
                val read = recorder.read(buffer, 0, BUFFER_SIZE)

                if (read > 0) {
                    lastSoundTime = System.currentTimeMillis()
                    silenceDuration = 0

                    // Передаём данные в Vosk
                    if (recognizer.acceptWaveForm(buffer, read)) {
                        val result = recognizer.result
                        val text = extractText(result)
                        Log.d(TAG, "Recognized: $text")

                        if (text.isNotEmpty()) {
                            if (isListeningForKeyword && onKeywordDetected != null) {
                                if (configManager.isActivationKeyword(text)) {
                                    Log.i(TAG, "🎯 KEYWORD DETECTED: $text")
                                    withContext(Dispatchers.Main) {
                                        onKeywordDetected()
                                    }
                                    break
                                }
                            }
                            else if (isProcessingCommand && onCommandReceived != null) {
                                Log.i(TAG, "📝 COMMAND RECEIVED: $text")
                                withContext(Dispatchers.Main) {
                                    onCommandReceived(text)
                                }
                                break
                            }
                        }
                    }

                    // Проверка частичных результатов (для логов/UI)
                    val partialResult = recognizer.partialResult
                    val partialText = extractText(partialResult)
                    if (partialText.isNotEmpty()) {
                        Log.v(TAG, "Partial: $partialText")
                    }

                }
                else if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "AudioRecord read error: $read")
                    throw IllegalStateException("AudioRecord read failed: $read")
                }

                // Детекция тишины (опционально - для рестарта recognizer)
                if (System.currentTimeMillis() - lastSoundTime > 3000) {
                    silenceDuration += 100
                    if (silenceDuration > 5000 && isListeningForKeyword) { // После 5 сек тишины - сбрасываем recognizer для лучшей точности
                        Log.d(TAG, "Silence detected, resetting recognizer")
                        recognizer.reset()
                        silenceDuration = 0
                        lastSoundTime = System.currentTimeMillis()
                    }
                }
            }

            // Финальный результат (если что-то осталось в буфере Vosk)
            if (isProcessingCommand && onCommandReceived != null) {
                val finalResult = recognizer.finalResult
                val finalText = extractText(finalResult)
                if (finalText.isNotEmpty()) {
                    Log.i(TAG, "Final command: $finalText")
                    withContext(Dispatchers.Main) {
                        onCommandReceived(finalText)
                    }
                }
            }

        }
        finally {
            try {
                recorder.stop()
                Log.d(TAG, "AudioRecord stopped")
            }
            catch (e: Exception) {
                Log.w(TAG, "Error stopping AudioRecord", e)
            }
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context,
                                                  Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun extractText(jsonResult: String): String {
        return try {
            val json = JSONObject(jsonResult)
            json.optString("text", "")
        }
        catch (e: Exception) {
            Log.w(TAG, "Failed to parse JSON result", e)
            ""
        }
    }

    /**
     * Остановить всё и освободить ресурсы
     */
    fun shutdown() {
        Log.i(TAG, "Shutting down...")

        isRunning = false
        isListeningForKeyword = false
        isProcessingCommand = false

        // Отменяем все корутины
        recordJob?.cancel()
        scope.cancel()

        try {
            audioRecord?.stop()
        }
        catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioRecord", e)
        }
        try {
            audioRecord?.release()
        }
        catch (e: Exception) {
            Log.w(TAG, "Error releasing AudioRecord", e)
        }
        audioRecord = null

        try {
            recognizer?.close()
        }
        catch (e: Exception) {
            Log.w(TAG, "Error closing recognizer", e)
        }
        recognizer = null
        Log.i(TAG, "Shutdown complete")
    }

    /**
     * Пауза (без освобождения ресурсов)
     */
    fun pause() {
        isRunning = false
        audioRecord?.stop()
        Log.i(TAG, "Paused")
    }

    /**
     * Возобновить после паузы
     */
    suspend fun resume(onKeywordDetected: () -> Unit, onError: (String) -> Unit) {
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            isRunning = true
            startKeywordSpotting(onKeywordDetected, onError)
        }
    }

    /**
     * Однократное прослушивание команды (для подтверждений и других сценариев)
     * @param timeout Таймаут в миллисекундах
     * @return Распознанный текст или пустая строка при таймауте/ошибке
     */
    suspend fun listenForCommand(timeout: Long = 3000): String {
        Log.d(TAG, "listenForCommand called, timeout: ${timeout}ms")

        return kotlinx.coroutines.withTimeoutOrNull(timeout) {
            suspendCoroutine<String> { continuation ->
                var resultReceived = false

                scope.launch {
                    try {
                        ensureAudioRecordInitialized()

                        isListeningForKeyword = false
                        isProcessingCommand = true
                        isRunning = true

                        continuousRecording(
                            onCommandReceived = { text ->
                                if (!resultReceived) {
                                    resultReceived = true
                                    Log.i(TAG, "listenForCommand: received '$text'")
                                    continuation.resume(text)
                                }
                            },
                            onTimeout = {
                                if (!resultReceived) {
                                    resultReceived = true
                                    Log.d(TAG, "listenForCommand: timeout")
                                    continuation.resume("")
                                }
                            },
                            timeoutMs = timeout
                        )
                    } catch (e: Exception) {
                        if (!resultReceived) {
                            resultReceived = true
                            Log.e(TAG, "listenForCommand: exception", e)
                            continuation.resume("")
                        }
                    }
                }
            }
        } ?: run {
            Log.w(TAG, "listenForCommand: withTimeoutOrNull returned null")
            ""
        }
    }
}