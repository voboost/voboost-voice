package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Обработка команды
 */
class ProcessingCommandState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val commandText: String,
    private val commandHandler: CommandHandler
) : State {
    companion object {
        private const val TAG = "ProcessingCommandState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering PROCESSING_COMMAND state: '$commandText'")

        return try {
            commandHandler.handleCommand(commandText)

            speechSM.finishCommand()
            IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
                // Callback для ключевого слова
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ProcessingCommandState → IdleState")
        
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ProcessingCommandState, ignoring")
        return this
    }
}
