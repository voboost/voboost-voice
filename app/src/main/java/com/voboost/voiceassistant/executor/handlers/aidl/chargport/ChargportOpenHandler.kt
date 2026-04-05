package com.voboost.voiceassistant.executor.handlers.aidl.chargport

import com.voboost.voiceassistant.canbus.CanBusServiceManager

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
class ChargportOpenHandler(
    canBusManager: CanBusServiceManager
) : AbstractChargportHandler("charge_port_open", canBusManager) {
    override fun getChargportValue(): Int = CanBusServiceManager.VALUE_OPEN
}
