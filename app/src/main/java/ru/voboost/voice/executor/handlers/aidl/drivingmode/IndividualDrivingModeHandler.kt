package ru.voboost.voice.executor.handlers.aidl.drivingmode

import ru.voboost.voice.canbus.CanBusServiceManager

class IndividualDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, INDIVIDUAL)

