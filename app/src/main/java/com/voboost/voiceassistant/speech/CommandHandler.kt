package com.voboost.voiceassistant.speech

import android.util.Log
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.nlu.NLUEngine

/**
 * Обработчик команд
 * Отвечает ТОЛЬКО за:
 * - Парсинг текста через NLU
 * - Выполнение команд через CommandExecutor
 */
class CommandHandler(
    private val nluEngine: NLUEngine,
    private val commandExecutor: CommandExecutor
) {
    companion object {
        const val TAG = "CommandHandler"
    }

    /**
     * Обработать распознанный текст
     * @param text Распознанный текст
     */
    suspend fun handleCommand(text: String) {
        try {
            Log.i(TAG, "Processing command: '$text'")

            // Парсим текст через NLU
            val recognizedCommand = nluEngine.parseCommand(text)
            
            if (recognizedCommand != null) {
                Log.d(TAG, "Parsed command: ${recognizedCommand.id}")
                // Выполняем команду
                commandExecutor.executeCommand(recognizedCommand)
                Log.i(TAG, "Command executed successfully")
            } else {
                Log.w(TAG, "Unrecognized command: '$text'")
                commandExecutor.handleUnrecognizedCommand(text)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command: '$text'", e)
            throw e
        }
    }
}
