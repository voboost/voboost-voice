package ru.voboost.voice.states.state

import android.util.Log
import kotlinx.coroutines.CancellationException
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.recognition.RecognitionServiceResult
import kotlinx.coroutines.flow.first
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.services.speech.SpeechService
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
class ConfirmationState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "ConfirmationState"
    }

    override val canCancel = true

    override suspend fun execute() {
        val commandData = context.commandData ?: run {
            Log.e(TAG, "No recognized command in context")
            finish(StateResult.Next(StateType.IDLE))
            return
        }
        Log.i(TAG, "Entering CONFIRMATION IState for: ${commandData.data.id}")
        try {
            context.recognitionService?.setMode(RecognitionService.Mode.COMMAND)
            // Спрашиваем подтверждение
            val config = context.configManager?.getCommandById(commandData.data.id)
            val question = context.nluEngine?.getConfirmationQuestion(config)
            if (!question.isNullOrEmpty()) {
                context.speechService?.enqueueAsync(question)
            }
            // Ждём ответ
            val result = context.recognitionService?.results?.first {
                it is RecognitionServiceResult.CommandReceived ||
                it is RecognitionServiceResult.Timeout ||
                it is RecognitionServiceResult.Error
            }

            when (result) {
                is RecognitionServiceResult.CommandReceived -> {
                    val answer = result.text.lowercase().trim()
                    if (context.nluEngine?.isConfirmationYes(answer, config) == true) {
                        Log.i(TAG, "Confirmation: YES")
                        finish(StateResult.Next(StateType.EXECUTING_COMMAND))
                    }
                    else {
                        Log.i(TAG, "Confirmation: NO")
                        finish(StateResult.Next(StateType.IDLE))
                    }
                }
                is RecognitionServiceResult.Timeout -> {
                    Log.w(TAG, "Confirmation timeout")
                    finish(StateResult.Next(StateType.IDLE))
                }
                is RecognitionServiceResult.Error -> {
                    Log.e(TAG, "Confirmation error: ${result.message}")
                    finish(StateResult.Next(StateType.COMMAND_ERROR))
                }
                else -> {
                    Log.w(TAG, "Unexpected result during confirmation: $result")
                    finish(StateResult.Next(StateType.IDLE))
                }
            }
        }
        catch (e: CancellationException) {
            Log.d(TAG, "ConfirmationState cancelled")
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            throw e
        }
        catch (e: Exception) {
            Log.e(TAG, "Error in ConfirmationState", e)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        context.soundEffectManager?.playEndSoundAsync()
        val cancelPhrase = context.configManager?.getDefaultPhrase(PhraseType.CANCEL)
        if(!cancelPhrase.isNullOrEmpty())
        {
            context.speechService?.enqueueAsync(cancelPhrase, SpeechService.PRIOR_HIGH)
        }

        context.voceAnimationManager?.hide()
        context.volumeManager?.restoreMedia()
        context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)

        cancelled("ConfirmationState cancelled by user")
    }
}


