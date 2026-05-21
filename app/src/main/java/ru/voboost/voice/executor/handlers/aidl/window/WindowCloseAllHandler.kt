package ru.voboost.voice.executor.handlers.aidl.window

import com.qinggan.canbus.VehicleState
import ru.voboost.voice.canbus.CanBusServiceManager

/**
 * Закрыть все окна
 *
 * config.json:
 *   id: "window_all_close"
 *   target: "Window", classify: 3, command: 1
 *
 * CAN-шина:
 *   VehicleState.ALL_WINDOW_CONTROL
 *   value: VALUE_WINDOW_ALL_CLOSE (1)
 */
class WindowCloseAllHandler(canBusManager: CanBusServiceManager) :
        AbstractWindowHandler(canBusManager) {
    override fun getWindowStateAndValue(): Pair<VehicleState, Int> =
        VehicleState.ALL_WINDOW_CONTROL to CanBusServiceManager.VALUE_WINDOW_ALL_CLOSE
}


