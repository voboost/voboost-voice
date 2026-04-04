package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Обработка команды
 * 
 * Логика:
 * 1. Выполнить команду через CommandHandler
 * 2. Если успех → IdleState
 * 3. Если ошибка → CommandErrorState
 */
class ProcessingCommandState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val commandText: String,
    private val commandHandler: com.voboost.voiceassistant.speech.CommandHandler
) : State {
    companion object {
        private const val TAG = "ProcessingCommandState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering PROCESSING_COMMAND state: '$commandText'")

        return try {
            // Выполняем команду
            commandHandler.handleCommand(commandText)

            // Успех → возвращаемся к ожиданию
            speechSM.finishCommand()
            IdleState(speechSM, overlayManager, volumeManager) {
                // Callback для ключевого слова
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, e.message ?: "Unknown error")
        }
    }
}
