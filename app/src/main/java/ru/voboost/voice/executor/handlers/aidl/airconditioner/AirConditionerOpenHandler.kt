package ru.voboost.voice.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.CommandResult
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

/**
 * Включить кондиционер
 *
 * Перед включением проверяет текущее состояние:
 * - Если уже включён — ничего не делает
 * - Если выключен — отправляет команду включения
 */
class AirConditionerOpenHandler(private val canBusManager: CanBusServiceManager)
    : ICommandHandler {

    companion object {
        const val TAG = "AirConditionerOpen"
    }

    override fun execute(commandData: CommandData): CommandResult {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return ICommandHandler.NEGATIVE_RESULT
        }

        val airCondition = canBusManager.getAirCondition()
        val currentStatus = airCondition?.airSWStatus ?: -1

        Log.d(TAG, "AC open: current airSWStatus=$currentStatus")

        if (currentStatus == 1) {
            Log.i(TAG, "AC already ON, skipping")
            return ICommandHandler.POSITIVE_RESULT   // Уже включён — считаем успешным
        }

        // Отправляем команду (value=1 — toggle, как с бензобаком)
        Log.d(TAG, "Turning AC ON with value=1")
        val result = canBusManager.setAirConditionState(AirConditionState.AC_POWER_SWITCH, 1)
        return CommandResult(result)
    }
}


