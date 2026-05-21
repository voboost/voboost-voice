package ru.voboost.voice.speech.state

import android.util.Log
import ru.voboost.voice.core.QueueSpeechSynthesis
import ru.voboost.voice.speech.SpeechRecognizer
import ru.voboost.voice.speech.SpeechResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import ru.voboost.voice.config.ConfigManager.PhraseType

/**
 * Состояние: Слушание команды
 *
 * Логика:
 * 1. Ждём CommandReceived/Timeout/Error
 * 2. Команда > RECOGNIZED_COMMAND, Timeout > TIMEOUT, Error > COMMAND_ERROR
 */
class ListeningCommandState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "ListeningCommand"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.i(TAG, "Entering LISTENING_COMMAND IState")

        try {
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.COMMAND)

            // Ждём результат
            val result = context.speechRecognizer?.results?.first {
                it is SpeechResult.CommandReceived || it is SpeechResult.Timeout || it is SpeechResult.Error
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
                    }
                    else {
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

        }
        catch (e: CancellationException) {
            Log.d(TAG, "ListeningCommandState cancelled")
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "ListeningCommandState cancelled (button pressed)")

        try { // Звук отмены
            context.soundEffectManager?.playEndSoundAsync()
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
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
        }

        context.overlayManager?.hideAnimation()
        context.volumeManager?.restoreMedia()

        cancelled("ListeningCommandState cancelled by user")
    }

    override fun reset() {
        context.speechRecognizer?.setMode(SpeechRecognizer.Mode.COMMAND)
    }
}

