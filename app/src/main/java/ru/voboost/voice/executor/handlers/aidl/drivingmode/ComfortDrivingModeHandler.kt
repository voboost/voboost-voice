package ru.voboost.voice.executor.handlers.aidl.drivingmode

import ru.voboost.voice.services.canbus.CanBusServiceManager

class ComfortDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, COMFORT)



