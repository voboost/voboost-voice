package ru.voboost.voice.speech.state

import android.util.Log
import ru.voboost.voice.speech.SpeechRecognizer
import kotlinx.coroutines.CancellationException
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.core.QueueSpeechSynthesis

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
            kotlinx.coroutines.delay(200)

            // Говорим "Отмена"
            val cancelPhrase = context.configManager?.getDefaultPhrase(PhraseType.CANCEL)
            if(!cancelPhrase.isNullOrEmpty())
            {
                context.queueSpeech?.enqueueAsync(cancelPhrase, QueueSpeechSynthesis.PRIOR_HIGH)
            }

            context.overlayManager?.hideAnimation()
            context.volumeManager?.restoreMedia()

            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        }
        catch (e: CancellationException) {
            Log.d(TAG, "TimeoutState cancelled")
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in TimeoutState", e)
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "TimeoutState cancelled (button pressed)")
        context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
        cancelled("TimeoutState cancelled by user")
    }
}


