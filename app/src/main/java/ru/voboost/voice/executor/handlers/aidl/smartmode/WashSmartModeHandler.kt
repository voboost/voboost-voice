package ru.voboost.voice.executor.handlers.aidl.smartmode

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

class WashSmartModeHandler(private val canBusManager: CanBusServiceManager)
    : ICommandHandler {

    companion object {
        const val TAG = "SmartModeCommand"
        const val ON = 1
    }

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return ICommandHandler.NEGATIVE_RESULT
        }

        Log.d(TAG, "SmartMode command: modeId=CAR_CLEANING_MODE_SWITCH")
        val result = canBusManager.setVehicleState(VehicleState.CAR_CLEANING_MODE_SWITCH, ON)
        return CommandResult(result)
    }
}

