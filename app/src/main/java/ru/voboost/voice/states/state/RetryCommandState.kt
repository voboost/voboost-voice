package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.ui.ToastMessengerManager

/**
 * Состояние: Повторная попытка распознавания команды
 *
 * Логика:
 * 1. Если была неоднозначность (contextCmd.size > 1) → возвращаемся в IDLE (без повтора)
 * 2. Проверяем лимит попыток: attemptsCount >= maxAttempts → COMMAND_ERROR
 * 3. Скажем "Повторите" через TTS и показываем уведомление
 * 4. Возвращаемся в LISTENING_COMMAND для новой попытки
 */
class RetryCommandState(private val context: StateContext,
                        private var speechService: ISpeechService,
                        private var configManager: ConfigManager,
                        private var soundEffectManager: SoundEffectManager,
                        private val toastMessengerManager: ToastMessengerManager)
    : BaseState() {

    companion object {
        const val TAG = "RetryCommandState"
    }

    override suspend fun entering() {
        // Проверяем, была ли неоднозначность
        if (context.commandData?.contextCmd?.size ?: 0 > 1) {
            Log.i(TAG, "Ambiguous detected in retry, returning to IDLE")
            onComplite(StateResult(StateType.IDLE))
            return
        }

        // Проверяем лимит попыток
        context.attemptsCount++
        val maxAttempts = configManager.getConfig().speech.offline.maxAttempts

        Log.i(TAG, "Attempt ${context.attemptsCount}/${maxAttempts}")

        if (context.attemptsCount >= maxAttempts) {
            Log.w(TAG, "Max attempts reached: ${context.attemptsCount}")
            onComplite(StateResult(StateType.COMMAND_ERROR))
            return
        }

        // Звук перед фразой повтора (как в AmbiguousState)
        soundEffectManager.playStartSoundAsync()

        // Говорим фразу повтора
        val retryPhrase = configManager.getDefaultPhrase(ConfigManager.PhraseType.NOT_UNDERSTOOD_RETRY)
        speechService.enqueueAsync(retryPhrase, SpeechService.PRIOR_MEDIUM)
        toastMessengerManager.show(retryPhrase)

        Log.i(TAG, "Returning to LISTENING_COMMAND for retry")
        onComplite(StateResult(StateType.LISTENING_COMMAND))
    }

    override suspend fun canceled() = onComplite(StateResult(StateType.CANCEL))
}
