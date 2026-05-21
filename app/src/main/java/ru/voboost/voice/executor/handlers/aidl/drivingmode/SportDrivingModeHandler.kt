package ru.voboost.voice.executor.handlers.aidl.drivingmode

import ru.voboost.voice.canbus.CanBusServiceManager

class SportDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, SPORT)



