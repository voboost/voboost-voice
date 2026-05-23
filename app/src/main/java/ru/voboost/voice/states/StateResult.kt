package ru.voboost.voice.states

/**
 * Типы состояний State Machine
 * Используются для перехода без пересоздания экземпляров
 */
enum class StateType {
    IDLE,
    ACTIVATED,
    LISTENING_COMMAND,
    RECOGNIZED_COMMAND,
    EXECUTING_COMMAND,
    CONFIRMATION,
    COMMAND_ERROR,
    KEYWORD_ERROR,
    TIMEOUT
}

/**
 * Результат завершения состояния.
 * Определяет, куда должен переключиться StateMachine.
 */
sealed class StateResult {
    /**
     * Нормальное завершение > переход к следующему состоянию
     */
    data class Next(val stateType: StateType) : StateResult()

    /**
     * Отмена пользователем > всегда возврат в IdleState
     */
    data class Cancel(val reason: String = "User cancelled") : StateResult()
}


