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
import kotlinx.coroutines.flow.first

/**
 * Состояние: Слушаем команду
 *
 * Логика:
 * 1. Установить режим COMMAND в SpeechRecognizer
 * 2. Ждём CommandReceived или Timeout из Channel
 * 3. Если команда получена → RecognizedCommandState
 * 4. Если таймаут → TimeoutState
 * 5. Если ошибка → CommandErrorState
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
) : State {
    companion object {
        private const val TAG = "ListeningCommandState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering LISTENING_COMMAND state")

        return try {
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
                        RecognizedCommandState(
                            speechRecognizer = speechRecognizer,
                            overlayManager = overlayManager,
                            volumeManager = volumeManager,
                            ttsEngine = ttsEngine,
                            configManager = configManager,
                            nluEngine = nluEngine,
                            commandExecutor = commandExecutor,
                            context = context
                        )
                    } else {
                        Log.w(TAG, "Empty command received")
                        TimeoutState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                    }
                }

                is SpeechResult.Timeout -> {
                    Log.w(TAG, "Command timeout")
                    TimeoutState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                }

                is SpeechResult.Error -> {
                    Log.e(TAG, "Recognition error: ${result.message}")
                    CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, result.message)
                }

                else -> {
                    Log.w(TAG, "Unexpected result: $result")
                    IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ListeningCommandState → IdleState")

        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

        return IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ListeningCommandState, ignoring")
        return this
    }
}
