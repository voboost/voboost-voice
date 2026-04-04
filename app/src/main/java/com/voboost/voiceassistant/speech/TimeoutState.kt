package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Таймаут команды
 * 
 * Логика:
 * 1. Сказать "Я не расслышал"
 * 2. Скрыть анимацию
 * 3. Восстановить громкость
 * 4. → IdleState
 */
class TimeoutState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?
) : State {
    companion object {
        private const val TAG = "TimeoutState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering TIMEOUT state")

        return try {
            // Сказать "Я не расслышал"
            // ttsEngine.speak("Я не расслышал, повторите пожалуйста")

            // Завершить команду
            speechSM.finishCommand()

            // Возвращаемся к ожиданию
            IdleState(speechSM, overlayManager, volumeManager) {
                // Callback для ключевого слова
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in TimeoutState", e)
            IdleState(speechSM, overlayManager, volumeManager) {
                // Callback для ключевого слова
            }
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in TimeoutState → IdleState")
        return IdleState(speechSM, overlayManager, volumeManager) {
            // Callback для ключевого слова
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Cannot activate from TimeoutState, ignoring")
        return this
    }
}
