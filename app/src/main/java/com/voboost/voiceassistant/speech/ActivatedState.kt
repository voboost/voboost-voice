package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
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
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager
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

            // Сказать что слушаем (опционально)
            val listeningPhrase = configManager.getConfig().phrases.listening
            if (!listeningPhrase.isNullOrEmpty()) {
                ttsEngine.speak(listeningPhrase)
            } else {
                Log.w(TAG, "Listening phrase is null or empty")
            }

            // Переходим к слушанию команды
            speechSM.activate()
            ListeningCommandState(speechSM, overlayManager, volumeManager, ttsEngine, configManager)

        } catch (e: Exception) {
            Log.e(TAG, "Error in ActivatedState", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, e.message ?: "Unknown error")
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
