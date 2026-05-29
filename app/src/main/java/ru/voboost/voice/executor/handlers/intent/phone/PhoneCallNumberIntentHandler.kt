package ru.voboost.voice.executor.handlers.intent.phone

import android.content.Context
import android.content.Intent
import android.util.Log
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.intent.AbstractIntentHandler

/**
 * Звонок по номеру через Broadcast Intent
 */
class PhoneCallNumberIntentHandler(context: Context)
    : AbstractIntentHandler(context) {

    companion object {
        private const val TAG = "PhoneCallNumberCmd"
        private const val PARAM_NUMBER = "number"

        // Карта для перевода слов-цифр в строковые символы
        private val RUSSIAN_DIGITS = mapOf(
            "ноль" to "0", "нолик" to "0",
            "один" to "1", "единица" to "1",
            "два" to "2", "двойка" to "2",
            "три" to "3", "тройка" to "3",
            "четыре" to "4", "четверка" to "4",
            "пять" to "5", "пятерка" to "5",
            "шесть" to "6", "шестерка" to "6",
            "семь" to "7", "семерка" to "7",
            "восемь" to "8", "восьмерка" to "8",
            "девять" to "9", "девятка" to "9"
                                          )

        // Карта десятков и сотен, если водитель диктует "девятьсот", "двадцать" и т.д.
        private val RUSSIAN_COMPLEX_NUMBERS = mapOf(
            "десять" to "10", "одиннадцать" to "11", "одинадцать" to "11",
            "двенадцать" to "12", "тринадцать" to "13", "четырнадцать" to "14",
            "пятнадцать" to "15", "шестнадцать" to "16", "семнадцать" to "17",
            "восемнадцать" to "18", "девятнадцать" to "19",
            "двадцать" to "20", "тридцать" to "30", "сорок" to "40",
            "пятьдесят" to "50", "шестьдесят" to "60", "семьдесят" to "70",
            "восемьдесят" to "80", "девяносто" to "90",
            "сто" to "100", "двести" to "200", "триста" to "300", "четыреста" to "400",
            "пятьсот" to "500", "шестьсот" to "600", "семьсот" to "700",
            "восемьсот" to "800", "девятьсот" to "900"
                                                   )
    }

    /**
     * Парсим сырую фразу от Vosk и собираем из неё сплошную строку номера телефона
     */
    override fun parsParams(commandData: CommandData): Map<String, String> {
        val params = super.parsParams(commandData).toMutableMap()

        val words = commandData.phrase.lowercase().trim().split(Regex("\\s+"))
        val phoneBuilder = StringBuilder()

        for (word in words) {
            // 1. Если Vosk выдал слово готовой цифрой (например, "8" или "112")
            if (word.toIntOrNull() != null) {
                phoneBuilder.append(word)
                continue
            }

            // 2. Если это простое слово-цифра ("девятка", "три")
            val simpleDigit = RUSSIAN_DIGITS[word]
            if (simpleDigit != null) {
                phoneBuilder.append(simpleDigit)
                continue
            }

            // 3. Если водитель диктует сотнями/десятками ("девятьсот", "двадцать")
            val complexDigit = RUSSIAN_COMPLEX_NUMBERS[word]
            if (complexDigit != null) {
                phoneBuilder.append(complexDigit)
            }
        }

        val resultNumber = phoneBuilder.toString()

        // Считаем номер валидным, если в нем есть хотя бы 3 цифры (для экстренных служб 112, 02)
        if (resultNumber.length >= 3) {
            params[PARAM_NUMBER] = resultNumber
            // Кладем это же значение в contact, чтобы TTS мог сказать: "Звоню на номер 8911..."
            params["contact"] = resultNumber
        }

        return params
    }

    override fun buildIntent(voiceParams: Map<String, Any>): Intent? {
        val phoneNumber = voiceParams[PARAM_NUMBER] as? String ?: ""

        Log.d(TAG, "Phone call to number: '$phoneNumber' Action: $ACTION_IVOKA_PHONE_CALL")

        if (phoneNumber.isNullOrEmpty()) {
            return null // Базовый класс автоматически вернет NEGATIVE_RESULT
        }

        return Intent(ACTION_IVOKA_PHONE_CALL).apply {
            putExtra(EXTRA_IVOKA_CALL_INFO, phoneNumber)
            putExtra(EXTRA_SCREEN_INT, 0)
            putExtra(EXTRA_MAC, "")
        }
    }
}
