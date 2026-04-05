package com.voboost.voiceassistant.executor.handlers.intent

import android.content.Context
import android.content.Intent
import android.util.Log
import com.voboost.voiceassistant.config.ActionConfig
import com.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Базовый обработчик для Broadcast Intent команд
 */
abstract class AbstractIntentHandler(
    override val commandId: String,
    protected val context: Context
) : ICommandHandler {

    override fun execute(
        config: ActionConfig,
        voiceParams: Map<String, Any>
    ): Boolean {
        return try {
            val intent = buildIntent(config, voiceParams)
            context.sendBroadcast(intent)
            Log.d(TAG, "Broadcast sent: commandId='$commandId'")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send broadcast: $commandId", e)
            false
        }
    }

    protected abstract fun buildIntent(config: ActionConfig, voiceParams: Map<String, Any>): Intent

    companion object {
        const val TAG = "IntentHandler"
        const val ACTION_TELEPHONE_CALL = "pateo.dls.ivoka.telephone.CALL"
        const val VOICE_PARAM_TELEPHONE_NAME = "voice.param.telephone.name"
        const val VOICE_PARAM_TELEPHONE_NUMBER = "voice.param.telephone.number"
        const val VOICE_PARAM_TELEPHONE_CMD = "voice.param.telephone.cmd"
        const val VOICE_PARAM_TELEPHONE_LOCATION = "voice.param.telephone.location"
    }
}
