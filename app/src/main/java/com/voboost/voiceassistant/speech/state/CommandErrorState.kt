package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Ошибка выполнения команды
 */
class CommandErrorState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext,
    private val error: String
) : State {
    companion object {
        private const val TAG = "CommandErrorState"
    }

    override suspend fun execute(): State {
        Log.e(TAG, "Entering COMMAND_ERROR state: $error")

        return try {
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)

        } catch (e: Exception) {
            Log.e(TAG, "Error in CommandErrorState", e)
            IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in CommandErrorState → IdleState")
        return IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Activate from CommandErrorState → ActivatedState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        return ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }
}
