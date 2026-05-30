package ru.voboost.voice.states.state

import android.util.Log
import kotlinx.coroutines.CancellationException
import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.ui.ToastMessengerManager
import java.util.concurrent.atomic.AtomicBoolean

class CancelState(private val phraseType: ConfigManager.PhraseType,
                  private var recognitionService: IRecognitionService,
                  private var speechService: ISpeechService,
                  private var configManager: ConfigManager,
                  private var soundEffectManager: SoundEffectManager,
                  private val toastMessengerManager: ToastMessengerManager)
    : BaseState() {

    companion object {
        const val TAG = "ActivatedState"
    }

    override val isCancelling: AtomicBoolean = AtomicBoolean(true)

    override suspend fun execute() {
        try {
            entering()
        }
        catch (e: CancellationException) {
            Log.d(TAG, "Cancelled")
        }
        catch (e: Exception) {
            Log.e(TAG, "Error canceled", e)
        }

        onComplite(StateResult(StateType.IDLE))
    }

    override suspend fun entering() {
        recognitionService.setMode(RecognitionService.Mode.MUTED)
        // Одинаковый звук отмены с TimeoutState
        soundEffectManager.playEndSoundAsync()
        // Говорим "Отмена" с высоким приоритетом
        val cancelPhrase = configManager.getDefaultPhrase(phraseType)
        if(cancelPhrase.isNotEmpty())
        {
            toastMessengerManager.show(cancelPhrase)
            speechService.enqueueAsync(cancelPhrase, SpeechService.Companion.PRIOR_HIGH)
        }

        Log.i(TAG, "Cancelled")
    }

    override suspend fun canceled(){}
}