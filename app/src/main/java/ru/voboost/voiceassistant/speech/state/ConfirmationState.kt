package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.speech.SpeechResult
import ru.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Состояние: Подтверждение команды
 *
 * Логика:
 * 1. Сказать вопрос подтверждения
 * 2. Ждём ответ (да/нет/таймаут)
 * 3. Да → EXECUTING_COMMAND, Нет/Таймаут → IDLE
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
        const val TAG = "Confirmation"
    }

    override val canCancel = true

    override suspend fun execute() {
        val command = context.recognizedCommand ?: run {
            Log.e(TAG, "No recognized command in context")
            finish(StateResult.Next(StateType.IDLE))
            return
        }

        Log.i(TAG, "Entering CONFIRMATION IState for: ${command.id}")

        try {
            speechRecognizer.setMode(SpeechRecognizer.Mode.COMMAND)

            // Спрашиваем подтверждение
            val question = nluEngine.getConfirmationQuestion(command.config)
            val latch = CountDownLatch(1)
            ttsEngine.speak(question) { latch.countDown() }
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                latch.await(5, TimeUnit.SECONDS)
            }

            // Ждём ответ
            val result = speechRecognizer.results.first {
                it is SpeechResult.CommandReceived ||
                it is SpeechResult.Timeout ||
                it is SpeechResult.Error
            }

            when (result) {
                is SpeechResult.CommandReceived -> {
                    val answer = result.text.lowercase().trim()
                    if (nluEngine.isConfirmationYes(answer, command.config)) {
                        Log.i(TAG, "Confirmation: YES")
                        finish(StateResult.Next(StateType.EXECUTING_COMMAND))
                    } else {
                        Log.i(TAG, "Confirmation: NO")
                        finish(StateResult.Next(StateType.IDLE))
                    }
                }

                is SpeechResult.Timeout -> {
                    Log.w(TAG, "Confirmation timeout")
                    finish(StateResult.Next(StateType.IDLE))
                }

                is SpeechResult.Error -> {
                    Log.e(TAG, "Confirmation error: ${result.message}")
                    finish(StateResult.Next(StateType.COMMAND_ERROR))
                }

                else -> {
                    Log.w(TAG, "Unexpected result during confirmation: $result")
                    finish(StateResult.Next(StateType.IDLE))
                }
            }

        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "ConfirmationState cancelled")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error in ConfirmationState", e)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "ConfirmationState cancelled (button pressed)")
        ttsEngine.stop()
        kotlinx.coroutines.delay(200)
        context.soundEffectManager?.playEndSound()
        kotlinx.coroutines.delay(400)
        val latch = CountDownLatch(1)
        ttsEngine.speak("Отмена") { latch.countDown() }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            latch.await(5, TimeUnit.SECONDS)
        }

        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

        cancelled("ConfirmationState cancelled by user")
    }
}
