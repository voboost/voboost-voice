package ru.voboost.voice.speech.state

import android.util.Log
import ru.voboost.voice.core.QueueSpeechSynthesis
import ru.voboost.voice.speech.SpeechRecognizer
import kotlinx.coroutines.delay
import ru.voboost.voice.config.ConfigManager.PhraseType
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Состояние: Активация (после ключевого слова)
 *
 * Логика:
 * 1. Показать анимацию
 * 2. Приглушить музыку
 * 3. Добавить "Слушаю вас" в очередь (низкий приоритет)
 * 4. > finish(StateResult.Next(StateType.LISTENING_COMMAND))
 */
class ActivatedState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "ActivatedState"
    }

    override val canCancel = true
    private val isCancelling = AtomicBoolean(false)

    override suspend fun execute() {
        Log.i(TAG, "Entering ACTIVATED IState")

        try { // Звук начала распознавания
            context.soundEffectManager?.playStartSoundAsync()

            // Показать анимацию и приглушить музыку
            context.overlayManager?.showAnimation()
            context.volumeManager?.duckMedia(targetVolume = 1)

            // ОТКЛЮЧИТЬ распознавание пока TTS говорит (чтобы не было ЭХО)
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.MUTED)

            // Сказать что слушаем (опционально)
            val listeningPhrase = context.configManager?.getConfig()?.phrases?.listening
            if (!listeningPhrase.isNullOrEmpty()) { // Низкий приоритет, так как это обычная фраза
                context.queueSpeech?.enqueueAsync(listeningPhrase, QueueSpeechSynthesis.PRIOR_LOW)
            }
            else {
                Log.w(TAG, "Listening phrase is null or empty")
            }

            // ВКЛЮЧИТЬ распознавание команд
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.COMMAND)

            // Переходим к слушанию команды
            finish(StateResult.Next(StateType.LISTENING_COMMAND))

        }
        catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "ActivatedState cancelled (rapid button press)")
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in ActivatedState", e)
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            Log.i(TAG, "ActivatedState cancelled (button pressed)")

            try { // Одинаковый звук отмены с TimeoutState
                context.soundEffectManager?.playEndSoundAsync()

                // Даём звуку закончиться
                delay(400)

                // Говорим "Отмена" с высоким приоритетом
                val cancelPhrase = context.configManager?.getDefaultPhrase(PhraseType.CANCEL)
                if(!cancelPhrase.isNullOrEmpty())
                {
                    context.queueSpeech?.enqueueAsync(cancelPhrase, QueueSpeechSynthesis.PRIOR_HIGH)
                }

                Log.d(TAG, "Cancel speech initiated")
            }
            finally {
                isCancelling.set(false)
            }

            context.overlayManager?.hideAnimation()
            context.volumeManager?.restoreMedia()
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)

            cancelled("ActivatedState cancelled by user")
        }
    }

    override fun reset() {
        isCancelling.set(false)
    }
}

