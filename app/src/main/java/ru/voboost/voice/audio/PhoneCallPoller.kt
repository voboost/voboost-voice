package ru.voboost.voice.audio

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.voboost.voice.core.ISpeechRecognizer
import ru.voboost.voice.speech.SpeechRecognizer

/**
 * Поллинг состояния телефона через AudioPolicyManager
 * Периодически проверяет isInCall() и переключает speechRecognizer в MUTED режим
 */
class PhoneCallPoller(private var speechRecognizer: ISpeechRecognizer,
                      private val audioPolicyManager: AudioPolicyServiceManager) {
    companion object {
        const val TAG = "PhoneCallPoller"
        private const val CHECK_INTERVAL_MS = 500L // Проверка каждые 500мс
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var pollingJob: Job? = null

    @Volatile private var isPolling = false

    /**
     * Запустить поллинг состояния телефона
     */
    fun start() {
        if (isPolling) {
            Log.w(TAG, "Already polling")
            return
        }

        isPolling = true

        pollingJob = scope.launch {
            while (isPolling) {
                try {
                    val inCall = audioPolicyManager.isInCall() // Get reference to speechRecognizer to avoid concurrent mutation issues

                    if (inCall) { // В звонке - мьютим распознавание
                        val currentMode = speechRecognizer.getMode()
                        if (currentMode != SpeechRecognizer.Mode.MUTED) {
                            Log.i(TAG, "📞 Call active - muting recognizer")
                            speechRecognizer.setModeSafe(SpeechRecognizer.Mode.MUTED)
                        }
                    }
                    else { // Нет звонка - возвращаем KEYWORD режим
                        val currentMode = speechRecognizer.getMode()
                        if (currentMode == SpeechRecognizer.Mode.MUTED) {
                            Log.i(TAG, "📞 Call ended - restoring keyword mode")
                            speechRecognizer.setModeSafe(SpeechRecognizer.Mode.KEYWORD)
                        }
                    }

                    delay(CHECK_INTERVAL_MS)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error in polling loop", e)
                    delay(1000L) // Retry через 1 сек
                }
            }
        }

        Log.i(TAG, "✅ Phone call polling started")
    }

    /**
     * Остановить поллинг
     */
    fun stop() {
        isPolling = false
        pollingJob?.cancel()
        pollingJob = null
        Log.i(TAG, "❌ Phone call polling stopped")
    }

    /**
     * Освободить ресурсы
     */
    fun release() {
        stop()
        Log.d(TAG, "PhoneCallPoller released")
    }
}