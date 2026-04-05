package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Состояние: Выполнение команды
 *
 * Логика:
 * 1. Выполнить команду через CommandExecutor
 * 2. Если успех → IdleState
 * 3. Если ошибка → CommandErrorState
 */
class ExecutingCommandState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "ExecutingCommandState"
    }

    override suspend fun execute(): State {
        val command = context.recognizedCommand ?: run {
            Log.e(TAG, "No recognized command in context")
            return CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, "No command to execute")
        }

        Log.i(TAG, "Entering EXECUTING_COMMAND state: ${command.id}")

        return try {
            // ОТКЛЮЧИТЬ распознавание пока TTS говорит ответ (чтобы не было ЭХО)
            speechRecognizer.setMode(SpeechRecognizer.Mode.MUTED)

            // Выполняем команду (внутри TTS скажет "Закрываю окно")
            commandExecutor.executeCommand(command)
            Log.i(TAG, "Command executed successfully: ${command.id}")

            // Ждём пока TTS закончит говорить (короткая задержка)
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                delay(2500) // Даем TTS время договорить
            }

            // ВКЛЮЧИТЬ распознавание ключевых слов (TTS закончил)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

            // Успех → возвращаемся к ожиданию ключевого слова
            IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)

        } catch (e: CancellationException) {
            Log.d(TAG, "ExecutingCommandState cancelled")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command: ${command.id}", e)
            // Восстановить распознавание при ошибке
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            CommandErrorState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ExecutingCommandState → IdleState")

        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)

        return IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ExecutingCommandState, ignoring")
        return this
    }
}
