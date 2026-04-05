package com.voboost.voiceassistant.executor.handlers.shell.window

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Открыть окно водителя через Shell
 */
class WindowOpenDriverShellHandler : AbstractShellHandler(
    "window_open",
    { _, _ -> "$CAN_SERVICE_BASE i32 DRIVER_WINDOW_CONTROL i32 2" }
)
