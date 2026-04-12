package ru.voboost.voiceassistant.executor.handlers.aidl.chargport

import ru.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Закрыть лючок зарядки
 *
 * config.json:
 *   id: "charge_port_close", classify: 35, command: 2
 *
 * CAN-шина:
 *   VehicleState.IVI_CHRG_PORT_CAP (779)
 *   value: VALUE_CLOSE (1)
 */
class ChargportCloseHandler(
    canBusManager: CanBusServiceManager
) : AbstractChargportHandler("charge_port_close", canBusManager) {
    override fun getChargportValue(): Int = CanBusServiceManager.VALUE_CLOSE
}
