package ru.voboost.voiceassistant.executor

import android.content.Context
import android.util.Log
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.executor.handlers.ICommandHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerCloseHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerOpenHandler
import ru.voboost.voiceassistant.executor.handlers.aidl.airconditioner.AirConditionerSetTempHandler
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
import ru.voboost.voiceassistant.executor.handlers.shell.airconditioner.AirConditionerCloseShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.airconditioner.AirConditionerOpenShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.airconditioner.AirConditionerSetTempShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.chargport.ChargportCloseShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.chargport.ChargportOpenShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.scuttle.FuelTankOpenShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.smartmode.SmartModeChildShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.smartmode.SmartModeLeisureShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.smartmode.SmartModeRomanticShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.window.WindowCloseAllShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.window.WindowCloseDriverShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.window.WindowOpenAllShellHandler
import ru.voboost.voiceassistant.executor.handlers.shell.window.WindowOpenDriverShellHandler

/**
 * Фабрика для создания реализаций VehicleCommandExecutor
 *
 * Все реализации используют один интерфейс executeByCommandId()
 * Разница только в наборе handlers (AIDL, Intent, Shell)
 */
object VehicleCommandExecutorFactory {

    const val TAG = "VehicleCommandFactory"

    enum class ExecutionMode {
        AIDL,        // Все 15 команд через AIDL CanBusService (рекомендуется)
        INTENT,      // Телефон через Intent, остальное через AIDL
        SHELL        // Все команды через Shell (требует root/привилегий)
    }

    /**
     * Создать реализацию VehicleCommandExecutor
     */
    fun create(
        context: Context,
        mode: ExecutionMode = ExecutionMode.AIDL
    ): IVehicleCommandExecutor {
        return when (mode) {
            ExecutionMode.AIDL -> createAidl(context)
            ExecutionMode.INTENT -> createIntent(context)
            ExecutionMode.SHELL -> createShell()
        }
    }

    /**
     * AIDL — все 15 команд через CanBusService
     */
    fun createAidl(context: Context): IVehicleCommandExecutor {
        Log.i(TAG, "Creating AIDLVehicleCommandExecutor (RECOMMENDED)")

        val canBusManager = CanBusServiceManager(context)

        val handlers: Map<String, ICommandHandler> = mapOf(
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
            "phone_call_contact" to PhoneCallContactIntentHandler(context),
            "phone_call_number" to PhoneCallNumberIntentHandler(context)
        )

        return VehicleCommandExecutor(handlers) { canBusManager.isConnected() }
    }

    /**
     * Гибрид — телефон через Intent, остальное через AIDL
     */
    fun createIntent(context: Context): IVehicleCommandExecutor {
        Log.i(TAG, "Creating IntentVehicleCommandExecutor")

        val canBusManager = CanBusServiceManager(context)

        val handlers: Map<String, ICommandHandler> = mapOf(
            "phone_call_contact" to PhoneCallContactIntentHandler(context),
            "phone_call_number" to PhoneCallNumberIntentHandler(context),
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
            "ac_set_temp" to AirConditionerSetTempHandler(canBusManager)
        )

        return VehicleCommandExecutor(handlers) { canBusManager.isConnected() }
    }

    /**
     * Shell — все 13 команд через shell (телефон не поддерживается)
     */
    fun createShell(): IVehicleCommandExecutor {
        Log.i(TAG, "Creating ShellVehicleCommandExecutor")

        val handlers: Map<String, ICommandHandler> = mapOf(
            "window_open" to WindowOpenDriverShellHandler(),
            "window_close" to WindowCloseDriverShellHandler(),
            "window_all_open" to WindowOpenAllShellHandler(),
            "window_all_close" to WindowCloseAllShellHandler(),
            "charge_port_open" to ChargportOpenShellHandler(),
            "charge_port_close" to ChargportCloseShellHandler(),
            "fuel_tank_open" to FuelTankOpenShellHandler(),
            "ac_open" to AirConditionerOpenShellHandler(),
            "ac_close" to AirConditionerCloseShellHandler(),
            "ac_set_temp" to AirConditionerSetTempShellHandler(),
            "smart_mode_leisure" to SmartModeLeisureShellHandler(),
            "smart_mode_child" to SmartModeChildShellHandler(),
            "smart_mode_romantic" to SmartModeRomanticShellHandler()
        )

        return VehicleCommandExecutor(handlers) { true }
    }

    /**
     * Создать из строкового параметра
     */
    fun createFromString(
        context: Context,
        modeString: String?
    ): IVehicleCommandExecutor {
        val mode = when (modeString?.lowercase()?.trim()) {
            "shell" -> ExecutionMode.SHELL
            "intent", -> ExecutionMode.INTENT
            "aidl", null -> ExecutionMode.AIDL
            else -> {
                Log.w(TAG, "Unknown mode: '$modeString', using AIDL")
                ExecutionMode.AIDL
            }
        }
        return create(context, mode)
    }
}
