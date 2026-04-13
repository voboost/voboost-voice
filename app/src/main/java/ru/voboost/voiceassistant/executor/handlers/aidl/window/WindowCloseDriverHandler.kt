package ru.voboost.voiceassistant.executor.handlers.aidl.window

import com.qinggan.canbus.VehicleState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Закрыть окно водителя
 *
 * config.json:
 *   id: "window_close"
 *   target: "Window", classify: 2, command: 1
 *
 * CAN-шина:
 *   VehicleState.DRIVER_WINDOW_CONTROL
 *   value: VALUE_WINDOW_DRIVER_CLOSE (51)
 */
class WindowCloseDriverHandler(
    canBusManager: CanBusServiceManager
) : AbstractWindowHandler(canBusManager) {
    override fun getWindowStateAndValue(): Pair<VehicleState, Int> =
        VehicleState.DRIVER_WINDOW_CONTROL to CanBusServiceManager.VALUE_WINDOW_DRIVER_CLOSE
}
