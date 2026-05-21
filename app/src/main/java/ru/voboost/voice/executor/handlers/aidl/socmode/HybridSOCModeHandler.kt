package ru.voboost.voice.executor.handlers.aidl.socmode

import ru.voboost.voice.canbus.CanBusServiceManager

class HybridSOCModeHandler(canBusManager: CanBusServiceManager)
    : AbstractSOCModeHandler (canBusManager, HYBRID)

