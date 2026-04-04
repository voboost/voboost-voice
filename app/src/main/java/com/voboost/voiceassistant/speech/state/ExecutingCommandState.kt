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
 * Состояние: Выполнение команды
 * 
 * Логика:
 * 1. Выполнить команду через CommandExecutor
 * 2. Если успех → IdleState
 * 3. Если ошибка → CommandErrorState
 */
class ExecutingCommandState(
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
        private const val TAG = "ExecutingCommandState"
    }

    override suspend fun execute(): State {
        val command = context.recognizedCommand ?: run {
            Log.e(TAG, "No recognized command in context")
            return CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, "No command to execute")
        }

        Log.i(TAG, "Entering EXECUTING_COMMAND state: ${command.id}")

        return try {
            // Выполняем команду
            commandExecutor.executeCommand(command)
            Log.i(TAG, "Command executed successfully: ${command.id}")

            // Успех → возвращаемся к ожиданию
            speechSM.finishCommand()
            IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context) {
                // Callback для ключевого слова
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command: ${command.id}", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ExecutingCommandState → IdleState")
        
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ExecutingCommandState, ignoring")
        return this
    }
}
