package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.speech.SpeechStateMachine
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Ожидание подтверждения команды
 * 
 * Логика:
 * 1. Сказать вопрос подтверждения
 * 2. Ждать ответ пользователя (да/нет)
 * 3. Если "да" → ExecutingCommandState
 * 4. Если "нет" → IdleState
 * 5. Если таймаут → IdleState
 */
class ConfirmationState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "ConfirmationState"
    }

    override suspend fun execute(): State {
        val command = context.recognizedCommand ?: run {
            Log.e(TAG, "No recognized command in context")
            return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context) {
                // Callback будет установлен при создании нового IdleState
            }
        }

        Log.i(TAG, "Entering CONFIRMATION state for: ${command.id}")

        return try {
            // Сказать вопрос подтверждения
            val question = command.config.confirmation.question ?: "Подтверждаете?"
            Log.d(TAG, "Asking: '$question'")
            ttsEngine.speak(question)

            // Ждать ответ пользователя
            // TODO: Реализовать ожидание ответа через SpeechRecognitionListener
            // Пока сразу переходим к выполнению (заглушка)
            Log.w(TAG, "Confirmation not fully implemented yet, executing command")
            
            ExecutingCommandState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context, commandExecutor)

        } catch (e: Exception) {
            Log.e(TAG, "Error in ConfirmationState", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ConfirmationState → IdleState")
        
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ConfirmationState, ignoring")
        return this
    }
}
