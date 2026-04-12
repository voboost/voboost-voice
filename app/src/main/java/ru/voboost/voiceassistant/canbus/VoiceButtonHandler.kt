package ru.voboost.voiceassistant.canbus

import android.content.Context
import android.util.Log
import com.qinggan.canbus.CanBusListener
import ru.voboost.voiceassistant.config.ActivationConfig
import ru.voboost.voiceassistant.config.ConfigManager

/**
 * Обработчик кнопки голосового помощника на руле
 *
 * Использует CanBusListener для регистрации в CanBusService
 * При нажатии кнопки уведомляет голосовой помощник через callback
 *
 * @param canBusManager Менеджер CanBusService для регистрации callback
 * @param voiceAssistantCallback Callback для уведомления о нажатии кнопки
 */
class VoiceButtonHandler(context: Context,
                         private val canBusManager: CanBusServiceManager,
                         private val voiceAssistantCallback: VoiceAssistantCallback) { // ❌ Убрали наследование : CanBusListener()


    private val configManager = ConfigManager.getInstance(context)

    // ✅ Создаем слушатель как анонимный объект внутри класса
    private val mCanBusListener = object : CanBusListener() {
        override fun onCarKeyChanged(keycode: Int, keyStatus: Int) {
            this@VoiceButtonHandler.handleCarKeyChanged(keycode, keyStatus)
        } // Если нужны другие методы, переопределяем их здесь же
    }

    companion object {
        const val TAG = "VoiceButtonHandler"
        private const val DEBOUNCE_DELAY_MS = 500L
        private var lastPressTime = 0L
    }

    private var isCallbackRegistered = false

    private fun handleCarKeyChanged(keycode: Int, keyStatus: Int) {
        val config = configManager.getConfig();
        // keycode 16, status 1 = нажатие кнопки голосового помощника
        if (keycode == config.activation.buttonKeycode && keyStatus == 1) {
            Log.i(TAG, "🎤 VOICE BUTTON PRESSED (keycode=$keycode, status=$keyStatus)")
            
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPressTime < DEBOUNCE_DELAY_MS) {
                Log.d(TAG, "Debounce: ignoring rapid press")
                return
            }
            
            lastPressTime = currentTime
            voiceAssistantCallback.onVoiceButtonPressed()
        } else {
            Log.d(TAG, "handleCarKeyChanged keycode: $keycode, status: $keyStatus")
        }
    }

    fun register(): Boolean {
        if (isCallbackRegistered) return true // ✅ Передаем созданный объект mCanBusListener
        val success = canBusManager.registerCallback(mCanBusListener)
        if (success) isCallbackRegistered = true
        Log.i(TAG, "Voice button handler registered: $success")
        return success
    }

    fun unregister(): Boolean {
        if (!isCallbackRegistered) return true
        val success = canBusManager.unregisterCallback(mCanBusListener)
        if (success) isCallbackRegistered = false
        Log.i(TAG, "Voice button handler unregistered: $success")
        return success
    }
}

/**
 * Callback для уведомления о нажатии кнопки голосового помощника
 */
interface VoiceAssistantCallback {
    /**
     * Вызывается при нажатии кнопки на руле
     */
    fun onVoiceButtonPressed()
}
