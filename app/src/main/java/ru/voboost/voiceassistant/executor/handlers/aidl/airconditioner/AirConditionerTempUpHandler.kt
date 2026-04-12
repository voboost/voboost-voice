package ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Мне холодно → увеличить температуру на 2°C
 *
 * config.json:
 *   id: "ac_temp_up", classify: 5, command: 3
 */
class AirConditionerTempUpHandler(
    private val canBusManager: CanBusServiceManager
) : ICommandHandler {

    override val commandId: String = "ac_temp_up"

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val airCondition = canBusManager.getAirCondition()
        val currentTemp = airCondition?.airLeftTemperature?.toInt() ?: 22

        // Ограничиваем диапазон 16-32
        val newTemp = (currentTemp + 2).coerceIn(16, 32)

        Log.d(TAG, "Temp UP: $currentTemp°C → $newTemp°C")

        return canBusManager.setAirConditionState(AirConditionState.AC_LEFT_TEMP, newTemp)
    }

    companion object {
        const val TAG = "AirConditionerTempUp"
    }
}
