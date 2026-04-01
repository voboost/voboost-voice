package com.voboost.voiceassistant.executor

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.voboost.voiceassistant.VoboostVoiceService
import com.voboost.voiceassistant.config.ActionConfig
import com.voboost.voiceassistant.config.CommandConfig
import com.voboost.voiceassistant.nlu.RecognizedCommand
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Выполнение команд
 * Использует VehicleCommandExecutor для отправки команд автомобилю
 * 
 * @param vehicleCommandExecutor Реализация выполнения команд (Intent или Shell)
 */
class CommandExecutor(
    private val context: Context,
    private val ttsEngine: SpeechSynthesis,
    private val nluEngine: NLUEngine,
    private val overlayManager: OverlayManager,
    private val coroutineScope: CoroutineScope,
    private val vehicleCommandExecutor: VehicleCommandExecutor  // ← Интерфейс выполнения
) {
    companion object {
        private const val TAG = "CommandExecutor"
    }

    private val configManager = ConfigManager.getInstance(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Сохраняем уровень громкости до приглушения
    private var originalVolume = -1

    init {
        Log.i(TAG, "Initialized with execution method: ${vehicleCommandExecutor.executionMethod}")
    }

    /**
     * Выполнить команду
     */
    suspend fun executeCommand(recognizedCommand: RecognizedCommand) {
        val commandConfig = recognizedCommand.config

        Log.i(TAG, "Executing command: ${commandConfig.id}")

        // Приглушить звук (Audio Ducking)
        duckAudio(true)

        try {
            // Проверка необходимости подтверждения
            if (nluEngine.requiresConfirmation(commandConfig)) {
                val confirmed = requestConfirmation(commandConfig)

                if (!confirmed) {
                    Log.i(TAG, "Command cancelled by user")
                    val cancelledPhrase = commandConfig.phrases?.cancelled
                        ?: configManager.getConfig().phrases.notUnderstood
                    ttsEngine.speak(cancelledPhrase)
                    overlayManager.showToast(cancelledPhrase)
                    return
                }
            }

            // Выполнение действия
            val success = executeAction(commandConfig, recognizedCommand.extractedParams)

            if (success) {
                Log.i(TAG, "Command executed successfully")
                val successPhrase = commandConfig.phrases?.success
                    ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.SUCCESS)

                // Подстановка параметров в фразу
                val finalPhrase = substituteParams(successPhrase, recognizedCommand.extractedParams)

                // Голос + Overlay (всегда!)
                ttsEngine.speak(finalPhrase)
                overlayManager.showToast(finalPhrase)
            } else {
                Log.w(TAG, "Command execution failed")
                val failurePhrase = commandConfig.phrases?.failure
                    ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)

                // Голос + Overlay (всегда!)
                ttsEngine.speak(failurePhrase)
                overlayManager.showToast(failurePhrase)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            val errorPhrase = configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)
            ttsEngine.speak(errorPhrase)
            overlayManager.showToast(errorPhrase)
        } finally {
            // Восстановить громкость
            duckAudio(false)
        }
    }

    /**
     * Запросить подтверждение у пользователя (голосовое)
     * Использует VoboostVoiceService для распознавания ответа
     */
    private suspend fun requestConfirmation(commandConfig: CommandConfig): Boolean {
        val question = nluEngine.getConfirmationQuestion(commandConfig)
        val timeout = nluEngine.getConfirmationTimeout(commandConfig)

        Log.d(TAG, "Requesting confirmation: '$question', timeout: ${timeout}s")

        // Получить сервис
        val service = VoboostVoiceService.getInstance()
            ?: run {
                Log.e(TAG, "VoboostVoiceService instance is null, auto-confirming")
                return true
            }

        // Запросить подтверждение через сервис
        val response = service.requestConfirmation(question, timeout * 1000L)

        if (response.isEmpty()) {
            Log.w(TAG, "No response to confirmation request")
            return false
        }

        Log.d(TAG, "Confirmation response: '$response'")

        // Проверяем ответ
        return if (nluEngine.isConfirmationYes(response, commandConfig)) {
            Log.i(TAG, "Confirmation: YES")
            true
        } else if (nluEngine.isConfirmationNo(response, commandConfig)) {
            Log.i(TAG, "Confirmation: NO")
            false
        } else {
            Log.w(TAG, "Unknown confirmation response")
            false
        }
    }

    /**
     * Приглушить/восстановить звук (Audio Ducking)
     */
    private fun duckAudio(duck: Boolean) {
        try {
            if (duck) {
                // Сохраняем текущую громкость
                originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                // Уменьшаем громкость на 50%
                val duckedVolume = (originalVolume * 0.5).toInt()
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    duckedVolume.coerceAtLeast(0),
                    0
                )
                Log.d(TAG, "Audio ducked: $originalVolume -> $duckedVolume")
            } else {
                // Восстанавливаем громкость через 1 секунду (чтобы TTS закончил)
                coroutineScope.launch {
                    delay(1000)
                    if (originalVolume >= 0 && isActive) {
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            originalVolume,
                            0
                        )
                        Log.d(TAG, "Audio restored: $originalVolume")
                        originalVolume = -1
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to duck audio", e)
        }
    }

    /**
     * Выполнить действие команды
     * @return true если успешно
     */
    private fun executeAction(
        commandConfig: CommandConfig,
        params: Map<String, String>
    ): Boolean {
        val action = commandConfig.action

        Log.d(TAG, "Executing via ${vehicleCommandExecutor.executionMethod}")
        Log.d(
            TAG,
            "Target: ${action.target}, Classify: ${action.classify}, Command: ${action.command}"
        )

        return try {
            val success = if (action.target == "telephone") {
                // Телефонные команды
                vehicleCommandExecutor.executePhoneCommand(
                    classify = action.classify,
                    command = action.command,
                    contact = params["contact"],
                    number = params["number"],
                    callType = params["call_type"] ?: "contact"
                )
            } else {
                // Команды автомобиля
                val vehicleParams = buildVehicleParams(action, params)
                vehicleCommandExecutor.execute(
                    target = action.target,
                    classify = action.classify,
                    command = action.command,
                    params = vehicleParams
                )
            }

            if (success) {
                Log.i(TAG, "Command executed successfully")
            } else {
                Log.w(TAG, "Command execution failed")
            }

            success

        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute command", e)
            false
        }
    }

    /**
     * Построить параметры для команды автомобиля
     */
    private fun buildVehicleParams(
        action: ActionConfig,
        params: Map<String, String>
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        // Добавляем параметры из действия
        action.params.forEach { entry ->
            result[entry.key] = entry.value
        }

        // Добавляем параметры из распознавания
        params.forEach { entry ->
            // Пытаемся преобразовать в число если возможно
            entry.value.toIntOrNull()?.let {
                result[entry.key] = it
            } ?: run {
                result[entry.key] = entry.value
            }
        }

        return result
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
    fun handleUnrecognizedCommand(text: String) {
        Log.w(TAG, "Command not recognized: $text")

        val notUnderstoodPhrase =
            configManager.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD)

        if (!notUnderstoodPhrase.isNullOrEmpty()) {
            ttsEngine.speak(notUnderstoodPhrase)
            overlayManager.showToast(notUnderstoodPhrase)
        } else {
            Log.w(TAG, "No phrase for NOT_UNDERSTOOD")
            overlayManager.showToast("Команда не распознана")
        }
    }

    /**
     * Очистить ресурсы
     */
    fun cleanup() {
        // coroutineScope.cancel() // Не отменяем - scope принадлежит сервису
    }
}