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
    // Поля состояния
    private var currentSpeedLimit = 0
    private var currentSpeed = 0
    private var isWarningPlayed = false
    private var isaWarningEnabled = true

    private val mCanBusListener = object : CanBusListener() {

        override fun onVehicleStateChanged(vehicle: VehicleState, IState: Int) {
            this@TSRSpeedLimitHandler.handleVehicleStateChanged(vehicle, IState)
        }

        override fun onVehicleSpeedChanged(speed: Int) {
            this@TSRSpeedLimitHandler.handleVehicleSpeedChanged(speed)
        }
    }

    private fun handleVehicleStateChanged(vehicle: VehicleState, IState: Int) {
        //Log.w(TAG, "⚠️ VehicleState: ${vehicle.toString()} (IState=$IState)")
        when (vehicle) { // Факт превышения скорости
            VehicleState.IPK_OVER_SPEED_WARNING -> {
                val isOverSpeed = (IState == 1)
                Log.w(TAG, "⚠️ IPK_OVER_SPEED_WARNING: $isOverSpeed (IState=$IState)")
                if (isOverSpeed && !isWarningPlayed) {
                    isWarningPlayed = true
                    ttsCallback.playWarning("Превышение скорости")
                }
                else if (!isOverSpeed) {
                    isWarningPlayed = false
                }
            }

            // Значение превышения (на сколько км/ч превысили)
            VehicleState.IPK_OVER_SPEED_VALUE -> {
                Log.w(TAG, "📊 IPK_OVER_SPEED_VALUE: $IState км/ч")
            }

            // Ограничение скорости от навигации
            VehicleState.NAVI_SPEED_LIMIT -> {
                val limit = IState
                Log.d(TAG, "🚦 NAVI_SPEED_LIMIT: $limit км/ч")
                currentSpeedLimit = limit
                checkSpeedLimit()
            }

            // Тип ограничения от навигации
            VehicleState.NAVI_SPEED_LIMIT_TYPE -> {
                Log.d(TAG, "🚦 NAVI_SPEED_LIMIT_TYPE: $IState")
            }

            // Ограничение от камеры TSR (Traffic Sign Recognition)
            VehicleState.TSR_SPEED_LIMIT -> {
                val limit = IState
                Log.d(TAG, "🚸 TSR_SPEED_LIMIT: $limit км/ч")
                currentSpeedLimit = limit

                if (limit > 0) {
                    ttsCallback.playWarning("Ограничение скорости $limit километров в час")
                }
            }

            // Единицы измерения ограничения
            VehicleState.TSR_SPEED_LIMIT_UNIT -> {
                Log.d(TAG, "🚸 TSR_SPEED_LIMIT_UNIT: $IState") // 0 = км/ч, 1 = mph
            }

            // Переключатель предупреждения ISA (Intelligent Speed Assistance)
            VehicleState.ISA_ISLC_OVER_SPEED_WARNING_SWITCH -> {
                Log.d(TAG, "🔧 ISA_OVER_SPEED_WARNING_SWITCH: $IState")
                isaWarningEnabled = (IState == 1)
            }

            // Остальные события игнорируем
            else -> {}
        }
    }

    private fun handleVehicleSpeedChanged(speed: Int) {
        Log.w(TAG, "⚠️ Speed: ${speed}")
        currentSpeed = speed // Log.d(TAG, "🚗 Скорость: $speed км/ч")
        checkSpeedLimit()
    }

    /**
     * Проверка превышения текущего лимита
     */
    private fun checkSpeedLimit() {
        if (currentSpeedLimit <= 0 || !isaWarningEnabled) return

        val diff = currentSpeed - currentSpeedLimit
        if (diff > 5 && !isWarningPlayed) {  // Превышение более чем на 5 км/ч
            isWarningPlayed = true
            Log.w(TAG, "⚠️ ПРЕВЫШЕНИЕ на $diff км/ч! (${currentSpeed}/${currentSpeedLimit})")
            ttsCallback.playWarning("Превышение скорости на $diff километров в час")
        }
        else if (diff <= 3 && isWarningPlayed) { // Сброс если скорость снизилась
            isWarningPlayed = false
        }
    }

    private var isCallbackRegistered = false

    /**
     * Зарегистрировать callback
     */
    fun register(): Boolean {
        if (isCallbackRegistered) return true
        val success = canBusManager.registerCallback(mCanBusListener)
        if (success) {
            isCallbackRegistered = true
            Log.i(TAG, "TSR handler registered")
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
     * Получить текущий лимит скорости
     */
    fun getCurrentSpeedLimit(): Int = currentSpeedLimit

    /**
     * Получить текущую скорость
     */
    fun getCurrentSpeed(): Int = currentSpeed

    /**
     * Проверить активно ли предупреждение ISA
     */
    fun isISAWarningEnabled(): Boolean = isaWarningEnabled

    companion object {
        const val TAG = "TSRSpeedLimitHandler"
    }
}

/**
 * Callback для воспроизведения TTS предупреждений
 */
interface TTSCallback {
    /**
     * Воспроизвести голосовое предупреждение
     */
    fun playWarning(text: String)
}
