package ru.voboost.voiceassistant.executor.handlers.aidl.smartmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Детский режим
 *
 * config.json:
 *   id: "smart_mode_child", classify: 22, command: 0
 *   params: mode=22
 *
 * CAN-шина: setVehicleSceneMode(22)
 */
class ChildSmartModeHandler(canBusManager: CanBusServiceManager) :
        AbstractSmartModeHandler(canBusManager, modeId = CHILD)
