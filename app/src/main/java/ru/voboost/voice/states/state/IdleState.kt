package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.recognition.RecognitionServiceResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType

/**
 * Состояние: Ожидание ключевого слова
 *
 * Логика:
 * 1. Скрыть анимацию, восстановить громкость
 * 2. Ждём KeywordDetected из SpeechRecognizer
 * 3. > finish(StateResult.Next(StateType.ACTIVATED))
 */
class IdleState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "IdleState"
    }

    override suspend fun execute() {
        Log.i(TAG, "Entering IDLE IState - waiting for keyword...")

        try {
            context.overlayManager?.hideAnimation()
            context.volumeManager?.restoreMedia()

            // Ждём ключевое слово из SharedFlow
            val result =
                context.recognitionService?.results?.first { it is RecognitionServiceResult.KeywordDetected }
            val keywordText = (result as RecognitionServiceResult.KeywordDetected).text
            val zone = result.zone
            Log.i(TAG, "?? Keyword detected: '$keywordText' (zone=$zone)")
            context.zone = zone

            // Ключевое слово получено > ACTIVATED
            finish(StateResult.Next(StateType.ACTIVATED))

        }
        catch (e: CancellationException) {
            Log.d(TAG, "IdleState coroutine cancelled (normal during activation)")
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in IdleState", e)
            finish(StateResult.Next(StateType.KEYWORD_ERROR))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "Cancel in IdleState - returning to IdleState")
        context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
        cancelled("IdleState cancelled")
    }

    override suspend fun activate(): IState? {
        Log.i(TAG, "Activate from IdleState > ACTIVATED")
        finish(StateResult.Next(StateType.ACTIVATED))
        return null
    }

    override fun reset() {
        context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
    }
}


