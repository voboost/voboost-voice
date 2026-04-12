package ru.voboost.voiceassistant.executor.handlers.shell.airconditioner

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Выключить кондиционер через Shell
 */
class AirConditionerCloseShellHandler : AbstractShellHandler(
    "ac_close",
    { _ -> "$CAN_SERVICE_BASE i32 AC_POWER_SWITCH i32 1" }
)
