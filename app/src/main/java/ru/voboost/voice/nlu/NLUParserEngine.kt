package ru.voboost.voice.nlu

import android.util.Log
import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.executor.CommandData
import java.util.regex.Pattern

/**
 * NLU Engine - парсинг и понимание команд
 * Сопоставляет распознанный текст с шаблонами команд из конфига
 */
class NLUParserEngine(private val configManager: ConfigManager)
    : INLUEngine {

    companion object {
        const val TAG = "NLUEngine"
    }

    /**
     * Распарсить команду из текста
     * @return RecognizedCommand или null если команда не найдена
     */
    override fun parseCommand(text: String, contextCmdIds: List<String>): CommandData? {
        val normalizedText = text.lowercase().trim()
        Log.d(TAG, "Parsing command: '$text' -> normalized: '$normalizedText'")

        val commands = configManager.getConfig().commands

        for (commandConfig in commands) {
            if (!commandConfig.enabled) {
                continue
            }

            for (pattern in commandConfig.patterns) {
                val matchResult = matchPattern(normalizedText, pattern)

                if (matchResult != null) { // Log.i(TAG, "Matched command '${commandConfig.id}' with pattern '$pattern'")
                    return CommandData(data = commandConfig, phrase = text)
                }
            }
        }

        Log.w(TAG, "No matching command found for: '$text'")
        return null
    }

    /**
     * Проверить, является ли текст подтверждением "Да"
     */
    override fun isConfirmationYes(text: String, commandConfig: CommandConfig?): Boolean {
        val normalizedText = text.lowercase().trim()

        // Проверяем паттерны из конфига команды
        commandConfig?.confirmation?.yesPatterns?.forEach { pattern ->
            if (normalizedText == pattern.lowercase().trim()) {
                return true
            }
        }
        return INLUEngine.DEFAULT_YES.any { normalizedText == it }
    }

    /**
     * Проверить, является ли текст отменой "Нет"
     */
    override fun isConfirmationNo(text: String, commandConfig: CommandConfig): Boolean {
        val normalizedText = text.lowercase().trim()
        // Проверяем паттерны из конфига команды
        commandConfig.confirmation.noPatterns?.forEach { pattern ->
            if (normalizedText == pattern.lowercase().trim()) {
                return true
            }
        }
        return INLUEngine.DEFAULT_NO.any { normalizedText == it }
    }

    /**
     * Проверить, требует ли команда подтверждения
     */
    override fun requiresConfirmation(commandConfig: CommandConfig): Boolean =
        commandConfig.confirmation.required

    /**
     * Получить вопрос для подтверждения
     */
    override fun getConfirmationQuestion(commandConfig: CommandConfig?): String =
        commandConfig?.confirmation?.question ?: "Подтверждаете выполнение команды?"

    /**
     * Получить таймаут подтверждения
     */
    override fun getConfirmationTimeout(commandConfig: CommandConfig): Int =
        commandConfig.confirmation.timeoutSec ?: 5

    /**
     * Сопоставить текст с шаблоном
     * @return Map с извлеченными параметрами или null если нет совпадения
     */
    private fun matchPattern(text: String,
                             pattern: String): Boolean { // Простое точное совпадение
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
    override fun release() {
        // Нет ресурсов для освобождения
    }
}


