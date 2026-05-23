package ru.voboost.voice.states.state

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
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
            context.voceAnimationManager?.show()
            context.volumeManager?.duckMedia(targetVolume = 1)

            // ОТКЛЮЧИТЬ распознавание пока TTS говорит (чтобы не было ЭХО)
            context.recognitionService?.setMode(RecognitionService.Mode.MUTED)

            // Сказать что слушаем (опционально)
            val listeningPhrase = context.configManager?.getConfig()?.phrases?.listening
            if (!listeningPhrase.isNullOrEmpty()) { // Низкий приоритет, так как это обычная фраза
                context.speechService?.enqueueAsync(listeningPhrase, SpeechService.Companion.PRIOR_LOW)
            }
            else {
                Log.w(TAG, "Listening phrase is null or empty")
            }

            // ВКЛЮЧИТЬ распознавание команд
            context.recognitionService?.setMode(RecognitionService.Mode.COMMAND)

            // Переходим к слушанию команды
            finish(StateResult.Next(StateType.LISTENING_COMMAND))

        }
        catch (e: CancellationException) {
            Log.d(TAG, "ActivatedState cancelled (rapid button press)")
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in ActivatedState", e)
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
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
                val cancelPhrase = context.configManager?.getDefaultPhrase(ConfigManager.PhraseType.CANCEL)
                if(!cancelPhrase.isNullOrEmpty())
                {
                    context.speechService?.enqueueAsync(cancelPhrase, SpeechService.Companion.PRIOR_HIGH)
                }

                Log.d(TAG, "Cancel speech initiated")
            }
            finally {
                isCancelling.set(false)
            }

            context.voceAnimationManager?.hide()
            context.volumeManager?.restoreMedia()
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)

            cancelled("ActivatedState cancelled by user")
        }
    }

    override fun reset() {
        isCancelling.set(false)
    }
}