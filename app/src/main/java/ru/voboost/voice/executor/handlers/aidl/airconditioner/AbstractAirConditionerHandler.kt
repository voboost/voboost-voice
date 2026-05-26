package ru.voboost.voice.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voice.audio.MultiChannelAudioSource
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
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

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return CommandResult(false)
        }

        val parsParams = parsParams(commandData)
        val (state, value) = getAirConditionStateAndValue(parsParams)
        Log.d(TAG, "AC command:, IState=$state (${state.ordinal}), value=$value")
        val result = canBusManager.setAirConditionState(state, value)
        return CommandResult(result)
    }

    /**
     * Возвращает пару (AirConditionState, значение) для отправки в CAN-шину
     * Для установки температуры значение берётся из voiceParams["temperature"]
     */
    protected abstract fun getAirConditionStateAndValue(voiceParams: Map<String, String>): Pair<AirConditionState, Int>

    protected fun parsParams(commandData: CommandData): Map<String, String> {
        val paramsText : MutableMap<String, String> = mutableMapOf()
        paramsText["_zone"] = commandData.zone ?: MultiChannelAudioSource.ZONE_FRONT_LEFT
        return paramsText;
    }
}


