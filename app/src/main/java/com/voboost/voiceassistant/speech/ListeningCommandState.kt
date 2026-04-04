package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Слушаем команду
 */
class ListeningCommandState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager
) : State {
    companion object {
        private const val TAG = "ListeningCommandState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering LISTENING_COMMAND state")

        return try {
            // SpeechStateMachine уже активирован в ActivatedState
            // Просто ждём результат через callback
            IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
                // Этот callback не будет вызван, т.к. мы уже в команде
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ListeningCommandState → IdleState")
        
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ListeningCommandState, ignoring")
        return this
    }
}
