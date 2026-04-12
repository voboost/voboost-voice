package ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Установить температуру кондиционера
 *
 * Учитывает зону говорящего из voiceParams:
 * - front_left → только левая сторона
 * - front_right → только правая сторона
 * - center / all_location / second_* → обе стороны
 *
 * config.json:
 *   id: "ac_set_temp", classify: 5, command: 3
 *   params: temperature
 */
class AirConditionerSetTempHandler(
    canBusManager: CanBusServiceManager
) : AbstractAirConditionerHandler("ac_set_temp", canBusManager) {
    override fun getAirConditionStateAndValue(voiceParams: Map<String, Any>): Pair<AirConditionState, Int> {
        val temperature = voiceParams["temperature"] as? Int
            ?: run {
                Log.w(TAG, "Temperature parameter not found, using default 22")
                22
            }

        Log.d(TAG, "Set temperature: $temperature°C")
        // Возвращаем любое значение — AbstractAirConditionerHandler
        // не учитывает зону, но это OK т.к. execute() переопределён
        // ниже для учёта зоны
        return AirConditionState.AC_LEFT_TEMP to temperature
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val temperature = voiceParams["temperature"] as? Int
            ?: run {
                Log.w(TAG, "Temperature parameter not found, using default 22")
                22
            }

        val zone = voiceParams["_zone"] as? String
        Log.d(TAG, "Set temperature: $temperature°C (zone=$zone)")

        return canBusManager.setTemperatureByZone(zone, temperature)
    }
}
