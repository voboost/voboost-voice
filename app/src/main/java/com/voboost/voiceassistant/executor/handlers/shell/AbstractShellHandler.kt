package com.voboost.voiceassistant.executor.handlers.shell

import android.util.Log
import com.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Базовый обработчик для Shell-команд
 */
abstract class AbstractShellHandler(
    override val commandId: String,
    private val commandBuilder: (voiceParams: Map<String, Any>) -> String
) : ICommandHandler {

    override fun execute(
        voiceParams: Map<String, Any>
    ): Boolean {
        return try {
            val shellCommand = commandBuilder(voiceParams)
            Log.d(TAG, "Executing shell: commandId='$commandId', cmd=$shellCommand")

            val process = Runtime.getRuntime().exec(shellCommand)
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Log.i(TAG, "Shell OK: $commandId")
                true
            } else {
                Log.w(TAG, "Shell failed: $commandId (exit code: $exitCode)")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Shell exception: $commandId", e)
            false
        }
    }

    companion object {
        const val TAG = "ShellHandler"
        protected const val CAN_SERVICE_BASE = "service call qg.canbus 58 i32 50"
    }
}
