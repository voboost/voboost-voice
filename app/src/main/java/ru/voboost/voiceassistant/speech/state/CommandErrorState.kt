package ru.voboost.voiceassistant.speech.state

import android.util.Log
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Состояние: Ошибка выполнения команды
 *
 * Логика:
 * 1. Восстановить распознавание
 * 2. finish(StateResult.Next(IdleState))
 *
 * canCancel = false — уже переходит в IdleState
 */
class CommandErrorState(
    private val speechRecognizer: SpeechRecognizer,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: ISpeechSynthesis,
    private val configManager: ConfigManager,
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext,
    private val error: String
) : BaseState() {
    companion object {
        const val TAG = "CommandErrorState"
    }

    override val canCancel = false

    override suspend fun execute() {
        Log.e(TAG, "Entering COMMAND_ERROR IState: $error")

        try {
            // Сказать пользователю что команда не распознана
            val notUnderstoodPhrase = configManager.getConfig().phrases.notUnderstood ?: "Не поняла команду"
            val ttsLatch = CountDownLatch(1)
            ttsEngine.speak(notUnderstoodPhrase) { ttsLatch.countDown() }
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                ttsLatch.await(5, TimeUnit.SECONDS)
            }

            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error in CommandErrorState", e)
            speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
            finish(StateResult.Next(
                IdleState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
            ))
        }
    }

    override suspend fun cancel() {
        // Не вызывается т.к. canCancel = false
        Log.w(TAG, "Cancel called but canCancel=false, ignoring")
    }

    override suspend fun activate(): IState? {
        Log.i(TAG, "Activate from CommandErrorState → ActivatedState")
        speechRecognizer.setMode(SpeechRecognizer.Mode.KEYWORD)
        return ActivatedState(speechRecognizer, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context)
    }
}
