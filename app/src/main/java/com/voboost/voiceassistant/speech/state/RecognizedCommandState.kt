package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CancellationException

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
        private const val TAG = "RecognizedCommandState"
    }

    override suspend fun execute(): State {
        val text = context.commandText ?: run {
            Log.e(TAG, "No command text in context")
            return CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, "No command text")
        }

        Log.i(TAG, "Processing command: '$text'")

        return try {
            // Парсим текст через NLU
            val recognizedCommand = nluEngine.parseCommand(text)

            if (recognizedCommand != null) {
                Log.d(TAG, "Command parsed: ${recognizedCommand.id}")
                context.recognizedCommand = recognizedCommand

                // Проверяем нужно ли подтверждение
                if (recognizedCommand.config.confirmation.required) {
                    Log.i(TAG, "Confirmation required for: ${recognizedCommand.id}")
                    return ConfirmationState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                }

                // Выполняем команду без подтверждения
                Log.i(TAG, "Executing command without confirmation: ${recognizedCommand.id}")
                return ExecutingCommandState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            } else {
                Log.w(TAG, "Unrecognized command: '$text'")
                CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, "Unrecognized command: $text")
            }

        } catch (e: CancellationException) {
            Log.d(TAG, "RecognizedCommandState cancelled")
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing command", e)
            CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in RecognizedCommandState → IdleState")

        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

        return IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in RecognizedCommandState, ignoring")
        return this
    }
}
