package ru.voboost.voice.executor.handlers.aidl.smartmode

import android.util.Log
import ru.voboost.voice.canbus.CanBusServiceManager
import ru.voboost.voice.executor.handlers.ICommandHandler

/**
 * Базовый абстрактный обработчик команд умных режимов
 *
 * @param commandId Уникальный ID команды (совпадает с config.json)
 * @param canBusManager Менеджер CAN-шины
 * @param modeId ID режима для отправки в CAN-шину
 */
abstract class AbstractSmartModeHandler(protected val canBusManager: CanBusServiceManager,
                                        private val modeId: Int) : ICommandHandler {

    companion object {
        const val TAG = "SmartModeCommand"

        const val CHILD = 22
        const val LEISURE = 18
        const val ROMANTIC = 6
        const val WASH = 33
    }

    override fun execute(voiceParams: Map<String, Any>): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        Log.d(TAG, "SmartMode command: modeId=$modeId")
        return canBusManager.setVehicleSceneMode(modeId)
    }
}


