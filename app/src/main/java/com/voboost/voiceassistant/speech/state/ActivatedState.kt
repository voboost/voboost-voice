package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.ISpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
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
 * 4. → finish(StateResult.Next(ListeningCommandState))
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
            finish(StateResult.Next(
                ListeningCommandState(speechRecognizer, overlayManager, volumeManager,
                                      ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))

        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "ActivatedState cancelled (rapid button press)")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine,
                          configManager, nluEngine, commandExecutor, context)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error in ActivatedState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(
                CommandErrorState(speechRecognizer, overlayManager, volumeManager,
                                  ttsEngine, configManager, nluEngine, commandExecutor,
                                  context, e.message ?: "Unknown error")
            ))
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

    override suspend fun activate(): IState? {
        Log.i(TAG, "Already in ActivatedState, ignoring")
        return this
    }
}
