package com.voboost.voiceassistant.executor.handlers.shell.window

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Открыть все окна через Shell
 */
class WindowOpenAllShellHandler : AbstractShellHandler(
    "window_all_open",
    { _ -> "$CAN_SERVICE_BASE i32 ALL_WINDOW_CONTROL i32 3" }
)
