package com.voboost.voiceassistant.executor.handlers.aidl.smartmode

import com.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Романтический режим
 *
 * config.json:
 *   id: "smart_mode_romantic", classify: 22, command: 0
 *   params: mode=6
 *
 * CAN-шина: setVehicleSceneMode(6)
 */
class SmartModeRomanticHandler(
    canBusManager: CanBusServiceManager
) : AbstractSmartModeHandler("smart_mode_romantic", canBusManager, modeId = 6)
