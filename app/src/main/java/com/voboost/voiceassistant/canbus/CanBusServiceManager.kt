package com.voboost.voiceassistant.canbus

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.qinggan.canbus.AirCondition
import com.qinggan.canbus.AirConditionState
import com.qinggan.canbus.ICanBusService
import com.qinggan.canbus.ICanBusServiceCallback
import com.qinggan.canbus.PhoneInfo
import com.qinggan.canbus.PhoneState
import com.qinggan.canbus.VehicleState
import com.qinggan.canbus.WindowStatus

/**
 * Менеджер для работы с CanBusService через AIDL
 *
 * Инкапсулирует всю логику подключения и работы с AIDL интерфейсом
 * Предоставляет удобные типизированные методы для управления автомобилем
 *
 * @param context Context приложения
 */
class CanBusServiceManager(context: Context) {

    companion object {
        private const val TAG = "CanBusServiceManager"
        private const val CANBUS_SERVICE_PACKAGE = "com.qinggan.canbus.service"
        private const val CANBUS_SERVICE_ACTION = "com.qinggan.canbus.CanBusService"

        // Время ожидания подключения к сервису (мс)
        private const val SERVICE_BIND_TIMEOUT_MS = 5000L

        // Константы для значений состояний
        const val VALUE_CLOSE = 1
        const val VALUE_OPEN = 2
        const val VALUE_ACTIVE = 2
    }

    // Состояние подключения
    private var isBound = false
    private var isConnecting = false

    // AIDL интерфейс сервиса
    private var canBusService: ICanBusService? = null

    // Callback'и для уведомления о подключении
    private val connectionCallbacks = mutableListOf<ConnectionCallback>()

    // ServiceConnection для подключения к CanBusService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "Connected to CanBusService")
            canBusService = ICanBusService.Stub.asInterface(service)
            isBound = true
            isConnecting = false

