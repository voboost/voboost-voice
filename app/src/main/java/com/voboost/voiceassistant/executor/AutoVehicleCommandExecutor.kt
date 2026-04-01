package com.voboost.voiceassistant.executor

import android.content.Context
import android.util.Log

/**
 * Автоматический выбор метода выполнения команд
 * Сначала пытается выполнить через Intent, при неудаче использует Shell
 */
class AutoVehicleCommandExecutor(
    private val context: Context
) : VehicleCommandExecutor {

    companion object {
        private const val TAG = "AutoVehicleCommand"
    }

    private val intentExecutor = IntentVehicleCommandExecutor(context)
    private val shellExecutor = ShellVehicleCommandExecutor()
    
    // Счётчик неудачных попыток для Intent
    private var intentFailureCount = 0
    
    // Порог переключения на Shell
    private val failureThreshold = 3
    
    // Флаг: использовать ли только Shell после неудач
    private var useShellOnly = false

    override val executionMethod: String
        get() = if (useShellOnly) "Shell (fallback)" else "Intent (auto)"

    override fun execute(
        target: String,
        classify: Int,
        command: Int,
        params: Map<String, Any>
    ): Boolean {
        // Если уже переключились на Shell
        if (useShellOnly) {
            Log.d(TAG, "Using Shell (fallback mode)")
            return shellExecutor.execute(target, classify, command, params)
        }

        // Пытаемся Intent
        Log.d(TAG, "Attempting Intent execution")
        val intentSuccess = intentExecutor.execute(target, classify, command, params)

        if (intentSuccess) {
            // Сбрасываем счётчик неудач
            intentFailureCount = 0
            return true
        }

        // Неудача Intent
        intentFailureCount++
        Log.w(TAG, "Intent failed (count: $intentFailureCount/$failureThreshold)")

        // Если достигли порога, переключаемся на Shell
        if (intentFailureCount >= failureThreshold) {
            Log.w(TAG, "Switching to Shell execution mode")
            useShellOnly = true
        }

        // Пытаемся Shell
        Log.d(TAG, "Falling back to Shell execution")
        return shellExecutor.execute(target, classify, command, params)
    }

    override fun executePhoneCommand(
        classify: Int,
        command: Int,
        contact: String?,
        number: String?,
        callType: String
    ): Boolean {
        // Телефонные команды только через Intent
        Log.d(TAG, "Phone command via Intent")
        return intentExecutor.executePhoneCommand(classify, command, contact, number, callType)
    }

    /**
     * Сбросить состояние (для тестирования)
     */
    fun reset() {
        intentFailureCount = 0
        useShellOnly = false
        Log.i(TAG, "Executor state reset")
    }

    /**
     * Получить текущий режим выполнения
     */
    fun getCurrentMode(): String {
        return if (useShellOnly) "SHELL" else "INTENT"
    }
}
