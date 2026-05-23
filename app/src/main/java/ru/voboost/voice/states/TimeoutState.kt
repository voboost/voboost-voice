package ru.voboost.voice.states

import android.util.Log
import ru.voboost.voice.services.recognition.RecognitionService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.states.state.BaseState

/**
 * Состояние: Таймаут распознавания
 *
 * Логика:
 * 1. Сказать "Отмена" + звук
 * 2. > finish(StateResult.Next(StateType.IDLE))
 */
class TimeoutState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "Timeout"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.w(TAG, "Entering TIMEOUT IState")

        try { // Звук окончания
            context.soundEffectManager?.playEndSoundAsync()
            delay(200)

            // Говорим "Отмена"
            val cancelPhrase = context.configManager?.getDefaultPhrase(PhraseType.CANCEL)
            if(!cancelPhrase.isNullOrEmpty())
            {
                context.speechService?.enqueueAsync(cancelPhrase, SpeechService.PRIOR_HIGH)
            }

            context.overlayManager?.hideAnimation()
            context.volumeManager?.restoreMedia()

            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        }
        catch (e: CancellationException) {
            Log.d(TAG, "TimeoutState cancelled")
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in TimeoutState", e)
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "TimeoutState cancelled (button pressed)")
        context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
        cancelled("TimeoutState cancelled by user")
    }
}


