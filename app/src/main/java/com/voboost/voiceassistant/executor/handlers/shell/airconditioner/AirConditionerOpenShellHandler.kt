package com.voboost.voiceassistant.executor.handlers.shell.airconditioner

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Включить кондиционер через Shell
 */
class AirConditionerOpenShellHandler : AbstractShellHandler(
    "ac_open",
    { _ -> "$CAN_SERVICE_BASE i32 AC_POWER_SWITCH i32 2" }
)
