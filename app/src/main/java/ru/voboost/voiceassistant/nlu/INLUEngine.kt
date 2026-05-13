package ru.voboost.voiceassistant.nlu

import ru.voboost.voiceassistant.config.CommandConfig

interface INLUEngine {

    companion object {
        val DEFAULT_YES = listOf("да",
                                 "ага",
                                 "угу",
                                 "подтверждаю",
                                 "ок",
                                 "открывай",
                                 "давай",
                                 "конечно",
                                 "yes",
                                 "yeah")
        val DEFAULT_NO = listOf("нет",
                                "не надо",
                                "отмена",
                                "отмени",
                                "не",
                                "отбой",
                                "стоп",
                                "no",
                                "nah")
    }

    fun parseCommand(text: String): RecognizedCommand?
    fun isConfirmationYes(text: String, commandConfig: CommandConfig): Boolean
    fun isConfirmationNo(text: String, commandConfig: CommandConfig): Boolean
    fun requiresConfirmation(commandConfig: CommandConfig): Boolean
    fun getConfirmationQuestion(commandConfig: CommandConfig): String
    fun getConfirmationTimeout(commandConfig: CommandConfig): Int
}