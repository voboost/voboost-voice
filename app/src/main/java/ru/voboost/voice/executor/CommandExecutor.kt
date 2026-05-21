package ru.voboost.voice.executor

import android.content.Context
import android.media.AudioManager
import android.util.Log
import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.nlu.RecognizedCommand
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.ui.OverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.voboost.voice.core.QueueSpeechSynthesis

/**
 * Выполнение команд
 * Использует VehicleCommandExecutor для отправки команд автомобилю
 *
 * @param vehicleCommandExecutor Реализация выполнения команд (Intent или Shell)
 */
class CommandExecutor(private val context: Context,
                      private val queueSpeech: QueueSpeechSynthesis,
                      private val overlayManager: OverlayManager,
                      private val coroutineScope: CoroutineScope,
                      private val vehicleCommandExecutor: IVehicleCommandExecutor,
                      private val configManager: ConfigManager) {
    companion object {
        const val TAG = "CommandExecutor"
    }

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
        val zone = recognizedCommand.zone

        Log.i(TAG, "Executing command: ${commandConfig.id} (zone=$zone)")

        // Приглушить звук (Audio Ducking)
        duckAudio(true)

        try { // Добавляем зону в voiceParams для команд климата
            val voiceParamsWithZone =
                recognizedCommand.extractedParams + ("_zone" to (zone ?: "center"))

            // Выполнение действия
            val success = executeAction(commandConfig, voiceParamsWithZone)

            if (success) {
                Log.i(TAG, "Command executed successfully")
                val successPhrase = commandConfig.phrases?.success
                    ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.SUCCESS)

                // Подстановка параметров в фразу
                val finalPhrase = substituteParams(successPhrase, recognizedCommand.extractedParams)

                // Голос + Overlay (всегда!)
                if (!finalPhrase.isNullOrEmpty()) {
                    val queueSpeechJob = coroutineScope.async {
                        queueSpeech.enqueueAsync(finalPhrase)
                    }
                    overlayManager.showToast(finalPhrase)
                    queueSpeechJob.await()
                }

            }
            else {
                Log.w(TAG, "Command execution failed")
                val failurePhrase = commandConfig.phrases?.failure
                    ?: configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)

                // Голос + Overlay (всегда!)
                if (!failurePhrase.isNullOrEmpty()) {
                    val queueSpeechJob = coroutineScope.async {
                        queueSpeech.enqueueAsync(failurePhrase)
                    }
                    overlayManager.showToast(failurePhrase)
                    queueSpeechJob.await()
                }
            }

        }
        catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            val errorPhrase = configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)
            if (!errorPhrase.isNullOrEmpty()) {
                val queueSpeechJob = coroutineScope.async {
                    queueSpeech.enqueueAsync(errorPhrase, QueueSpeechSynthesis.PRIOR_HIGH)
                }
                overlayManager.showToast(errorPhrase)
                queueSpeechJob.await()
            }

        }
        finally { // Восстановить громкость
            duckAudio(false)
        }
    }

    /**
     * Приглушить/восстановить звук (Audio Ducking)
     */
    private fun duckAudio(duck: Boolean) {
        try {
            if (duck) { // Сохраняем текущую громкость
                originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                // Уменьшаем громкость на 50%
                val duckedVolume = (originalVolume * 0.5).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                             duckedVolume.coerceAtLeast(0),
                                             0)
                Log.d(TAG, "Audio ducked: $originalVolume -> $duckedVolume")
            }
            else { // Восстанавливаем громкость через 1 секунду (чтобы TTS закончил)
                coroutineScope.launch {
                    delay(1000)
                    if (originalVolume >= 0 && isActive) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                        Log.d(TAG, "Audio restored: $originalVolume")
                        originalVolume = -1
                    }
                }
            }
        }
        catch (e: Exception) {
            Log.w(TAG, "Failed to duck audio", e)
        }
    }

    /**
     * Выполнить действие команды
     * @return true если успешно
     */
    private fun executeAction(commandConfig: CommandConfig,
                              voiceParams: Map<String, Any>): Boolean {
        Log.d(TAG, "Executing via ${vehicleCommandExecutor.executionMethod}")
        Log.d(TAG, "Command: ${commandConfig.id}")

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

        val notUnderstoodPhrase =
            configManager.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD)

        if (!notUnderstoodPhrase.isNullOrEmpty()) {
            val queueSpeechJob = coroutineScope.async {
                queueSpeech.enqueueAsync(notUnderstoodPhrase, QueueSpeechSynthesis.PRIOR_MEDIUM)
            }
            overlayManager.showToast(notUnderstoodPhrase)
            queueSpeechJob.await()
        }
        else {
            Log.w(TAG, "No phrase for NOT_UNDERSTOOD")
            overlayManager.showToast("Команда не распознана")
        }
    }

    /**
     * Очистить ресурсы
     */
    fun cleanup() { // coroutineScope.cancel() // Не отменяем - scope принадлежит сервису
    }
}

