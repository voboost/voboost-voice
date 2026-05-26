package ru.voboost.voice.executor

import android.util.Log
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.ui.ToastMessengerManager

/**
 * Выполнение команд
 * Использует VehicleCommandExecutor для отправки команд автомобилю
 *
 * @param vehicleCommandExecutor Реализация выполнения команд (Intent или Shell)
 */
class CommandExecutor(private val speechService: ISpeechService,
                      private val toastMessengerManager: ToastMessengerManager,
                      private val vehicleCommandExecutor: IVehicleCommandExecutor,
                      private val configManager: ConfigManager) {
    companion object {
        const val TAG = "CommandExecutor"
    }

    init {
        Log.i(TAG, "Initialized with execution method: ${vehicleCommandExecutor.executionMethod}")
    }

    /**
     * Выполнить команду
     */
    suspend fun executeCommand(commandData: CommandData) {
        val commandConfig = configManager.getCommandById(commandData.data.id)
        val zone = commandData.zone

        Log.i(TAG, "Executing command: ${commandData.data.id} (zone=$zone)") // Показывать ли уведомление через системный лаунчер
        val showNotification = commandConfig?.showNotification?:false

        try { // Добавляем зону в voiceParams для команд климата
            val commandResult = executeAction(commandData)

            if (commandResult.result) {
                Log.i(TAG, "Command executed successfully")
                val successPhrase = commandConfig?.phrases?.success
                                    ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.SUCCESS) // Подстановка параметров в фразу
                val finalPhrase = substituteParams(successPhrase, commandResult.extractedParams) // Голос + Overlay (если включено)
                if (!finalPhrase.isNullOrEmpty()) {
                    if (showNotification) {
                        toastMessengerManager.show(finalPhrase)
                    }
                    speechService.enqueueAsync(finalPhrase)
                }
            }
            else {
                Log.w(TAG, "Command execution failed")
                val failurePhrase = commandConfig?.phrases?.failure
                                    ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE) // Голос + Overlay (если включено)
                if (!failurePhrase.isNullOrEmpty()) {
                    toastMessengerManager.show(failurePhrase)
                    speechService.enqueueAsync(failurePhrase)
                }
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            val errorPhrase = configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)
            if (!errorPhrase.isNullOrEmpty()) {
                toastMessengerManager.show(errorPhrase)
                speechService.enqueueAsync(errorPhrase, SpeechService.PRIOR_HIGH)
            }
        }
    }

    /**
     * Выполнить действие команды
     * @return true если успешно
     */
    private fun executeAction(commandData: CommandData): CommandResult {
        Log.d(TAG, "Command: ${commandData.data.id} via: ${vehicleCommandExecutor.executionMethod}")
        return try {
            vehicleCommandExecutor.executeByCommandId(commandData)
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to execute command", e)
            CommandResult(false)
        }
    }

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

    /**
     * Обработать нераспознанную команду
     */
    suspend fun handleUnrecognizedCommand(text: String) {
        Log.w(TAG, "Command not recognized: $text")
        val notUnderstoodPhrase = configManager.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD) // Показывать уведомление для нераспознанных команд (по умолчанию true)
        if (!notUnderstoodPhrase.isNullOrEmpty()) {
            speechService.enqueueAsync(notUnderstoodPhrase, SpeechService.PRIOR_MEDIUM)
            toastMessengerManager.show(notUnderstoodPhrase)
        }
        else {
            Log.w(TAG, "No phrase for NOT_UNDERSTOOD")
            toastMessengerManager.show("Команда не распознана")
        }
    }

    /**
     * Очистить ресурсы
     */
    fun cleanup() {}
}

