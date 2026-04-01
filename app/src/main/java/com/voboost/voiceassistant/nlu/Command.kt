package com.voboost.voiceassistant.nlu

/**
 * Распознанная команда
 */
data class RecognizedCommand(
    val id: String,
    val config: com.voboost.voiceassistant.config.CommandConfig,
    val matchedPattern: String,
    val extractedParams: Map<String, String> = emptyMap()
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
