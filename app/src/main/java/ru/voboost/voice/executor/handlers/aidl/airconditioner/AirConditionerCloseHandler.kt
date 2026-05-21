package ru.voboost.voice.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import ru.voboost.voice.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

/**
 * Выключить кондиционер
 *
 * Перед выключением проверяет текущее состояние:
 * - Если уже выключен — ничего не делает
 * - Если включён — отправляет команду выключения
 */
class AirConditionerCloseHandler(private val canBusManager: CanBusServiceManager) :
        ICommandHandler {

    companion object {
        const val TAG = "AirConditionerClose"
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val airCondition = canBusManager.getAirCondition()
        val currentStatus = airCondition?.airSWStatus ?: -1

        Log.d(TAG, "AC close: current airSWStatus=$currentStatus")

        if (currentStatus == 0) {
            Log.i(TAG, "AC already OFF, skipping")
            return true  // Уже выключен — считаем успешным
        }

        // Отправляем команду (value=1 — toggle, как с бензобаком)
        Log.d(TAG, "Turning AC OFF with value=1")
        return canBusManager.setAirConditionState(AirConditionState.AC_POWER_SWITCH, 1)
    }
}


