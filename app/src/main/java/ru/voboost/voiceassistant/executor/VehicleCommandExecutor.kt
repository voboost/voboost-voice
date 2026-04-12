package ru.voboost.voiceassistant.executor

import android.util.Log
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Универсальный исполнитель команд через реестр handlers
 * Может работать с любым набором handlers (AIDL, Intent, Shell, микс)
 *
 * @param handlers Реестр команд: commandId → ICommandHandler
 * @param isConnectedChecker Функция проверки подключения
 */
class VehicleCommandExecutor(
    private val handlers: Map<String, ICommandHandler>,
    private val isConnectedChecker: () -> Boolean = { true }
) : IVehicleCommandExecutor {

    companion object {
        const val TAG = "VehicleCommandExec"
    }

    override val executionMethod: String = handlers.values.firstOrNull()?.let {
        it.javaClass.simpleName.replace("Handler", "")
    } ?: "Unknown"

    override fun executeByCommandId(
        commandId: String,
        voiceParams: Map<String, Any>
    ): Boolean {
        if (!isConnectedChecker()) {
            Log.w(TAG, "Not connected")
            return false
        }

        val handler = handlers[commandId]
            ?: return false.also { Log.w(TAG, "No handler for command: '$commandId'") }

        Log.d(TAG, "Executing: commandId='$commandId', handler=${handler.javaClass.simpleName}")
        return try {
            handler.execute(voiceParams)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during command execution: $commandId", e)
            false
        }
    }
}
