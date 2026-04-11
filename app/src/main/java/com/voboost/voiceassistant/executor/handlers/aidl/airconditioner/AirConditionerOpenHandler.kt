package com.voboost.voiceassistant.executor.handlers.aidl.airconditioner

import android.util.Log
import com.qinggan.canbus.AirConditionState
import com.voboost.voiceassistant.canbus.CanBusServiceManager
import com.voboost.voiceassistant.config.ActionConfig
import com.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Включить кондиционер
 *
 * Перед включением проверяет текущее состояние:
 * - Если уже включён — ничего не делает
 * - Если выключен — отправляет команду включения
 */
class AirConditionerOpenHandler(
    private val canBusManager: CanBusServiceManager
) : ICommandHandler {

    override val commandId: String = "ac_open"

    override fun execute(
        config: ActionConfig,
        voiceParams: Map<String, Any>
    ): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val airCondition = canBusManager.getAirCondition()
        val currentStatus = airCondition?.airSWStatus ?: -1

        Log.d(TAG, "AC open: current airSWStatus=$currentStatus")

        if (currentStatus == 1) {
            Log.i(TAG, "AC already ON, skipping")
            return true  // Уже включён — считаем успешным
        }

        // Отправляем команду (value=1 — toggle, как с бензобаком)
        Log.d(TAG, "Turning AC ON with value=1")
        return canBusManager.setAirConditionState(AirConditionState.AC_POWER_SWITCH, 1)
    }

    companion object {
        const val TAG = "AirConditionerOpen"
    }
}
