package ru.voboost.voice.executor.handlers.aidl.smartmode

import ru.voboost.voice.services.canbus.CanBusServiceManager

/**
 * Романтический режим
 *
 * config.json:
 *   id: "smart_mode_romantic", classify: 22, command: 0
 *   params: mode=6
 *
 * CAN-шина: setVehicleSceneMode(6)
 */
class RomanticSmartModeHandler(canBusManager: CanBusServiceManager) :
        AbstractSmartModeHandler(canBusManager, modeId = ROMANTIC)



