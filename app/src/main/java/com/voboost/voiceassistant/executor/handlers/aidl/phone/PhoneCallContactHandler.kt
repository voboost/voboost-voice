package com.voboost.voiceassistant.executor.handlers.aidl.phone

import android.util.Log
import com.qinggan.canbus.PhoneInfo
import com.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Звонок контакту (через CAN-шину)
 *
 * config.json:
 *   id: "phone_call_contact"
 *   target: "telephone", classify: 1, command: 1
 *   params: contact, call_type="contact"
 */
class PhoneCallContactHandler(
    canBusManager: CanBusServiceManager
) : AbstractPhoneHandler("phone_call_contact", canBusManager) {

    override fun buildPhoneInfo(voiceParams: Map<String, Any>): PhoneInfo? {
        val contact = voiceParams["contact"] as? String
            ?: return null.also { Log.w(TAG, "Contact parameter not found") }

        return createPhoneInfo(name = contact)
    }
}
