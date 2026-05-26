package ru.voboost.voice.executor.handlers

import ru.voboost.voice.executor.CommandData

/**
 * Универсальный обработчик одной команды
 * Все команды (окна, кондиционер, телефон и т.д.) используют этот интерфейс
 *
 * Каждая конкретная реализация получает свои зависимости через конструктор:
 * - CAN-зависимые: CanBusServiceManager
 * - Intent-зависимые: Context
 * - Shell-зависимые: ничего не нужно
 *
 * @property commandId Уникальный ID команды (совпадает с config.json)
 *                     Например: "window_open", "ac_set_temp", "phone_call_contact"
 */
interface ICommandHandler {

    companion object {
       val NEGATIVE_RESULT = CommandResult(false)
       val POSITIVE_RESULT = CommandResult(false)
    }
    /**
     * Выполнить команду
     *
     * @param voiceParams Параметры, распознанные из голоса ({temp}, {contact}, {number} и т.д.)
     * @return true если команда успешно выполнена
     */
    fun execute(commandData: CommandData): CommandResult
}

data class CommandResult(val result: Boolean,
                         val extractedParams: Map<String, String> = emptyMap())


