package ru.voboost.voice.executor.handlers.aidl.socmode

import ru.voboost.voice.services.canbus.CanBusServiceManager

class SaveSOCModeHandler(canBusManager: CanBusServiceManager)
    : AbstractSOCModeHandler (canBusManager, SAVE)

