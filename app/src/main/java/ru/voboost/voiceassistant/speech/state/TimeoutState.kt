package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Состояние: Таймаут распознавания
 *
 * Логика:
 * 1. Сказать "Отмена" + звук
 * 2. → finish(StateResult.Next(StateType.IDLE))
 */
class TimeoutState(
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
        const val TAG = "Timeout"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.w(TAG, "Entering TIMEOUT IState")

        try {
            // Звук окончания
            context.soundEffectManager?.playEndSound()
            kotlinx.coroutines.delay(200)

            // Говорим "Отмена"
            val latch = CountDownLatch(1)
            ttsEngine.speak("Отмена") { latch.countDown() }
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                latch.await(5, TimeUnit.SECONDS)
            }

            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()

            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        } catch (e: CancellationException) {
            Log.d(TAG, "TimeoutState cancelled")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error in TimeoutState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "TimeoutState cancelled (button pressed)")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        cancelled("TimeoutState cancelled by user")
    }
}