            // Уведомляем callback'и
            connectionCallbacks.forEach { it.onConnected() }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "Disconnected from CanBusService")
            canBusService = null
            isBound = false

            // Уведомляем callback'и
            connectionCallbacks.forEach { it.onDisconnected() }
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.e(TAG, "Binding died for CanBusService")
            isBound = false
            isConnecting = false
            canBusService = null
        }
    }

    init {
        // Автоматически подключаемся к сервису при создании
        bindToService(context)
    }

    /**
     * Подключиться к CanBusService
     */
    private fun bindToService(context: Context) {
        if (isBound || isConnecting) {
            Log.d(TAG, "Already bound or connecting to CanBusService")
            return
        }

        try {
            isConnecting = true
            val intent = Intent(CANBUS_SERVICE_ACTION).apply {
                setPackage(CANBUS_SERVICE_PACKAGE)
            }
            val result = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService result: $result")

            if (!result) {
                Log.e(TAG, "Failed to bind to CanBusService - bindService returned false")
                isConnecting = false
                connectionCallbacks.forEach { it.onConnectionFailed("bindService returned false") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to CanBusService", e)
            isConnecting = false
            connectionCallbacks.forEach { it.onConnectionFailed(e.message ?: "Unknown error") }
        }
    }

    /**
     * Зарегистрировать callback подключения
     */
    fun addConnectionCallback(callback: ConnectionCallback) {
        connectionCallbacks.add(callback)
    }

    /**
     * Удалить callback подключения
     */
    fun removeConnectionCallback(callback: ConnectionCallback) {
        connectionCallbacks.remove(callback)
    }

    /**
     * Проверить подключение к сервису
     */
    fun isConnected(): Boolean = isBound && canBusService != null

    /**
     * Дождаться подключения к сервису (suspend-friendly)
     */
    suspend fun waitForConnection(timeoutMs: Long = SERVICE_BIND_TIMEOUT_MS): Boolean {
        if (isConnected()) return true

        return try {
            kotlinx.coroutines.withTimeout(timeoutMs) {
                while (!isConnected()) {
                    kotlinx.coroutines.delay(100)
                }
                true
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Timeout waiting for CanBusService connection")
            false
        }
    }

    // ============================================================================
    // МЕТОДЫ УПРАВЛЕНИЯ АВТОМОБИЛЕМ
    // ============================================================================

    /**
     * Установить состояние автомобиля
     *
     * @param state Состояние автомобиля (VehicleState)
     * @param value Значение (1=CLOSE, 2=OPEN, и т.д.)
     * @return true если успешно
     */
    fun setVehicleState(state: VehicleState, value: Int): Boolean {
        if (!ensureConnected()) return false

        return try {
            canBusService?.setVehicleState(state, value)
            Log.d(TAG, "setVehicleState: $state = $value")
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to set vehicle state", e)
            false
        }
    }

    /**
     * Получить состояние автомобиля
     *
     * @param state Состояние автомобиля
     * @return Значение или -1 если ошибка
     */
    fun getVehicleState(state: VehicleState): Int {
        if (!ensureConnected()) return -1

        return try {
            canBusService?.getVehicleState(state) ?: -1
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get vehicle state", e)
            -1
        }
    }

    /**
     * Установить режим автомобиля (сцену)
     *
     * @param mode ID режима (6=romantic, 18=leisure, 22=child, и т.д.)
     * @return true если успешно
     */
    fun setVehicleSceneMode(mode: Int): Boolean {
        if (!ensureConnected()) return false

        return try {
            val result = canBusService?.setVehicleSceneMode(mode)
            Log.d(TAG, "setVehicleSceneMode: mode=$mode, result=$result")
            result == 0 || result == 1
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to set vehicle scene mode", e)
            false
        }
    }

    /**
     * Получить скорость автомобиля
     *
     * @return Скорость в км/ч или 0 если ошибка
     */
    fun getVehicleSpeed(): Int {
        if (!ensureConnected()) return 0

        return try {
            canBusService?.getVehicleSpeed() ?: 0
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get vehicle speed", e)
            0
        }
    }

    // ============================================================================
    // МЕТОДЫ УПРАВЛЕНИЯ КОНДИЦИОНЕРОМ
    // ============================================================================

    /**
     * Установить состояние кондиционера
     *
     * @param state Состояние кондиционера (AirConditionState)
     * @param value Значение (1=CLOSE, 2=OPEN, 3=ACTIVE, и т.д.)
     * @return true если успешно
     */
    fun setAirConditionState(state: AirConditionState, value: Int): Boolean {
        if (!ensureConnected()) return false

        return try {
            canBusService?.setAirConditionState(state, value)
            Log.d(TAG, "setAirConditionState: $state = $value")
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to set air condition state", e)
            false
        }
    }

    /**
     * Получить состояние кондиционера
     *
     * @return AirCondition или null если ошибка
     */
    fun getAirCondition(): AirCondition? {
        if (!ensureConnected()) return null

        return try {
            canBusService?.getAirCondition()
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get air condition", e)
            null
        }
    }

    /**
     * Включить кондиционер
     */
    fun turnOnAirConditioner(): Boolean {
        return setAirConditionState(AirConditionState.AC_POWER_SWITCH, AirConditionState.OPEN)
    }

    /**
     * Выключить кондиционер
     */
    fun turnOffAirConditioner(): Boolean {
        return setAirConditionState(AirConditionState.AC_POWER_SWITCH, AirConditionState.CLOSE)
    }

    // ============================================================================
    // МЕТОДЫ УПРАВЛЕНИЯ ОКНАМИ
    // ============================================================================

    /**
     * Получить состояние окон
     *
     * @return WindowStatus или null если ошибка
     */
    fun getWindowStatus(): WindowStatus? {
        if (!ensureConnected()) return null

        return try {
            canBusService?.getWindowStatus()
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get window status", e)
            null
        }
    }

    /**
     * Открыть все окна
     */
    fun openAllWindows(): Boolean {
        return setVehicleState(VehicleState.ALL_WINDOW_CONTROL, VALUE_OPEN)
    }

    /**
     * Закрыть все окна
     */
    fun closeAllWindows(): Boolean {
        return setVehicleState(VehicleState.ALL_WINDOW_CONTROL, VALUE_CLOSE)
    }

    // ============================================================================
    // МЕТОДЫ УПРАВЛЕНИЯ ТЕЛЕФОНОМ (ЗВОНКИ)
    // ============================================================================

    /**
     * Обновить информацию о телефоне (для звонков)
     *
     * @param phoneInfo Информация о телефоне
     * @return true если успешно
     */
    fun updatePhoneInfo(phoneInfo: PhoneInfo): Boolean {
        if (!ensureConnected()) return false

        return try {
            canBusService?.updatePhoneInfo(phoneInfo)
            Log.d(TAG, "updatePhoneInfo: $phoneInfo")
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to update phone info", e)
            false
        }
    }

    /**
     * Позвонить по контакту
     *
     * @param contactName Имя контакта
     * @return true если успешно
     */
    fun callContact(contactName: String): Boolean {
        val phoneInfo = createPhoneInfoForCall(contact = contactName)
        return updatePhoneInfo(phoneInfo)
    }

    /**
     * Позвонить по номеру
     *
     * @param phoneNumber Номер телефона
     * @return true если успешно
     */
    fun callNumber(phoneNumber: String): Boolean {
        val phoneInfo = createPhoneInfoForCall(number = phoneNumber)
        return updatePhoneInfo(phoneInfo)
    }

    /**
     * Создать PhoneInfo для звонка
     */
    private fun createPhoneInfoForCall(
        contact: String? = null,
        number: String? = null
    ): PhoneInfo {
        return PhoneInfo().apply {
            this.name = contact ?: ""
            this.phoneNum = number ?: ""
            this.phoneState = PhoneState.GOING_CALL  // Исходящий звонок
            this.duration = 0
            this.connectedStatus = 1  // connected
            this.isVehicleCall = 1    // vehicle call
        }
    }

    // ============================================================================
    // МЕТОДЫ РЕГИСТРАЦИИ CALLBACK'ОВ
    // ============================================================================

    /**
     * Зарегистрировать callback для получения событий от CanBus
     *
     * @param callback Callback для регистрации
     * @return true если успешно
     */
    fun registerCallback(callback: ICanBusServiceCallback): Boolean {
        if (!ensureConnected()) return false

        return try {
            canBusService?.addCallback(callback) == true
            Log.i(TAG, "Callback registered: ${callback.javaClass.simpleName}")
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to register callback", e)
            false
        }
    }

    /**
     * Отрегистрировать callback
     *
     * @param callback Callback для удаления
     * @return true если успешно
     */
    fun unregisterCallback(callback: ICanBusServiceCallback): Boolean {
        if (!ensureConnected()) return false

        return try {
            canBusService?.removeCallback(callback) == true
            Log.i(TAG, "Callback unregistered: ${callback.javaClass.simpleName}")
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister callback", e)
            false
        }
    }

    // ============================================================================
    // МЕТОДЫ ОЧИСТКИ РЕСУРСОВ
    // ============================================================================

    /**
     * Отключиться от сервиса
     */
    fun unbind(context: Context) {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
                Log.i(TAG, "Unbound from CanBusService")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unbind from CanBusService", e)
            } finally {
                isBound = false
                isConnecting = false
                canBusService = null
            }
        }
    }

    // ============================================================================
    // ВНУТРЕННИЕ МЕТОДЫ
    // ============================================================================

    /**
     * Проверить подключение и попытаться подключиться если не подключено
     */
    private fun ensureConnected(): Boolean {
        if (isConnected()) return true

        Log.w(TAG, "Not connected to CanBusService, attempting to use anyway")
        return false
    }
}

/**
 * Callback для уведомления о подключении к CanBusService
 */
interface ConnectionCallback {
    /**
     * Вызывается при успешном подключении
     */
    fun onConnected() {}

    /**
     * Вызывается при отключении
     */
    fun onDisconnected() {}

    /**
     * Вызывается при ошибке подключения
     */
    fun onConnectionFailed(error: String) {}
}
