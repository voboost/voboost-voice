package com.voboost.voiceassistant.executor

/**
 * Интерфейс для выполнения команд автомобилю
 * Позволяет легко переключаться между разными способами отправки команд
 */
interface VehicleCommandExecutor {

    /**
     * Выполнить команду
     * @param target Цель команды (например, "Chargport", "Window", "AirConditioner")
     * @param classify Класс команды (числовой идентификатор типа устройства)
     * @param command Команда (0 = открыть/вкл, 1 = закрыть/выкл, и т.д.)
     * @param params Дополнительные параметры команды
     * @return true если команда успешно отправлена
     */
    fun execute(
        target: String,
        classify: Int,
        command: Int,
        params: Map<String, Any> = emptyMap()
    ): Boolean

    /**
     * Выполнить команду для телефона
     * @param classify Класс команды (1 = звонок)
     * @param command Команда (1 = позвонить)
     * @param contact Контакт или номер
     * @param callType Тип вызова ("contact" или "number")
     * @return true если команда успешно отправлена
     */
    fun executePhoneCommand(
        classify: Int,
        command: Int,
        contact: String? = null,
        number: String? = null,
        callType: String = "contact"
    ): Boolean

    /**
     * Название метода выполнения (для логирования)
     */
    val executionMethod: String
}
