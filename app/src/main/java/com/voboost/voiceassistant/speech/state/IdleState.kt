package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.speech.SpeechResult
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.flow.first

/**
 * Состояние: Ожидание ключевого слова
 *
 * Логика:
 * 1. Скрыть анимацию, восстановить громкость
 * 2. Ждём KeywordDetected из SpeechRecognizer
 * 3. → ActivatedState
 */
class IdleState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "IdleState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering IDLE state - waiting for keyword...")

        return try {
            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()

            // Ждём ключевое слово из SharedFlow
            val result = speechRecognizer.results.first { it is SpeechResult.KeywordDetected }
            val keywordText = (result as SpeechResult.KeywordDetected).text
            Log.i(TAG, "🎯 Keyword detected: '$keywordText'")

            // Ключевое слово получено → ActivatedState
            ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)

        } catch (e: Exception) {
            Log.e(TAG, "Error in IdleState", e)
            KeywordErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in IdleState - returning to IdleState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        return IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Activate from IdleState → ActivatedState")
        return ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }
}
