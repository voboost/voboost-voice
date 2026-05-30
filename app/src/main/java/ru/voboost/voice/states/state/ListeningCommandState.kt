package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.recognition.RecognitionServiceResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.audio.VolumeManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.executor.CommandExecutor
import ru.voboost.voice.nlu.INLUEngine
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.ui.ToastMessengerManager
import ru.voboost.voice.ui.VoceAnimationManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Состояние: Слушание команды
 *
 * Логика:
 * 1. Ждём CommandReceived/Timeout/Error
 * 2. Команда > RECOGNIZED_COMMAND, Timeout > TIMEOUT, Error > COMMAND_ERROR
 */
class ListeningCommandState(private val context: StateContext,
                            private var recognitionService: IRecognitionService,
                            private var speechService: ISpeechService,
                            private var configManager: ConfigManager,
                            private var nluEngine: INLUEngine,
                            private val toastMessengerManager: ToastMessengerManager)
    : BaseState() {

    companion object {
        const val TAG = "ListeningCommandState"
    }

    override suspend fun entering() {
        Log.i(TAG, "Waiting for command...")
        recognitionService.setMode(RecognitionService.Mode.COMMAND) // Ждём результат
        val result = recognitionService.results.first {
            it is RecognitionServiceResult.CommandReceived ||
            it is RecognitionServiceResult.Timeout ||
            it is RecognitionServiceResult.Error
        }

        when (result) {
            is RecognitionServiceResult.CommandReceived -> {
                val commandText = result.text
                val zone = result.zone

                if (commandText.isNotEmpty()) {
                    Log.i(TAG,"Command received: '$commandText' (zone=$zone)") // Передаём контекст команд в NLU (если он есть)
                    val contextCmd = context.commandData?.contextCmd ?: emptyList()
                    val recognizedCommand = nluEngine.parseCommand(commandText, contextCmd)
                    context.commandData = recognizedCommand

                    if (recognizedCommand != null) { // Проверяем на неоднозначность
                        if (recognizedCommand.contextCmd.size > 1) {
                            Log.i(TAG, "Ambiguous commands detected in LISTENING_COMMAND")
                            onComplite(StateResult(StateType.AMBIGUOUS))
                            return
                        }
                        // Проверяем нужно ли подтверждение
                        val config = configManager.getCommandById(recognizedCommand.data.id)
                        if (config?.confirmation?.required ?: false) {
                            Log.i(TAG, "Confirmation required for: ${recognizedCommand.data.id}")
                            onComplite(StateResult(StateType.CONFIRMATION))
                        }
                        else { // Выполняем команду без подтверждения
                            Log.i(TAG,
                                  "Executing command without confirmation: ${recognizedCommand.data.id}")
                            onComplite(StateResult(StateType.EXECUTING_COMMAND))
                        }
                    }
                    else {
                        Log.w(TAG, "Unrecognized command: '$commandText'")
                        handleUnrecognizedCommand(commandText)
                        onComplite(StateResult(StateType.IDLE))
                    }
                }
                else {
                    Log.w(TAG, "Empty command received")
                    onComplite(StateResult(StateType.TIMEOUT))
                }
            }

            is RecognitionServiceResult.Timeout -> {
                Log.w(TAG, "Command timeout")
                context.commandData = null
                onComplite(StateResult(StateType.TIMEOUT))
            }

            is RecognitionServiceResult.Error -> {
                Log.e(TAG, "Recognition error: ${result.message}")
                context.commandData = null
                onComplite(StateResult(StateType.COMMAND_ERROR))
            }

            else -> {
                Log.w(TAG, "Unexpected result: $result")
                context.commandData = null
                onComplite(StateResult(StateType.TIMEOUT))
            }
        }
    }

    override suspend fun canceled() = onComplite(StateResult(StateType.CANCEL))

    /**
     * Обработать нераспознанную команду
     */
    suspend fun handleUnrecognizedCommand(text: String) {
        Log.w(TAG, "Command not recognized: $text")
        val notUnderstoodPhrase = configManager.getDefaultPhrase(PhraseType.NOT_UNDERSTOOD) // Показывать уведомление для нераспознанных команд (по умолчанию true)
        if (notUnderstoodPhrase.isNotEmpty()) {
            speechService.enqueueAsync(notUnderstoodPhrase, SpeechService.PRIOR_MEDIUM)
            toastMessengerManager.show(notUnderstoodPhrase)
        }
        else {
            Log.w(CommandExecutor.Companion.TAG, "No phrase for NOT_UNDERSTOOD")
            toastMessengerManager.show("Команда не распознана")
        }
    }
}

