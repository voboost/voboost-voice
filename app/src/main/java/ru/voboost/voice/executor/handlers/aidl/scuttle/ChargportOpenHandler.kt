package ru.voboost.voice.executor.handlers.aidl.scuttle

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

/**
 * Открыть лючок зарядки
 *
 * config.json:
 *   id: "charge_port_open", classify: 35, command: 1
 *
 * CAN-шина:
 *   VehicleState.IVI_CHRG_PORT_CAP (779)
 *   value: VALUE_OPEN (2)
 */
class ChargportOpenHandler(private val canBusManager: CanBusServiceManager)
    : ICommandHandler {

    companion object {
        const val TAG = "ChargportCommand"
    }

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return ICommandHandler.NEGATIVE_RESULT
        }

        val value = CanBusServiceManager.VALUE_OPEN
        val state = VehicleState.IVI_CHRG_PORT_CAP
        Log.d(TAG, "Chargport command:, IState=$state (ordinal=${state.ordinal}), value=$value")
        val result = canBusManager.setVehicleState(state, value)
        return CommandResult(result)
    }
}

