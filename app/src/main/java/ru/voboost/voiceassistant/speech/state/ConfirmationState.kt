package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.speech.SpeechResult
import kotlinx.coroutines.flow.first
import ru.voboost.voiceassistant.config.ConfigManager.PhraseType
import ru.voboost.voiceassistant.core.QueueSpeechSynthesis

/**
 * Состояние: Подтверждение команды
 *
 * Логика:
 * 1. Сказать вопрос подтверждения
 * 2. Ждём ответ (да/нет/таймаут)
 * 3. Да → EXECUTING_COMMAND, Нет/Таймаут → IDLE
 */
class ConfirmationState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "Confirmation"
    }

    override val canCancel = true

    override suspend fun execute() {
        val command = context.recognizedCommand ?: run {
            Log.e(TAG, "No recognized command in context")
            finish(StateResult.Next(StateType.IDLE))
            return
        }

        Log.i(TAG, "Entering CONFIRMATION IState for: ${command.id}")

        try {
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.COMMAND)

            // Спрашиваем подтверждение
            val question = context.nluEngine?.getConfirmationQuestion(command.config)
            if (!question.isNullOrEmpty()) {
                context.queueSpeech?.enqueueAsync(question)
            }

            // Ждём ответ
            val result = context.speechRecognizer?.results?.first {
                it is SpeechResult.CommandReceived || it is SpeechResult.Timeout || it is SpeechResult.Error
            }

            when (result) {
                is SpeechResult.CommandReceived -> {
                    val answer = result.text.lowercase().trim()
                    if (context.nluEngine?.isConfirmationYes(answer, command.config) == true) {
                        Log.i(TAG, "Confirmation: YES")
                        finish(StateResult.Next(StateType.EXECUTING_COMMAND))
                    }
                    else {
                        Log.i(TAG, "Confirmation: NO")
                        finish(StateResult.Next(StateType.IDLE))
                    }
                }

                is SpeechResult.Timeout -> {
                    Log.w(TAG, "Confirmation timeout")
                    finish(StateResult.Next(StateType.IDLE))
                }

                is SpeechResult.Error -> {
                    Log.e(TAG, "Confirmation error: ${result.message}")
                    finish(StateResult.Next(StateType.COMMAND_ERROR))
                }

                else -> {
                    Log.w(TAG, "Unexpected result during confirmation: $result")
                    finish(StateResult.Next(StateType.IDLE))
                }
            }

        }
        catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "ConfirmationState cancelled")
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in ConfirmationState", e)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        context.soundEffectManager?.playEndSoundAsync()
        kotlinx.coroutines.delay(400)
        val cancelPhrase = context.configManager?.getDefaultPhrase(PhraseType.CANCEL)
        if(!cancelPhrase.isNullOrEmpty())
        {
            context.queueSpeech?.enqueueAsync(cancelPhrase, QueueSpeechSynthesis.PRIOR_HIGH)
        }

        context.overlayManager?.hideAnimation()
        context.volumeManager?.restoreMedia()
        context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)

        cancelled("ConfirmationState cancelled by user")
    }
}
