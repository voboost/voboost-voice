package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.recognition.RecognitionServiceResult
import kotlinx.coroutines.flow.first
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.nlu.INLUEngine
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType

/**
 * Состояние: Слушание команды
 *
 * Логика:
 * 1. Ждём CommandReceived/Timeout/Error
 * 2. Команда > RECOGNIZED_COMMAND, Timeout > TIMEOUT, Error > COMMAND_ERROR
 */
class ListeningCommandState(private val context: StateContext,
                            private var recognitionService: IRecognitionService,
                            private var configManager: ConfigManager,
                            private var nluEngine: INLUEngine)
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

                        // Проверяем лимит попыток
                        val maxAttempts = configManager.getConfig().speech.offline.maxAttempts

                        if (context.attemptsCount < maxAttempts - 1) {
                            // Есть ещё попытки → переход в RETRY_COMMAND
                            Log.i(TAG, "Unrecognized command, retry attempt ${context.attemptsCount + 1}")
                            onComplite(StateResult(StateType.RETRY_COMMAND))
                        }
                        else {
                            // Попытки исчерпаны → COMMAND_ERROR (фразу выдаст CommandErrorState)
                            context.commandData = null
                            onComplite(StateResult(StateType.COMMAND_ERROR))
                        }
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
}

