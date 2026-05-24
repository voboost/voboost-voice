package ru.voboost.voice.executor.handlers.aidl.window

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

/**
 * Базовый абстрактный обработчик оконных команд
 *
 * Конкретные подклассы определяют только состояние и значение для CAN-шины
 *
 * @param commandId Уникальный ID команды (совпадает с config.json)
 * @param canBusManager Менеджер CAN-шины для отправки команд
 */
abstract class AbstractWindowHandler(protected val canBusManager: CanBusServiceManager)
    : ICommandHandler {

    companion object {
        const val TAG = "WindowCommand"
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val (state, value) = getWindowStateAndValue()
        Log.d(TAG, "Window command: IState=$state (ordinal=${state.ordinal}), value=$value")
        return canBusManager.setVehicleState(state, value)
    }

    /**
     * Возвращает пару (VehicleState, значение) для отправки в CAN-шину
     * Каждый конкретный обработчик возвращает свои значения
     */
    protected abstract fun getWindowStateAndValue(): Pair<VehicleState, Int>
}


