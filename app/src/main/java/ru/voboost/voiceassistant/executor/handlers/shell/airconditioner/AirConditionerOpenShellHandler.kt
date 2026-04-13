package ru.voboost.voiceassistant.executor.handlers.shell.airconditioner

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Включить кондиционер через Shell
 */
class AirConditionerOpenShellHandler : AbstractShellHandler(
        { _ -> "$CAN_SERVICE_BASE i32 AC_POWER_SWITCH i32 2" }
)
