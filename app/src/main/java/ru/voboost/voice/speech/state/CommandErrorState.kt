package ru.voboost.voice.speech.state

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.core.QueueSpeechSynthesis
import ru.voboost.voice.speech.SpeechRecognizer
import kotlinx.coroutines.CancellationException

/**
 * Состояние: Ошибка команды
 *
 * Логика:
 * 1. Сказать "Не понял" (высокий приоритет, чтобы перебить если что)
 * 2. > finish(StateResult.Next(StateType.IDLE))
 */
class CommandErrorState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "CommandError"
    }

    override val canCancel = true

    override suspend fun execute() {
        Log.e(TAG,
              "Entering COMMAND_ERROR IState: ${
                  context.configManager?.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)
              }")

        try { // Отключаем микрофон, чтобы не перехватывать команду
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.MUTED)

            val notUnderstoodPhrase =
                context.configManager?.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD)

            if (!notUnderstoodPhrase.isNullOrEmpty()) {
                context.queueSpeech?.enqueueAsync(notUnderstoodPhrase, QueueSpeechSynthesis.PRIOR_HIGH)
            }

            // Сразу переключаемся в режим ожидания ключевого слова
            // Не ждем окончания TTS!
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))

        }
        catch (e: CancellationException) {
            Log.d(TAG, "CommandErrorState cancelled")
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error in CommandErrorState", e)
            context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.IDLE))
        }
    }

    override suspend fun cancel() {
        Log.i(TAG, "CommandErrorState cancelled (button pressed)")
        context.speechRecognizer?.setMode(SpeechRecognizer.Mode.KEYWORD)
        cancelled("CommandErrorState cancelled by user")
    }

    override fun reset() {}
}

