package ru.voboost.voice.nlu

import android.util.Log
import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.executor.CommandData

/**
 * Базовый класс для NLU движков.
 * Реализует общую логику подтверждения и обработки исключений.
 */
abstract class BaseNluEngine(protected val configManager: ConfigManager) : INLUEngine {

    companion object {
        const val TAG = "BaseNluEngine"
    }

    /**
     * Основная логика парсинга (разная для каждого движка).
     * Реализуется в подклассах.
     */
    protected abstract fun doParseCommand(text: String, contextCmd: List<String>): CommandData?

    override fun parseCommand(text: String, contextCmd: List<String>): CommandData? {
        return try {
            doParseCommand(text, contextCmd)
        } catch (e: Exception) {
            Log.e(TAG, "NLU inference failed", e)
            null
        }
    }

    // === ОБЩИЕ МЕТОДЫ ДЛЯ ПОДТВЕРЖДЕНИЯ ===

    override fun isConfirmationYes(text: String, commandConfig: CommandConfig?): Boolean {
        val yesPatterns = configManager.getYesPatterns()
        return ((commandConfig?.confirmation?.yesPatterns ?: emptyList()) + yesPatterns).any {
            text.lowercase().trim() == it.lowercase().trim()
        }
    }

    override fun isConfirmationNo(text: String, commandConfig: CommandConfig): Boolean {
        val noPatterns = configManager.getNoPatterns()
        return ((commandConfig.confirmation.noPatterns ?: emptyList()) + noPatterns).any {
            text.lowercase().trim() == it.lowercase().trim()
        }
    }

    override fun requiresConfirmation(commandConfig: CommandConfig): Boolean =
        commandConfig.confirmation.required

    override fun getConfirmationQuestion(commandConfig: CommandConfig?): String =
        commandConfig?.confirmation?.question ?: "Подтверждаете?"

    override fun getConfirmationTimeout(commandConfig: CommandConfig): Int =
        commandConfig.confirmation.timeoutSec ?: 5
}
