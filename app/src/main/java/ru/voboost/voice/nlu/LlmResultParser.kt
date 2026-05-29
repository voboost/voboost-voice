package ru.voboost.voice.nlu

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.executor.CommandData

class LlmResultParser(private val configManager: ConfigManager) {

    companion object {
        private const val TAG = "LlmResultParser"
    }

    /**
     * Парсит текстовый ответ от LLM вида "id_команды|значение_параметра"
     * и превращает его в валидный объект CommandData для системы.
     *
     * @param llmResponse Ответ от OnnxPureLlmEngine (например: "ac_set_temp|24" или "phone_call_contact|маме")
     * @param originalPhrase Исходный сырой текст из Vosk (например: "установи температуру двадцать два градуса")
     */
    fun parse(llmResponse: String, originalPhrase: String): CommandData? {
        val cleanResult = llmResponse.trim()
        if (cleanResult.isEmpty()) {
            Log.w(TAG, "LLM returned an empty response")
            return null
        }

        Log.d(TAG, "Parsing LLM response: '$cleanResult' for phrase: '$originalPhrase'")

        // Разделяем строку по символу "|"
        val parts = cleanResult.split("|")
        if (parts.isEmpty()) {
            Log.w(TAG, "LLM response format is invalid (missing '|' separator)")
            return null
        }

        // Первый элемент — это всегда ID команды (удаляем лишние пробелы)
        val commandId = parts[0].trim()

        // Если модель явно вернула неизвестный интент, выходим
        if (commandId == "unknown" || commandId.isEmpty()) {
            Log.d(TAG, "LLM recognized intent as 'unknown'")
            return null
        }

        // Ищем конфигурацию команды в вашем ConfigManager (проверяем, существует ли такой ID)
        val commandConfig = configManager.getCommandById(commandId)
        if (commandConfig == null) {
            Log.w(TAG, "LLM returned commandId '$commandId', but it does not exist in config.json")
            return null
        }

        // Возвращаем готовый CommandData.
        // Передаем внутрь ОРИГИНАЛЬНУЮ фразу от Vosk, чтобы наши хэндлеры климата
        // и звонков со стеммингом сами вытащили оттуда точные данные.
        return CommandData(
            data = commandConfig,
            phrase = originalPhrase
                          )
    }
}
