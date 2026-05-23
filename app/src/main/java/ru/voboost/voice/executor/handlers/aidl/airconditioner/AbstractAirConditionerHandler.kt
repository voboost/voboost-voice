package ru.voboost.voice.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

/**
 * Базовый абстрактный обработчик команд кондиционера
 *
 * @param commandId Уникальный ID команды (совпадает с config.json)
 * @param canBusManager Менеджер CAN-шины
 */
abstract class AbstractAirConditionerHandler(protected val canBusManager: CanBusServiceManager) :
        ICommandHandler {

    companion object {
        const val TAG = "AirConditionerCmd"
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val (state, value) = getAirConditionStateAndValue(voiceParams)
        Log.d(TAG, "AC command:, IState=$state (${state.ordinal}), value=$value")
        return canBusManager.setAirConditionState(state, value)
    }

    /**
     * Возвращает пару (AirConditionState, значение) для отправки в CAN-шину
     * Для установки температуры значение берётся из voiceParams["temperature"]
     */
    protected abstract fun getAirConditionStateAndValue(voiceParams: Map<String, Any>): Pair<AirConditionState, Int>
}


