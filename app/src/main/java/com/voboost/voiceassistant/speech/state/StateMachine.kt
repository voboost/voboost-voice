package com.voboost.voiceassistant.speech.state

import android.util.Log
import kotlinx.coroutines.*

/**
 * State Machine для голосового помощника
 *
 * Запускает цикл выполнения состояний:
 * ```
 * var state = initialState
 * while (true) {
 *     state = state.execute()  // ← Цепочка состояний
 * }
 * ```
 *
 * Преимущества:
 * - ✅ Автоматический цикл выполнения
 * - ✅ Явные переходы между состояниями
 * - ✅ Легко добавить новые состояния
 * - ✅ Каждое состояние тестируется отдельно
 * - ✅ Поддержка внешних прерываний (activate/cancel)
 */
class StateMachine(
    private val initialState: State,
    private val scope: CoroutineScope,
    val context: StateContext = StateContext()
) {
    companion object {
        private const val TAG = "StateMachine"
    }

    private var job: Job? = null

    @Volatile
    private var currentState: State = initialState

    // Job текущего состояния (для отмены при внешних прерываниях)
    @Volatile
    private var stateJob: Job? = null

    /**
     * Запустить State Machine
     */
    fun start() {
        if (job?.isActive == true) {
            Log.w(TAG, "Already running, ignoring")
            return
        }

        job = scope.launch {
            try {
                Log.i(TAG, "Starting State Machine from: ${currentState::class.simpleName}")

                var state = currentState
                while (isActive) {
                    // Создаём дочерний job для текущего состояния
                    stateJob = launch {
                        try {
                            state = state.execute()  // ← Цепочка!
                            Log.d(TAG, "State transition: ${currentState::class.simpleName} → ${state::class.simpleName}")
                            currentState = state
                        } catch (e: CancellationException) {
                            // Нормальная ситуация при activate/cancel — состояние уже обновлено через activate()/cancel()
                            Log.d(TAG, "State execution cancelled (normal during activation/cancellation)")
                            // currentState уже обновлён через activate()/cancel(), продолжаем цикл
                        } catch (e: Exception) {
                            Log.e(TAG, "State execution error", e)
                            currentState = state  // Остаёмся на текущем состоянии
                        }
                    }

                    // Ждём завершения состояния
                    stateJob?.join()

                    // Если job был отменён (из activate/cancel) — продолжаем цикл с новым состоянием
                    state = currentState
                }
            } catch (e: CancellationException) {
                Log.i(TAG, "State Machine cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "State Machine error", e)
            }
        }
    }

    /**
     * Остановить State Machine
     */
    fun stop() {
        job?.cancel()
        job = null
    }

    /**
     * Текущее состояние
     */
    fun getCurrentState(): State = currentState

    /**
     * Принудительно перейти к состоянию
     */
    fun transitionTo(state: State) {
        currentState = state
        Log.i(TAG, "Forced transition to: ${state::class.simpleName}")
    }

    /**
     * Отменить текущее состояние
     */
    suspend fun cancel() {
        Log.i(TAG, "Cancel requested in: ${currentState::class.simpleName}")

        // Отменяем текущий job выполнения состояния
        stateJob?.cancel()
        stateJob = null

        val nextState = currentState.cancel()
        if (nextState !== currentState) {
            currentState = nextState
            Log.i(TAG, "Cancel transition to: ${nextState::class.simpleName}")
        }
    }

    /**
     * Активировать помощник
     */
    suspend fun activate() {
        Log.i(TAG, "Activate requested from: ${currentState::class.simpleName}")

        // Отменяем текущий job выполнения состояния (это прервёт blocked first{})
        stateJob?.cancel()
        stateJob = null

        val nextState = currentState.activate()
        if (nextState !== currentState) {
            currentState = nextState
            Log.i(TAG, "Activate transition to: ${nextState::class.simpleName}")
        }
    }
}
