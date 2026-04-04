package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.*

/**
 * Состояния голосового помощника
 * 
 * Включает бизнес-логику поверх распознавания речи:
 * - UI (анимации)
 * - Громкость (приглушение музыки)
 * - Подтверждения команд
 */
enum class AssistantState {
    /** Ожидание ключевой фразы */
    IDLE,
    
    /** Ключевое слово распознано, активация */
    ACTIVATED,
    
    /** Слушаем команду */
    LISTENING_COMMAND,
    
    /** Ждём подтверждения от пользователя */
    WAITING_CONFIRMATION,
    
    /** Выполняем команду */
    PROCESSING_COMMAND,
    
    /** Ошибка */
    ERROR
}

/**
 * Callbacks для событий голосового помощника
 */
interface VoiceAssistantCallback {
    /** Ключевое слово распознано */
    fun onKeywordDetected()
    
    /** Команда распознана */
    fun onCommandReceived(text: String)
    
    /** Ошибка распознавания */
    fun onError(error: String)
    
    /** Таймаут ожидания */
    suspend fun onTimeout()
}

/**
 * State Machine для голосового помощника
 * 
 * Оборачивает SpeechStateMachine и добавляет бизнес-логику:
 * - Управление UI (OverlayManager)
 * - Управление громкостью (VolumeManager)
 * - Состояния подтверждения
 * 
 * Преимущества:
 * - ✅ Нет флагов в VoboostVoiceService
 * - ✅ Автоматическое обновление UI и громкости
 * - ✅ Централизованное управление состоянием
 * - ✅ Легко тестировать
 */
class VoiceAssistantStateMachine(
    private val speechStateMachine: SpeechStateMachine,
    private val overlayManager: OverlayManager,
    private val volumeManager: VolumeManager?,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "VoiceAssistantSM"
    }
    
    @Volatile
    private var state: AssistantState = AssistantState.IDLE
    
    /**
     * Текущее состояние
     */
    fun getState(): AssistantState = state
    
    /**
     * Проверка что помощник слушает команду
     */
    fun isListeningCommand(): Boolean = 
        state == AssistantState.LISTENING_COMMAND || state == AssistantState.WAITING_CONFIRMATION
    
    /**
     * Проверка что ожидается подтверждение
     */
    fun isWaitingConfirmation(): Boolean = state == AssistantState.WAITING_CONFIRMATION
    
    /**
     * Запустить распознавание ключевого слова
     */
    fun startListeningForKeyword(callbacks: VoiceAssistantListener) {
        transitionTo(AssistantState.IDLE)
        
        speechStateMachine.start(object : SpeechRecognitionListener {
            override fun onKeywordDetected() {
                callbacks.onKeywordDetected()
            }
            
            override fun onError(error: String) {
                transitionTo(AssistantState.ERROR)
                callbacks.onError(error)
            }
            
            override fun onStateChanged(speechState: SpeechState) {
                Log.d(TAG, "Speech state changed: $speechState")
            }
        })
    }
    
    /**
     * Активировать помощник (после ключевого слова или кнопки)
     */
    fun activate(callbacks: VoiceAssistantCallback) {
        transitionTo(AssistantState.ACTIVATED)
        
        speechStateMachine.activate()
        speechStateMachine.startListeningCommand(object : SpeechRecognitionListener {
            override fun onCommandReceived(text: String) {
                transitionTo(AssistantState.PROCESSING_COMMAND)
                callbacks.onCommandReceived(text)
            }
            
            override fun onError(error: String) {
                transitionTo(AssistantState.ERROR)
                callbacks.onError(error)
            }
            
            override suspend fun onTimeout() {
                callbacks.onTimeout()
            }
        })
    }
    
    /**
     * Запросить подтверждение от пользователя
     * @return true если успешно, false если уже в другом состоянии
     */
    fun requestConfirmation(): Boolean {
        if (state != AssistantState.LISTENING_COMMAND && state != AssistantState.ACTIVATED) {
            Log.w(TAG, "Cannot request confirmation in state: $state")
            return false
        }
        
        transitionTo(AssistantState.WAITING_CONFIRMATION)
        return true
    }
    
    /**
     * Завершить подтверждение (пользователь ответил)
     */
    fun finishConfirmation() {
        if (state == AssistantState.WAITING_CONFIRMATION) {
            transitionTo(AssistantState.LISTENING_COMMAND)
        }
    }
    
    /**
     * Завершить выполнение команды и вернуться к ожиданию
     */
    fun finishCommand() {
        transitionTo(AssistantState.IDLE)
        speechStateMachine.returnToKeywordListening()
    }
    
    /**
     * Отменить распознавание
     */
    fun cancel() {
        Log.i(TAG, "Cancelling recognition")
        transitionTo(AssistantState.IDLE)
        speechStateMachine.returnToKeywordListening()
    }
    
    /**
     * Остановить всё
     */
    fun shutdown() {
        transitionTo(AssistantState.IDLE)
        speechStateMachine.shutdown()
    }
    
    // ==================== ВНУТРЕННИЕ МЕТОДЫ ====================
    
    /**
     * Перейти в новое состояние с автоматическим обновлением UI и громкости
     */
    private fun transitionTo(newState: AssistantState) {
        val oldState = state
        state = newState
        
        Log.d(TAG, "State transition: $oldState → $newState")
        
        // Автоматически обновляем UI и громкость
        scope.launch {
            when (newState) {
                AssistantState.ACTIVATED,
                AssistantState.LISTENING_COMMAND -> {
                    // Показываем анимацию и приглушаем музыку
                    withContext(Dispatchers.Main) {
                        overlayManager.showAnimation()
                    }
                    volumeManager?.duckMedia(targetVolume = 1)
                }
                
                AssistantState.IDLE,
                AssistantState.ERROR -> {
                    // Скрываем анимацию и восстанавливаем музыку
                    withContext(Dispatchers.Main) {
                        overlayManager.hideAnimation()
                    }
                    volumeManager?.restoreMedia()
                }
                
                AssistantState.WAITING_CONFIRMATION -> {
                    // Ждём подтверждения — анимация остаётся
                    // Громкость уже приглушена
                }
                
                AssistantState.PROCESSING_COMMAND -> {
                    // Выполняем команду — анимация остаётся
                    // Громкость уже приглушена
                }
            }
        }
    }
}

/**
 * Упрощённый интерфейс слушателя для VoiceAssistantStateMachine
 */
interface VoiceAssistantListener {
    fun onKeywordDetected()
    fun onError(error: String)
}
