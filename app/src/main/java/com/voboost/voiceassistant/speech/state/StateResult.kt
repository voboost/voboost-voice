package com.voboost.voiceassistant.speech.state

/**
 * Результат завершения состояния.
 * Определяет, куда должен переключиться StateMachine.
 */
sealed class StateResult {
    /**
     * Нормальное завершение → переход к следующему состоянию
     */
    data class Next(val state: State) : StateResult()

    /**
     * Отмена пользователем → всегда возврат в IdleState
     */
    data class Cancel(val reason: String = "User cancelled") : StateResult()
}
