package ru.voboost.voiceassistant.executor.handlers.shell.smartmode

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Детский режим через Shell
 */
class SmartModeChildShellHandler : AbstractShellHandler(
        { _ -> "echo 22 > /sdcard/Download/myvoyah/files/power_mode.txt" }
)
