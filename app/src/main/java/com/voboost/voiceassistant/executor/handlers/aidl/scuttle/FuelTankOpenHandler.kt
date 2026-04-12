package com.voboost.voiceassistant.executor.handlers.aidl.scuttle

import android.util.Log
import com.qinggan.canbus.VehicleState
import com.voboost.voiceassistant.canbus.CanBusServiceManager
import com.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Открыть крышку бензобака
 *
 * config.json:
 *   id: "fuel_tank_open", classify: 19, command: 0
 *
 * CAN-шина:
 *   VehicleState.IVI_FUEL_PORT_CAP (778)
 *   value: VALUE_OPEN (2)
 *
 * Примечание: команда закрытия бензобака в config.json отсутствует
 * Если понадобится — добавить FuelTankCloseHandler по аналогии
 */
class FuelTankOpenHandler(
    private val canBusManager: CanBusServiceManager
) : ICommandHandler {

    override val commandId: String = "fuel_tank_open"

    override fun execute(
        voiceParams: Map<String, Any>
    ): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val IState = VehicleState.IVI_FUEL_PORT_CAP
        val value = CanBusServiceManager.VALUE_OPEN  // 1 для этого авто
        Log.d(TAG, "Scuttle command: id='$commandId', IState=$IState (ordinal=${IState.ordinal}), value=$value")
        return canBusManager.setVehicleState(IState, value)
    }

    companion object {
        const val TAG = "ScuttleCommand"
    }
}
