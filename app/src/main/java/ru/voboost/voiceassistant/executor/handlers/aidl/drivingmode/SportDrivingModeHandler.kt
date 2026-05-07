package ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class SportDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, SPORT)

