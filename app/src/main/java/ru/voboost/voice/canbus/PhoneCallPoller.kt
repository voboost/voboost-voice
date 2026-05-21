package ru.voboost.voice.canbus

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.voboost.voice.audio.AudioPolicyServiceManager
import ru.voboost.voice.core.ISpeechRecognizer
import ru.voboost.voice.speech.SpeechRecognizer

/**
 * Поллинг состояния телефона через AudioPolicyManager
 * Периодически проверяет isInCall() и переключает speechRecognizer в MUTED режим
 */
class PhoneCallPoller(private val context: Context,
                      @Volatile private var _speechRecognizer: ISpeechRecognizer?) {


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
                    val inCall = checkPhoneState()
                    
                    // Get reference to speechRecognizer to avoid concurrent mutation issues
                    val recognizer = _speechRecognizer
                    
                    if (inCall && recognizer != null) { // В звонке - мьютим распознавание
                        val currentMode = recognizer.getMode()
                        if (currentMode != SpeechRecognizer.Mode.MUTED) {
                            Log.i(TAG, "📞 Call active - muting recognizer")
                            recognizer.setModeSafe(SpeechRecognizer.Mode.MUTED)
                        }
                    }
                    else if (!inCall && recognizer != null) { // Нет звонка - возвращаем KEYWORD режим
                        val currentMode = recognizer.getMode()
                        if (currentMode == SpeechRecognizer.Mode.MUTED) {
                            Log.i(TAG, "📞 Call ended - restoring keyword mode")
                            recognizer.setModeSafe(SpeechRecognizer.Mode.KEYWORD)
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
     * Проверить состояние телефона через AudioPolicyManager
     */
    private fun checkPhoneState(): Boolean {
        return try { // Создаем временную connection к AudioPolicyService
            val audioPolicyChecker = AudioPolicyServiceManager(context)
            val inCall = audioPolicyChecker.isInCall()
            
            // Освобождаем ресурсы сразу после проверки
            audioPolicyChecker.clearCallbacks()

            if (inCall) {
                Log.d(TAG, "📞 isInCall: true")
            }
            inCall
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to check phone state", e)
            false
        }
    }

    /**
     * Освободить ресурсы
     */
    fun release() {
        stop()
        _speechRecognizer = null
        Log.d(TAG, "PhoneCallPoller released")
    }
}
