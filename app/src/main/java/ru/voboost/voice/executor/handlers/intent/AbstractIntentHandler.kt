package ru.voboost.voice.executor.handlers.intent

import android.content.Context
import android.content.Intent
import android.util.Log
import ru.voboost.voice.audio.MultiChannelAudioSource
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.executor.handlers.ICommandHandler

/**
 * Базовый обработчик для Broadcast Intent команд
 */
abstract class AbstractIntentHandler(protected val context: Context) : ICommandHandler {
    companion object {
        const val TAG = "IntentHandler"

        // Action для звонков через BluetoothPhone (тот же что использует Ivoka)
        const val ACTION_IVOKA_PHONE_CALL = "com.qinggan.broadcast.action.ivokaphonecall"
        // Параметры которые ожидает BluetoothPhone
        const val EXTRA_IVOKA_CALL_INFO = "Ivoka_CallInfo"
        const val EXTRA_SCREEN_INT = "screen_int"
        const val EXTRA_MAC = "mac"
    }

    override fun execute(commandData: CommandData): CommandResult {
        return try {

            val parsParams = parsParams(commandData)
            val intent = buildIntent(parsParams) ?: return ICommandHandler.NEGATIVE_RESULT

            context.sendBroadcast(intent)
            Log.d(TAG, "Broadcast sent: action='${intent.action}'")
            Log.d(TAG, "  Extras: ${intent.extras?.keySet()?.joinToString(", ")}")
            CommandResult(true, parsParams)
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to send broadcast", e)
            ICommandHandler.NEGATIVE_RESULT
        }
    }

    protected abstract fun buildIntent(voiceParams: Map<String, Any>): Intent?

    protected fun parsParams(commandData: CommandData): Map<String, String> {
        val paramsText : MutableMap<String, String> = mutableMapOf()
        paramsText["_zone"] = commandData.zone ?: MultiChannelAudioSource.ZONE_FRONT_LEFT
        return paramsText;
    }
}


