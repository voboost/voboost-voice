package ru.voboost.voice.executor.handlers.aidl.scuttle

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

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
class FuelTankOpenHandler(private val canBusManager: CanBusServiceManager) : ICommandHandler {

    companion object {
        const val TAG = "ScuttleCommand"
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val state = VehicleState.IVI_FUEL_PORT_CAP
        val value = CanBusServiceManager.VALUE_OPEN  // 1 для этого авто
        Log.d(TAG, "Scuttle command: IState=$state (ordinal=${state.ordinal}), value=$value")
        return canBusManager.setVehicleState(state, value)
    }
}


