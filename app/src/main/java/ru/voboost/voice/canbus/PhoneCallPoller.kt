package ru.voboost.voice.canbus

import android.content.Context
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
class PhoneCallPoller(private val context: Context,
                      private var speechRecognizer: ISpeechRecognizer?) {


//    companion object {
//        const val TAG = "PhoneCallPoller"
//        private const val CHECK_INTERVAL_MS = 500L // Проверка каждые 500мс
//    }
//
//    private val scope = CoroutineScope(Dispatchers.IO + Job())
//    private var pollingJob: Job? = null
//
//    @Volatile private var isPolling = false
//
//    /**
//     * Запустить поллинг состояния телефона
//     */
    fun start() {
//        if (isPolling) {
//            Log.w(TAG, "Already polling")
//            return
//        }
//
//        isPolling = true
//
//        pollingJob = scope.launch {
//            while (isPolling) {
//                try {
//                    val inCall = checkPhoneState()
//
//                    if (inCall && speechRecognizer != null) { // В звонке - мьютим распознавание
//                        val currentMode = speechRecognizer.getMode()
//                        if (currentMode != SpeechRecognizer.Mode.MUTED) {
//                            Log.i(TAG, "📞 Call active - muting recognizer")
//                            speechRecognizer.setModeSafe(SpeechRecognizer.Mode.MUTED)
//                        }
//                    }
//                    else if (!inCall && speechRecognizer != null) { // Нет звонка - возвращаем KEYWORD режим
//                        val currentMode = speechRecognizer.getMode()
//                        if (currentMode == SpeechRecognizer.Mode.MUTED) {
//                            Log.i(TAG, "📞 Call ended - restoring keyword mode")
//                            speechRecognizer.setModeSafe(SpeechRecognizer.Mode.KEYWORD)
//                        }
//                    }
//
//                    delay(CHECK_INTERVAL_MS)
//                }
//                catch (e: Exception) {
//                    Log.e(TAG, "Error in polling loop", e)
//                    delay(1000L) // Рetry через 1 сек
//                }
//            }
//        }
//
//        Log.i(TAG, "✅ Phone call polling started")
   }
//
//    /**
//     * Остановить поллинг
//     */
    fun stop() {
//        isPolling = false
//        pollingJob?.cancel()
//        pollingJob = null
//        Log.i(TAG, "❌ Phone call polling stopped")
//    }
//
//    /**
//     * Проверить состояние телефона через AudioPolicyManager
//     */
//    private fun checkPhoneState(): Boolean {
//        return try { // Создаем временную connection к AudioPolicyService
//            val audioPolicyChecker = PhoneStateChecker(context)
//            audioPolicyChecker.connect()
//            val inCall = audioPolicyChecker.isInCall()
//            audioPolicyChecker.disconnect()
//
//            if (inCall) {
//                Log.d(TAG, "📞 isInCall: true")
//            }
//            inCall
//        }
//        catch (e: Exception) {
//            Log.e(TAG, "Failed to check phone state", e)
//            false
//        }
    }
//
//    /**
//     * Освободить ресурсы
//     */
   fun release() {
//        stop()
//        speechRecognizer = null
//        Log.d(TAG, " PhoneCallPoller released")
    }
}


