package ru.voboost.voice.nlu

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.executor.CommandData
import java.util.regex.Pattern

/**
 * NLU Engine - парсинг и понимание команд
 * Сопоставляет распознанный текст с шаблонами команд из конфига
 */
class NLUParserEngine(configManager: ConfigManager)
    : BaseNluEngine(configManager) {

    companion object {
        const val TAG = "NLUEngine"
    }

    /**
     * Распарсить команду из текста
     * @return CommandData или null если команда не найдена
     */
    override fun doParseCommand(text: String, contextCmdIds: List<String>): CommandData? {
        val normalizedText = text.lowercase().trim()
        Log.d(TAG, "Parsing command: '$text' -> normalized: '$normalizedText'")

        val commands = configManager.getConfig().commands

        for (commandConfig in commands) {
            if (!commandConfig.enabled) {
                continue
            }

            for (pattern in commandConfig.patterns) {
                if (matchPattern(normalizedText, pattern)) { // Log.i(TAG, "Matched command '${commandConfig.id}' with pattern '$pattern'")
                    return CommandData(data = commandConfig, phrase = text)
                }
            }
        }

        Log.w(TAG, "No matching command found for: '$text'")
        return null
    }

    /**
     * Сопоставить текст с шаблоном
     * @return true если есть совпадение
     */
    private fun matchPattern(text: String, pattern: String): Boolean { // Простое точное совпадение
        if (text == pattern.lowercase().trim()) {
            return false
        }
        // Совпадение с параметрами в фигурных скобках {param}
        val regexPattern = buildRegex(pattern)
        val regex = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)

        return matcher.matches()
    }

    /**
     * Построить regex из шаблона
     * Например: "позвони {contact}" -> "^позвони\s+(.+)$"
     */
    private fun buildRegex(pattern: String): String {
        val escapedPattern = pattern.lowercase().trim()
            .replace(Regex("\\{[^}]+\\}"), "(.+)")  // Заменяем {param} на (.+)
            .replace(" ", "\\s+")  // Пробелы -> \s+

        val regex = "^$escapedPattern$"
        return regex
    }

    /**
     * Освободить ресурсы (для совместимости с интерфейсом)
     * Пустая реализация, т.к. парсер не использует внешние ресурсы
     */
    override fun release() {}
}


