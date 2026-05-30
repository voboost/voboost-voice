package ru.voboost.voice.states

/**
 * Типы состояний State Machine
 * Используются для перехода без пересоздания экземпляров
 */
enum class StateType {
    IDLE,
    ACTIVATED,
    LISTENING_COMMAND,
    EXECUTING_COMMAND,
    CONFIRMATION,
    COMMAND_ERROR,
    KEYWORD_ERROR,
    TIMEOUT,
    AMBIGUOUS,
    CANCEL
}

/**
 * Результат завершения состояния.
 * Определяет, куда должен переключиться StateMachine.
 */
data class StateResult(val stateType: StateType)


