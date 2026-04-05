package com.voboost.voiceassistant.executor.handlers.shell.airconditioner

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Установить температуру кондиционера через Shell
 */
class AirConditionerSetTempShellHandler : AbstractShellHandler(
    "ac_set_temp",
    { _, voiceParams ->
        val temp = voiceParams["temperature"] as? Int ?: 22
        "$CAN_SERVICE_BASE i32 AC_POWER_SWITCH i32 $temp"
    }
)
