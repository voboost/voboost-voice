package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Состояние: Ожидание подтверждения команды
 *
 * Логика:
 * 1. Сказать вопрос подтверждения
 * 2. finish(StateResult.Next(ExecutingCommandState)) — пока без ожидания ответа
 *
 * canCancel = true — можно отменить подтверждение
 */
class ConfirmationState(
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
        const val TAG = "ConfirmationState"
    }

    override val canCancel = true
    private val isCancelling = AtomicBoolean(false)

    override suspend fun execute() {
        val command = context.recognizedCommand ?: run {
            Log.e(TAG, "No recognized command in context")
            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine,
                          configManager, nluEngine, commandExecutor, context)
            ))
            return
        }

        Log.i(TAG, "Entering CONFIRMATION IState for: ${command.id}")

        try {
            // Сказать вопрос подтверждения
            val question = command.config.confirmation.question ?: "Подтверждаете?"
            Log.d(TAG, "Asking: '$question'")
            
            val latch = CountDownLatch(1)
            ttsEngine.speak(question) { latch.countDown() }
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                latch.await(5, TimeUnit.SECONDS)
            }

            // TODO: Реализовать ожидание ответа пользователя
            // Пока сразу переходим к выполнению (заглушка)
            Log.w(TAG, "Confirmation not fully implemented yet, executing command")

            finish(StateResult.Next(
                ExecutingCommandState(speechRecognizer, overlayManager, volumeManager,
                                      ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error in ConfirmationState", e)
            finish(StateResult.Next(
                CommandErrorState(speechRecognizer, overlayManager, volumeManager,
                                  ttsEngine, configManager, nluEngine, commandExecutor,
                                  context, e.message ?: "Unknown error")
            ))
        }
    }

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            Log.i(TAG, "ConfirmationState cancelled (button pressed)")

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

            cancelled("ConfirmationState cancelled by user")
        }
    }

    override suspend fun activate(): IState? {
        Log.i(TAG, "Already in ConfirmationState, ignoring")
        return this
    }
}
