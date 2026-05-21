package ru.voboost.voiceassistant.canbus.handlers

import android.util.Log
import com.qinggan.canbus.CanBusListener
import com.qinggan.canbus.VehicleState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.canbus.ICanBusServiceConnectionCallback
import ru.voboost.voiceassistant.core.QueueSpeechSynthesis


/**
 * Обработчик предупреждений TSR (Traffic Sign Recognition)
 *
 * Слушает события от камеры, считывающей дорожные знаки
 * и предупреждает о превышении скорости
 *
 * @param canBusManager Менеджер CanBusService для регистрации callback
 * @param ttsCallback Callback для воспроизведения TTS предупреждений
 */
class TSRSpeedLimitHandler(private val queueSpeech: QueueSpeechSynthesis) :
        ICanBusServiceConnectionCallback {
    private var canBusManager: CanBusServiceManager? = null
    private var currentSpeed = 0
    private var isaWarningEnabled = true
    private var isCallbackRegistered = false

    companion object {
        const val TAG = "TSRSpeedLimitHandler"
    }

    private val mCanBusListener = object : CanBusListener() {

        override fun onVehicleStateChanged(vehicle: VehicleState, state: Int) {
            this@TSRSpeedLimitHandler.handleVehicleStateChanged(vehicle, state)
        }

        override fun onVehicleSpeedChanged(speed: Int) {
            this@TSRSpeedLimitHandler.handleVehicleSpeedChanged(speed)
        }
    }

    private fun handleVehicleStateChanged(vehicle: VehicleState, state: Int) {
           when (vehicle) {
            VehicleState.ISA_ISLC_STATUS -> { // Приняли статус — теперь запросим детали
                if (state == 7) {
                    if (isaWarningEnabled) { // Факт превышения скорости
                        queueSpeech.enqueue("Превышение скорости",
                                            QueueSpeechSynthesis.Companion.PRIOR_CRITICAL)
                    }
                }
            }
            // Переключатель предупреждения ISA (Intelligent Speed Assistance)
            VehicleState.ISA_ISLC_OVER_SPEED_WARNING_SWITCH -> {
                Log.d(TAG, "🔧 ISA_OVER_SPEED_WARNING_SWITCH: $state")
                isaWarningEnabled = (state == 2)
            }
            else -> {}
        }
    }

    private fun handleVehicleSpeedChanged(speed: Int) { // Log.w(TAG, "⚠️ Speed: ${speed}")
        currentSpeed = speed // Log.d(TAG, "🚗 Скорость: $speed км/ч")
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

    /**
     * Зарегистрировать callback
     */
    private fun register(): Boolean {
        if (isCallbackRegistered) return true
        val success = canBusManager?.registerCallback(mCanBusListener)
        if (success == true) {
            isCallbackRegistered = true
            val isaWarningState =
                canBusManager?.getVehicleState(VehicleState.ISA_ISLC_OVER_SPEED_WARNING_SWITCH)
            isaWarningEnabled = (isaWarningState == 2)
            Log.d(TAG, "🔧 start get ISA_OVER_SPEED_WARNING_SWITCH: $isaWarningState")
        }
        return success == true
    }

    /**
     * Отписаться от callback
     */
    private fun unregister(): Boolean {
        if (!isCallbackRegistered) return true
        val success = canBusManager?.unregisterCallback(mCanBusListener)
        if (success == true) {
            isCallbackRegistered = false
            Log.i(TAG, "TSR handler unregistered")
        }
        return success == true
    }

    fun release() {
        unregister()
        canBusManager = null
    }

    /**
     * Получить текущую скорость
     */
    fun getCurrentSpeed(): Int = currentSpeed

    /**
     * Проверить активно ли предупреждение ISA
     */
    fun isISAWarningEnabled(): Boolean = isaWarningEnabled
}