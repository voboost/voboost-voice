package ru.voboost.voiceassistant.executor.handlers.aidl.smartmode

import android.util.Log
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Базовый абстрактный обработчик команд умных режимов
 *
 * @param commandId Уникальный ID команды (совпадает с config.json)
 * @param canBusManager Менеджер CAN-шины
 * @param modeId ID режима для отправки в CAN-шину
 */
abstract class AbstractSmartModeHandler(
    override val commandId: String,
    protected val canBusManager: CanBusServiceManager,
    private val modeId: Int
) : ICommandHandler {

    override fun execute(
        voiceParams: Map<String, Any>
    ): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        Log.d(TAG, "SmartMode command: id='$commandId', modeId=$modeId")
        return canBusManager.setVehicleSceneMode(modeId)
    }

    companion object {
        const val TAG = "SmartModeCommand"
    }
}
