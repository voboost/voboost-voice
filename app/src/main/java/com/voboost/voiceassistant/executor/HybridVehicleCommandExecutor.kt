package com.voboost.voiceassistant.executor

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.canbus.CanBusServiceManager

/**
 * Гибридный исполнитель команд
 * Автоматически выбирает способ выполнения в зависимости от типа команды:
 *
 * - telephone → IntentVehicleCommandExecutor (звонки через Intent)
 * - остальное → AIDLVehicleCommandExecutor (автомобиль через AIDL)
 *
 * Это оптимальный подход, так как:
 * 1. Звонки лучше работают через Intent (оригинальный способ Ivoka)
 * 2. Команды автомобиля работают через AIDL (прямое взаимодействие с CanBus)
 * 3. Логика маршрутизации инкапсулирована в одном месте
 * 4. Конфиг остается простым (не нужно указывать execution_mode)
 *
 * @param context Context приложения
 * @param canBusManager Менеджер CanBusService для AIDL команд
 */
class HybridVehicleCommandExecutor(
    private val context: Context,
    private val canBusManager: CanBusServiceManager
) : VehicleCommandExecutor {

    companion object {
        private const val TAG = "HybridVehicleCommand"
        private const val TARGET_TELEPHONE = "telephone"
    }

    // Создаем оба исполнителя
    private val intentExecutor = IntentVehicleCommandExecutor(context)
    private val aidlExecutor = AIDLVehicleCommandExecutor(canBusManager)

    /**
     * Определить какой исполнитель нужен для данной команды
     */
    private fun selectExecutor(target: String): VehicleCommandExecutor {
        return when (target) {
            TARGET_TELEPHONE -> {
                Log.d(TAG, "Selecting IntentExecutor for telephone command")
                intentExecutor
            }
            else -> {
                Log.d(TAG, "Selecting AIDLExecutor for command: $target")
                aidlExecutor
            }
        }
    }

    override val executionMethod: String
        get() = "Hybrid (Intent for phone, AIDL for vehicle)"

    /**
     * Выполнить команду автомобиля
     * Автоматически выбирает нужный executor
     */
    override fun execute(
        target: String,
        classify: Int,
        command: Int,
        params: Map<String, Any>
    ): Boolean {
        val executor = selectExecutor(target)
        return executor.execute(target, classify, command, params)
    }

    /**
     * Выполнить команду звонка
     * Всегда использует IntentExecutor (надежнее для звонков)
     */
    override fun executePhoneCommand(
        classify: Int,
        command: Int,
        contact: String?,
        number: String?,
        callType: String
    ): Boolean {
        Log.d(TAG, "Phone command via IntentExecutor: contact=$contact, number=$number")
        return intentExecutor.executePhoneCommand(classify, command, contact, number, callType)
    }

    /**
     * Получить информацию о выполнении (для отладки)
     */
    fun getExecutorInfo(): Map<String, String> {
        return mapOf(
            "method" to executionMethod,
            "intent_executor" to intentExecutor.executionMethod,
            "aidl_executor" to aidlExecutor.executionMethod
        )
    }
}
