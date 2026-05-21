package ru.voboost.voice.speech.state

/**
 * Базовый класс состояния с поддержкой колбэков.
 * StateMachine подписывается на completionCallback и cancellationCallback.
 * Состояние вызывает finish() или cancelled() когда готово перейти дальше.
 */
abstract class BaseState : IState {
    private var completionCallback: ((StateResult) -> Unit)? = null
    private var cancellationCallback: ((String) -> Unit)? = null

    override fun setCompletionCallback(callback: (StateResult) -> Unit) {
        completionCallback = callback
    }

    override fun setCancellationCallback(callback: (String) -> Unit) {
        cancellationCallback = callback
    }

    /**
     * Вызывается когда состояние завершилось нормально.
     */
    protected fun finish(result: StateResult) {
        completionCallback?.invoke(result) ?: run {
            throw IllegalStateException("completionCallback not set in ${this::class.simpleName}")
        }
    }

    /**
     * Вызывается когда состояние отменено (пользователь/таймаут/ошибка).
     */
    protected fun cancelled(reason: String = "User cancelled") {
        cancellationCallback?.invoke(reason) ?: run {
            throw IllegalStateException("cancellationCallback not set in ${this::class.simpleName}")
        }
    }

    /**
     * Сбросить состояние по умолчанию — ничего не делает.
     * Переопределите если нужно сбросить внутренние поля.
     */
    override fun reset() { }
}


