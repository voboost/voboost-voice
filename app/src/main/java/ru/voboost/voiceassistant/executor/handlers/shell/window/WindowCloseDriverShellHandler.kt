package ru.voboost.voiceassistant.executor.handlers.shell.window

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Закрыть окно водителя через Shell
 */
class WindowCloseDriverShellHandler : AbstractShellHandler(
        { _ -> "$CAN_SERVICE_BASE i32 DRIVER_WINDOW_CONTROL i32 1" }
)
