package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.withContext

/**
 * Состояние: Активация (после ключевого слова)
 *
 * Логика:
 * 1. Показать анимацию
 * 2. Приглушить музыку
 * 3. Сказать "Слушаю вас"
 * 4. → ListeningCommandState
 */
class ActivatedState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "ActivatedState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering ACTIVATED state")

        return try {
            // Показать анимацию и приглушить музыку
            overlayManager.showAnimation()
            volumeManager?.duckMedia(targetVolume = 1)

            // ОТКЛЮЧИТЬ распознавание пока TTS говорит (чтобы не было ЭХО)
            speechRecognizer.setMode(SpeechRecognizer.Mode.MUTED)

            // Сказать что слушаем (опционально)
            val listeningPhrase = configManager.getConfig().phrases.listening
            if (!listeningPhrase.isNullOrEmpty()) {
                // Используем callback чтобы включить распознавание ПОСЛЕ TTS
                val phraseLatch = java.util.concurrent.CountDownLatch(1)
                ttsEngine.speak(listeningPhrase) {
                    phraseLatch.countDown()
                }
                // Ждём завершения TTS (синхронно в suspend функции)
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    phraseLatch.await(5, java.util.concurrent.TimeUnit.SECONDS)
                }
            } else {
                Log.w(TAG, "Listening phrase is null or empty")
            }

            // ВКЛЮЧИТЬ распознавание команд (TTS закончил)
            speechRecognizer.setMode(SpeechRecognizer.Mode.COMMAND)

            // Переходим к слушанию команды
            ListeningCommandState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)

        } catch (e: kotlinx.coroutines.CancellationException) {
            // Нормальная ситуация при быстром повторном нажатии кнопки
            Log.d(TAG, "ActivatedState cancelled (rapid button press)")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            // Переходим к IdleState чтобы можно было снова активировать
            IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)

        } catch (e: Exception) {
            Log.e(TAG, "Error in ActivatedState", e)
            // Восстановить распознавание при ошибке
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ActivatedState → IdleState")

        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

        return IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ActivatedState, ignoring")
        return this
    }
}
