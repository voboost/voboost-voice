package ru.voboost.voiceassistant.executor.handlers.shell.scuttle

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Открыть крышку бензобака через Shell
 */
class FuelTankOpenShellHandler : AbstractShellHandler(
        { _ -> "$CAN_SERVICE_BASE i32 IVI_FUEL_PORT_CAP i32 2" }
)
