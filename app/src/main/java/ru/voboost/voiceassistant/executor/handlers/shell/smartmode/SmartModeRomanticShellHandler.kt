package ru.voboost.voiceassistant.executor.handlers.shell.smartmode

import ru.voboost.voiceassistant.executor.handlers.shell.AbstractShellHandler

/**
 * Романтический режим через Shell
 */
class SmartModeRomanticShellHandler : AbstractShellHandler(
        { _ -> "echo 6 > /sdcard/Download/myvoyah/files/energy_mode.txt" }
)
