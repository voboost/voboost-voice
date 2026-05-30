package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.recognition.RecognitionServiceResult
import kotlinx.coroutines.flow.first
import ru.voboost.voice.audio.VolumeManager
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.ui.VoceAnimationManager

/**
 * Состояние: Ожидание ключевого слова
 *
 * Логика:
 * 1. Скрыть анимацию, восстановить громкость
 * 2. Ждём KeywordDetected из SpeechRecognizer
 * 3. > finish(StateResult.Next(StateType.ACTIVATED))
 */
class IdleState(private val context: StateContext,
                private var recognitionService: IRecognitionService,
                private var voceAnimationManager: VoceAnimationManager,
                private var volumeManager: VolumeManager)
    : BaseState() {
    companion object {
        const val TAG = "IdleState"
    }

    override val executeError: StateResult = StateResult(StateType.KEYWORD_ERROR)

    override suspend fun entering() {
        Log.i(TAG, "Waiting for keyword...")
        voceAnimationManager.hide()
        volumeManager.restoreMedia()
        
        // Очищаем контекст в начале нового цикла распознавания
        context.commandData = null
        context.attemptsCount = 0
        
        recognitionService.setMode(RecognitionService.Mode.KEYWORD)

        val result = recognitionService.results.first { it is RecognitionServiceResult.KeywordDetected }
                as RecognitionServiceResult.KeywordDetected

        Log.i(TAG, "Keyword detected: '${result.text}' (zone=${result.zone})")

        onComplite(StateResult(StateType.ACTIVATED))
    }

    override suspend fun canceled() {
        Log.i(TAG, "Canceled")
        onComplite(StateResult(StateType.ACTIVATED))
    }
}


