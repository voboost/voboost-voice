package ru.voboost.voice.speech.state

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.speech.SpeechRecognizer
import kotlinx.coroutines.CancellationException
import ru.voboost.voice.core.QueueSpeechSynthesis

/**
 * Состояние: Ошибка ключевого слова
 *
 * Логика:
 * 1. Сказать "Не понял"
 * 2. > finish(StateResult.Next(StateType.IDLE))
 */
class KeywordErrorState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "KeywordError"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.e(TAG, "Entering KEYWORD_ERROR IState")

        try {
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.MUTED)

            val notUnderstoodPhrase =
                context.configManager?.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD)

            if (!notUnderstoodPhrase.isNullOrEmpty()) {
                context.queueSpeech?.enqueueAsync(notUnderstoodPhrase, QueueSpeechSynthesis.PRIOR_HIGH)
            }

            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        }
        catch (e: CancellationException) {
            Log.d(TAG, "KeywordErrorState cancelled")
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in KeywordErrorState", e)
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "KeywordErrorState cancelled (button pressed)")
        context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
        cancelled("KeywordErrorState cancelled by user")
    }
}


