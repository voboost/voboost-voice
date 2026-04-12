package com.voboost.voiceassistant.executor.handlers.shell.smartmode

import com.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Романтический режим через Shell
 */
class SmartModeRomanticShellHandler : AbstractShellHandler(
    "smart_mode_romantic",
    { _ -> "echo 6 > /sdcard/Download/myvoyah/files/energy_mode.txt" }
)
