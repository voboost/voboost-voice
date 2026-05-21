package ru.voboost.voice.executor.handlers.aidl.smartmode

import ru.voboost.voice.canbus.CanBusServiceManager

/**
 * Режим отдыха
 *
 * config.json:
 *   id: "smart_mode_leisure", classify: 22, command: 0
 *   params: mode=18
 *
 * CAN-шина: setVehicleSceneMode(18)
 */
class LeisureSmartModeHandler(canBusManager: CanBusServiceManager) :
        AbstractSmartModeHandler(canBusManager, modeId = LEISURE)


