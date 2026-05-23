package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.recognition.RecognitionService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType

/**
 * Состояние: Выполнение команды
 *
 * Логика:
 * 1. Выполнить команду через CommandExecutor
 * 2. Ждём пока TTS закончит говорить
 * 3. > finish(StateResult.Next(StateType.IDLE))
 *
 * canCancel = false — кнопка игнорируется (команда уже отправлена)
 */
class ExecutingCommandState(private val context: StateContext) : BaseState() {
    companion object {
        const val TAG = "ExecutingCommand"
    }

    // Команда уже выполняется — отменять поздно
    override val canCancel = false

    override suspend fun execute() {
        val commandData = context.commandData ?: run {
            Log.e(TAG, "No recognized command in context")
            finish(StateResult.Next(StateType.COMMAND_ERROR))
            return
        }

        Log.i(TAG, "Entering EXECUTING_COMMAND IState: ${commandData.id}")

        try { // ОТКЛЮЧИТЬ распознавание пока TTS говорит ответ (чтобы не было ЭХО)
            context.recognitionService?.setMode(RecognitionService.Mode.MUTED)

            // Выполняем команду (внутри TTS скажет "Закрываю окно")
            context.commandExecutor?.executeCommand(commandData)
            Log.i(TAG, "Command executed successfully: ${commandData.id}")

            // Ждём пока TTS закончит говорить (короткая задержка)
            withContext(Dispatchers.IO) {
                delay(2500) // Даем TTS время договорить
            }

            // ВКЛЮЧИТЬ распознавание ключевых слов (TTS закончил)
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)

            // Небольшая задержка перед возвратом громкости, чтобы последняя фраза не съедалась
            withContext(Dispatchers.IO) {
                delay(500)
            }

            // Успех > возвращаемся к ожиданию ключевого слова
            finish(StateResult.Next(StateType.IDLE))

        }
        catch (e: CancellationException) {
            Log.d(TAG, "ExecutingCommandState cancelled")
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            throw e

        }
        catch (e: Exception) {
            Log.e(TAG, "Error executing command: ${commandData.id}", e)
            context.recognitionService?.setMode(RecognitionService.Mode.KEYWORD)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() { // Не вызывается т.к. canCancel = false
        Log.w(TAG, "Cancel called but canCancel=false, ignoring")
    }
}


