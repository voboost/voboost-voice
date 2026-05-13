package ru.voboost.voiceassistant.executor.handlers.aidl.smartmode

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

class WashSmartModeHandler(private val canBusManager: CanBusServiceManager) : ICommandHandler {

    companion object {
        const val TAG = "SmartModeCommand"

        const val ON = 1
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        Log.d(TAG, "SmartMode command: modeId=CAR_CLEANING_MODE_SWITCH")
        return canBusManager.setVehicleState(VehicleState.CAR_CLEANING_MODE_SWITCH, ON)
    }
}