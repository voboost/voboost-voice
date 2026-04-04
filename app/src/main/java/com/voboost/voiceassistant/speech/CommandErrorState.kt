package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Ошибка выполнения команды
 * 
 * Логика:
 * 1. Сказать "Произошла ошибка"
 * 2. Скрыть анимацию
 * 3. Восстановить громкость
 * 4. → IdleState
 */
class CommandErrorState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val error: String
) : State {
    companion object {
        private const val TAG = "CommandErrorState"
    }

    override suspend fun execute(): State {
        Log.e(TAG, "Entering COMMAND_ERROR state: $error")

        return try {
            // Сказать "Произошла ошибка"
            // ttsEngine.speak("Произошла ошибка, попробуйте ещё раз")

            // Завершить команду
            speechSM.finishCommand()

            // Возвращаемся к ожиданию
            IdleState(speechSM, overlayManager, volumeManager) {
                // Callback для ключевого слова
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in CommandErrorState", e)
            IdleState(speechSM, overlayManager, volumeManager) {
                // Callback для ключевого слова
            }
        }
    }
}
