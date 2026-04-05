package com.voboost.voiceassistant.executor.handlers.aidl.phone

import android.util.Log
import com.qinggan.canbus.PhoneInfo
import com.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Звонок по номеру телефона (через CAN-шину)
 *
 * config.json:
 *   id: "phone_call_number"
 *   target: "telephone", classify: 1, command: 1
 *   params: number, call_type="number"
 */
class PhoneCallNumberHandler(
    canBusManager: CanBusServiceManager
) : AbstractPhoneHandler("phone_call_number", canBusManager) {

    override fun buildPhoneInfo(voiceParams: Map<String, Any>): PhoneInfo? {
        val number = voiceParams["number"] as? String
            ?: return null.also { Log.w(TAG, "Number parameter not found") }

        return createPhoneInfo(phoneNum = number)
    }
}
