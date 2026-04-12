package ru.voboost.voiceassistant.executor.handlers

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

    val commandId: String

    /**
     * Выполнить команду
     *
     * @param voiceParams Параметры, распознанные из голоса ({temp}, {contact}, {number} и т.д.)
     * @return true если команда успешно выполнена
     */
    fun execute(voiceParams: Map<String, Any>): Boolean
}
