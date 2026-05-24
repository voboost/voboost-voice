package ru.voboost.voice.executor

import android.util.Log
import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.config.ConfigManager
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
        val commandConfig = commandData.config
        val zone = commandData.zone

        Log.i(TAG,
              "Executing command: ${commandConfig.id} (zone=$zone)") // Показывать ли уведомление через системный лаунчер
        val showNotification = commandConfig.showNotification

        try { // Добавляем зону в voiceParams для команд климата
            val voiceParamsWithZone = commandData.extractedParams + ("_zone" to (zone?: "center")) // Выполнение действия
            val success = executeAction(commandConfig, voiceParamsWithZone)

            if (success) {
                Log.i(TAG, "Command executed successfully")
                val successPhrase = commandConfig.phrases?.success
                                    ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.SUCCESS) // Подстановка параметров в фразу
                val finalPhrase = substituteParams(successPhrase, commandData.extractedParams) // Голос + Overlay (если включено)
                if (!finalPhrase.isNullOrEmpty()) {
                    if (showNotification) {
                        toastMessengerManager.show(finalPhrase)
                    }
                    speechService.enqueueAsync(finalPhrase)
                }
            }
            else {
                Log.w(TAG, "Command execution failed")
                val failurePhrase = commandConfig.phrases?.failure
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
    private fun executeAction(commandConfig: CommandConfig,
                              voiceParams: Map<String, Any>): Boolean {
        Log.d(TAG, "Command: ${commandConfig.id} via: ${vehicleCommandExecutor.executionMethod}")
        return try {
            val success = vehicleCommandExecutor.executeByCommandId(commandId = commandConfig.id,
                                                                    voiceParams = voiceParams)
            if (success) {
                Log.i(TAG, "Command executed successfully")
            }
            else {
                Log.w(TAG, "Command execution failed")
            }
            success
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to execute command", e)
            false
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

