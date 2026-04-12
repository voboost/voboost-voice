package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.speech.SpeechResult
import ru.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Состояние: Ожидание ключевого слова
 *
 * Логика:
 * 1. Скрыть анимацию, восстановить громкость
 * 2. Ждём KeywordDetected из SpeechRecognizer
 * 3. → finish(StateResult.Next(StateType.ACTIVATED))
 */
class IdleState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: ISpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : BaseState() {
    companion object {
        const val TAG = "IdleState"
    }

    override suspend fun execute() {
        Log.i(TAG, "Entering IDLE IState - waiting for keyword...")

        try {
            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()

            // Ждём ключевое слово из SharedFlow
            val result = speechRecognizer.results.first { it is SpeechResult.KeywordDetected }
            val keywordText = (result as SpeechResult.KeywordDetected).text
            val zone = result.zone
            Log.i(TAG, "🎯 Keyword detected: '$keywordText' (zone=$zone)")
            context.zone = zone

            // Ключевое слово получено → ACTIVATED
            finish(StateResult.Next(StateType.ACTIVATED))

        } catch (e: CancellationException) {
            Log.d(TAG, "IdleState coroutine cancelled (normal during activation)")
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error in IdleState", e)
            finish(StateResult.Next(StateType.KEYWORD_ERROR))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "Cancel in IdleState - returning to IdleState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        cancelled("IdleState cancelled")
    }

    override suspend fun activate(): IState? {
        Log.i(TAG, "Activate from IdleState → ACTIVATED")
        finish(StateResult.Next(StateType.ACTIVATED))
        return null
    }

    override fun reset() {
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
    }
}
