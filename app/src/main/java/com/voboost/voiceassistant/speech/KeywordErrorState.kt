package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Ошибка распознавания ключевого слова
 * 
 * Логика:
 * 1. Сказать "Произошла ошибка"
 * 2. Скрыть анимацию (если была показана)
 * 3. Восстановить громкость
 * 4. → IdleState
 */
class KeywordErrorState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val error: String
) : State {
    companion object {
        private const val TAG = "KeywordErrorState"
    }

    override suspend fun execute(): State {
        Log.e(TAG, "Entering KEYWORD_ERROR state: $error")

        return try {
            // Сказать "Произошла ошибка"
            // ttsEngine.speak("Произошла ошибка, попробуйте ещё раз")

            // Скрыть анимацию и восстановить громкость
            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()

            // Пауза перед возвратом к ожиданию
            kotlinx.coroutines.delay(1000)

            // Возвращаемся к ожиданию
            IdleState(speechSM, overlayManager, volumeManager) {
                // Callback для ключевого слова
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in KeywordErrorState", e)
            IdleState(speechSM, overlayManager, volumeManager) {
                // Callback для ключевого слова
            }
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in KeywordErrorState → IdleState")
        return IdleState(speechSM, overlayManager, volumeManager) {
            // Callback для ключевого слова
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Cannot activate from KeywordErrorState, ignoring")
        return this
    }
}
