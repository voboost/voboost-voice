package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.recognition.RecognitionService
import kotlinx.coroutines.CancellationException
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType

/**
 * Состояние: Ошибка ключевого слова
 *
 * Логика:
 * 1. Сказать "Не понял"
 * 2. > finish(StateResult.Next(StateType.IDLE))
 */
class KeywordErrorState(private val context: StateContext) : BaseState() {

    companion object {
        const val TAG = "KeywordErrorState"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.e(TAG, "Entering KEYWORD_ERROR IState")
        try {
            context.recognitionService?.setMode(RecognitionService.Mode.MUTED)
            val notUnderstoodPhrase =
                context.configManager?.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD)

            if (!notUnderstoodPhrase.isNullOrEmpty()) {
                context.speechService?.enqueueAsync(notUnderstoodPhrase, SpeechService.PRIOR_HIGH)
            }
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
        catch (e: CancellationException) {
            Log.d(TAG, "KeywordErrorState cancelled")
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            throw e
        }
        catch (e: Exception) {
            Log.e(TAG, "Error in KeywordErrorState", e)
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "KeywordErrorState cancelled (button pressed)")
        context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
        cancelled("KeywordErrorState cancelled by user")
    }
}


