package ru.voboost.voiceassistant.executor.handlers.aidl.chargport

import android.util.Log
import com.qinggan.canbus.VehicleState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Базовый абстрактный обработчик команд лючка зарядки
 *
 * @param commandId Уникальный ID команды (совпадает с config.json)
 * @param canBusManager Менеджер CAN-шины
 */
abstract class AbstractChargportHandler(
    override val commandId: String,
    protected val canBusManager: CanBusServiceManager
) : ICommandHandler {

    override fun execute(
        voiceParams: Map<String, Any>
    ): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val value = getChargportValue()
        val IState = VehicleState.IVI_CHRG_PORT_CAP
        Log.d(TAG, "Chargport command: id='$commandId', IState=$IState (ordinal=${IState.ordinal}), value=$value")
        return canBusManager.setVehicleState(IState, value)
    }

    /**
     * Возвращает значение для отправки в CAN-шину
     * VALUE_OPEN (2) или VALUE_CLOSE (1)
     */
    protected abstract fun getChargportValue(): Int

    companion object {
        const val TAG = "ChargportCommand"
    }
}
