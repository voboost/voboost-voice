package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.ISpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Состояние: Распознана команда
 *
 * Логика:
 * 1. Распарсить текст через NLU
 * 2. Если команда распознана:
 *    - Если нужно подтверждение → finish(StateResult.Next(ConfirmationState))
 *    - Если нет → finish(StateResult.Next(ExecutingCommandState))
 * 3. Если не распознана → finish(StateResult.Next(CommandErrorState))
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
        const val TAG = "RecognizedCommandState"
    }

    override val canCancel = true
    private val isCancelling = AtomicBoolean(false)

    override suspend fun execute() {
        val text = context.commandText ?: run {
            Log.e(TAG, "No command text in context")
            finish(StateResult.Next(
                CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, "No command text")
            ))
            return
        }

        Log.i(TAG, "Processing command: '$text'")

        try {
            // Парсим текст через NLU
            val recognizedCommand = nluEngine.parseCommand(text)

            if (recognizedCommand != null) {
                Log.d(TAG, "Command parsed: ${recognizedCommand.id}")
                context.recognizedCommand = recognizedCommand

                // Проверяем нужно ли подтверждение
                if (recognizedCommand.config.confirmation.required) {
                    Log.i(TAG, "Confirmation required for: ${recognizedCommand.id}")
                    finish(StateResult.Next(
                        ConfirmationState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                    ))
                } else {
                    // Выполняем команду без подтверждения
                    Log.i(TAG, "Executing command without confirmation: ${recognizedCommand.id}")
                    finish(StateResult.Next(
                        ExecutingCommandState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                    ))
                }
            } else {
                Log.w(TAG, "Unrecognized command: '$text'")
                finish(StateResult.Next(
                    CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, "Unrecognized command: $text")
                ))
            }

        } catch (e: CancellationException) {
            Log.d(TAG, "RecognizedCommandState cancelled")
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing command", e)
            finish(StateResult.Next(
                CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
            ))
        }
    }

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            Log.i(TAG, "RecognizedCommandState cancelled (button pressed)")

            try {
                ttsEngine.stop()
                kotlinx.coroutines.delay(200)

                context.soundEffectManager?.playEndSound()
                kotlinx.coroutines.delay(400)

                val latch = CountDownLatch(1)
                ttsEngine.speak("Отмена") { latch.countDown() }
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    latch.await(5, TimeUnit.SECONDS)
                }
            } finally {
                isCancelling.set(false)
            }

            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

            cancelled("RecognizedCommandState cancelled by user")
        }
    }

    override suspend fun activate(): IState? {
        Log.i(TAG, "Already in RecognizedCommandState, ignoring")
        return this
    }
}
