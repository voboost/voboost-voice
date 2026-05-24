package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.services.recognition.RecognitionService
import kotlinx.coroutines.CancellationException
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType

/**
 * Состояние: Ошибка команды
 *
 * Логика:
 * 1. Сказать "Не понял" (высокий приоритет, чтобы перебить если что)
 * 2. > finish(StateResult.Next(StateType.IDLE))
 */
class CommandErrorState(private val context: StateContext)
    : BaseState() {

    companion object {
        const val TAG = "CommandErrorState"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.e(TAG,"Entering COMMAND_ERROR IState: " +
                  "${context.configManager?.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)}")

        try { // Отключаем микрофон, чтобы не перехватывать команду
            context.recognitionService?.setMode(RecognitionService.Mode.MUTED)
            val notUnderstoodPhrase =
                context.configManager?.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD)

            if (!notUnderstoodPhrase.isNullOrEmpty()) {
                context.speechService?.enqueueAsync(notUnderstoodPhrase, SpeechService.PRIOR_HIGH)
            }

            // Сразу переключаемся в режим ожидания ключевого слова
            // Не ждем окончания TTS!
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
        catch (e: CancellationException) {
            Log.d(TAG, "CommandErrorState cancelled")
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            throw e
        }
        catch (e: Exception) {
            Log.e(TAG, "Error in CommandErrorState", e)
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "CommandErrorState cancelled (button pressed)")
        context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
        cancelled("CommandErrorState cancelled by user")
    }

    override fun reset() {}
}

