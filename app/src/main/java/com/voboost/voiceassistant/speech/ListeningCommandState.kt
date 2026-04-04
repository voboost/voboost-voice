package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager

/**
 * Состояние: Слушаем команду
 * 
 * Логика:
 * 1. Запустить распознавание команды
 * 2. Если команда получена → ProcessingCommandState
 * 3. Если таймаут → TimeoutState
 * 4. Если ошибка → CommandErrorState
 * 5. Если отмена → IdleState
 */
class ListeningCommandState(
    private val speechSM: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?
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
            IdleState(speechSM, overlayManager, volumeManager) {
                // Этот callback не будет вызван, т.к. мы уже в команде
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, e.message ?: "Unknown error")
        }
    }
}
