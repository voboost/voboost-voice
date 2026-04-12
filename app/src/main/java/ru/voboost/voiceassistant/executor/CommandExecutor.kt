package ru.voboost.voiceassistant.executor

import android.content.Context
import android.media.AudioManager
import android.util.Log
import ru.voboost.voiceassistant.VoboostVoiceService
import ru.voboost.voiceassistant.config.CommandConfig
import ru.voboost.voiceassistant.nlu.RecognizedCommand
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.ui.OverlayManager
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
    private val ttsEngine: ISpeechSynthesis,
    private val nluEngine: NLUEngine,
    private val overlayManager: OverlayManager,
    private val coroutineScope: CoroutineScope,
    private val vehicleCommandExecutor: IVehicleCommandExecutor  // ← Интерфейс выполнения
) {
    companion object {
        const val TAG = "CommandExecutor"
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
        voiceParams: Map<String, Any>
    ): Boolean {
        Log.d(TAG, "Executing via ${vehicleCommandExecutor.executionMethod}")
        Log.d(TAG, "Command: ${commandConfig.id}")

        return try {
            val success = vehicleCommandExecutor.executeByCommandId(
                commandId = commandConfig.id,
                voiceParams = voiceParams
            )

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