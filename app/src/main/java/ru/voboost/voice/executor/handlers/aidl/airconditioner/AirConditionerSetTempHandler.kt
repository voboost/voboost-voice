package ru.voboost.voice.executor.handlers.aidl.airconditioner

import android.util.Log
import ru.voboost.voice.audio.MultiChannelAudioSource
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
class AirConditionerSetTempHandler(private val canBusManager: CanBusServiceManager) :
        ICommandHandler {

    companion object {
        private const val TAG = "AirConditionerCmd"
        private const val PARAM_NAME = "temp"
        private const val ZONE_NAME = "_zone"
        private const val MIN_TEMP = 18

        // Оптимизированный словарь (только базовые числительные)
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
                                            "одиннадцать" to 11,
                                            "одинадцать" to 11, // Vosk может выдать оба варианта
                                            "двенадцать" to 12,
                                            "тринадцать" to 13,
                                            "четырнадцать" to 14,
                                            "пятнадцать" to 15,
                                            "шестнадцать" to 16,
                                            "семнадцать" to 17,
                                            "восемнадцать" to 18,
                                            "девятнадцать" to 19,
                                            "двадцать" to 20,
                                            "тридцать" to 30)
    }

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return ICommandHandler.NEGATIVE_RESULT
        }
        val parsParams = parsParams(commandData)
        val temperature = parsParams[PARAM_NAME]?.toIntOrNull()
        if (temperature == null || temperature < MIN_TEMP) {
            return ICommandHandler.NEGATIVE_RESULT
        }
        val zone = parsParams[ZONE_NAME]
        Log.d(TAG, "Set temperature: $temperature°C (raw='$temperature', zone=$zone)")

        val result = canBusManager.setTemperatureByZone(zone, temperature)
        return CommandResult(result, parsParams)
    }

    /**
     * Распознать число из текста (поддержка русских числительных)
     * Также обрабатывает обычные цифры: "24" > 24
     */
    private fun parsParams(commandData: CommandData): MutableMap<String, String> {
        val params = mutableMapOf<String, String>()
        params[ZONE_NAME] = commandData.zone ?: MultiChannelAudioSource.ZONE_FRONT_LEFT

        val words = commandData.phrase.lowercase().trim().split(Regex("\\s+"))
        var totalSum = 0

        for (word in words) {
            val digit = word.toIntOrNull()
            if (digit != null) {
                params[PARAM_NAME] = digit.toString()
                return params
            }
            // Если это слово-число ("двадцать")
            val value = RUSSIAN_NUMBERS[word]
            if (value != null) {
                totalSum += value
            }
        }

        params[PARAM_NAME] = totalSum.toString()
        return params
    }
}
