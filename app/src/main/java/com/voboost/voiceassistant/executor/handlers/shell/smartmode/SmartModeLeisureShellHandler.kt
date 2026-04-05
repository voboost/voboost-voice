package com.voboost.voiceassistant.executor.handlers.shell.smartmode

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Режим отдыха через Shell
 */
class SmartModeLeisureShellHandler : AbstractShellHandler(
    "smart_mode_leisure",
    { _, _ -> "echo 18 > /sdcard/Download/myvoyah/files/drive_mode.txt" }
)
