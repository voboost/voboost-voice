package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.speech.SpeechStateMachine
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Таймаут команды
 */
class TimeoutState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "TimeoutState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering TIMEOUT state")

        return try {
            speechSM.finishCommand()
            IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
                // Callback для ключевого слова
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in TimeoutState", e)
            IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
                // Callback для ключевого слова
            }
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in TimeoutState → IdleState")
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
            // Callback для ключевого слова
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Cannot activate from TimeoutState, ignoring")
        return this
    }
}
