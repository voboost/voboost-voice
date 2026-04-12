package com.voboost.voiceassistant.executor.handlers.shell.window

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Закрыть окно водителя через Shell
 */
class WindowCloseDriverShellHandler : AbstractShellHandler(
    "window_close",
    { _ -> "$CAN_SERVICE_BASE i32 DRIVER_WINDOW_CONTROL i32 1" }
)
