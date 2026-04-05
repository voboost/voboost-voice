package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.ISpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Таймаут команды
 *
 * Логика:
 * 1. Воспроизвести звук "пик-пик" (время вышло)
 * 2. finish(StateResult.Next(IdleState))
 *
 * canCancel = false — уже переходит в IdleState, кнопка не нужна
 */
class TimeoutState(
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
        const val TAG = "TimeoutState"
    }

    override val canCancel = false

    override suspend fun execute() {
        Log.i(TAG, "Entering TIMEOUT IState")

        try {
            // Воспроизвести звук "пик-пик" (короткий)
            context.soundEffectManager?.playEndSound()

            // Переключить режим обратно на KEYWORD
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error in TimeoutState", e)
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

    override suspend fun activate(): IState? {
        Log.i(TAG, "Activate from TimeoutState → ActivatedState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        return ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }
}
