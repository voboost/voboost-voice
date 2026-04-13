package ru.voboost.voiceassistant.executor

import android.content.Context
import android.util.Log
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerCloseHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerOpenHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerSetTempHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerTempOffsetHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.chargport.ChargportCloseHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.chargport.ChargportOpenHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.scuttle.FuelTankOpenHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.smartmode.SmartModeChildHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.smartmode.SmartModeLeisureHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.smartmode.SmartModeRomanticHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.window.WindowCloseAllHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.window.WindowCloseDriverHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.window.WindowOpenAllHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.window.WindowOpenDriverHandler
import ru.voboost.voiceassistant.executor.handlers.intent.phone.PhoneCallContactIntentHandler
import ru.voboost.voiceassistant.executor.handlers.intent.phone.PhoneCallNumberIntentHandler

/**
 * Универсальный исполнитель команд через реестр handlers
 * Может работать с любым набором handlers (AIDL, Intent, Shell, микс)
 *
 * @param handlers Реестр команд: commandId → ICommandHandler
 * @param isConnectedChecker Функция проверки подключения
 */
class VehicleCommandExecutor(
    private val context: Context,
    private val canBusManager: CanBusServiceManager,

) : IVehicleCommandExecutor {

    private val handlers: Map<String, ICommandHandler> = mapOf(
        "window_open" to WindowOpenDriverHandler(canBusManager),
        "window_close" to WindowCloseDriverHandler(canBusManager),
        "window_all_open" to WindowOpenAllHandler(canBusManager),
        "window_all_close" to WindowCloseAllHandler(canBusManager),
        "charge_port_open" to ChargportOpenHandler(canBusManager),
        "charge_port_close" to ChargportCloseHandler(canBusManager),
        "fuel_tank_open" to FuelTankOpenHandler(canBusManager),
        "smart_mode_leisure" to SmartModeLeisureHandler(canBusManager),
        "smart_mode_child" to SmartModeChildHandler(canBusManager),
        "smart_mode_romantic" to SmartModeRomanticHandler(canBusManager),
        "ac_open" to AirConditionerOpenHandler(canBusManager),
        "ac_close" to AirConditionerCloseHandler(canBusManager),
        "ac_set_temp" to AirConditionerSetTempHandler(canBusManager),
        "ac_temp_up" to AirConditionerTempOffsetHandler(canBusManager, +2),
        "ac_temp_down" to AirConditionerTempOffsetHandler(canBusManager, -2),
        "phone_call_contact" to PhoneCallContactIntentHandler(context),
        "phone_call_number" to PhoneCallNumberIntentHandler(context)
    )

    companion object {
        const val TAG = "VehicleCommandExec"
    }

    override val executionMethod: String = handlers.values.firstOrNull()?.let {
        it.javaClass.simpleName.replace("Handler", "")
    } ?: "Unknown"

    override fun executeByCommandId(
        commandId: String,
        voiceParams: Map<String, Any>
    ): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected")
            return false
        }

        val handler = handlers[commandId]
            ?: return false.also { Log.w(TAG, "No handler for command: '$commandId'") }

        Log.d(TAG, "Executing: commandId='$commandId', handler=${handler.javaClass.simpleName}")
        return try {
            handler.execute(voiceParams)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during command execution: $commandId", e)
            false
        }
    }
}
