package ru.voboost.voice.services.recognition

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.voboost.voice.audio.AudioBuffer
import ru.voboost.voice.audio.IAudioSource
import ru.voboost.voice.audio.MultiChannelAudioSource
import ru.voboost.voice.engine.IRecognitionEngine
import ru.voboost.voice.engine.RecognitionEngineResult
import ru.voboost.voice.nlu.NLUKeyword
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Распознаватель речи с детектором тишины.
 *
 * Фраза считается законченной, когда:
 * 1. Обнаружена тишина дольше [minSilenceDurationMs], ИЛИ
 * 2. Истёк таймаут (страховка)
 */
class RecognitionService(private val audioSource: IAudioSource,
                         private val recognitionEngine: IRecognitionEngine,
                         private val keywordChecker: NLUKeyword,
                         private val config: RecognizerServiceConfig = RecognizerServiceConfig.Default)
    : IRecognitionService {

    companion object {
        const val TAG = "RecognitionService"
    }

    enum class Mode {
        KEYWORD,   // Ждём ключевое слово
        COMMAND,   // Ждём команду
        MUTED      // TTS активен — игнорируем вход
    }

    override val results: MutableSharedFlow<RecognitionServiceResult> =
            MutableSharedFlow(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @Volatile private var mode: Mode = Mode.KEYWORD
    private val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(1) + SupervisorJob())
    private var recognitionJob: Job? = null
    // Единый буфер с детектором тишины
    private val audioBuffer: AudioBuffer
    private val processingBuffer: ByteArray // переиспользуемый буфер для engine
    private val isRunning = AtomicBoolean(false)
    private var currentZone: String = MultiChannelAudioSource.ZONE_FRONT_LEFT
    private var timeoutStart: Long = 0
    private var phraseStartedAt: Long = 0 // для отладки/метрик

    init {
        val bufferSize = (config.bufferSizeSec * config.sampleRate * (config.bitsPerSample / 8)).toInt()
        audioBuffer = AudioBuffer(capacityBytes = bufferSize,
                                  sampleRate = config.sampleRate,
                                  bitsPerSample = config.bitsPerSample,
                                  silenceThresholdDb = config.silenceThresholdDb,
                                  minSilenceDurationMs = config.minSilenceDurationMs,
                                  rmsWindowSizeMs = config.rmsWindowSizeMs)
        // Выделяем буфер для передачи в engine ОДИН РАЗ
        processingBuffer = ByteArray(bufferSize.coerceAtMost(32 * 1024))
    }

    override fun start() {
        if (recognitionJob?.isActive == true) {
            Log.w(TAG, "Already running")
            return
        }

        isRunning.set(true)
        timeoutStart = System.currentTimeMillis()
        phraseStartedAt = 0

        recognitionJob = scope.launch {
            try {
                Log.i(TAG, "Starting recognition")
                val listener = IAudioSource.Listener { data, bytesRead, zone ->
                    if (isRunning.get() && mode != Mode.MUTED) { // ?? Критический путь: НИКАКИХ аллокаций!
                        val phraseEnded = audioBuffer.put(data, 0, bytesRead)
                        if (phraseEnded) { // Обнаружен конец фразы — форсируем обработку
                            forceProcessCurrentPhrase(zone)
                        }
                        currentZone = zone
                    }
                }

                audioSource.addListener(listener)
                audioSource.start()

                try {
                    recognitionLoop()
                }
                finally {
                    audioSource.removeListener(listener)
                }

            }
            catch (e: CancellationException) {
                Log.i(TAG, "Recognition cancelled")
            }
            catch (e: Exception) {
                Log.e(TAG, "Recognition error", e)
                results.tryEmit(RecognitionServiceResult.Error(e.message ?: "Unknown"))
            }
        }
    }

    /**
     * Основной цикл обработки
     */
    private suspend fun recognitionLoop() {
        while (isRunning.get()) {
            if (mode == Mode.MUTED) {
                delay(config.processingIntervalMs)
                continue
            }
            // 1. Проверка таймаута (страховка)
            if (hasTimedOut()) {
                handleTimeout()
                timeoutStart = System.currentTimeMillis()
                continue
            }
            // 2. Обработка накопленных данных (если есть)
            if (audioBuffer.hasData(minBytes = config.sampleRate / 10)) { // минимум 100ms
                processAvailableAudio()
            }
            delay(config.processingIntervalMs)
        }
        // 3. Финальная обработка при остановке
        finalizeRecognition()
    }

    /**
     * Обработать доступные аудио-данные
     */
    private fun processAvailableAudio() {
        val available = audioBuffer.availableBytes()
        if (available == 0) return
        // Берём данные в переиспользуемый буфер
        val copied = audioBuffer.take(processingBuffer, maxSize = available)
        if (copied == 0) return
        // Отправляем в engine
        val result = recognitionEngine.acceptWaveform(processingBuffer, 0, copied)

        if (result?.isFinal == true && result.text.isNotEmpty()) {
            onRecognitionResult(result)
            timeoutStart = System.currentTimeMillis()
            // После финального результата в режиме COMMAND — сбрасываем буфер
            if (mode == Mode.COMMAND) {
                audioBuffer.reset()
            }
        }
    }

    /**
     * Форсировать обработку текущей фразы (при обнаружении тишины)
     */
    private fun forceProcessCurrentPhrase(zone: String) {
        // Обрабатываем всё, что есть в буфере
        while (audioBuffer.hasData()) {
            val copied = audioBuffer.take(processingBuffer)
            if (copied == 0) break

            val result = recognitionEngine.acceptWaveform(processingBuffer, 0, copied)
            if (result?.isFinal == true && result.text.isNotEmpty()) {
                onRecognitionResult(result.copy(zone = zone))
                break // одна фраза — один финальный результат
            }
        }
        // Сбрасываем буфер после обработки фразы
        audioBuffer.reset()
        timeoutStart = System.currentTimeMillis()
    }

    /**
     * Финальная обработка при stop()
     */
    private fun finalizeRecognition() {
        if (mode != Mode.COMMAND) return
        // Пытаемся получить финальный результат из engine
        val result = recognitionEngine.getFinalResult()
        if (result?.text?.isNotEmpty() == true) {
            results.tryEmit(RecognitionServiceResult.CommandReceived(result.text, currentZone))
        }
        audioBuffer.reset()
    }

    private fun onRecognitionResult(result: RecognitionEngineResult) {
        when (mode) {
            Mode.KEYWORD -> {
                if (keywordChecker.isActivationKeyword(result.text)) {
                    Log.i(TAG, "?? KEYWORD: '${result.text}' (zone=${result.zone})")
                    results.tryEmit(RecognitionServiceResult.KeywordDetected(result.text,
                                                                            result.zone)) // После ключа — переходим в режим команды
                    setMode(Mode.COMMAND)
                }
            }
            Mode.COMMAND -> {
                Log.i(TAG, "?? COMMAND: '${result.text}' (zone=${result.zone})")
                results.tryEmit(RecognitionServiceResult.CommandReceived(result.text, result.zone))
            }
            Mode.MUTED -> {
                Log.d(TAG, "Ignoring during TTS: ${result.text}")
            }
        }
    }

    private fun handleTimeout() {
        when (mode) {
            Mode.KEYWORD -> {
                Log.d(TAG, "Keyword timeout")
                recognitionEngine.reset()
                audioBuffer.reset()
            }
            Mode.COMMAND -> {
                Log.d(TAG, "Command timeout")
                results.tryEmit(RecognitionServiceResult.Timeout)
                audioBuffer.reset()
            }
            Mode.MUTED -> {}
        }
    }

    private fun hasTimedOut(): Boolean {
        val elapsed = System.currentTimeMillis() - timeoutStart
        return when (mode) {
            Mode.KEYWORD -> elapsed > config.keywordTimeoutMs
            Mode.COMMAND -> elapsed > config.commandTimeoutMs
            Mode.MUTED -> false
        }
    }

    override fun setMode(newMode: Mode) {
        if(newMode != mode)
        {
            if(newMode == Mode.MUTED)
            {
                if(audioSource.isRecording())
                {
                    audioSource.stop()
                }
            }
            else
            {
                if(!audioSource.isRecording())
                {
                    audioSource.start()
                }
            }
        }

        val old = mode
        mode = newMode
        Log.d(TAG, "Mode: $old > $newMode")

        // Сброс при смене режима
        audioBuffer.reset()
        recognitionEngine.reset()
        timeoutStart = System.currentTimeMillis()
        phraseStartedAt = if (newMode != Mode.MUTED) System.currentTimeMillis() else 0
    }

    /**
     * Get current mode (public for PhoneCallPoller)
     */
    override fun getMode(): Mode = mode

    /**
     * Set mode safely from external threads using coroutine scope
     */
    override fun setModeSafe(newMode: Mode) {
        scope.launch {
            setMode(newMode)
        }
    }

    fun reset() {
        recognitionEngine.reset()
        audioBuffer.reset()
        timeoutStart = System.currentTimeMillis()
    }

    override fun stop() {
        isRunning.set(false)
        recognitionJob?.cancel()
        audioSource.stop()
    }

    override fun shutdown() {
        stop()
        scope.cancel()
        results.resetReplayCache()
        recognitionEngine.release()
    }
}