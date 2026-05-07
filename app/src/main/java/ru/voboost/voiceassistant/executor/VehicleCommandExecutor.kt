package ru.voboost.voiceassistant.executor

import android.content.Context
import android.util.Log
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerCloseHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerOpenHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerSetTempHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerTempOffsetHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode.ComfortDrivingModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode.EcoDrivingModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode.IndividualDrivingModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode.OffRoadDrivingModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode.SnowDrivingModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.drivingmode.SportDrivingModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.scuttle.ChargportOpenHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.scuttle.FuelTankOpenHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.smartmode.ChildSmartModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.smartmode.LeisureSmartModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.smartmode.RomanticSmartModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.smartmode.WashSmartModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.socmode.ElectroSOCModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.socmode.HybridSOCModeHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.socmode.SaveSOCModeHandler
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
        "fuel_tank_open" to FuelTankOpenHandler(canBusManager),

        "ac_open" to AirConditionerOpenHandler(canBusManager),
        "ac_close" to AirConditionerCloseHandler(canBusManager),
        "ac_set_temp" to AirConditionerSetTempHandler(canBusManager),
        "ac_temp_up" to AirConditionerTempOffsetHandler(canBusManager, +2),
        "ac_temp_down" to AirConditionerTempOffsetHandler(canBusManager, -2),

        "phone_call_contact" to PhoneCallContactIntentHandler(context),
        "phone_call_number" to PhoneCallNumberIntentHandler(context),

        "eco_driving_mode" to EcoDrivingModeHandler(canBusManager),
        "comfort_driving_mode" to ComfortDrivingModeHandler(canBusManager),
        "sport_driving_mode" to SportDrivingModeHandler(canBusManager),
        "off_road_driving_mode" to OffRoadDrivingModeHandler(canBusManager),
        "individual_driving_mode" to IndividualDrivingModeHandler(canBusManager),
        "snow_driving_mode" to SnowDrivingModeHandler(canBusManager),

        "electro_soc_mode" to ElectroSOCModeHandler(canBusManager),
        "hybrid_soc_mode" to HybridSOCModeHandler(canBusManager),
        "save_soc_mode" to SaveSOCModeHandler(canBusManager),

        //TODO: эти сценарии не работают
        "wash_smart_mode" to WashSmartModeHandler(canBusManager),
        "leisure_smart_mode" to LeisureSmartModeHandler(canBusManager),
        "child_smart_mode" to ChildSmartModeHandler(canBusManager),
        "romantic_smart_mode" to RomanticSmartModeHandler(canBusManager),
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
