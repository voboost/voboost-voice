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
 * Состояние: Ошибка ключевого слова
 *
 * Логика:
 * 1. Сказать "Не понял"
 * 2. → finish(StateResult.Next(StateType.IDLE))
 */
class KeywordErrorState(
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
        const val TAG = "KeywordError"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.e(TAG, "Entering KEYWORD_ERROR IState")

        try {
            speechRecognizer.setMode(SpeechRecognizer.Mode.MUTED)

            val notUnderstoodPhrase =
                configManager.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD)

            if (!notUnderstoodPhrase.isNullOrEmpty()) {
                val ttsLatch = CountDownLatch(1)
                ttsEngine.speak(notUnderstoodPhrase) { ttsLatch.countDown() }
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    ttsLatch.await(5, TimeUnit.SECONDS)
                }
            }

            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        } catch (e: CancellationException) {
            Log.d(TAG, "KeywordErrorState cancelled")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error in KeywordErrorState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "KeywordErrorState cancelled (button pressed)")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        cancelled("KeywordErrorState cancelled by user")
    }
}
