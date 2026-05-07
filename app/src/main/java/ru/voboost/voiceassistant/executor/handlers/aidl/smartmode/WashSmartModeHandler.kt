package ru.voboost.voiceassistant.executor.handlers.aidl.smartmode

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

class WashSmartModeHandler(canBusManager: CanBusServiceManager) :
        AbstractSmartModeHandler(canBusManager, modeId = WASH)