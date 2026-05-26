package ru.voboost.voice.executor.handlers.aidl.drivingmode

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

abstract class AbstractDrivingModeHandler(protected val canBusManager: CanBusServiceManager,
                                          private val modeId: Int)
    : ICommandHandler {

    companion object {
        const val TAG = "DrivingModeCommand"
        const val ECO = 1
        const val COMFORT = 2
        const val SPORT = 3
        const val OFF_ROAD = 4
        const val INDIVIDUAL = 5
        const val SNOW = 6
    }

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return ICommandHandler.NEGATIVE_RESULT
        }

        Log.d(TAG, "DrivingMode command: modeId=$modeId")
        val result = canBusManager.setVehicleState(VehicleState.DRIVING_MODE_SET, modeId)
        return CommandResult(result)
    }
}



