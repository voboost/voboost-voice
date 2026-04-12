package com.voboost.voiceassistant.executor.handlers.intent.phone

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.config.ActionConfig
import com.voboost.voiceassistant.executor.handlers.intent.AbstractIntentHandler

/**
 * Звонок по номеру через Broadcast Intent
 *
 * Отправляет broadcast на action "com.qinggan.broadcast.action.ivokaphonecall"
 * с параметрами, которые ожидает BluetoothPhone:
 *   - Ivoka_CallInfo: номер телефона (String)
 *   - screen_int: текущий экран (int, по умолчанию 0)
 *   - mac: MAC-адрес Bluetooth (пустая строка)
 */
class PhoneCallNumberIntentHandler(context: Context) : AbstractIntentHandler("phone_call_number", context) {

    override fun buildIntent(config: ActionConfig, voiceParams: Map<String, Any>) = android.content.Intent(ACTION_IVOKA_PHONE_CALL).apply {
        val phoneNumber = voiceParams["number"] as? String ?: ""
        putExtra(EXTRA_IVOKA_CALL_INFO, phoneNumber)
        putExtra(EXTRA_SCREEN_INT, 0)
        putExtra(EXTRA_MAC, "")
        
        Log.d(TAG, "Phone call to number: '$phoneNumber'")
        Log.d(TAG, "  Action: $ACTION_IVOKA_PHONE_CALL")
        Log.d(TAG, "  Extra Ivoka_CallInfo: '$phoneNumber'")
    }
}
