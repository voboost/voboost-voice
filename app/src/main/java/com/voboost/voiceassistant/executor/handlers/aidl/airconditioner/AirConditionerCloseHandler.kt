package com.voboost.voiceassistant.executor.handlers.aidl.airconditioner

import com.qinggan.canbus.AirConditionState
import com.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Выключить кондиционер
 *
 * config.json:
 *   id: "ac_close", classify: 5, command: 1
 *
 * CAN-шина:
 *   AirConditionState.AC_POWER_SWITCH
 *   value: CLOSE (0)
 */
class AirConditionerCloseHandler(
    canBusManager: CanBusServiceManager
) : AbstractAirConditionerHandler("ac_close", canBusManager) {
    override fun getAirConditionStateAndValue(voiceParams: Map<String, Any>): Pair<AirConditionState, Int> =
        AirConditionState.AC_POWER_SWITCH to AirConditionState.CLOSE
}
