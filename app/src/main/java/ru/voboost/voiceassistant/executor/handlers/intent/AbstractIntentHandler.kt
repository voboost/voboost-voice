package ru.voboost.voiceassistant.executor.handlers.intent

import android.content.Context
import android.content.Intent
import android.util.Log
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Базовый обработчик для Broadcast Intent команд
 */
abstract class AbstractIntentHandler(protected val context: Context)
    : ICommandHandler {
    companion object {
        const val TAG = "IntentHandler"

        // Action для звонков через BluetoothPhone (тот же что использует Ivoka)
        const val ACTION_IVOKA_PHONE_CALL = "com.qinggan.broadcast.action.ivokaphonecall"

        // Параметры которые ожидает BluetoothPhone
        const val EXTRA_IVOKA_CALL_INFO = "Ivoka_CallInfo"
        const val EXTRA_SCREEN_INT = "screen_int"
        const val EXTRA_MAC = "mac"
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        return try {
            val intent = buildIntent(voiceParams)

            if(intent == null)
            {
                return false;
            }
            context.sendBroadcast(intent)
            Log.d(TAG, "Broadcast sent: action='${intent.action}'")
            Log.d(TAG, "  Extras: ${intent.extras?.keySet()?.joinToString(", ")}")
            true
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to send broadcast", e)
            false
        }
    }

    protected abstract fun buildIntent(voiceParams: Map<String, Any>): Intent?
}
