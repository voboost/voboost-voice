package ru.voboost.voiceassistant.executor.handlers.shell.chargport

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Закрыть лючок зарядки через Shell
 */
class ChargportCloseShellHandler : AbstractShellHandler(
    "charge_port_close",
    { _ -> "$CAN_SERVICE_BASE i32 IVI_CHRG_PORT_CAP i32 1" }
)
