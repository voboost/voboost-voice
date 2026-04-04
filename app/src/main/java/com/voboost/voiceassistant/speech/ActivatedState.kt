package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Активация (после ключевого слова)
 * 
 * Логика:
 * 1. Показать анимацию
 * 2. Приглушить музыку
 * 3. Воспроизвести звук начала распознавания
 * 4. Сказать "Слушаю вас"
 * 5. → ListeningCommandState
 */
class ActivatedState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?
) : State {
    companion object {
        private const val TAG = "ActivatedState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering ACTIVATED state")

        return try {
            // Показать анимацию и приглушить музыку
            overlayManager.showAnimation()
            volumeManager?.duckMedia(targetVolume = 1)

            // Звук начала распознавания
            // soundEffectManager.playStartSound()  // ← Можно добавить

            // Сказать "Слушаю вас"
            // ttsEngine.speak("Слушаю вас")  // ← Можно добавить

            // Переходим к слушанию команды
            speechSM.activate()
            ListeningCommandState(speechSM, overlayManager, volumeManager)

        } catch (e: Exception) {
            Log.e(TAG, "Error in ActivatedState", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ActivatedState → IdleState")
        
        // Скрыть анимацию и восстановить громкость
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        
        // Вернуться к ожиданию
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ActivatedState, ignoring")
        return this
    }
}
