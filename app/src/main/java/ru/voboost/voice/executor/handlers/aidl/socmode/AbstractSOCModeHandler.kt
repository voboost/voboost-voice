package ru.voboost.voice.executor.handlers.aidl.socmode

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

abstract class AbstractSOCModeHandler(protected val canBusManager: CanBusServiceManager,
                                      private val modeId: Int)
    : ICommandHandler {
    companion object {
        const val TAG = "SOCModeCommand"
        const val ELECTRO = 2
        const val HYBRID = 3
        const val SAVE = 4
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        Log.d(TAG, "SOCMode command: modeId=$modeId")
        return canBusManager.setVehicleState(VehicleState.IVI_SOC_MODESET, modeId)
    }
}




