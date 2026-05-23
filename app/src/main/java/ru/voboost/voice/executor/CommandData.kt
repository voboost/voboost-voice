package ru.voboost.voice.executor

import ru.voboost.voice.config.CommandConfig

/**
 * Распознанная команда
 */
data class CommandData(val id: String,
                       val config: CommandConfig,
                       val matchedPattern: String,
                       val extractedParams: Map<String, String> = emptyMap(),
                       val zone: String? = null)


