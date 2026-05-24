package ru.voboost.voice.audio

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.voboost.voice.services.audiopolicy.AudioPolicyServiceManager
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.recognition.RecognitionService

/**
 * Поллинг состояния телефона через AudioPolicyManager
 * Периодически проверяет isInCall() и переключает speechRecognizer в MUTED режим
 */
class PhoneCallPoller(private var speechRecognizer: IRecognitionService,
                      private val audioPolicyManager: AudioPolicyServiceManager) {
    companion object {
        const val TAG = "PhoneCallPoller"
        private const val CHECK_INTERVAL_MS = 500L // Проверка каждые 500мс
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var pollingJob: Job? = null
    @Volatile private var isPolling = false
    private var isCallStarted = false

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

                    if (!isCallStarted && inCall) { // В звонке - мьютим распознавание
                        isCallStarted = true;
                        val currentMode = speechRecognizer.getMode()
                        if (currentMode != RecognitionService.Mode.MUTED) {
                            Log.i(TAG, "📞 Call active - muting recognizer")
                            speechRecognizer.setModeSafe(RecognitionService.Mode.MUTED)
                        }
                    }
                    if (isCallStarted && !inCall){ // Нет звонка - возвращаем KEYWORD режим
                        isCallStarted = false;
                        val currentMode = speechRecognizer.getMode()
                        if (currentMode == RecognitionService.Mode.MUTED) {
                            Log.i(TAG, "📞 Call ended - restoring keyword mode")
                            speechRecognizer.setModeSafe(RecognitionService.Mode.KEYWORD)
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
        isCallStarted = false
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