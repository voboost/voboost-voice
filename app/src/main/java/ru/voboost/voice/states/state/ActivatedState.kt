package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.audio.VolumeManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.ui.VoceAnimationManager

/**
 * Состояние: Активация (после ключевого слова)
 *
 * Логика:
 * 1. Показать анимацию
 * 2. Приглушить музыку
 * 3. Добавить "Слушаю вас" в очередь (низкий приоритет)
 * 4. > finish(StateResult.Next(StateType.LISTENING_COMMAND))
 */
class ActivatedState(private val context: StateContext,
                     private var recognitionService: IRecognitionService,
                     private var voceAnimationManager: VoceAnimationManager,
                     private var volumeManager: VolumeManager,
                     private var speechService: ISpeechService,
                     private var configManager: ConfigManager,
                     private var soundEffectManager: SoundEffectManager)
    : BaseState() {

    companion object {
        const val TAG = "ActivatedState"
    }

    override suspend fun entering() {
        Log.i(TAG, "Entering state")
        context.attemptsCount = 0
        // ОТКЛЮЧИТЬ распознавание пока TTS говорит (чтобы не было ЭХО)
        recognitionService.setMode(RecognitionService.Mode.MUTED)
        // Звук начала распознавания
        soundEffectManager.playStartSoundAsync()
        // Показать анимацию и приглушить музыку
        voceAnimationManager.show()
        volumeManager.duckMedia(targetVolume = 1)
        // Сказать что слушаем (опционально)
        val listeningPhrase = configManager.getDefaultPhrase(PhraseType.LISTENING)
        if (listeningPhrase.isNotEmpty()) { // Низкий приоритет, так как это обычная фраза
            speechService.enqueueAsync(listeningPhrase, SpeechService.PRIOR_LOW)
        }
        else {
            Log.w(TAG, "Listening phrase is null or empty")
        }
        // Переходим к слушанию команды
        onComplite(StateResult(StateType.LISTENING_COMMAND))
    }

    override suspend fun canceled() = onComplite(StateResult(StateType.CANCEL))
}

