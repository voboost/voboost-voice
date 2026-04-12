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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Состояние: Слушание команды
 *
 * Логика:
 * 1. Ждём CommandReceived/Timeout/Error
 * 2. Команда → RECOGNIZED_COMMAND, Timeout → TIMEOUT, Error → COMMAND_ERROR
 */
class ListeningCommandState(
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
        const val TAG = "ListeningCommand"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.i(TAG, "Entering LISTENING_COMMAND IState")

        try {
            speechRecognizer.setMode(SpeechRecognizer.Mode.COMMAND)

            // Ждём результат
            val result = speechRecognizer.results.first {
                it is SpeechResult.CommandReceived ||
                it is SpeechResult.Timeout ||
                it is SpeechResult.Error
            }

            when (result) {
                is SpeechResult.CommandReceived -> {
                    val commandText = result.text
                    val zone = result.zone
                    if (commandText.isNotEmpty()) {
                        Log.i(TAG, "Command received: '$commandText' (zone=$zone)")
                        context.commandText = commandText
                        context.zone = zone
                        finish(StateResult.Next(StateType.RECOGNIZED_COMMAND))
                    } else {
                        Log.w(TAG, "Empty command received")
                        finish(StateResult.Next(StateType.TIMEOUT))
                    }
                }

                is SpeechResult.Timeout -> {
                    Log.w(TAG, "Command timeout")
                    finish(StateResult.Next(StateType.TIMEOUT))
                }

                is SpeechResult.Error -> {
                    Log.e(TAG, "Recognition error: ${result.message}")
                    context.error = result.message
                    finish(StateResult.Next(StateType.COMMAND_ERROR))
                }

                else -> {
                    Log.w(TAG, "Unexpected result: $result")
                    finish(StateResult.Next(StateType.TIMEOUT))
                }
            }

        } catch (e: CancellationException) {
            Log.d(TAG, "ListeningCommandState cancelled")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "ListeningCommandState cancelled (button pressed)")

        try {
            // Остановить TTS если говорит
            ttsEngine.stop()
            kotlinx.coroutines.delay(200)

            // Звук отмены
            context.soundEffectManager?.playEndSound()
            kotlinx.coroutines.delay(400)

            // Говорим "Отмена"
            val latch = CountDownLatch(1)
            ttsEngine.speak("Отмена") { latch.countDown() }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                latch.await(5, TimeUnit.SECONDS)
            }
        } finally {
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        }

        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()

        cancelled("ListeningCommandState cancelled by user")
    }

    override fun reset() {
        speechRecognizer.setMode(SpeechRecognizer.Mode.COMMAND)
    }
}
