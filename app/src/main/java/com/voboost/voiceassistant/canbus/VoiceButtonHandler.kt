package com.voboost.voiceassistant.canbus

import android.util.Log
import com.qinggan.canbus.CanBusListener
import com.qinggan.canbus.SWCAngle
/**
 * Обработчик кнопки голосового помощника на руле
 *
 * Использует CanBusListener для регистрации в CanBusService
 * При нажатии кнопки уведомляет голосовой помощник через callback
 *
 * @param canBusManager Менеджер CanBusService для регистрации callback
 * @param voiceAssistantCallback Callback для уведомления о нажатии кнопки
 */
class VoiceButtonHandler(private val canBusManager: CanBusServiceManager,
                         private val voiceAssistantCallback: VoiceAssistantCallback) { // ❌ Убрали наследование : CanBusListener()

    // ✅ Создаем слушатель как анонимный объект внутри класса
    private val mCanBusListener = object : CanBusListener() {
        override fun onSWCAngleChanged(swcAngle: SWCAngle?) { //this@VoiceButtonHandler.handleAngleChanged(swcAngle)
        }

        override fun onCarKeyChanged(keycode: Int, keyStatus: Int) {
            this@VoiceButtonHandler.handleCarKeyChanged(keycode, keyStatus)
        } // Если нужны другие методы, переопределяем их здесь же
    }

    companion object {
        private const val TAG = "VoiceButtonHandler"
        private const val SWC_VOICE_BUTTON_ANGLE_MIN = 100
        private const val SWC_VOICE_BUTTON_ANGLE_MAX = 200
        private const val DEBOUNCE_DELAY_MS = 500L
        private var lastPressTime = 0L
    }

    private var isCallbackRegistered = false

    // Вынесли логику обработки в отдельный метод класса
    private fun handleAngleChanged(swcAngle: SWCAngle?) {
        swcAngle ?: return
        Log.d(TAG,
              "SWC Angle changed: direction=${swcAngle.mWheelDirection}, angle=${swcAngle.mWheelAngle}")

        if (isVoiceButtonPress(swcAngle)) {
            Log.i(TAG, "Voice button pressed on steering wheel!")

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPressTime < DEBOUNCE_DELAY_MS) {
                Log.d(TAG, "Debounce: ignoring rapid press")
                return
            }

            lastPressTime = currentTime
            voiceAssistantCallback.onVoiceButtonPressed()
        }
    }

    private fun handleCarKeyChanged(keycode: Int, keyStatus: Int) {
        Log.i(TAG, "handleCarKeyChanged keycode: $keycode, status: $keyStatus")
    }

    private fun isVoiceButtonPress(swcAngle: SWCAngle): Boolean {
        return swcAngle.mWheelAngle in SWC_VOICE_BUTTON_ANGLE_MIN..SWC_VOICE_BUTTON_ANGLE_MAX || swcAngle.mWheelDirection == SWCAngle.SWC_DIR_CENTER
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
