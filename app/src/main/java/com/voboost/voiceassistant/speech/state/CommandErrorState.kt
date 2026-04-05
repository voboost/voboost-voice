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
 *
 * Логика:
 * 1. Восстановить распознавание
 * 2. finish(StateResult.Next(IdleState))
 *
 * canCancel = false — уже переходит в IdleState
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
) : BaseState() {
    companion object {
        private const val TAG = "CommandErrorState"
    }

    override val canCancel = false

    override suspend fun execute() {
        Log.e(TAG, "Entering COMMAND_ERROR state: $error")

        try {
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error in CommandErrorState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))
        }
    }

    override suspend fun cancel() {
        // Не вызывается т.к. canCancel = false
        Log.w(TAG, "Cancel called but canCancel=false, ignoring")
    }

    override suspend fun activate(): State? {
        Log.i(TAG, "Activate from CommandErrorState → ActivatedState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        return ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }
}
