package ru.voboost.voice.states.state

import android.util.Log
import kotlinx.coroutines.CancellationException
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.states.state.ExecutingCommandState.Companion.TAG
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Базовый класс состояния с поддержкой колбэков.
 * StateMachine подписывается на completionCallback и cancellationCallback.
 * Состояние вызывает finish() или cancelled() когда готово перейти дальше.
 */
abstract class BaseState : IState {

    override val isCancelling: AtomicBoolean = AtomicBoolean(false)
    protected open val executeError: StateResult =StateResult(StateType.COMMAND_ERROR)

    private var completionCallback: ((StateResult) -> Unit)? = null

    override fun setCompletionCallback(callback: (StateResult) -> Unit) {
        completionCallback = callback
    }
    /**
     * Вызывается когда состояние завершилось нормально.
     */
    protected fun onComplite(result: StateResult) {
        completionCallback?.invoke(result) ?: run {
            throw IllegalStateException("completionCallback not set in ${this::class.simpleName}")
        }
    }

    override suspend fun execute() {
        try {
            entering()
        }
        catch (e: CancellationException) {
            Log.d(TAG, "Cancelled")
        }
        catch (e: Exception) { // Очищаем контекст при ошибке
            Log.e(TAG, "Error: ", e)
            onComplite(executeError)
        }
    }

    protected abstract suspend fun entering()

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            canceled()
            isCancelling.set(false)
        }
    }

    protected abstract suspend fun canceled()
}