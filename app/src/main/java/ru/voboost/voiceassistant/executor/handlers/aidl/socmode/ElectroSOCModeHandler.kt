package ru.voboost.voiceassistant.executor.handlers.aidl.socmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class ElectroSOCModeHandler(canBusManager: CanBusServiceManager)
    : AbstractSOCModeHandler (canBusManager, ELECTRO)