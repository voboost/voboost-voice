package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Состояние: Таймаут команды
 *
 * Логика:
 * 1. Остановить TTS (если что-то ещё говорит)
 * 2. Воспроизвести звук "пик-пик" (время вышло)
 * 3. Вернуться в IdleState
 */
class TimeoutState(
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
        private const val TAG = "TimeoutState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering TIMEOUT state")

        return try {
            // Сначала звук "пик-пик" (короткий), потом TTS (если нужен)
            context.soundEffectManager?.playEndSound()
            
            // Переключить режим обратно на KEYWORD
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)

        } catch (e: Exception) {
            Log.e(TAG, "Error in TimeoutState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in TimeoutState → IdleState")
        return IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Activate from TimeoutState → ActivatedState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        return ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }
}
