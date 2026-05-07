package ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class EcoDrivingModeHandler(canBusManager: CanBusServiceManager)
    : AbstractDrivingModeHandler(canBusManager, ECO)