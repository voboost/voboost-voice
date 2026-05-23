package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.recognition.RecognitionServiceResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType

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
            context.recognitionService?.setMode(RecognitionService.Mode.COMMAND)

            // Ждём результат
            val result = context.recognitionService?.results?.first {
                it is RecognitionServiceResult.CommandReceived || it is RecognitionServiceResult.Timeout || it is RecognitionServiceResult.Error
            }

            when (result) {
                is RecognitionServiceResult.CommandReceived -> {
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

                is RecognitionServiceResult.Timeout -> {
                    Log.w(TAG, "Command timeout")
                    finish(StateResult.Next(StateType.TIMEOUT))
                }

                is RecognitionServiceResult.Error -> {
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
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
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
                context.speechService?.enqueueAsync(cancelPhrase, SpeechService.PRIOR_HIGH)
            }

            Log.d(TAG, "Cancel speech initiated")
        }
        finally {
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
        }

        context.overlayManager?.hideAnimation()
        context.volumeManager?.restoreMedia()

        cancelled("ListeningCommandState cancelled by user")
    }

    override fun reset() {
        context.recognitionService?.setMode(RecognitionService.Mode.COMMAND)
    }
}

