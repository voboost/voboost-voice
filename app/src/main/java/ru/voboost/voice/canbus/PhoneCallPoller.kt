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
 * РџРѕР»Р»РёРЅРі СЃРѕСЃС‚РѕСЏРЅРёСЏ С‚РµР»РµС„РѕРЅР° С‡РµСЂРµР· AudioPolicyManager
 * РџРµСЂРёРѕРґРёС‡РµСЃРєРё РїСЂРѕРІРµСЂСЏРµС‚ isInCall() Рё РїРµСЂРµРєР»СЋС‡Р°РµС‚ speechRecognizer РІ MUTED СЂРµР¶РёРј
 */
class PhoneCallPoller(private val context: Context,
                      @Volatile private var speechRecognizer: ISpeechRecognizer?) {


    companion object {
        const val TAG = "PhoneCallPoller"
        private const val CHECK_INTERVAL_MS = 500L // РџСЂРѕРІРµСЂРєР° РєР°Р¶РґС‹Рµ 500РјСЃ
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var pollingJob: Job? = null

    @Volatile private var isPolling = false

    /**
     * Р—Р°РїСѓСЃС‚РёС‚СЊ РїРѕР»Р»РёРЅРі СЃРѕСЃС‚РѕСЏРЅРёСЏ С‚РµР»РµС„РѕРЅР°
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
                    val recognizer = speechRecognizer
                    
                    if (inCall && recognizer != null) { // Р’ Р·РІРѕРЅРєРµ - РјСЊСЋС‚РёРј СЂР°СЃРїРѕР·РЅР°РІР°РЅРёРµ
                        val currentMode = recognizer.getMode()
                        if (currentMode != SpeechRecognizer.Mode.MUTED) {
                            Log.i(TAG, "рџ“ћ Call active - muting recognizer")
                            recognizer.setModeSafe(SpeechRecognizer.Mode.MUTED)
                        }
                    }
                    else if (!inCall && recognizer != null) { // РќРµС‚ Р·РІРѕРЅРєР° - РІРѕР·РІСЂР°С‰Р°РµРј KEYWORD СЂРµР¶РёРј
                        val currentMode = recognizer.getMode()
                        if (currentMode == SpeechRecognizer.Mode.MUTED) {
                            Log.i(TAG, "рџ“ћ Call ended - restoring keyword mode")
                            recognizer.setModeSafe(SpeechRecognizer.Mode.KEYWORD)
                        }
                    }

                    delay(CHECK_INTERVAL_MS)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error in polling loop", e)
                    delay(1000L) // Retry С‡РµСЂРµР· 1 СЃРµРє
                }
            }
        }

        Log.i(TAG, "вњ… Phone call polling started")
    }

    /**
     * РћСЃС‚Р°РЅРѕРІРёС‚СЊ РїРѕР»Р»РёРЅРі
     */
    fun stop() {
        isPolling = false
        pollingJob?.cancel()
        pollingJob = null
        Log.i(TAG, "вќЊ Phone call polling stopped")
    }

    /**
     * РџСЂРѕРІРµСЂРёС‚СЊ СЃРѕСЃС‚РѕСЏРЅРёРµ С‚РµР»РµС„РѕРЅР° С‡РµСЂРµР· AudioPolicyManager
     */
    private fun checkPhoneState(): Boolean {
        return try { // РЎРѕР·РґР°РµРј РІСЂРµРјРµРЅРЅСѓСЋ connection Рє AudioPolicyService
            val audioPolicyChecker = AudioPolicyServiceManager(context)
            val inCall = audioPolicyChecker.isInCall()
            
            // РћСЃРІРѕР±РѕР¶РґР°РµРј СЂРµСЃСѓСЂСЃС‹ СЃСЂР°Р·Сѓ РїРѕСЃР»Рµ РїСЂРѕРІРµСЂРєРё
            audioPolicyChecker.clearCallbacks()

            if (inCall) {
                Log.d(TAG, "рџ“ћ isInCall: true")
            }
            inCall
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to check phone state", e)
            false
        }
    }

    /**
     * РћСЃРІРѕР±РѕРґРёС‚СЊ СЂРµСЃСѓСЂСЃС‹
     */
    fun release() {
        stop()
        speechRecognizer = null
        Log.d(TAG, "PhoneCallPoller released")
    }
}
