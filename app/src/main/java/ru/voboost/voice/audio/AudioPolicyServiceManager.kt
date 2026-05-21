package ru.voboost.voice.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.qinggan.audiopolicy.IAudioPolicyService
import ru.voboost.voice.canbus.CanBusServiceManager

class AudioPolicyServiceManager(context: Context) {

    companion object {
        const val TAG = "AudioPolicyServiceManager"
        private const val AUDIO_POLICY_SERVICE_PACKAGE = "com.qinggan.audiopolicy.service"
        private const val AUDIO_POLICY_SERVICE_ACTION = "com.qinggan.audiopolicy.action.AUDIO_POLICY_SERVICE"
        private const val CALLING_PACKAGE_NAME = "com.qinggan.app.launcher"

    }

    private var audioPolicyService: IAudioPolicyService? = null
    private var isBound: Boolean = false
    private var isConnecting = false
    private val connectionCallbacks = mutableListOf<IAudioPolicyServiceConnectionCallback>()

    init {
        bindToService(context)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            this@AudioPolicyServiceManager.onServiceConnected(name, service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            this@AudioPolicyServiceManager.onServiceDisconnected(name)
        }

        override fun onBindingDied(name: ComponentName?) {
            this@AudioPolicyServiceManager.onBindingDied(name)
        }
    }

    fun isConnected(): Boolean = isBound && audioPolicyService != null

    private fun bindToService(context: Context) {
        if (isBound || isConnecting) {
            Log.d(TAG, "Already bound or connecting to CanBusService")
            return
        }

        try {
            isConnecting = true
            val intent = Intent(AUDIO_POLICY_SERVICE_ACTION).apply {
                setPackage(AUDIO_POLICY_SERVICE_PACKAGE)
            }
            val result = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService result: $result")

            if (!result) {
                Log.e(TAG, "Failed to bind to CanBusService - bindService returned false")
                isConnecting = false
                onConnectionFailed("bindService returned false")
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to bind to CanBusService", e)
            isConnecting = false
            onConnectionFailed(e.message ?: "Unknown error")
        }
    }

    private fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.i(CanBusServiceManager.Companion.TAG, "Connected to CanBusService")
        audioPolicyService = IAudioPolicyService.Stub.asInterface(service)
        isBound = true
        isConnecting = false // Уведомляем callback'и
        onConnected()
    }

    private fun onServiceDisconnected(name: ComponentName?) {
        Log.w(CanBusServiceManager.Companion.TAG, "Disconnected from CanBusService")
        audioPolicyService = null
        isBound = false // Уведомляем callback'и
        onDisconnected()
    }

    private fun onBindingDied(name: ComponentName?) {
        Log.e(CanBusServiceManager.Companion.TAG, "Binding died for CanBusService")
        isBound = false
        isConnecting = false
        audioPolicyService = null
    }

    public fun isInCall(): Boolean =
            audioPolicyService?.isInCall(CALLING_PACKAGE_NAME) ?: false

    fun registerConnectionCallback(callback: IAudioPolicyServiceConnectionCallback) =
            connectionCallbacks.add(callback)
    /**
     * Удалить callback подключения
     */
    fun unregisterConnectionCallback(callback: IAudioPolicyServiceConnectionCallback) =
            connectionCallbacks.remove(callback)

    fun onConnected() =
            connectionCallbacks.forEach { it.handlerConnected(this) }
    fun onDisconnected() =
            connectionCallbacks.forEach { it.handlerDisconnected(this) }
    private fun onConnectionFailed(error: String) =
            connectionCallbacks.forEach {it.handlerConnectionFailed(this, error)}

    /**
     * Отключиться от сервиса
     */
    fun unbind(context: Context) {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
                Log.i(CanBusServiceManager.Companion.TAG, "Unbound from CanBusService")
            }
            catch (e: Exception) {
                Log.e(CanBusServiceManager.Companion.TAG, "Failed to unbind from CanBusService", e)
            }
            finally {
                isBound = false
                isConnecting = false
                audioPolicyService = null
            }
        }
    }

    /**
     * Проверить подключение и попытаться подключиться если не подключено
     */
    private fun ensureConnected(): Boolean {
        if (isConnected()) return true

        Log.w(CanBusServiceManager.Companion.TAG,
              "Not connected to CanBusService, attempting to use anyway")
        return false
    }

    /**
     * Удалить все callback'и (освобождение ресурсов)
     */
    fun clearCallbacks() {
        connectionCallbacks.clear()
    }
}

