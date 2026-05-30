package ru.voboost.voice.states.state

import ru.voboost.voice.states.StateResult
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Интерфейс состояния голосового помощника
 *
 * Каждое состояние:
 * 1. Выполняет свою логику в execute()
 * 2. Вызывает finish(StateResult) или cancelled() через колбэки
 * 3. StateMachine переключает на следующее состояние
 *
 * Это классический IState Machine паттерн:
 * - ? SRP: каждое состояние в отдельном классе
 * - ? OCP: новые состояния без изменения старых
 * - ? Тестируемость: каждое состояние отдельно
 * - ? Event-driven: состояния сами решают когда завершиться
 */
interface IState {

    val isCancelling: AtomicBoolean
    /**
     * Подписка на колбэк завершения (нормальный переход).
     * Вызывается StateMachine при переходе в это состояние.
     */
    fun setCompletionCallback(callback: (StateResult) -> Unit)
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
}


