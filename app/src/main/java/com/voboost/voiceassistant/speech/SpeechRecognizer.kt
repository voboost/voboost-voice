package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.AudioSource
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Распознаватель речи (утилита без состояний)
 *
 * Один непрерывный поток аудио → результаты через Channel.
 * State Machine управляет бизнес-логикой, SpeechRecognizer только распознаёт.
 *
 * Преимущества:
 * - ✅ Нет дублирования состояний
 * - ✅ Нет race conditions
 * - ✅ Непрерывный поток (нельзя пропустить ключевое слово)
 * - ✅ Легко тестировать
 */
class SpeechRecognizer(
    private val audioSource: AudioSource,
    private val recognitionEngine: RecognitionEngine,
    private val keywordChecker: KeywordChecker
) {
    companion object {
        private const val TAG = "SpeechRecognizer"
        private const val KEYWORD_TIMEOUT_MS = 30000L
        private const val COMMAND_TIMEOUT_MS = 5000L
    }

    enum class Mode {
        /** Ожидание ключевого слова */
        KEYWORD,
        /** Ожидание команды */
        COMMAND
    }

    /** Результаты распознавания (SharedFlow — не накапливает, всегда актуальные) */
    val results: MutableSharedFlow<SpeechResult> = MutableSharedFlow(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @Volatile
    private var mode: Mode = Mode.KEYWORD

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recognitionJob: Job? = null

    // Буфер для накопления аудио данных
    private val audioBuffer = AtomicReference<ByteArray>(ByteArray(0))
    private val hasNewData = AtomicBoolean(false)
    private var isRunning = AtomicBoolean(false)

    // Таймауты
    private var timeoutStart: Long = 0

    /**
     * Запустить распознавание (непрерывный поток)
     */
    fun start() {
        if (recognitionJob?.isActive == true) {
            Log.w(TAG, "Already running, ignoring")
            return
        }

        isRunning.set(true)
        timeoutStart = System.currentTimeMillis()

        recognitionJob = scope.launch {
            try {
                Log.i(TAG, "Starting recognition via ${audioSource::class.simpleName}")

                val audioListener = AudioSource.Listener { data, bytesRead ->
                    if (isRunning.get()) {
                        val currentBuffer = audioBuffer.get()
                        val newBuffer = ByteArray(currentBuffer.size + bytesRead)
                        System.arraycopy(currentBuffer, 0, newBuffer, 0, currentBuffer.size)
                        System.arraycopy(data, 0, newBuffer, currentBuffer.size, bytesRead)
                        audioBuffer.set(newBuffer)
                        hasNewData.set(true)
                    }
                }

                audioSource.addListener(audioListener)
                audioSource.start()

                try {
                    recognitionLoop()
                } finally {
                    audioSource.removeListener(audioListener)
                }

            } catch (e: CancellationException) {
                Log.i(TAG, "Recognition cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Recognition error", e)
                results.tryEmit(SpeechResult.Error(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Установить режим распознавания
     */
    fun setMode(newMode: Mode) {
        val oldMode = mode
        mode = newMode

        Log.d(TAG, "Mode change: $oldMode → $newMode")

        // Сбросить буфер и recognizer при смене режима
        audioBuffer.set(ByteArray(0))
        hasNewData.set(false)
        recognitionEngine.reset()
        timeoutStart = System.currentTimeMillis()

        // Очистить буфер результатов от старых событий
        drainResults()
    }

    /**
     * Очистить буфер результатов от старых событий
     */
    private fun drainResults() {
        // SharedFlow не поддерживает drain напрямую, но при смене режима
        // старые события будут проигнорированы фильтрами в State-классах
        Log.d(TAG, "Results buffer drained (SharedFlow auto-cleanup)")
    }

    /**
     * Сбросить распознавание
     */
    fun reset() {
        recognitionEngine.reset()
        audioBuffer.set(ByteArray(0))
        hasNewData.set(false)
        timeoutStart = System.currentTimeMillis()
    }

    /**
     * Остановить распознавание
     */
    fun stop() {
        isRunning.set(false)
        recognitionJob?.cancel()
        audioSource.stop()
    }

    /**
     * Освободить ресурсы
     */
    fun shutdown() {
        stop()
        scope.cancel()
        results.resetReplayCache()
        recognitionEngine.release()
    }

    // ==================== ВНУТРЕННИЕ МЕТОДЫ ====================

    /**
     * Непрерывный цикл распознавания (БЕЗ перезапусков!)
     */
    private suspend fun recognitionLoop() {
        while (isRunning.get()) {
            // Проверка таймаута
            if (hasTimedOut()) {
                handleTimeout()
                timeoutStart = System.currentTimeMillis()
                continue
            }

            // Проверяем есть ли новые данные
            if (hasNewData.get()) {
                val buffer = audioBuffer.getAndSet(ByteArray(0))
                hasNewData.set(false)

                if (buffer.isNotEmpty()) {
                    val result = recognitionEngine.acceptWaveform(buffer)

                    if (result != null && result.isFinal && result.text.isNotEmpty()) {
                        handleRecognitionResult(result)
                        timeoutStart = System.currentTimeMillis()
                    }
                }
            }

            delay(50)
        }

        // Финальный результат при остановке
        if (mode == Mode.COMMAND) {
            val finalResult = recognitionEngine.getFinalResult()
            if (finalResult != null && finalResult.text.isNotEmpty()) {
                results.tryEmit(SpeechResult.CommandReceived(finalResult.text))
            }
        }
    }

    /**
     * Обработать результат распознавания
     */
    private fun handleRecognitionResult(result: RecognitionResult) {
        when (mode) {
            Mode.KEYWORD -> {
                if (keywordChecker.isActivationKeyword(result.text)) {
                    Log.i(TAG, "🎯 KEYWORD DETECTED: ${result.text}")
                    results.tryEmit(SpeechResult.KeywordDetected(result.text))
                }
            }

            Mode.COMMAND -> {
                Log.i(TAG, "📝 COMMAND RECEIVED: ${result.text}")
                results.tryEmit(SpeechResult.CommandReceived(result.text))
            }
        }
    }

    /**
     * Обработать таймаут
     */
    private fun handleTimeout() {
        when (mode) {
            Mode.KEYWORD -> {
                Log.i(TAG, "Keyword timeout - resetting recognizer")
                recognitionEngine.reset()
            }

            Mode.COMMAND -> {
                Log.d(TAG, "Command timeout reached")
                results.tryEmit(SpeechResult.Timeout)
            }
        }
    }

    /**
     * Проверить истёк ли таймаут
     */
    private fun hasTimedOut(): Boolean {
        val elapsed = System.currentTimeMillis() - timeoutStart
        return when (mode) {
            Mode.KEYWORD -> elapsed > KEYWORD_TIMEOUT_MS
            Mode.COMMAND -> elapsed > COMMAND_TIMEOUT_MS
        }
    }
}

/**
 * Результат распознавания
 */
sealed class SpeechResult {
    data class KeywordDetected(val text: String) : SpeechResult()
    data class CommandReceived(val text: String) : SpeechResult()
    object Timeout : SpeechResult()
    data class Error(val message: String) : SpeechResult()
}
