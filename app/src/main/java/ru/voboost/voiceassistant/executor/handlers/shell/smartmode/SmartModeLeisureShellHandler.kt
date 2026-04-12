package ru.voboost.voiceassistant.executor.handlers.shell.smartmode

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Режим отдыха через Shell
 */
class SmartModeLeisureShellHandler : AbstractShellHandler(
    "smart_mode_leisure",
    { _ -> "echo 18 > /sdcard/Download/myvoyah/files/drive_mode.txt" }
)
