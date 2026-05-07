package ru.voboost.voiceassistant.executor.handlers.aidl.socmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class SaveSOCModeHandler(canBusManager: CanBusServiceManager)
    : AbstractSOCModeHandler (canBusManager, SAVE)