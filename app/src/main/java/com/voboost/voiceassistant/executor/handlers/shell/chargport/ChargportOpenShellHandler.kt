package com.voboost.voiceassistant.executor.handlers.shell.chargport

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Открыть лючок зарядки через Shell
 */
class ChargportOpenShellHandler : AbstractShellHandler(
    "charge_port_open",
    { _, _ -> "$CAN_SERVICE_BASE i32 IVI_CHRG_PORT_CAP i32 2" }
)
