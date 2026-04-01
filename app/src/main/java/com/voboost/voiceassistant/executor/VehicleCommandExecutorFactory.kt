package com.voboost.voiceassistant.executor

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Фабрика для создания реализаций VehicleCommandExecutor
 *
 * Позволяет легко переключаться между способами выполнения команд:
 * - HYBRID - автоматический выбор (телефон=Intent, остальное=AIDL) ← РЕКОМЕНДУЕМЫЙ
 * - Intent (Broadcast) - стандартный способ через системный сервис
 * - Shell (CAN) - прямой вызов через shell-команды (требует привилегий)
 * - AIDL - прямой вызов CanBusService
 * - Auto - попытка Intent, при неудаче Shell
 */
object VehicleCommandExecutorFactory {

    private const val TAG = "VehicleCommandFactory"

    enum class ExecutionMode {
        HYBRID,      // Телефон=Intent, остальное=AIDL (рекомендуется)
        INTENT,      // Только Broadcast Intent
        SHELL,       // Только Shell CAN
        AIDL,        // Только AIDL CanBusService
        AUTO         // Intent с fallback на Shell
    }

    /**
     * Создать реализацию VehicleCommandExecutor
     *
     * @param context Context приложения
     * @param canBusManager Менеджер CanBusService (для AIDL режимов)
     * @param mode Режим выполнения команд
     * @return Реализация VehicleCommandExecutor
     */
    fun create(
        context: Context,
        canBusManager: CanBusServiceManager,
        mode: ExecutionMode = ExecutionMode.HYBRID
    ): VehicleCommandExecutor {
        return when (mode) {
            ExecutionMode.HYBRID -> {
                Log.i(TAG, "Creating HybridVehicleCommandExecutor (RECOMMENDED)")
                HybridVehicleCommandExecutor(context, canBusManager)
            }
            ExecutionMode.INTENT -> {
                Log.i(TAG, "Creating IntentVehicleCommandExecutor")
                IntentVehicleCommandExecutor(context)
            }
            ExecutionMode.SHELL -> {
                Log.i(TAG, "Creating ShellVehicleCommandExecutor")
                ShellVehicleCommandExecutor()
            }
            ExecutionMode.AIDL -> {
                Log.i(TAG, "Creating AIDLVehicleCommandExecutor")
                AIDLVehicleCommandExecutor(canBusManager)
            }
            ExecutionMode.AUTO -> {
                Log.i(TAG, "Creating AutoVehicleCommandExecutor (Intent with Shell fallback)")
                AutoVehicleCommandExecutor(context)
            }
        }
    }

    /**
     * Создать из строкового параметра (например, из config)
     *
     * @param context Context приложения
     * @param canBusManager Менеджер CanBusService (для AIDL режимов)
     * @param modeString Строка режима: "hybrid", "intent", "shell", "aidl", "auto"
     * @return Реализация VehicleCommandExecutor
     */
    fun createFromString(
        context: Context,
        canBusManager: CanBusServiceManager,
        modeString: String?
    ): VehicleCommandExecutor {
        val mode = when (modeString?.lowercase()?.trim()) {
            "shell" -> ExecutionMode.SHELL
            "aidl" -> ExecutionMode.AIDL
            "auto" -> ExecutionMode.AUTO
            "hybrid", null -> ExecutionMode.HYBRID  // HYBRID по умолчанию
            "intent" -> ExecutionMode.INTENT
            else -> {
                Log.w(TAG, "Unknown mode: '$modeString', using HYBRID")
                ExecutionMode.HYBRID
            }
        }
        return create(context, canBusManager, mode)
    }
}
