package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.executor.CommandExecutor
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import java.util.concurrent.atomic.AtomicBoolean

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
class ExecutingCommandState(private val context: StateContext,
                            private var recognitionService: IRecognitionService,
                            private var commandExecutor: CommandExecutor)
    : BaseState() {
    companion object {
        const val TAG = "ExecutingCommandState"
    }

    override val isCancelling: AtomicBoolean = AtomicBoolean(true)
    override val executeError: StateResult = StateResult(StateType.KEYWORD_ERROR)

    override suspend fun entering() {
        val commandData = context.commandData ?: run {
            Log.e(TAG, "No recognized command in context")
            onComplite(StateResult(StateType.COMMAND_ERROR))
            return
        }
        Log.i(TAG, "Entering EXECUTING_COMMAND IState: ${commandData.data.id}")
        recognitionService.setMode(RecognitionService.Mode.MUTED)
        // Выполняем команду (внутри TTS скажет "Закрываю окно")
        commandExecutor.executeCommand(commandData)
        Log.i(TAG, "Command executed successfully: ${commandData.data.id}")
        // ВКЛЮЧИТЬ распознавание ключевых слов (TTS закончил)
        recognitionService.setMode(RecognitionService.Mode.KEYWORD)
        // Успех > возвращаемся к ожиданию ключевого слова
        onComplite(StateResult(StateType.IDLE))
    }

    override suspend fun canceled() {}
}


