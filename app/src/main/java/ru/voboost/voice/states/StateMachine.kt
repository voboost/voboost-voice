package ru.voboost.voice.states

import android.util.Log
import kotlinx.coroutines.*
import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.audio.VolumeManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.executor.CommandExecutor
import ru.voboost.voice.nlu.INLUEngine
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.states.state.ActivatedState
import ru.voboost.voice.states.state.AmbiguousState
import ru.voboost.voice.states.state.CancelState
import ru.voboost.voice.states.state.ConfirmationState
import ru.voboost.voice.states.state.ExecutingCommandState
import ru.voboost.voice.states.state.IState
import ru.voboost.voice.states.state.IdleState
import ru.voboost.voice.states.state.ListeningCommandState
import ru.voboost.voice.states.state.RetryCommandState
import ru.voboost.voice.ui.ToastMessengerManager
import ru.voboost.voice.ui.VoceAnimationManager

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
                   recognitionService: IRecognitionService,
                   voceAnimationManager: VoceAnimationManager,
                   volumeManager: VolumeManager,
                   speechService: ISpeechService,
                   configManager: ConfigManager,
                   nluEngine: INLUEngine,
                   commandExecutor: CommandExecutor,
                   soundEffectManager: SoundEffectManager,
                   toastMessengerManager: ToastMessengerManager) {
    companion object {
        const val TAG = "StateMachine"
    }

    private val states: MutableMap<StateType, IState> = mutableMapOf()
    private var mainJob: Job? = null
    @Volatile private var currentState: IState? = null
    @Volatile private var executionJob: Job? = null

    init {
        val stateContext = StateContext()

        states[StateType.IDLE] = IdleState(stateContext,
                                           recognitionService,
                                           voceAnimationManager,
                                           volumeManager)
        states[StateType.ACTIVATED] = ActivatedState(stateContext,
                                                     recognitionService,
                                                     voceAnimationManager,
                                                     volumeManager,
                                                     speechService,
                                                     configManager,
                                                     soundEffectManager)
        states[StateType.LISTENING_COMMAND] = ListeningCommandState(stateContext,
                                                                    recognitionService,
                                                                    speechService,
                                                                    configManager,
                                                                    nluEngine,
                                                                    toastMessengerManager)

        states[StateType.CONFIRMATION] = ConfirmationState(stateContext,
                                                           recognitionService,
                                                           speechService,
                                                           configManager,
                                                           nluEngine)
        states[StateType.EXECUTING_COMMAND] = ExecutingCommandState(stateContext,
                                                                    recognitionService,
                                                                    commandExecutor)
        states[StateType.AMBIGUOUS] = AmbiguousState(stateContext,
                                                     recognitionService,
                                                     speechService,
                                                     configManager,
                                                     soundEffectManager)
        states[StateType.RETRY_COMMAND] = RetryCommandState(stateContext,
                                                            speechService,
                                                            configManager,
                                                            soundEffectManager,
                                                            toastMessengerManager)
        states[StateType.CANCEL] = CancelState(ConfigManager.PhraseType.CANCEL,
                                               recognitionService,
                                               speechService,
                                               configManager,
                                               soundEffectManager,
                                               toastMessengerManager)
        states[StateType.COMMAND_ERROR]  = CancelState(ConfigManager.PhraseType.FAILURE,
                                                       recognitionService,
                                                       speechService,
                                                       configManager,
                                                       soundEffectManager,
                                                       toastMessengerManager)
        states[StateType.KEYWORD_ERROR]  = CancelState(ConfigManager.PhraseType.NOT_UNDERSTOOD,
                                                       recognitionService,
                                                       speechService,
                                                       configManager,
                                                       soundEffectManager,
                                                       toastMessengerManager)
        states[StateType.TIMEOUT] = CancelState(ConfigManager.PhraseType.CANCEL,
                                                recognitionService,
                                                speechService,
                                                configManager,
                                                soundEffectManager,
                                                toastMessengerManager)
    }

    /**
     * Запустить State Machine
     */
    fun start() {
        if (mainJob?.isActive == true) {
            Log.w(TAG, "Already running, ignoring")
            return
        }
        mainJob = scope.launch {
            transitionTo(StateType.IDLE)
            Log.i(TAG, "Starting State Machine from: ${StateType.IDLE}")
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
    fun getCurrentState(): IState? = currentState

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

        currentState = nextState
        Log.d(TAG, "Transition to: ${nextState::class.simpleName}")

        // Подписываемся на колбэки
        nextState.setCompletionCallback { result ->
             Log.d(TAG, "Completion > ${result.stateType}")
             transitionTo(result.stateType)
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
     * Активировать помощник (кнопка или keyword).
     * Если можно отменить > onButtonPressed(), иначе активирует.
     */
    suspend fun activate() {
        val current = currentState ?: return

        if (!current.isCancelling.get()) {
            Log.i(TAG,
                  "Button pressed > cancelling ${current::class.simpleName}") // Отменяем текущее выполнение
            executionJob?.cancel()
            executionJob = null // Вызываем cancel() состояния
            current.cancel()
            return
        }
    }
}

