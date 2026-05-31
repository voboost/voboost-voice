package ru.voboost.voice.states.state

import android.util.Log
import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.config.ConfigManager.PhraseType
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.services.recognition.RecognitionService
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.services.speech.SpeechService
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.StateResult
import ru.voboost.voice.states.StateType
import ru.voboost.voice.ui.ToastMessengerManager

/**
 * Состояние: Неоднозначность в распознавании
 *
 * Логика:
 * 1. NLU вернул 2+ команды с похожим score
 * 2. Через TTS произносим фразу уточнения: "id1 или id2?"
 * 3. Возвращаемся в LISTENING_COMMAND с контекстом для повторного распознавания
 */
class AmbiguousState(private val context: StateContext,
                     private var recognitionService: IRecognitionService,
                     private var speechService: ISpeechService,
                     private var configManager: ConfigManager,
                     private var soundEffectManager: SoundEffectManager,
                     private val toastMessengerManager: ToastMessengerManager)
    : BaseState() {
    companion object {
        const val TAG = "AmbiguousState"
        const val CONJUNCTION = "или"
    }

    override val executeError: StateResult = StateResult(StateType.KEYWORD_ERROR)

    override suspend fun entering() {
        // Получаем ID команд из commandData.contextCmd
        val cmdIds = context.commandData?.contextCmd ?: emptyList()
        if (cmdIds.size < 2) {
            Log.w(TAG, "Less than 2 commands in context, returning to IDLE")
            onComplite(StateResult(StateType.IDLE))
            return
        }
        Log.i(TAG, "Ambiguous commands: $cmdIds")
        // Формируем фразу уточнения
        val ambiguousPhrase = buildAmbiguousPhrase(cmdIds)
        Log.d(TAG,"Ambiguous phrase: '$ambiguousPhrase'")
        recognitionService.setMode(RecognitionService.Mode.MUTED)
        // Проигрываем звук "дважды" перед вопросом
        soundEffectManager.playStartSoundAsync()
        // Говорим через TTS
        toastMessengerManager.show(ambiguousPhrase)
        speechService.enqueueAsync(ambiguousPhrase, SpeechService.PRIOR_HIGH)
        // Сохраняем контекст команд в StateContext для передачи в NLU
        Log.i(TAG, "Returning to LISTENING_COMMAND with context: $cmdIds")
        onComplite(StateResult(StateType.LISTENING_COMMAND))
    }

    override suspend fun canceled() = onComplite(StateResult(StateType.CANCEL))

    /**
     * Формирует фразу уточнения на основе ID команд.
     * Если для каждой команды есть phrases.ambiguous → использовать её
     * Иначе → использовать id команды как заглушку
     */
    private fun buildAmbiguousPhrase(cmdIds: List<String>): String {
        val cmdPhrases = cmdIds.map { cmdId ->
            val config = configManager.getCommandById(cmdId)
            if (config != null) {
                // Пробуем взять phrases.ambiguous
                val ambiguousPhrase = config.phrases?.ambiguous ?: config.phrases?.success
                if (!ambiguousPhrase.isNullOrEmpty()) {
                    ambiguousPhrase
                } else {
                    // Если нет фразы → используем id команды как заглушку
                    Log.w(TAG, "No phrase for command '$cmdId', using id as fallback")
                    cmdId
                }
            } else {
                // Конфиг не найден
                Log.e(TAG, "Command config not found: $cmdId")
                cmdId
            }
        }.take(2)  // Берём только первые 2 варианта

        if (cmdPhrases.size < 2) {
            return configManager.getDefaultPhrase(PhraseType.NOT_UNDERSTOOD)
        }

        return "${cmdPhrases[0]} $CONJUNCTION ${cmdPhrases[1]}?"
    }
}
