package ru.voboost.voiceassistant.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.qinggan.qinglink.api.hu.IMicphoneMode
import com.qinggan.qinglink.api.hu.IMicphoneModeListener

/**
 * Менеджер для определения зоны говорящего через микрофонный массив
 *
 * Использует AIDL интерфейс IMicphoneMode для получения:
 * - Текущего режима микрофона (MASTER/SLAVE/BOTH/FRONT)
 * - Угла источника звука (audio source angle)
 *
 * Маппинг режимов:
 * - MASTER(1)  → front_left   (водитель)
 * - SLAVE(2)   → front_right  (передний пассажир)
 * - BOTH(3)    → all_location  (все)
 * - FRONT(4)   → front        (передние)
 * - BT(5)      → bluetooth    (bluetooth)
 */
class MicphoneModeManager(private val context: Context) {
    companion object {
        const val TAG = "MicphoneModeManager"
        private const val INTENT_MICPHONE_MODE = "com.qinggan.qinglink.hu.MICPHONEMODE"

        // Режимы микрофона из Constant.MicphoneMode.Mode
        const val MODE_MASTER = 1    // Водитель
        const val MODE_SLAVE = 2     // Пассажир
        const val MODE_BOTH = 3      // Оба
        const val MODE_FRONT = 4     // Передний
        const val MODE_BT = 5        // Bluetooth
        const val MODE_MAX = 6
    }

    private var micphoneModeService: IMicphoneMode? = null
    private var isBound = false

    @Volatile
    private var currentMicMode: Int = MODE_MASTER

    @Volatile
    private var currentAudioAngle: Int = 0

    private val listeners = mutableListOf<MicphoneModeListener>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "✅ Connected to MicphoneMode service")
            micphoneModeService = IMicphoneMode.Stub.asInterface(service)
            isBound = true

            // Регистрируем слушатель
            try {
                micphoneModeService?.registerListener(micphoneModeListener)
                Log.i(TAG, "✅ MicphoneMode listener registered")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to register listener", e)
            }

            // Уведомляем слушателей
            listeners.forEach { it.onConnected() }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "❌ Disconnected from MicphoneMode service")
            micphoneModeService = null
            isBound = false
            listeners.forEach { it.onDisconnected() }
        }
    }

    /**
     * AIDL слушатель для получения callbacks от HU
     */
    private val micphoneModeListener = object : IMicphoneModeListener.Stub() {
        @Throws(RemoteException::class)
        override fun onConnect(connect: Boolean) {
            Log.d(TAG, "onConnect: $connect")
            listeners.forEach { it.onConnected() }
        }

        @Throws(RemoteException::class)
        override fun onSetMicMode(mode: Int) {
            Log.i(TAG, "🎤 Mic mode changed to: $mode (${modeToZone(mode)})")
            currentMicMode = mode
            listeners.forEach { it.onMicModeChanged(mode) }
        }

        @Throws(RemoteException::class)
        override fun onGetCurrentMicMode() {
            Log.d(TAG, "HU requesting current mic mode: $currentMicMode")
            try {
                micphoneModeService?.sendCurrentMicModeResponse(currentMicMode)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send mic mode response", e)
            }
        }

        @Throws(RemoteException::class)
        override fun onGetCurrentAudioSourceAngle() {
            Log.d(TAG, "HU requesting current audio source angle: $currentAudioAngle")
            try {
                micphoneModeService?.sendCurrentAudioSourceAngleResponse(currentAudioAngle)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send angle response", e)
            }
        }
    }

    /**
     * Подключиться к сервису
     */
    fun connect() {
        if (isBound) {
            Log.d(TAG, "Already connected")
            return
        }

        try {
            val intent = Intent(INTENT_MICPHONE_MODE)
            intent.setPackage("com.qinggan.sttservice")

            // Сначала запускаем сервис (как в AbstractManager.bindService())
            try {
                context.startService(intent)
                Log.d(TAG, "startService called")
            } catch (e: Exception) {
                Log.w(TAG, "startService failed: ${e.message}")
            }

            // Затем подключаемся
            val result = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService result: $result")

            if (!result) {
                Log.w(TAG, "⚠️ MicphoneMode service not available - will fallback to default zone")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to bind to MicphoneMode service", e)
        }
    }

    /**
     * Отключиться от сервиса
     */
    fun disconnect() {
        if (isBound) {
            try {
                micphoneModeService?.unregisterListener(micphoneModeListener)
                context.unbindService(serviceConnection)
                Log.i(TAG, "Disconnected from MicphoneMode service")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to disconnect", e)
            }
            isBound = false
            micphoneModeService = null
        }
    }

    /**
     * Добавить слушатель
     */
    fun addListener(listener: MicphoneModeListener) {
        listeners.add(listener)
        // Если уже подключены — сразу уведомить
        if (isBound) {
            listener.onConnected()
            listener.onMicModeChanged(currentMicMode)
        }
    }

    /**
     * Удалить слушатель
     */
    fun removeListener(listener: MicphoneModeListener) {
        listeners.remove(listener)
    }

    /**
     * Обновить угол источника звука (вызывается при распознавании)
     * @param angle Угол в градусах (0-360)
     */
    fun updateAudioSourceAngle(angle: Int) {
        currentAudioAngle = angle
        Log.d(TAG, "📐 Audio source angle updated: $angle°")
        try {
            micphoneModeService?.sendCurrentAudioSourceAngleResponse(angle)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send angle update", e)
        }
    }

    /**
     * Получить текущий режим микрофона
     */
    fun getCurrentMicMode(): Int = currentMicMode

    /**
     * Получить текущий угол источника звука
     */
    fun getCurrentAudioAngle(): Int = currentAudioAngle

    /**
     * Получить зону говорящего
     */
    fun getCurrentZone(): String = modeToZone(currentMicMode)

    /**
     * Подключены ли к сервису
     */
    fun isConnected(): Boolean = isBound && micphoneModeService != null

    /**
     * Преобразовать режим в зону
     */
    private fun modeToZone(mode: Int): String = when (mode) {
        MODE_MASTER -> "front_left"
        MODE_SLAVE -> "front_right"
        MODE_BOTH -> "all_location"
        MODE_FRONT -> "front"
        MODE_BT -> "bluetooth"
        else -> "front_left" // По умолчанию — водитель
    }
}

/**
 * Слушатель изменений режима микрофона
 */
interface MicphoneModeListener {
    /** Сервис подключен */
    fun onConnected() {}

    /** Сервис отключен */
    fun onDisconnected() {}

    /** Изменился режим микрофона */
    fun onMicModeChanged(mode: Int) {}
}
