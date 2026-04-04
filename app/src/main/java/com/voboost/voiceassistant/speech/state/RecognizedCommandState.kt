package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechStateMachine
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Распознана команда
 * 
 * Логика:
 * 1. Распарсить текст через NLU
 * 2. Если команда распознана:
 *    - Если нужно подтверждение → ConfirmationState
 *    - Если нет → ExecutingCommandState
 * 3. Если не распознана → CommandErrorState
 */
class RecognizedCommandState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "RecognizedCommandState"
    }

    override suspend fun execute(): State {
        val text = context.commandText ?: run {
            Log.e(TAG, "No command text in context")
            return CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context, "No command text")
        }

        Log.i(TAG, "Processing command: '$text'")

        return try {
            // Парсим текст через NLU
            val recognizedCommand = nluEngine.parseCommand(text)
            
            if (recognizedCommand != null) {
                Log.d(TAG, "Command parsed: ${recognizedCommand.id}")
                context.recognizedCommand = recognizedCommand
                
                // Проверяем нужно ли подтверждение
                if (recognizedCommand.config.requiresConfirmation) {
                    Log.i(TAG, "Confirmation required for: ${recognizedCommand.id}")
                    return ConfirmationState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, commandExecutor, context)
                }
                
                // Выполняем команду без подтверждения
                Log.i(TAG, "Executing command without confirmation: ${recognizedCommand.id}")
                return ExecutingCommandState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context, commandExecutor)
            } else {
                Log.w(TAG, "Unrecognized command: '$text'")
                CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context, "Unrecognized command: $text")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing command", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in RecognizedCommandState → IdleState")
        
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in RecognizedCommandState, ignoring")
        return this
    }
}
