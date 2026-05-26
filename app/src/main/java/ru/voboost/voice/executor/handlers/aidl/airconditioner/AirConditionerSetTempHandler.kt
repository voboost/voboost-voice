package ru.voboost.voice.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.executor.handlers.ICommandHandler
import ru.voboost.voice.services.canbus.CanBusServiceManager

/**
 * Установить температуру кондиционера
 *
 * Учитывает зону говорящего из voiceParams:
 * - front_left > только левая сторона
 * - front_right > только правая сторона
 * - center / all_location / second_* > обе стороны
 *
 * config.json:
 *   id: "ac_set_temp", classify: 5, command: 3
 *   params: temperature
 */
class AirConditionerSetTempHandler(canBusManager: CanBusServiceManager) :
        AbstractAirConditionerHandler(canBusManager) {

    companion object {
        private const val TAG = "AirConditionerCmd"
        private val RUSSIAN_NUMBERS = mapOf("ноль" to 0,
                                            "один" to 1,
                                            "два" to 2,
                                            "три" to 3,
                                            "четыре" to 4,
                                            "пять" to 5,
                                            "шесть" to 6,
                                            "семь" to 7,
                                            "восемь" to 8,
                                            "девять" to 9,
                                            "десять" to 10,
                                            "одинадцать" to 11,
                                            "двенадцать" to 12,
                                            "тринадцать" to 13,
                                            "четырнадцать" to 14,
                                            "пятнадцать" to 15,
                                            "шестнадцать" to 16,
                                            "семнадцать" to 17,
                                            "восемнадцать" to 18,
                                            "девятнадцать" to 19,
                                            "двадцать" to 20,
                                            "двадцать один" to 21,
                                            "двадцать два" to 22,
                                            "двадцать три" to 23,
                                            "двадцать четыре" to 24,
                                            "двадцать пять" to 25,
                                            "двадцать шесть" to 26,
                                            "двадцать семь" to 27,
                                            "двадцать восемь" to 28,
                                            "двадцать девять" to 29,
                                            "тридцать" to 30,
                                            "тридцать один" to 31,
                                            "тридцать два" to 32)
    }

    /**
     * Распознать число из текста (поддержка русских числительных 0-32)
     * Также обрабатывает обычные цифры: "24" > 24
     */
    private fun parseTemperature(raw: String): Int {
        val text = raw.lowercase().trim()

        // Прямое совпадение
        RUSSIAN_NUMBERS[text]?.let { return it }

        // Цифры: "24" или "24.0"
        text.toFloatOrNull()?.let { return it.toInt() }

        // Комбинации: "двадцать" + " четыре"
        val parts = text.split(Regex("\\s+"))
        if (parts.size == 2) {
            val tens = RUSSIAN_NUMBERS[parts[0]] ?: 0
            val ones = RUSSIAN_NUMBERS[parts[1]] ?: 0
            if (tens > 0 || ones > 0) return tens + ones
        }

        Log.w(TAG, "Unknown temperature format: '$text', using default 22")
        return 22
    }

    override fun getAirConditionStateAndValue(voiceParams: Map<String, String>): Pair<AirConditionState, Int> {
        val rawTemp = voiceParams["temp"]?: "22"
        val temperature = parseTemperature(rawTemp)
        Log.d(TAG, "Set temperature: $temperature°C (raw='$rawTemp')")
        return AirConditionState.AC_LEFT_TEMP to temperature
    }

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return ICommandHandler.NEGATIVE_RESULT
        }
        val parsParams = parsParams(commandData)
        val rawTemp = parsParams["temp"]?: "22"
        val temperature = parseTemperature(rawTemp)
        val zone = parsParams["_zone"]
        Log.d(TAG, "Set temperature: $temperature°C (raw='$rawTemp', zone=$zone)")

        val result = canBusManager.setTemperatureByZone(zone, temperature)
        return CommandResult(result, parsParams)
    }
}


