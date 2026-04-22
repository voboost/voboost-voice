package ru.voboost.voiceassistant.canbus

import android.util.Log
import com.qinggan.canbus.CanBusListener
import com.qinggan.canbus.VehicleState

/**
 * Обработчик предупреждений TSR (Traffic Sign Recognition)
 *
 * Слушает события от камеры, считывающей дорожные знаки
 * и предупреждает о превышении скорости
 *
 * @param canBusManager Менеджер CanBusService для регистрации callback
 * @param ttsCallback Callback для воспроизведения TTS предупреждений
 */
class TSRSpeedLimitHandler(private val canBusManager: CanBusServiceManager,
                           private val ttsCallback: TTSCallback) {
    private var currentSpeed = 0
    private var isaWarningEnabled = true
    private var isCallbackRegistered = false

    companion object {
        const val TAG = "TSRSpeedLimitHandler"
    }

    private val mCanBusListener = object : CanBusListener() {

        override fun onVehicleStateChanged(vehicle: VehicleState, IState: Int) {
            this@TSRSpeedLimitHandler.handleVehicleStateChanged(vehicle, IState)
        }

        override fun onVehicleSpeedChanged(speed: Int) {
            this@TSRSpeedLimitHandler.handleVehicleSpeedChanged(speed)
        }
    }

    private fun handleVehicleStateChanged(vehicle: VehicleState, IState: Int) {
        when (vehicle) {
            VehicleState.ISA_ISLC_STATUS -> { // Приняли статус — теперь запросим детали
                if (IState == 7) {
                    if(isaWarningEnabled){// Факт превышения скорости
                        ttsCallback.playWarning("Превышение скорости")
                    }
                }
            }

            // Переключатель предупреждения ISA (Intelligent Speed Assistance)
            VehicleState.ISA_ISLC_OVER_SPEED_WARNING_SWITCH -> {
                Log.d(TAG, "🔧 ISA_OVER_SPEED_WARNING_SWITCH: $IState")
                isaWarningEnabled = (IState == 2)
            }

            // Остальные события игнорируем
            else -> {}
        }
    }

    private fun handleVehicleSpeedChanged(speed: Int) { // Log.w(TAG, "⚠️ Speed: ${speed}")
        currentSpeed = speed // Log.d(TAG, "🚗 Скорость: $speed км/ч")
    }

    /**
     * Зарегистрировать callback
     */
    fun register(): Boolean {
        if (isCallbackRegistered) return true
        val success = canBusManager.registerCallback(mCanBusListener)
        if (success) {
            isCallbackRegistered = true
            val isaWarningState = canBusManager.getVehicleState(VehicleState.ISA_ISLC_OVER_SPEED_WARNING_SWITCH)
            isaWarningEnabled = (isaWarningState == 2)
            Log.d(TAG, "🔧 start get ISA_OVER_SPEED_WARNING_SWITCH: $isaWarningState")
        }
        return success
    }

    /**
     * Отписаться от callback
     */
    fun unregister(): Boolean {
        if (!isCallbackRegistered) return true
        val success = canBusManager.unregisterCallback(mCanBusListener)
        if (success) {
            isCallbackRegistered = false
            Log.i(TAG, "TSR handler unregistered")
        }
        return success
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

