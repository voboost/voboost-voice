package ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class IndividualDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, INDIVIDUAL)