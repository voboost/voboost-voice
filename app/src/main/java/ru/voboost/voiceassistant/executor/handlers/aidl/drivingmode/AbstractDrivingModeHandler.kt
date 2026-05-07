package ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

abstract class AbstractDrivingModeHandler(protected val canBusManager: CanBusServiceManager,
                                          private val modeId: Int) : ICommandHandler {
    companion object {
        const val TAG = "DrivingModeCommand"

        const val ECO = 1
        const val COMFORT = 2
        const val SPORT = 3
        const val OFF_ROAD = 4
        const val INDIVIDUAL = 5
        const val SNOW = 6

    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        Log.d(TAG, "DrivingMode command: modeId=$modeId")
        return canBusManager.setVehicleState(VehicleState.DRIVING_MODE_SET, modeId)
    }
}

