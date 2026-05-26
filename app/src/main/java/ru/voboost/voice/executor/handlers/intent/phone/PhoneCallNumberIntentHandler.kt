package ru.voboost.voice.executor.handlers.intent.phone

import android.content.Context
import android.content.Intent
import android.util.Log
import ru.voboost.voice.executor.handlers.intent.AbstractIntentHandler

/**
 * Звонок по номеру через Broadcast Intent
 *
 * Отправляет broadcast на action "com.qinggan.broadcast.action.ivokaphonecall"
 * с параметрами, которые ожидает BluetoothPhone:
 *   - Ivoka_CallInfo: номер телефона (String)
 *   - screen_int: текущий экран (int, по умолчанию 0)
 *   - mac: MAC-адрес Bluetooth (пустая строка)
 */
class PhoneCallNumberIntentHandler(context: Context)
    : AbstractIntentHandler(context) {

    override fun buildIntent(voiceParams: Map<String, Any>): Intent? {
        val phoneNumber = voiceParams["number"] as? String ?: ""

        Log.d(TAG, "Phone call to number: '$phoneNumber' Action: $ACTION_IVOKA_PHONE_CALL")

        if (phoneNumber.isNullOrEmpty()) {
            return null;
        }

        return Intent(ACTION_IVOKA_PHONE_CALL).apply {
            putExtra(EXTRA_IVOKA_CALL_INFO, phoneNumber)
            putExtra(EXTRA_SCREEN_INT, 0)
            putExtra(EXTRA_MAC, "")
        }
    }
}


