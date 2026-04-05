package com.voboost.voiceassistant.executor

import com.voboost.voiceassistant.config.ActionConfig

/**
 * Интерфейс для выполнения команд автомобилю
 * Позволяет легко переключаться между разными способами отправки команд
 * (AIDL, Broadcast Intent, Shell)
 *
 * Все команды (включая телефонные) выполняются через единый executeByCommandId()
 */
interface IVehicleCommandExecutor {

    /**
     * Выполнить команду по ID
     *
     * @param commandId ID команды из config.json
     *                 Например: "window_open", "ac_set_temp", "phone_call_contact"
     * @param config Конфигурация команды (target, classify, command, static params)
     * @param voiceParams Параметры, распознанные из голоса ({temp}, {contact}, {number})
     * @return true если команда успешно отправлена
     */
    fun executeByCommandId(
        commandId: String,
        config: ActionConfig,
        voiceParams: Map<String, Any> = emptyMap()
    ): Boolean

    /**
     * Название метода выполнения (для логирования)
     */
    val executionMethod: String
}
