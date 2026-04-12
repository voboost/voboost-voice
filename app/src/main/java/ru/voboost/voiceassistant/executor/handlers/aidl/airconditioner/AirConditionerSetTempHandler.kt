package ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Установить температуру кондиционера
 *
 * config.json:
 *   id: "ac_set_temp", classify: 5, command: 3
 *   params: temperature
 *
 * CAN-шина:
 *   Отправляет абсолютное значение температуры
 *   (в отличие от инкрементов/декрементов)
 *
 * Примечание: если на реальном автомобиле нужны
 * инкременты/декременты до нужной температуры,
 * логику нужно будет доработать.
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
        // Отправляем абсолютное значение температуры
        return AirConditionState.AC_LEFT_TEMP to temperature
    }
}
