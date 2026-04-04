package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.AudioSource
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * State Machine для управления распознаванием речи
 * 
 * Универсальная для всех движков (Vosk, Sherpa, и т.д.)
 * 
 * Ключевая идея: ОДИН непрерывный поток без перезапусков!
 * State Machine только меняет логику обработки результатов.
 * 
 * Преимущества:
 * - ✅ Нет race conditions с флагами
 * - ✅ Нельзя пропустить ключевое слово при перезапуске
 * - ✅ Прозрачное управление состоянием
 * - ✅ Легко тестировать
 * - ✅ Работает с любым движком через RecognitionEngine интерфейс
 */
class SpeechStateMachine(
    private val audioSource: AudioSource,
    private val recognitionEngine: RecognitionEngine,
    private val keywordChecker: KeywordChecker
) {
    companion object {
        private const val TAG = "SpeechStateMachine"
        private const val KEYWORD_TIMEOUT_MS = 30000L
        private const val COMMAND_TIMEOUT_MS = 5000L
    }

    @Volatile
    private var state: SpeechState = SpeechState.STOPPED

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recognitionJob: Job? = null

    // Буфер для накопления аудио данных
    private val audioBuffer = AtomicReference<ByteArray>(ByteArray(0))
    private val hasNewData = AtomicBoolean(false)

    // Слушатели
    private var listener: SpeechRecognitionListener? = null
    private var keywordListener: SpeechRecognitionListener? = null  // Сохраняем для возврата
    private var commandListener: SpeechRecognitionListener? = null  // Сохраняем для команд

    // Таймауты
    private var startTime: Long = 0

    /**
     * Запустить распознавание
     * Начинает с состояния LISTENING_KEYWORD
     */
    fun start(listener: SpeechRecognitionListener) {
        if (recognitionJob?.isActive == true) {
            Log.w(TAG, "Already running, ignoring")
            return
        }

        this.listener = listener
        this.keywordListener = listener  // Сохраняем для возврата
        transitionTo(SpeechState.LISTENING_KEYWORD)

        recognitionJob = scope.launch {
            try {
                Log.i(TAG, "Starting recognition via ${audioSource::class.simpleName}")

                // Подписываемся на аудио-поток ОДИН раз
                val audioListener = AudioSource.Listener { data, bytesRead ->
                    if (state != SpeechState.STOPPED) {
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
                    // Непрерывный цикл распознавания
                    recognitionLoop()
                } finally {
                    audioSource.removeListener(audioListener)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Recognition error", e)
                listener.onError(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Запустить слушание ключевого слова (удобный метод)
     */
    fun startListeningForKeyword(listener: com.voboost.voiceassistant.speech.VoiceAssistantListener) {
        start(object : SpeechRecognitionListener {
            override fun onKeywordDetected() {
                listener.onKeywordDetected()
            }
            
            override fun onError(error: String) {
                listener.onError(error)
            }
        })
    }

    /**
     * Завершить команду и вернуться к ожиданию ключевого слова
     */
    fun finishCommand() {
        returnToKeywordListening()
    }

    /**
     * Активировать распознавание (перейти в режим команды)
     * Вызывается когда ключевое слово распознано
     */
    fun activate() {
        transitionTo(SpeechState.ACTIVATED)
    }

    /**
     * Начать слушивание команды
     * Вызывается после активации
     */
    fun startListeningCommand(listener: SpeechRecognitionListener? = null) {
        // Обновляем слушатель если передан
        if (listener != null) {
            this.listener = listener
            this.commandListener = listener  // Сохраняем для команд
        }
        transitionTo(SpeechState.LISTENING_COMMAND)
    }

    /**
     * Вернуться к ожиданию ключевого слова
     */
    fun returnToKeywordListening() {
        // Возвращаем слушатель для ключевого слова
        keywordListener?.let { this.listener = it }
        transitionTo(SpeechState.LISTENING_KEYWORD)
    }

    /**
     * Остановить распознавание
     */
    fun stop() {
        transitionTo(SpeechState.STOPPED)
        recognitionJob?.cancel()
        audioSource.stop()
    }

    /**
     * Освободить ресурсы
     */
    fun shutdown() {
        stop()
        scope.cancel()
        recognitionEngine.release()
    }

    /**
     * Текущее состояние
     */
    fun getState(): SpeechState = state

    // ==================== ВНУТРЕННИЕ МЕТОДЫ ====================

    /**
     * Непрерывный цикл распознавания (БЕЗ перезапусков!)
     */
    private suspend fun recognitionLoop() {
        var keywordStartTime = System.currentTimeMillis()

        while (state != SpeechState.STOPPED) {
            // Проверка таймаута
            if (hasTimedOut(keywordStartTime)) {
                handleTimeout()
                keywordStartTime = System.currentTimeMillis()
                continue
            }

            // Проверяем есть ли новые данные
            if (hasNewData.get()) {
                val buffer = audioBuffer.getAndSet(ByteArray(0))
                hasNewData.set(false)

                if (buffer.isNotEmpty()) {
                    // Распознаём через универсальный интерфейс
                    val result = recognitionEngine.acceptWaveform(buffer)

                    if (result != null && result.isFinal && result.text.isNotEmpty()) {
                        handleRecognitionResult(result, keywordStartTime)
                        keywordStartTime = System.currentTimeMillis()
                    }
                }
            }

            delay(50)
        }

        // Финальный результат при остановке
        if (state == SpeechState.LISTENING_COMMAND) {
            val finalResult = recognitionEngine.getFinalResult()
            if (finalResult != null && finalResult.text.isNotEmpty()) {
                listener?.onCommandReceived(finalResult.text)
            }
        }
    }

    /**
     * Обработать результат распознавания
     */
    private fun handleRecognitionResult(result: RecognitionResult, keywordStartTime: Long) {
        when (state) {
            SpeechState.LISTENING_KEYWORD -> {
                if (keywordChecker.isActivationKeyword(result.text)) {
                    Log.i(TAG, "🎯 KEYWORD DETECTED: ${result.text}")
                    listener?.onKeywordDetected()
                }
            }

            SpeechState.LISTENING_COMMAND -> {
                Log.i(TAG, "📝 COMMAND RECEIVED: ${result.text}")
                listener?.onCommandReceived(result.text)
                // НЕ возвращаемся к keyword listening - пусть State Machine решает
            }

            else -> {
                // ACTIVATED или STOPPED — игнорируем
            }
        }
    }

    /**
     * Обработать таймаут
     */
    private suspend fun handleTimeout() {
        when (state) {
            SpeechState.LISTENING_KEYWORD -> {
                Log.i(TAG, "Keyword timeout - resetting recognizer")
                recognitionEngine.reset()
            }

            SpeechState.LISTENING_COMMAND -> {
                Log.d(TAG, "Command timeout reached")
                listener?.onTimeout()
                returnToKeywordListening()
            }

            else -> {
                // ACTIVATED или STOPPED — игнорируем
            }
        }
    }

    /**
     * Проверить истёк ли таймаут
     */
    private fun hasTimedOut(startTime: Long): Boolean {
        val elapsed = System.currentTimeMillis() - startTime
        return when (state) {
            SpeechState.LISTENING_KEYWORD -> elapsed > KEYWORD_TIMEOUT_MS
            SpeechState.LISTENING_COMMAND -> elapsed > COMMAND_TIMEOUT_MS
            else -> false
        }
    }

    /**
     * Перейти в новое состояние
     */
    private fun transitionTo(newState: SpeechState) {
        val oldState = state
        state = newState

        Log.d(TAG, "State transition: $oldState → $newState")

        // Сбросить буфер при смене состояния
        audioBuffer.set(ByteArray(0))
        hasNewData.set(false)

        // Сбросить recognizer при смене режима
        if (oldState != newState) {
            recognitionEngine.reset()
        }

        // Уведомить о смене состояния
        listener?.onStateChanged(newState)
    }
}

/**
 * Универсальный интерфейс движка распознавания
 * Позволяет использовать любой движок (Vosk, Sherpa, и т.д.)
 */
interface RecognitionEngine {
    /**
     * Принять порцию PCM данных и распознать
     * @param pcm PCM данные (16-bit, mono, 16000 Hz)
     * @return Результат распознавания или null
     */
    fun acceptWaveform(pcm: ByteArray): RecognitionResult?
    
    /**
     * Получить финальный результат
     */
    fun getFinalResult(): RecognitionResult?
    
    /**
     * Сбросить распознавание
     */
    fun reset()
    
    /**
     * Освободить ресурсы
     */
    fun release()
}
