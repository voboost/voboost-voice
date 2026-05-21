package ru.voboost.voice.executor.handlers.aidl.window

import com.qinggan.canbus.VehicleState
import ru.voboost.voice.canbus.CanBusServiceManager

/**
 * Открыть окно водителя
 *
 * config.json:
 *   id: "window_open"
 *   target: "Window", classify: 2, command: 0
 *
 * CAN-шина:
 *   VehicleState.DRIVER_WINDOW_CONTROL
 *   value: VALUE_WINDOW_DRIVER_OPEN (97)
 */
class WindowOpenDriverHandler(canBusManager: CanBusServiceManager) :
        AbstractWindowHandler(canBusManager) {
    override fun getWindowStateAndValue(): Pair<VehicleState, Int> =
        VehicleState.DRIVER_WINDOW_CONTROL to CanBusServiceManager.VALUE_WINDOW_DRIVER_OPEN
}


