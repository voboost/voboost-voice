package ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner

import android.util.Log
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Изменить температуру климата на заданное смещение
 * Учитывает зону говорящего из voiceParams:
 * - front_left → только левая сторона
 * - front_right → только правая сторона
 * - center / all_location / second_* → обе стороны
 *
 * @param commandId ID команды (ac_temp_up / ac_temp_down)
 * @param offsetDelta Смещение температуры (+2 или -2)
 *
 * config.json:
 *   id: "ac_temp_up", classify: 5, command: 3
 *   id: "ac_temp_down", classify: 5, command: 3
 */
class AirConditionerTempOffsetHandler(
    private val canBusManager: CanBusServiceManager,
    private val offsetDelta: Int
) : ICommandHandler {

    companion object {
        const val TAG = "AirConditionerTempOffset"
        private const val TEMP_MIN = 16
        private const val TEMP_MAX = 32
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val airCondition = canBusManager.getAirCondition()
        val currentTemp = airCondition?.airLeftTemperature?.toInt() ?: 22

        val newTemp = (currentTemp + offsetDelta).coerceIn(TEMP_MIN, TEMP_MAX)
        val sign = if (offsetDelta > 0) "+" else ""
        val zone = voiceParams["_zone"] as? String

        Log.d(TAG, "Temp $sign${offsetDelta}: $currentTemp°C → $newTemp°C (zone=$zone)")

        return canBusManager.setTemperatureByZone(zone, newTemp)
    }
}
