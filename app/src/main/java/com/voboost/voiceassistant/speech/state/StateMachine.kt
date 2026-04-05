package com.voboost.voiceassistant.speech.state

import android.util.Log
import kotlinx.coroutines.*

/**
 * State Machine для голосового помощника (Event-driven версия)
 *
 * Принцип работы:
 * 1. StateMachine подписывается на completionCallback и cancellationCallback
 * 2. Состояние само вызывает finish() или cancelled() когда готово
 * 3. StateMachine переключает на следующее состояние
 *
 * Преимущества:
 * - ✅ Состояния сами решают когда завершиться
 * - ✅ Нет гонок с currentState
 * - ✅ Чистое разделение ответственности
 * - ✅ canCancel явно декларирует можно ли отменить
 */
class StateMachine(
    private val initialState: State,
    private val scope: CoroutineScope,
    val context: StateContext = StateContext()
) {
    companion object {
        private const val TAG = "StateMachine"
    }

    private var mainJob: Job? = null

    @Volatile
    private var currentState: State = initialState

    @Volatile
    private var executionJob: Job? = null

    /**
     * Запустить State Machine
     */
    fun start() {
        if (mainJob?.isActive == true) {
            Log.w(TAG, "Already running, ignoring")
            return
        }

        mainJob = scope.launch {
            Log.i(TAG, "Starting State Machine from: ${currentState::class.simpleName}")
            transitionTo(currentState)
        }
    }

    /**
     * Остановить State Machine
     */
    fun stop() {
        mainJob?.cancel()
        executionJob?.cancel()
        mainJob = null
        executionJob = null
    }

    /**
     * Текущее состояние
     */
    fun getCurrentState(): State = currentState

    /**
     * Перейти к новому состоянию.
     * Подписывается на колбэки и запускает execute() в фоне.
     */
    private fun transitionTo(state: State) {
        // Отменяем предыдущее выполнение
        executionJob?.cancel()
        executionJob = null

        currentState = state
        Log.d(TAG, "Transition to: ${state::class.simpleName}")

        // Подписываемся на колбэки
        state.setCompletionCallback { result ->
            when (result) {
                is StateResult.Next -> {
                    Log.d(TAG, "Completion → ${result.state::class.simpleName}")
                    transitionTo(result.state)
                }
                is StateResult.Cancel -> {
                    Log.d(TAG, "Completion with Cancel → IdleState")
                    transitionTo(createIdleState())
                }
            }
        }

        state.setCancellationCallback { reason ->
            Log.d(TAG, "Cancellation: $reason → IdleState")
            transitionTo(createIdleState())
        }

        // Запускаем execute() в фоне
        executionJob = scope.launch {
            try {
                state.execute()
            } catch (e: CancellationException) {
                Log.d(TAG, "State execution cancelled (normal during activation/cancellation)")
            } catch (e: Exception) {
                Log.e(TAG, "State execution error", e)
                transitionTo(createIdleState())
            }
        }
    }

    /**
     * Обработать нажатие кнопки.
     * Если состояние можно отменить → вызывает cancel().
     */
    suspend fun onButtonPressed() {
        val current = currentState

        if (!current.canCancel) {
            Log.d(TAG, "Button ignored in ${current::class.simpleName}")
            return
        }

        if (context.isCancelling.get()) {
            Log.d(TAG, "Cancellation already in progress, ignoring")
            return
        }

        Log.i(TAG, "Button pressed → cancelling ${current::class.simpleName}")

        // Отменяем текущее выполнение
        executionJob?.cancel()
        executionJob = null

        // Вызываем cancel() состояния
        current.cancel()
    }

    /**
     * Активировать помощник (кнопка или keyword).
     * Если можно отменить → onButtonPressed(), иначе активирует.
     */
    suspend fun activate() {
        val current = currentState

        if (current.canCancel) {
            onButtonPressed()
        } else {
            Log.i(TAG, "Activate from non-cancellable state: ${current::class.simpleName}")
            val nextState = current.activate()
            if (nextState != null && nextState !== current) {
                transitionTo(nextState)
            }
        }
    }

    /**
     * Создать IdleState с актуальным контекстом.
     */
    private fun createIdleState(): State {
        return IdleState(
            speechRecognizer = context.speechRecognizer!!,
            overlayManager = context.overlayManager!!,
            volumeManager = context.volumeManager,
            ttsEngine = context.ttsEngine!!,
            configManager = context.configManager!!,
            nluEngine = context.nluEngine!!,
            commandExecutor = context.commandExecutor!!,
            context = context
        )
    }
}
