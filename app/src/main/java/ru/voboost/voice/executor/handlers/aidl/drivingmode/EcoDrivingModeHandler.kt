package ru.voboost.voice.executor.handlers.aidl.drivingmode

import ru.voboost.voice.canbus.CanBusServiceManager

class EcoDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, ECO)

