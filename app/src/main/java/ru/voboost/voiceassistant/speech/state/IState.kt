package ru.voboost.voiceassistant.speech.state

/**
 * Интерфейс состояния голосового помощника
 *
 * Каждое состояние:
 * 1. Выполняет свою логику в execute()
 * 2. Вызывает finish(StateResult) или cancelled() через колбэки
 * 3. StateMachine переключает на следующее состояние
 *
 * Это классический IState Machine паттерн:
 * - ✅ SRP: каждое состояние в отдельном классе
 * - ✅ OCP: новые состояния без изменения старых
 * - ✅ Тестируемость: каждое состояние отдельно
 * - ✅ Event-driven: состояния сами решают когда завершиться
 */
interface IState {
    /**
     * Может ли это состояние быть отменено кнопкой.
     * false = кнопка игнорируется (например, во время выполнения команды)
     */
    val canCancel: Boolean
        get() = false

    /**
     * Подписка на колбэк завершения (нормальный переход).
     * Вызывается StateMachine при переходе в это состояние.
     */
    fun setCompletionCallback(callback: (StateResult) -> Unit)

    /**
     * Подписка на колбэк отмены (пользователь/ошибка).
     * Вызывается StateMachine при переходе в это состояние.
     */
    fun setCancellationCallback(callback: (String) -> Unit)

    /**
     * Выполнить логику состояния.
     * В конце должно вызвать finish(StateResult.Next(...)) или cancelled().
     */
    suspend fun execute()

    /**
     * Отменить текущее состояние (вызывается при нажатии кнопки).
     * Должно вызвать cancelled() когда отмена завершена.
     */
    suspend fun cancel()

    /**
     * Активировать помощник (перейти к слушанию команды).
     * Только для IdleState → ActivatedState.
     */
    suspend fun activate(): IState? = null

    /**
     * Сбросить внутреннее состояние перед повторным использованием.
     * Вызывается StateMachine перед каждым transitionTo().
     */
    fun reset()
}
