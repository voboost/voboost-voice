package ru.voboost.voiceassistant.executor.handlers.aidl.smartmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Режим отдыха
 *
 * config.json:
 *   id: "smart_mode_leisure", classify: 22, command: 0
 *   params: mode=18
 *
 * CAN-шина: setVehicleSceneMode(18)
 */
class SmartModeLeisureHandler(
    canBusManager: CanBusServiceManager
) : AbstractSmartModeHandler("smart_mode_leisure", canBusManager, modeId = 18)
