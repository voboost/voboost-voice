package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.recognition.RecognitionServiceResult
import kotlinx.coroutines.flow.first
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.nlu.INLUEngine
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType

/**
 * Состояние: Подтверждение команды
 *
 * Логика:
 * 1. Сказать вопрос подтверждения
 * 2. Ждём ответ (да/нет/таймаут)
 * 3. Да > EXECUTING_COMMAND, Нет/Таймаут > IDLE
 */
class ConfirmationState(private val context: StateContext,
                        private var recognitionService: IRecognitionService,
                        private var speechService: ISpeechService,
                        private var configManager: ConfigManager,
                        private var nluEngine: INLUEngine)
    : BaseState() {
    companion object {
        const val TAG = "ConfirmationState"
    }

    override suspend fun entering() {
        val commandData = context.commandData ?: run {
            Log.e(TAG, "No recognized command in context")
            onComplite(StateResult(StateType.IDLE))
            return
        }
        Log.i(TAG, "Entering CONFIRMATION IState for: ${commandData.data.id}")
        recognitionService.setMode(RecognitionService.Mode.COMMAND)
        // Спрашиваем подтверждение
        val config = configManager.getCommandById(commandData.data.id)
        val question = nluEngine.getConfirmationQuestion(config)
        if (question.isNotEmpty()) {
            speechService.enqueueAsync(question)
        }
        // Ждём ответ
        val result = recognitionService.results.first {
            it is RecognitionServiceResult.CommandReceived ||
            it is RecognitionServiceResult.Timeout ||
            it is RecognitionServiceResult.Error
        }

        when (result) {
            is RecognitionServiceResult.CommandReceived -> {
                val answer = result.text.lowercase().trim()
                if (nluEngine.isConfirmationYes(answer, config)) {
                    Log.i(TAG, "Confirmation: YES")
                    onComplite(StateResult(StateType.EXECUTING_COMMAND))
                }
                else {
                    Log.i(TAG, "Confirmation: NO")
                    onComplite(StateResult(StateType.IDLE))
                }
            }
            is RecognitionServiceResult.Timeout -> {
                Log.w(TAG, "Confirmation timeout")
                onComplite(StateResult(StateType.IDLE))
            }
            is RecognitionServiceResult.Error -> {
                Log.e(TAG, "Confirmation error: ${result.message}")
                onComplite(StateResult(StateType.COMMAND_ERROR))
            }
            else -> {
                Log.w(TAG, "Unexpected result during confirmation: $result")
                onComplite(StateResult(StateType.IDLE))
            }
        }
    }

    override suspend fun canceled() = onComplite(StateResult(StateType.CANCEL))
}


