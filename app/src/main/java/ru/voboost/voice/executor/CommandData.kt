package ru.voboost.voice.executor

import ru.voboost.voice.config.CommandConfig

/**
 * Распознанная команда
 */
data class CommandData(val data: CommandConfig,
                       val phrase: String,
                       val zone: String? = null)


