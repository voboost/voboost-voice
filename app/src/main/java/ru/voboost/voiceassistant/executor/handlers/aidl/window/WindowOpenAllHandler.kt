package ru.voboost.voiceassistant.executor.handlers.aidl.window

import com.qinggan.canbus.VehicleState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Открыть все окна
 *
 * config.json:
 *   id: "window_all_open"
 *   target: "Window", classify: 3, command: 0
 *
 * CAN-шина:
 *   VehicleState.ALL_WINDOW_CONTROL
 *   value: VALUE_WINDOW_ALL_OPEN (3)
 */
class WindowOpenAllHandler(
    canBusManager: CanBusServiceManager
) : AbstractWindowHandler(canBusManager) {
    override fun getWindowStateAndValue(): Pair<VehicleState, Int> =
        VehicleState.ALL_WINDOW_CONTROL to CanBusServiceManager.VALUE_WINDOW_ALL_OPEN
}
