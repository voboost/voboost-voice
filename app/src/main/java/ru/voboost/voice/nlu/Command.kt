package ru.voboost.voice.nlu

import ru.voboost.voice.config.CommandConfig

/**
 * Распознанная команда
 */
data class RecognizedCommand(
    val id: String,
    val config: CommandConfig,
    val matchedPattern: String,
    val extractedParams: Map<String, String> = emptyMap(),
    val zone: String? = null
)

/**
 * Результат подтверждения пользователем
 */
enum class ConfirmationResult {
    CONFIRMED,      // Пользователь подтвердил
    CANCELLED,      // Пользователь отменил
    TIMEOUT,        // Истекло время ожидания
    NOT_REQUIRED    // Подтверждение не требуется
}


