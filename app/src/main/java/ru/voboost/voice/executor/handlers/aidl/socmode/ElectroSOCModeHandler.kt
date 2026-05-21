package ru.voboost.voice.executor.handlers.aidl.socmode

import ru.voboost.voice.canbus.CanBusServiceManager

class ElectroSOCModeHandler(canBusManager: CanBusServiceManager)
    : AbstractSOCModeHandler (canBusManager, ELECTRO)

