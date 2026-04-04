package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.speech.SpeechStateMachine
import com.voboost.voiceassistant.speech.VoiceAssistantListener
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Ожидание ключевого слова
 */
class IdleState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val onKeywordDetected: () -> Unit
) : State {
    companion object {
        private const val TAG = "IdleState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering IDLE state - waiting for keyword...")

        return try {
            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()

            speechSM.startListeningForKeyword(object : VoiceAssistantListener {
                override fun onKeywordDetected() {
                    onKeywordDetected()
                }

                override fun onError(error: String) {
                    Log.e(TAG, "Keyword spotting error: $error")
                }
            })

            ActivatedState(speechSM, overlayManager, volumeManager, ttsEngine, configManager)

        } catch (e: Exception) {
            Log.e(TAG, "Error in IdleState", e)
            KeywordErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in IdleState - already idle, ignoring")
        return this
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Activate from IdleState → ActivatedState")
        return ActivatedState(speechSM, overlayManager, volumeManager, ttsEngine, configManager)
    }
}
