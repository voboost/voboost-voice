package ru.voboost.voiceassistant.executor.handlers.shell.window

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Открыть окно водителя через Shell
 */
class WindowOpenDriverShellHandler : AbstractShellHandler(
        { _ -> "$CAN_SERVICE_BASE i32 DRIVER_WINDOW_CONTROL i32 2" }
)
