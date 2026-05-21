package ru.voboost.voice.executor.handlers.aidl.drivingmode

import ru.voboost.voice.canbus.CanBusServiceManager

class ComfortDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, COMFORT)



