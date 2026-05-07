package ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class ComfortDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, COMFORT)

