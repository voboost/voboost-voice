package com.voboost.voiceassistant.executor

import android.util.Log
import com.voboost.voiceassistant.canbus.CanBusServiceManager
import com.qinggan.canbus.AirConditionState
import com.qinggan.canbus.PhoneInfo
import com.qinggan.canbus.PhoneState
import com.qinggan.canbus.VehicleState

/**
 * Выполнение команд через AIDL интерфейс CanBusService
 * Правильный способ взаимодействия с CAN-шиной автомобиля
 *
 * Использует CanBusServiceManager для абстракции над AIDL
 *
 * @param canBusManager Менеджер для работы с CanBusService
 */
class AIDLVehicleCommandExecutor(
    private val canBusManager: CanBusServiceManager
) : VehicleCommandExecutor {

    companion object {
        private const val TAG = "AIDLVehicleCommand"
    }

    override val executionMethod: String = "AIDL CanBus"

    override fun execute(
        target: String,
        classify: Int,
        command: Int,
        params: Map<String, Any>
    ): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        return try {
            Log.d(TAG, "Executing via AIDL: target=$target, classify=$classify, command=$command")

            when (target) {
                "AirConditioner" -> executeAirConditioner(classify, command, params)
                "Window" -> executeWindow(classify, command)
                "SmartMode" -> executeSmartMode(params)
                "Chargport" -> executeChargport(command)
                "Scuttle" -> executeScuttle(command)
                else -> {
                    Log.w(TAG, "Unknown target: $target")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during command execution", e)
            false
        }
    }

    /**
     * Выполнение команд кондиционера
     * @param classify класс команды (5=питание, 47/48=температура водителя, 44/45=вентилятор)
     * @param command команда (0=открыть/вкл, 1=закрыть/выкл, 2=активно, 3=установить температуру)
     * @param params параметры (temperature=температура)
     */
    private fun executeAirConditioner(classify: Int, command: Int, params: Map<String, Any>): Boolean {
        val acState = when (classify) {
            5 -> AirConditionState.AC_POWER_SWITCH
            8 -> AirConditionState.AC_FRONT_DEFROST_SWITCH
            44 -> AirConditionState.AC_FRONT_BLOWER_DECRE_TC
            45 -> AirConditionState.AC_FRONT_BLOWER_INCRE_TC
            47 -> AirConditionState.AC_DRIVER_TEMP_DECRE_TC
            48 -> AirConditionState.AC_DRIVER_TEMP_INCRE_TC
            else -> {
                Log.w(TAG, "Unknown AC classify: $classify")
                return false
            }
        }

        val acValue = when (command) {
            0 -> AirConditionState.OPEN
            1 -> AirConditionState.CLOSE
            2 -> AirConditionState.ACTIVE
            3 -> {
                // Установка температуры - передаем значение из params
                val temp = params["temperature"] as? Int ?: 22
                Log.d(TAG, "Set temperature: $temp°C")
                // Для установки температуры используем инкремент/декремент
                // В реальном автомобиле нужно отправлять конкретное значение
                canBusManager.setAirConditionState(acState, temp)
                return true
            }
            else -> command
        }

        Log.d(TAG, "AC Command: state=$acState (${acState.ordinal}), value=$acValue")
        canBusManager.setAirConditionState(acState, acValue)
        return true
    }

    /**
     * Выполнение команд окон
     * @param classify класс команды (2=окна)
     * @param command команда (0=открыть, 1=закрыть)
     *
     * Используем VehicleState.ALL_WINDOW_CONTROL для управления всеми окнами
     */
    private fun executeWindow(classify: Int, command: Int): Boolean {
        val windowState = when (command) {
            0 -> {
                Log.d(TAG, "Window OPEN command")
                CanBusServiceManager.VALUE_OPEN
            }
            1 -> {
                Log.d(TAG, "Window CLOSE command")
                CanBusServiceManager.VALUE_CLOSE
            }
            else -> {
                Log.w(TAG, "Unknown window command: $command")
                return false
            }
        }

        // Используем IVI для управления окнами
        val vehicleState = VehicleState.ALL_WINDOW_CONTROL
        Log.d(TAG, "Window Command: state=$vehicleState (${vehicleState.ordinal}), value=$windowState")
        canBusManager.setVehicleState(vehicleState, windowState)
        return true
    }

    /**
     * Выполнение смарт режимов
     * @param params параметры (mode=номер режима)
     *
     * Режимы:
     * - 6: Романтический режим
     * - 18: Режим отдыха
     * - 22: Детский режим
     */
    private fun executeSmartMode(params: Map<String, Any>): Boolean {
        val mode = params["mode"] as? Int ?: run {
            Log.w(TAG, "SmartMode: mode parameter not found")
            return false
        }
        Log.d(TAG, "SmartMode Command: mode=$mode")
        return canBusManager.setVehicleSceneMode(mode)
    }

    /**
     * Управление лючком зарядки
     * @param command команда (1=открыть, 2=закрыть)
     *
     * VehicleState.IVI_CHRG_PORT_CAP (779)
     * Значения: 1=CLOSE, 2=OPEN
     */
    private fun executeChargport(command: Int): Boolean {
        // command: 1=OPEN, 2=CLOSE (из config.json)
        val value = when (command) {
            1 -> CanBusServiceManager.VALUE_OPEN   // открыть
            2 -> CanBusServiceManager.VALUE_CLOSE  // закрыть
            else -> {
                Log.w(TAG, "Unknown chargport command: $command")
                return false
            }
        }

        val state = VehicleState.IVI_CHRG_PORT_CAP
        Log.d(TAG, "Chargport Command: state=$state (${state.ordinal}), value=$value")
        canBusManager.setVehicleState(state, value)
        return true
    }

    /**
     * Управление крышкой бензобака
     * @param command команда (0=открыть, 1=закрыть)
     *
     * VehicleState.IVI_FUEL_PORT_CAP (778)
     * Значения: 1=CLOSE, 2=OPEN
     */
    private fun executeScuttle(command: Int): Boolean {
        // command: 0=OPEN, 1=CLOSE (из config.json)
        val value = when (command) {
            0 -> CanBusServiceManager.VALUE_OPEN   // открыть
            1 -> CanBusServiceManager.VALUE_CLOSE  // закрыть
            else -> {
                Log.w(TAG, "Unknown scuttle command: $command")
                return false
            }
        }

        val state = VehicleState.IVI_FUEL_PORT_CAP
        Log.d(TAG, "Scuttle Command: state=$state (${state.ordinal}), value=$value")
        canBusManager.setVehicleState(state, value)
        return true
    }

    /**
     * Телефонные команды через AIDL
     * Используем updatePhoneInfo для отправки команды звонка
     *
     * PhoneInfo параметры:
     * - phoneNum: номер телефона
     * - name: имя контакта
     * - phoneState: состояние (для звонка можно использовать)
     */
    override fun executePhoneCommand(
        classify: Int,
        command: Int,
        contact: String?,
        number: String?,
        callType: String
    ): Boolean {
        return try {
            val phoneInfo = PhoneInfo()

            if (callType == "contact" && contact != null) {
                // Звонок по контакту
                phoneInfo.name = contact
                Log.d(TAG, "Phone call to contact: $contact")
            } else if (callType == "number" && number != null) {
                // Звонок по номеру
                phoneInfo.phoneNum = number
                Log.d(TAG, "Phone call to number: $number")
            } else {
                Log.w(TAG, "Phone command: no contact or number specified")
                return false
            }

            // Устанавливаем состояние для звонка
            // PhoneState: NOTHING=0, COMING_CALL=1, HOLD=2, HANG_UP=3, GOING_CALL=4, CONNECTED=5
            phoneInfo.phoneState = PhoneState.GOING_CALL  // Исходящий звонок
            phoneInfo.duration = 0
            phoneInfo.connectedStatus = 1  // connected
            phoneInfo.isVehicleCall = 1    // vehicle call

            Log.d(TAG, "Sending phone command via AIDL: phoneInfo=$phoneInfo")
            canBusManager.updatePhoneInfo(phoneInfo)
            Log.i(TAG, "Phone command sent successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send phone command via AIDL", e)
            false
        }
    }

    /**
     * Получить скорость автомобиля (для будущего использования)
     */
    fun getVehicleSpeed(): Int {
        return canBusManager.getVehicleSpeed()
    }

    /**
     * Получить состояние автомобиля (для будущего использования)
     */
    fun getVehicleState(state: VehicleState): Int {
        return canBusManager.getVehicleState(state)
    }

    /**
     * Получить состояние окна (для будущего использования)
     */
    fun getWindowStatus(): com.qinggan.canbus.WindowStatus? {
        return canBusManager.getWindowStatus()
    }

    /**
     * Получить состояние кондиционера (для будущего использования)
     */
    fun getAirCondition(): com.qinggan.canbus.AirCondition? {
        return canBusManager.getAirCondition()
    }
}
