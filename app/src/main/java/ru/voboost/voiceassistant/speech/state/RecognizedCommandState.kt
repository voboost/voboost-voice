package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.ui.OverlayManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Состояние: Распознанная команда
 *
 * Логика:
 * 1. Парсим текст через NLU
 * 2. Если найдена → проверяем подтверждение
 *    - требуется → CONFIRMATION
 *    - не требуется → EXECUTING_COMMAND
 * 3. Не найдена → COMMAND_ERROR
 */
class RecognizedCommandState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: ISpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : BaseState() {
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

        try {
            // Парсим текст через NLU
            val recognizedCommand = nluEngine.parseCommand(text)

            if (recognizedCommand != null) {
                Log.d(TAG, "Command parsed: ${recognizedCommand.id} (zone=${context.zone})")
                // Добавляем зону в команду
                val commandWithZone = recognizedCommand.copy(zone = context.zone)
                context.recognizedCommand = commandWithZone

                // Проверяем нужно ли подтверждение
                if (recognizedCommand.config.confirmation.required) {
                    Log.i(TAG, "Confirmation required for: ${recognizedCommand.id}")
                    finish(StateResult.Next(StateType.CONFIRMATION))
                } else {
                    // Выполняем команду без подтверждения
                    Log.i(TAG, "Executing command without confirmation: ${recognizedCommand.id}")
                    finish(StateResult.Next(StateType.EXECUTING_COMMAND))
                }
            } else {
                Log.w(TAG, "Unrecognized command: '$text'")
                commandExecutor.handleUnrecognizedCommand(text)
                finish(StateResult.Next(StateType.IDLE))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing command: '$text'", e)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            try {
                ttsEngine.stop()
                kotlinx.coroutines.delay(200)
                context.soundEffectManager?.playEndSound()
                kotlinx.coroutines.delay(400)
                val latch = java.util.concurrent.CountDownLatch(1)
                ttsEngine.speak("Отмена") { latch.countDown() }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
                }
            } finally {
                isCancelling.set(false)
            }
            cancelled("RecognizedCommandState cancelled")
        }
    }

    override fun reset() {
        isCancelling.set(false)
    }
}
