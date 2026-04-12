package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Состояние: Выполнение команды
 *
 * Логика:
 * 1. Выполнить команду через CommandExecutor
 * 2. Ждём пока TTS закончит говорить
 * 3. → finish(StateResult.Next(StateType.IDLE))
 *
 * canCancel = false — кнопка игнорируется (команда уже отправлена)
 */
class ExecutingCommandState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: ISpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : BaseState() {
    companion object {
        const val TAG = "ExecutingCommand"
    }

    // Команда уже выполняется — отменять поздно
    override val canCancel = false

    override suspend fun execute() {
        val command = context.recognizedCommand ?: run {
            Log.e(TAG, "No recognized command in context")
            finish(StateResult.Next(StateType.COMMAND_ERROR))
            return
        }

        Log.i(TAG, "Entering EXECUTING_COMMAND IState: ${command.id}")

        try {
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

            // Небольшая задержка перед возвратом громкости, чтобы последняя фраза не съедалась
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                delay(500)
            }

            // Успех → возвращаемся к ожиданию ключевого слова
            finish(StateResult.Next(StateType.IDLE))

        } catch (e: CancellationException) {
            Log.d(TAG, "ExecutingCommandState cancelled")
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            throw e

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command: ${command.id}", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(StateType.COMMAND_ERROR))
        }
    }

    override suspend fun cancel() {
        // Не вызывается т.к. canCancel = false
        Log.w(TAG, "Cancel called but canCancel=false, ignoring")
    }
}
