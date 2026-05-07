package ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class OffRoadDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, OFF_ROAD)