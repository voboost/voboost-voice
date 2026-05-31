package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.executor.IVehicleCommandExecutor
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.ui.ToastMessengerManager
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
                            private var configManager: ConfigManager,
                            private var speechService: ISpeechService,
                            private val toastMessengerManager: ToastMessengerManager,
                            private val vehicleCommandExecutor: IVehicleCommandExecutor)
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
        val showNotification = commandData.data.showNotification
        // Выполняем команду (внутри TTS скажет "Закрываю окно")
        val commandResult = vehicleCommandExecutor.executeByCommandId(commandData)
        if (commandResult.result) {
            Log.i(TAG, "Command executed successfully")

            val successPhrase = commandData.data.phrases?.success
                                ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.SUCCESS) // Подстановка параметров в фразу
            val finalPhrase = substituteParams(successPhrase, commandResult.extractedParams) // Голос + Overlay (если включено)
            if (finalPhrase.isNotEmpty()) {
                if (showNotification) {
                    toastMessengerManager.show(finalPhrase)
                }
                speechService.enqueueAsync(finalPhrase)
            }
        }
        else {
            Log.w(TAG, "Command execution failed")
            val failurePhrase = commandData.data.phrases?.failure
                                ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE) // Голос + Overlay (если включено)
            if (failurePhrase.isNotEmpty()) {
                toastMessengerManager.show(failurePhrase)
                speechService.enqueueAsync(failurePhrase)
            }
        }
        Log.i(TAG, "Command executed successfully: ${commandData.data.id}")

        onComplite(StateResult(StateType.IDLE))
    }

    override suspend fun canceled() {}

    /**
     * Подставить параметры в фразу
     */
    private fun substituteParams(phrase: String, params: Map<String, String>): String {
        var result = phrase
        for ((key, value) in params) {
            result = result.replace("{$key}", value)
        }
        return result
    }
}


