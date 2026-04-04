package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Ожидание ключевого слова
 * 
 * Логика:
 * 1. Скрыть анимацию
 * 2. Восстановить громкость музыки
 * 3. Запустить распознавание ключевого слова
 * 4. Если ключевое слово получено → ActivatedState
 * 5. Если ошибка → KeywordErrorState
 */
class IdleState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val onKeywordDetected: () -> Unit
) : State {
    companion object {
        private const val TAG = "IdleState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering IDLE state - waiting for keyword...")

        return try {
            // Скрыть анимацию и восстановить громкость
            overlayManager.hideAnimation()
            volumeManager?.restoreMedia()

            // Ждём ключевое слово
            speechSM.startListeningForKeyword(object : VoiceAssistantListener {
                override fun onKeywordDetected() {
                    onKeywordDetected()
                }

                override fun onError(error: String) {
                    Log.e(TAG, "Keyword spotting error: $error")
                }
            })

            // Ключевое слово получено → ActivatedState
            ActivatedState(speechSM, overlayManager, volumeManager)

        } catch (e: Exception) {
            Log.e(TAG, "Error in IdleState", e)
            KeywordErrorState(speechSM, overlayManager, volumeManager, e.message ?: "Unknown error")
        }
    }
}
