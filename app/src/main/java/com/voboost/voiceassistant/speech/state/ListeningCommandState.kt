package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.speech.SpeechStateMachine
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Слушаем команду
 * 
 * Логика:
 * 1. Запустить распознавание команды
 * 2. Если команда получена → RecognizedCommandState
 * 3. Если таймаут → TimeoutState
 * 4. Если ошибка → CommandErrorState
 * 5. Если отмена → IdleState
 */
class ListeningCommandState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val ttsEngine: SpeechSynthesis,
    private val configManager: ConfigManager,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "ListeningCommandState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering LISTENING_COMMAND state")

        return try {
            // SpeechStateMachine уже активирован в ActivatedState
            // Просто ждём результат через callback
            // Этот State завершается сразу, т.к. SpeechStateMachine асинхронный
            
            // В реальной реализации здесь нужно ждать результат
            // Но пока SpeechStateMachine сам вызывает callback
            // Поэтому возвращаем IdleState как fallback
            IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context) {
                // Этот callback не будет вызван, т.к. мы уже в команде
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context, e.message ?: "Unknown error")
        }
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ListeningCommandState → IdleState")
        
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, context) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ListeningCommandState, ignoring")
        return this
    }
}
