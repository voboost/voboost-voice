package com.voboost.voiceassistant.executor

import android.util.Log
import java.io.IOException

/**
 * Выполнение команд через shell-команды (прямой вызов CAN)
 * Аналогично реализации в MyVoya
 * 
 * Требует системных привилегий или root-доступа для выполнения service call
 */
class ShellVehicleCommandExecutor : VehicleCommandExecutor {

    companion object {
        private const val TAG = "ShellVehicleCommand"
        private const val CAN_SERVICE_BASE = "service call qg.canbus 58 i32 50"
        private const val CAN_SERVICE_QUERY = "service call qg.canbus 57 i32 50 i32"
    }

    override val executionMethod: String = "Shell CAN"

    /**
     * Маппинг target на VehicleState description (из MyVoya VehicleState.java)
     * Соответствует описанию в системе
     */
    private val vehicleStateMap = mapOf(
        // Зарядка
        "Chargport" to "IVI_CHRG_PORT_CAP",
        // Бензобак
        "Scuttle" to "IVI_FUEL_PORT_CAP",
        // Окна
        "Window" to "ALL_WINDOW_CONTROL",
        "DRIVER_WINDOW_CONTROL" to "DRIVER_WINDOW_CONTROL",
        "PAS_WIDOW_CONTROL" to "PAS_WIDOW_CONTROL",
        "LEFT_BACK_WINDOW_CONTROL" to "LEFT_BACK_WINDOW_CONTROL",
        "RIGHT_BACK_WINDOW_CONTROL" to "RIGHT_BACK_WINDOW_CONTROL",
        // Кондиционер
        "AirConditioner" to "AC_POWER_SWITCH",
        // Режимы
        "SmartMode" to "DRIVING_MODE_SET",
        // Двери
        "Door" to "CENTRAL_LOCK_CONTROL",
        // Свет
        "Light" to "AUTO_LAMP_SWITCH",
        // Сиденья
        "Seat" to "FRONT_SEAT_HEATING_SWITCH_LEFT"
    )

    /**
     * Маппинг command на значения (открыть/закрыть и т.д.)
     */
    private val commandValueMap = mapOf(
        0 to 2,  // Открыть/Включить
        1 to 1,  // Закрыть/Выключить
        2 to 3,  // Альтернативное значение
        3 to 4   // Специальное значение
    )

    override fun execute(
        target: String,
        classify: Int,
        command: Int,
        params: Map<String, Any>
    ): Boolean {
        Log.d(TAG, "Executing shell command: target=$target, classify=$classify, command=$command")

        return try {
            // Получаем описание VehicleState
            val vehicleStateDesc = getVehicleStateDescription(target, classify)
            
            // Получаем значение команды
            val value = getCommandValue(command, params)

            // Формируем shell-команду
            val shellCommand = buildShellCommand(vehicleStateDesc, value)
            
            Log.d(TAG, "Executing: $shellCommand")
            
            // Выполняем команду
            val process = Runtime.getRuntime().exec(shellCommand)
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.i(TAG, "Shell command executed successfully")
                true
            } else {
                Log.w(TAG, "Shell command exited with code: $exitCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute shell command", e)
            false
        }
    }

    override fun executePhoneCommand(
        classify: Int,
        command: Int,
        contact: String?,
        number: String?,
        callType: String
    ): Boolean {
        Log.w(TAG, "Phone commands not supported via shell. Use Intent executor.")
        // Телефонные команды не поддерживаются через shell
        // Для них нужно использовать Intent
        return false
    }

    /**
     * Получить описание VehicleState по target и classify
     */
    private fun getVehicleStateDescription(target: String, classify: Int): String {
        // Проверяем маппинг
        val mapped = vehicleStateMap[target]
        if (mapped != null) {
            return mapped
        }

        // Если есть прямое соответствие в VehicleState
        return when (classify) {
            35 -> "IVI_CHRG_PORT_CAP"      // Зарядка
            19 -> "IVI_FUEL_PORT_CAP"      // Бензобак
            2 -> "ALL_WINDOW_CONTROL"      // Окна
            5 -> "AC_POWER_SWITCH"         // Кондиционер
            22 -> "DRIVING_MODE_SET"       // Режимы
            else -> target
        }
    }

    /**
     * Получить значение команды
     */
    private fun getCommandValue(command: Int, params: Map<String, Any>): Int {
        // Проверяем специальные параметры
        val mode = params["mode"] as? Int
        if (mode != null) {
            return mode  // Для режимов передаем значение mode напрямую
        }

        val temperature = params["temperature"] as? Int
        if (temperature != null) {
            return temperature  // Для температуры передаем значение
        }

        // Стандартное значение команды
        return commandValueMap[command] ?: command
    }

    /**
     * Построить shell-команду
     */
    private fun buildShellCommand(vehicleState: String, value: Int): String {
        // Для режимов используем запись в файл (как в MyVoya)
        if (vehicleState == "DRIVING_MODE_SET") {
            return buildModeCommand(value)
        }

        // Стандартная CAN команда
        return "$CAN_SERVICE_BASE i32 $vehicleState i32 $value"
    }

    /**
     * Команда для переключения режимов (через файл)
     */
    private fun buildModeCommand(value: Int): String {
        // Определяем тип режима по значению (из MyVoya)
        val modeFile = when (value) {
            18 -> "/sdcard/Download/myvoyah/files/drive_mode.txt"      // Режим отдыха
            22 -> "/sdcard/Download/myvoyah/files/power_mode.txt"      // Детский режим
            6 -> "/sdcard/Download/myvoyah/files/energy_mode.txt"      // Романтический режим
            else -> "/sdcard/Download/myvoyah/files/drive_mode.txt"
        }
        
        // Команда записи в файл
        return "echo $value > $modeFile"
    }

    /**
     * Получить текущее состояние VehicleState (для отладки)
     */
    fun getVehicleState(vehicleState: String): Int {
        return try {
            val shellCommand = "$CAN_SERVICE_QUERY $vehicleState i32 1"
            val process = Runtime.getRuntime().exec(shellCommand)
            
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            
            // Парсим результат (формат: Result: Parcel(...))
            val hexValue = extractHexValue(output)
            hexValue?.toInt(16) ?: -1
        } catch (e: IOException) {
            Log.e(TAG, "Failed to get vehicle state", e)
            -1
        }
    }

    /**
     * Извлечь hex значение из результата
     */
    private fun extractHexValue(output: String): String? {
        val regex = Regex("\\b([0-9a-fA-F]{8})\\b")
        return regex.findAll(output)
            .map { it.groupValues[1] }
            .elementAtOrNull(0)
    }
}
