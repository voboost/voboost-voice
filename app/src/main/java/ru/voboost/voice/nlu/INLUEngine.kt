package ru.voboost.voice.nlu

import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.executor.CommandData

interface INLUEngine {

    fun parseCommand(text: String, contextCmd: List<String> = emptyList()): CommandData?
    fun isConfirmationYes(text: String, commandConfig: CommandConfig?): Boolean
    fun isConfirmationNo(text: String, commandConfig: CommandConfig): Boolean
    fun requiresConfirmation(commandConfig: CommandConfig): Boolean
    fun getConfirmationQuestion(commandConfig: CommandConfig?): String
    fun getConfirmationTimeout(commandConfig: CommandConfig): Int

    /**
     * Освободить ресурсы движка (для ONNX/LLM моделей)
     */
    fun release()
}

