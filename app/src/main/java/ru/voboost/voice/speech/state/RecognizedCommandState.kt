package ru.voboost.voice.speech.state

import android.util.Log
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.core.QueueSpeechSynthesis
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Состояние: Распознанная команда
 *
 * Логика:
 * 1. Парсим текст через NLU
 * 2. Если найдена > проверяем подтверждение
 *    - требуется > CONFIRMATION
 *    - не требуется > EXECUTING_COMMAND
 * 3. Не найдена > COMMAND_ERROR
 */
class RecognizedCommandState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "RecognizedCommand"
    }

    override val canCancel = true
    private val isCancelling = AtomicBoolean(false)

    override suspend fun execute() {
        val text = context.commandText ?: run {
            Log.e(TAG, "No command text in context")
            finish(StateResult.Next(StateType.COMMAND_ERROR))
            return
        }

        Log.i(TAG, "Processing command: '$text'")

        try { // Парсим текст через NLU
            val recognizedCommand = context.nluEngine?.parseCommand(text)

            if (recognizedCommand != null) {
                Log.d(TAG,
                      "Command parsed: ${recognizedCommand.id} (zone=${context.zone})") // Добавляем зону в команду
                val commandWithZone = recognizedCommand.copy(zone = context.zone)
                context.recognizedCommand = commandWithZone

                // Проверяем нужно ли подтверждение
                if (recognizedCommand.config.confirmation.required) {
                    Log.i(TAG, "Confirmation required for: ${recognizedCommand.id}")
                    finish(StateResult.Next(StateType.CONFIRMATION))
                }
                else { // Выполняем команду без подтверждения
                    Log.i(TAG, "Executing command without confirmation: ${recognizedCommand.id}")
                    finish(StateResult.Next(StateType.EXECUTING_COMMAND))
                }
            }
            else {
                Log.w(TAG, "Unrecognized command: '$text'")
                context.commandExecutor?.handleUnrecognizedCommand(text)
                finish(StateResult.Next(StateType.IDLE))
            }

        }
        catch (e: Exception) {
            Log.e(TAG, "Error parsing command: '$text'", e)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            try {
                context.soundEffectManager?.playEndSoundAsync()
                kotlinx.coroutines.delay(400)
                val cancelPhrase = context.configManager?.getDefaultPhrase(PhraseType.CANCEL)
                if(!cancelPhrase.isNullOrEmpty())
                {
                    context.queueSpeech?.enqueueAsync(cancelPhrase, QueueSpeechSynthesis.PRIOR_HIGH)
                }
            }
            finally {
                isCancelling.set(false)
            }
            cancelled("RecognizedCommandState cancelled")
        }
    }

    override fun reset() {
        isCancelling.set(false)
    }
}


