package com.voboost.voiceassistant.executor.handlers.aidl.airconditioner

import com.qinggan.canbus.AirConditionState
import com.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Включить кондиционер
 *
 * config.json:
 *   id: "ac_open", classify: 5, command: 0
 *
 * CAN-шина:
 *   AirConditionState.AC_POWER_SWITCH
 *   value: OPEN (1)
 */
class AirConditionerOpenHandler(
    canBusManager: CanBusServiceManager
) : AbstractAirConditionerHandler("ac_open", canBusManager) {
    override fun getAirConditionStateAndValue(voiceParams: Map<String, Any>): Pair<AirConditionState, Int> =
        AirConditionState.AC_POWER_SWITCH to AirConditionState.OPEN
}
