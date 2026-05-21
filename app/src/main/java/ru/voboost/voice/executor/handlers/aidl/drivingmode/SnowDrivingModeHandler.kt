package ru.voboost.voice.executor.handlers.aidl.drivingmode

import ru.voboost.voice.canbus.CanBusServiceManager

class SnowDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, SNOW)

