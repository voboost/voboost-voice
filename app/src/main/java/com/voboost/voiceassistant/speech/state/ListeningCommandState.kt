package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.speech.SpeechResult
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Состояние: Слушаем команду
 *
 * Логика:
 * 1. Установить режим COMMAND в SpeechRecognizer
 * 2. Ждём CommandReceived или Timeout из SharedFlow
 * 3. Если команда получена → finish(StateResult.Next(RecognizedCommandState))
 * 4. Если таймаут → finish(StateResult.Next(TimeoutState))
 * 5. Если ошибка → finish(StateResult.Next(CommandErrorState))
 * 6. Если нажата кнопка (cancel) → звук отмены + TTS "Отмена" → cancelled()
 */
class ListeningCommandState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : BaseState() {
    companion object {
        private const val TAG = "ListeningCommandState"
    }

    override val canCancel = true
    private val isCancelling = AtomicBoolean(false)

    override suspend fun execute() {
        Log.i(TAG, "Entering LISTENING_COMMAND state")

        try {
            // Устанавливаем режим распознавания команд
            speechRecognizer.setMode(SpeechRecognizer.Mode.COMMAND)

            // Ждём результат из SharedFlow
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
                        finish(StateResult.Next(
                            RecognizedCommandState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                        ))
                    } else {
                        Log.w(TAG, "Empty command received")
                        finish(StateResult.Next(
                            TimeoutState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                        ))
                    }
                }

                is SpeechResult.Timeout -> {
                    Log.w(TAG, "Command timeout")
                    finish(StateResult.Next(
                        TimeoutState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                    ))
                }

                is SpeechResult.Error -> {
                    Log.e(TAG, "Recognition error: ${result.message}")
                    finish(StateResult.Next(
                        CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, result.message)
                    ))
                }

                else -> {
                    Log.w(TAG, "Unexpected result: $result")
                    finish(StateResult.Cancel("Unexpected result"))
                }
            }

        } catch (e: CancellationException) {
            Log.d(TAG, "ListeningCommandState cancelled (normal during cancellation)")
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            finish(StateResult.Next(
                CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
            ))
        }
    }

    override suspend fun cancel() {
        if (isCancelling.compareAndSet(false, true)) {
            Log.i(TAG, "ListeningCommandState cancelled (button pressed)")

            try {
                // Сначала воспроизводим звук "пик"
                context.soundEffectManager?.playCancelSound()

                // Ждём 400ms чтобы звук "пик" успел закончиться перед TTS
                kotlinx.coroutines.delay(400)

                // Потом говорим "Отмена"
                val ttsLatch = CountDownLatch(1)
                ttsEngine.speak("Отмена") {
                    ttsLatch.countDown()
                }
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    ttsLatch.await(5, TimeUnit.SECONDS)
                }

                Log.d(TAG, "Cancel speech completed")
            } finally {
                isCancelling.set(false)
            }

            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

            cancelled("ListeningCommandState cancelled by user")
        }
    }

    override suspend fun activate(): State? {
        Log.i(TAG, "Already in ListeningCommandState, ignoring")
        return this
    }
}
