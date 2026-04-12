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
 * Состояние: Активация (после ключевого слова)
 *
 * Логика:
 * 1. Показать анимацию
 * 2. Приглушить музыку
 * 3. Сказать "Слушаю вас"
 * 4. → finish(StateResult.Next(StateType.LISTENING_COMMAND))
 */
class ActivatedState(
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
        const val TAG = "ActivatedState"
    }

    override val canCancel = true
    private val isCancelling = AtomicBoolean(false)

    override suspend fun execute() {
        Log.i(TAG, "Entering ACTIVATED IState")

        try {
            // Звук начала распознавания
            context.soundEffectManager?.playStartSound()

            // Показать анимацию и приглушить музыку
            overlayManager.showAnimation()
            volumeManager?.duckMedia(targetVolume = 1)

            // ОТКЛЮЧИТЬ распознавание пока TTS говорит (чтобы не было ЭХО)
            speechRecognizer.setMode(SpeechRecognizer.Mode.MUTED)

            // Сказать что слушаем (опционально)
            val listeningPhrase = configManager.getConfig().phrases.listening
            if (!listeningPhrase.isNullOrEmpty()) {
                val phraseLatch = CountDownLatch(1)
                ttsEngine.speak(listeningPhrase) {
                    phraseLatch.countDown()
                }
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    phraseLatch.await(5, TimeUnit.SECONDS)
                }
            } else {
                Log.w(TAG, "Listening phrase is null or empty")
            }

            // ВКЛЮЧИТЬ распознавание команд (TTS закончил)
            speechRecognizer.setMode(SpeechRecognizer.Mode.COMMAND)

            // Переходим к слушанию команды
            finish(StateResult.Next(StateType.LISTENING_COMMAND))

        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "ActivatedState cancelled (rapid button press)")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        } catch (e: Exception) {
            Log.e(TAG, "Error in ActivatedState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            Log.i(TAG, "ActivatedState cancelled (button pressed)")

            try {
                // Остановить TTS если ещё говорит "Слушаю вас"
                ttsEngine.stop()

                // Небольшая задержка чтобы TTS успел остановиться
                kotlinx.coroutines.delay(200)

                // Одинаковый звук отмены с TimeoutState
                context.soundEffectManager?.playEndSound()

                // Даём звуку закончиться
                kotlinx.coroutines.delay(400)

                // Говорим "Отмена"
                val latch = CountDownLatch(1)
                ttsEngine.speak("Отмена") { latch.countDown() }
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    latch.await(5, TimeUnit.SECONDS)
                }

                Log.d(TAG, "Cancel speech completed")
            } finally {
                isCancelling.set(false)
            }

            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

            cancelled("ActivatedState cancelled by user")
        }
    }

    override fun reset() {
        isCancelling.set(false)
    }
}
