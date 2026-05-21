package ru.voboost.voiceassistant.canbus.handlers

import android.util.Log
import com.qinggan.canbus.CanBusListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.voboost.voiceassistant.VoboostVoiceService
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.canbus.ICanBusServiceConnectionCallback
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.speech.state.StateMachine

/**
 * Обработчик кнопки голосового помощника на руле
 *
 * Использует CanBusListener для регистрации в CanBusService
 * При нажатии кнопки уведомляет голосовой помощник через callback
 *
 * @param canBusManager Менеджер CanBusService для регистрации callback
 * @param voiceAssistantCallback Callback для уведомления о нажатии кнопки
 */
class VoiceButtonHandler(private val serviceScope: CoroutineScope,
                         private val configManager: ConfigManager) :
        ICanBusServiceConnectionCallback {

    companion object {
        const val TAG = "VoiceButtonHandler"
        private const val DEBOUNCE_DELAY_MS = 500L
        private var lastPressTime = 0L
    }

    var stateMachine: StateMachine? = null
    private var canBusManager: CanBusServiceManager? = null;
    private var isCallbackRegistered = false

    private val mCanBusListener = object : CanBusListener() {
        override fun onCarKeyChanged(keycode: Int, keyStatus: Int) {
            this@VoiceButtonHandler.handleCarKeyChanged(keycode, keyStatus)
        }
    }

    private fun handleCarKeyChanged(keycode: Int, keyStatus: Int) {
        val config =
                configManager.getConfig(); // keycode 16, status 1 = нажатие кнопки голосового помощника
        if (keycode == config.activation.buttonKeycode && keyStatus == 1) {
            Log.i(TAG, "🎤 VOICE BUTTON PRESSED (keycode=$keycode, status=$keyStatus)")

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPressTime < DEBOUNCE_DELAY_MS) {
                Log.d(TAG, "Debounce: ignoring rapid press")
                return
            }

            lastPressTime = currentTime
            Log.i(VoboostVoiceService.Companion.TAG, "Activating voice assistant...")

            serviceScope.launch {
                try { // IState Machine сам обработает активацию в текущем IState
                    stateMachine?.activate()
                }
                catch (e: Exception) {
                    Log.e(VoboostVoiceService.Companion.TAG, "Error activating voice assistant", e)
                }
            }
        }
        else {
            Log.d(TAG, "handleCarKeyChanged keycode: $keycode, status: $keyStatus")
        }
    }

    override fun handlerConnected(canBusServiceManager: CanBusServiceManager) {
        canBusManager = canBusServiceManager
        register()
        Log.d(TAG, "register")
    }

    override fun handlerDisconnected(canBusServiceManager: CanBusServiceManager) {
        unregister()
        canBusManager = null
        Log.d(TAG, "unregistered")
    }

    override fun handlerConnectionFailed(canBusServiceManager: CanBusServiceManager,
                                         error: String) {
    }

    private fun register(): Boolean {
        if (isCallbackRegistered) return true // ✅ Передаем созданный объект mCanBusListener
        val success = canBusManager?.registerCallback(mCanBusListener)
        if (success == true) isCallbackRegistered = true
        Log.i(TAG, "Voice button handler registered: $success")
        return success == true
    }

    private fun unregister(): Boolean {
        if (!isCallbackRegistered) return true
        val success = canBusManager?.unregisterCallback(mCanBusListener)
        if (success == true) isCallbackRegistered = false
        Log.i(TAG, "Voice button handler unregistered: $success")
        return success == true
    }

    fun release() {
        unregister()
        canBusManager = null
    }
}

