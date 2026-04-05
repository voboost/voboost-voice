package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.ISpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.delay

/**
 * Состояние: Ошибка распознавания ключевого слова
 *
 * canCancel = false — уже переходит в IdleState
 */
class KeywordErrorState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: ISpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext,
    private val error: String
) : BaseState() {
    companion object {
        const val TAG = "KeywordErrorState"
    }

    override val canCancel = false

    override suspend fun execute() {
        Log.e(TAG, "Entering KEYWORD_ERROR IState: $error")

        try {
            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()

            delay(1000)

            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error in KeywordErrorState", e)
            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))
        }
    }

    override suspend fun cancel() {
        Log.w(TAG, "Cancel called but canCancel=false, ignoring")
    }

    override suspend fun activate(): IState? {
        Log.i(TAG, "Activate from KeywordErrorState → ActivatedState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        return ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }
}
