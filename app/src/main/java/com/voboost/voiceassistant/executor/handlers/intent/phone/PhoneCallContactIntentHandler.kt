package com.voboost.voiceassistant.executor.handlers.intent.phone

import android.content.Context
import com.voboost.voiceassistant.config.ActionConfig
import com.voboost.voiceassistant.executor.handlers.intent.AbstractIntentHandler

/**
 * Звонок контакту через Broadcast Intent
 */
class PhoneCallContactIntentHandler(context: Context) : AbstractIntentHandler("phone_call_contact", context) {
    override fun buildIntent(config: ActionConfig, voiceParams: Map<String, Any>) = android.content.Intent(ACTION_TELEPHONE_CALL).apply {
        putExtra(VOICE_PARAM_TELEPHONE_NAME, voiceParams["contact"] as? String ?: "")
        putExtra(VOICE_PARAM_TELEPHONE_CMD, config.command.toString())
        putExtra(VOICE_PARAM_TELEPHONE_LOCATION, "local")
    }
}
