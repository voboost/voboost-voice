package ru.voboost.voiceassistant.executor.handlers.aidl.socmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class HybridSOCModeHandler(canBusManager: CanBusServiceManager)
    : AbstractSOCModeHandler (canBusManager, HYBRID)