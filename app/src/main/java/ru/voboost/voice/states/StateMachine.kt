package ru.voboost.voice.states

import android.util.Log
import kotlinx.coroutines.*
import ru.voboost.voice.states.state.ActivatedState
import ru.voboost.voice.states.state.CommandErrorState
import ru.voboost.voice.states.state.ConfirmationState
import ru.voboost.voice.states.state.ExecutingCommandState
import ru.voboost.voice.states.state.IState
import ru.voboost.voice.states.state.IdleState
import ru.voboost.voice.states.state.KeywordErrorState
import ru.voboost.voice.states.state.ListeningCommandState
import ru.voboost.voice.states.state.RecognizedCommandState

/**
 * State Machine для голосового помощника (Event-driven версия)
 *
 * Принцип работы:
 * 1. Все состояния создаются один раз при инициализации
 * 2. StateMachine подписывается на completionCallback и cancellationCallback
 * 3. Состояние само вызывает finish() или cancelled() когда готово
 * 4. StateMachine переключает на следующее состояние по типу
 * 5. Перед переходом вызывается reset() для сброса внутреннего состояния
 *
 * Преимущества:
 * - ? Нет циклических зависимостей между состояниями
 * - ? Нет пересоздания при каждом переходе
 * - ? Состояния сами решают когда завершиться
 * - ? canCancel явно декларирует можно ли отменить
 */
class StateMachine(private val scope: CoroutineScope,
                   private val context: StateContext) {
    companion object {
        const val TAG = "StateMachine"
    }

    private val states = mapOf(
        StateType.IDLE to IdleState(context),
        StateType.ACTIVATED to ActivatedState(context),
        StateType.LISTENING_COMMAND to ListeningCommandState(context),
        StateType.RECOGNIZED_COMMAND to RecognizedCommandState(context),
        StateType.EXECUTING_COMMAND to ExecutingCommandState(context),
        StateType.CONFIRMATION to ConfirmationState(context),
        StateType.COMMAND_ERROR to CommandErrorState(context),
        StateType.KEYWORD_ERROR to KeywordErrorState(context),
        StateType.TIMEOUT to TimeoutState(context),
    )

    private var mainJob: Job? = null
    @Volatile
    private var currentState: IState = states[StateType.IDLE]!!
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
            transitionTo(StateType.IDLE)
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
    fun getCurrentState(): IState = currentState

    /**
     * Получить состояние по типу
     */
    private fun getState(type: StateType): IState {
        return states[type] ?: throw IllegalArgumentException("Unknown state: $type")
    }

    /**
     * Перейти к новому состоянию по типу.
     * Подписывается на колбэки и запускает execute() в фоне.
     */
    private fun transitionTo(stateType: StateType) { // Отменяем предыдущее выполнение
        executionJob?.cancel()
        executionJob = null

        val nextState = getState(stateType)
        nextState.reset()

        currentState = nextState
        Log.d(TAG, "Transition to: ${nextState::class.simpleName}")

        // Подписываемся на колбэки
        nextState.setCompletionCallback { result ->
            when (result) {
                is StateResult.Next -> {
                    Log.d(TAG, "Completion > ${result.stateType}")
                    transitionTo(result.stateType)
                }
                is StateResult.Cancel -> {
                    Log.d(TAG, "Completion with Cancel > IDLE")
                    transitionTo(StateType.IDLE)
                }
            }
        }

        nextState.setCancellationCallback { reason ->
            Log.d(TAG, "Cancellation: $reason > IDLE")
            transitionTo(StateType.IDLE)
        }

        // Запускаем execute() в фоне
        executionJob = scope.launch {
            try {
                nextState.execute()
            }
            catch (e: CancellationException) {
                Log.d(TAG, "State execution cancelled (normal during activation/cancellation)")
            }
            catch (e: Exception) {
                Log.e(TAG, "State execution error", e)
                transitionTo(StateType.IDLE)
            }
        }
    }

    /**
     * Обработать нажатие кнопки.
     * Если состояние можно отменить > вызывает cancel().
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
        Log.i(TAG, "Button pressed > cancelling ${current::class.simpleName}")
        // Отменяем текущее выполнение
        executionJob?.cancel()
        executionJob = null
        // Вызываем cancel() состояния
        current.cancel()
    }

    /**
     * Активировать помощник (кнопка или keyword).
     * Если можно отменить > onButtonPressed(), иначе активирует.
     */
    suspend fun activate() {
        val current = currentState

        if (current.canCancel) {
            onButtonPressed()
        }
        else {
            Log.i(TAG, "Activate from non-cancellable state: ${current::class.simpleName}")
            val nextState = current.activate()
            if (nextState != null && nextState !== current) { // activate() возвращает IState — используем его напрямую
                executionJob?.cancel()
                executionJob = null
                currentState = nextState
                currentState.reset()
                currentState.setCompletionCallback { result ->
                    when (result) {
                        is StateResult.Next -> transitionTo(result.stateType)
                        is StateResult.Cancel -> transitionTo(StateType.IDLE)
                    }
                }
                currentState.setCancellationCallback { reason ->
                    Log.d(TAG, "Cancellation: $reason > IDLE")
                    transitionTo(StateType.IDLE)
                }
                executionJob = scope.launch {
                    try {
                        currentState.execute()
                    }
                    catch (e: CancellationException) {
                        Log.d(TAG, "State execution cancelled")
                    }
                    catch (e: Exception) {
                        Log.e(TAG, "State execution error", e)
                        transitionTo(StateType.IDLE)
                    }
                }
            }
        }
    }
}

