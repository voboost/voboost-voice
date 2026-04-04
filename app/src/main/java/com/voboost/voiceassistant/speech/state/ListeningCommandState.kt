package com.voboost.voiceassistant.speech.state

import android.util.Log
import com.voboost.voiceassistant.audio.VolumeManager
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.speech.SpeechRecognitionListener
import com.voboost.voiceassistant.speech.SpeechStateMachine
import com.voboost.voiceassistant.ui.OverlayManager
import kotlinx.coroutines.CompletableDeferred

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
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor,
    private val context: StateContext
) : State {
    companion object {
        private const val TAG = "ListeningCommandState"
    }

    override suspend fun execute(): State {
        Log.i(TAG, "Entering LISTENING_COMMAND state")

        return try {
            // Ждём результат распознавания
            val commandText = waitForCommand()
            
            if (commandText != null && commandText.isNotEmpty()) {
                Log.i(TAG, "Command received: '$commandText'")
                context.commandText = commandText
                
                // Переходим к распознаванию команды
                return RecognizedCommandState(
                    speechSM = speechSM,
                    overlayManager = overlayManager,
                    volumeManager = volumeManager,
                    ttsEngine = ttsEngine,
                    configManager = configManager,
                    nluEngine = nluEngine,
                    commandExecutor = commandExecutor,
                    context = context
                )
            } else {
                Log.w(TAG, "No command received or empty")
                IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context) {
                    // Callback будет установлен при создании нового IdleState
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in ListeningCommandState", e)
            CommandErrorState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context, e.message ?: "Unknown error")
        }
    }

    /**
     * Ждём результат распознавания команды
     * @return Распознанный текст или null при таймауте/ошибке
     */
    private suspend fun waitForCommand(): String? {
        val result = CompletableDeferred<String?>()
        
        val listener = object : SpeechRecognitionListener {
            override fun onCommandReceived(text: String) {
                if (!result.isCompleted) {
                    result.complete(text)
                }
            }
            
            override fun onError(error: String) {
                if (!result.isCompleted) {
                    result.complete(null)
                }
            }
            
            override suspend fun onTimeout() {
                if (!result.isCompleted) {
                    result.complete(null)
                }
            }
        }
        
        // Запускаем распознавание команды
        speechSM.startListeningCommand(listener)
        
        // Ждём результат (таймаут обрабатывается внутри SpeechStateMachine)
        return result.await()
    }

    override suspend fun cancel(): State {
        Log.i(TAG, "Cancel in ListeningCommandState → IdleState")
        
        overlayManager.hideAnimation()
        volumeManager?.restoreMedia()
        speechSM.returnToKeywordListening()
        
        return IdleState(speechSM, overlayManager, volumeManager, ttsEngine, configManager, nluEngine, commandExecutor, context) {
            // Callback будет установлен при создании нового IdleState
        }
    }

    override suspend fun activate(): State {
        Log.i(TAG, "Already in ListeningCommandState, ignoring")
        return this
    }
}
