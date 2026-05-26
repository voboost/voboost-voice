package ru.voboost.voice.executor.handlers.aidl.airconditioner

import android.util.Log
import ru.voboost.voice.audio.MultiChannelAudioSource
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

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
class AirConditionerTempOffsetHandler(private val canBusManager: CanBusServiceManager,
                                      private val offsetDelta: Int)
    : ICommandHandler {

    companion object {
        const val TAG = "AirConditionerTempOffset"
        private const val TEMP_MIN = 16
        private const val TEMP_MAX = 32
    }

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return ICommandHandler.NEGATIVE_RESULT
        }

        val airCondition = canBusManager.getAirCondition()
        val currentTemp = airCondition?.airLeftTemperature?.toInt() ?: 22

        val newTemp = (currentTemp + offsetDelta).coerceIn(TEMP_MIN, TEMP_MAX)
        val sign = if (offsetDelta > 0) "+" else ""
        val parsParams = parsParams(commandData)
        val zone = parsParams["_zone"]

        Log.d(TAG, "Temp $sign${offsetDelta}: $currentTemp°C → $newTemp°C (zone=$zone)")

        val result = canBusManager.setTemperatureByZone(zone, newTemp)
        return CommandResult(result)
    }

    private fun parsParams(commandData: CommandData): Map<String, String> {
        val paramsText : MutableMap<String, String> = mutableMapOf()
        paramsText["_zone"] = commandData.zone ?: MultiChannelAudioSource.ZONE_FRONT_LEFT
        return paramsText;
    }
}


