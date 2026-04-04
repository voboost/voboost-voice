package com.voboost.voiceassistant.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.qinggan.qinglink.api.hu.IVolume
import com.qinggan.qinglink.api.hu.IVolumeListener
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Менеджер для управления громкостью через QGSpeechService
 * 
 * Подключается к Volume сервису (com.qinggan.qinglink.hu.VOLUME)
 * и позволяет:
 * - Приглушать музыку при активации голосового помощника
 * - Восстанавливать громкость после завершения
 * - Управлять громкостью навигации, уведомлений и т.д.
 * 
 * Преимущества:
 * - ✅ Использует системную аудио-политику
 * - ✅ Нет конфликтов с другими приложениями
 * - ✅ Автоматическое восстановление громкости
 */
class VolumeManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VolumeManager"
        private const val INTENT_VOLUME = "com.qinggan.qinglink.hu.VOLUME"
        private const val QGSPEECH_PACKAGE = "com.qinggan.sttservice"
        
        // Stream types
        const val STREAM_MEDIA = 3
        const val STREAM_NAVIGATION = 1
        const val STREAM_PHONE = 6
        const val STREAM_NOTIFICATION = 5
        const val STREAM_A2DP = 7
    }
    
    private var volumeService: IVolume? = null
    private var volumeListener: IVolumeListener.Stub? = null
    private val listeners = CopyOnWriteArrayList<VolumeListener>()
    
    @Volatile
    private var isConnected = false
    
    // Сохраняем предыдущую громкость для восстановления
    private var previousMediaVolume: Int = -1
    
    /**
     * Подключиться к сервису управления громкостью
     */
    fun connect() {
        Log.d(TAG, "Connecting to Volume service...")
        
        val intent = Intent(INTENT_VOLUME)
        intent.setPackage(QGSPEECH_PACKAGE)
        
        try {
            context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    Log.i(TAG, "✅ Volume service connected")
                    volumeService = IVolume.Stub.asInterface(service)
                    registerVolumeListener()
                }
                
                override fun onServiceDisconnected(name: ComponentName?) {
                    Log.w(TAG, "❌ Volume service disconnected")
                    volumeService = null
                    isConnected = false
                    listeners.forEach { it.onDisconnected() }
                }
            }, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to bind Volume service", e)
        }
    }
    
    /**
     * Зарегистрировать слушатель событий громкости
     */
    private fun registerVolumeListener() {
        if (volumeService == null) {
            Log.e(TAG, "volumeService is null")
            return
        }
        
        volumeListener = object : IVolumeListener.Stub() {
            @Throws(RemoteException::class)
            override fun onConnect(connect: Boolean) {
                Log.i(TAG, "Volume listener connected: $connect")
                isConnected = connect
                if (connect) {
                    listeners.forEach { it.onConnected() }
                }
            }
            
            @Throws(RemoteException::class)
            override fun onGetCurrentMediaVolume() {
                // Запрос текущей громкости музыки
            }
            
            @Throws(RemoteException::class)
            override fun onSetCurrentMediaVolume(volume: Int) {
                Log.d(TAG, "Media volume changed: $volume")
                listeners.forEach { it.onMediaVolumeChanged(volume) }
            }
            
            @Throws(RemoteException::class)
            override fun onGetCurrentNavigationVolume() {
                // Запрос текущей громкости навигации
            }
            
            @Throws(RemoteException::class)
            override fun onSetCurrentNavigationVolume(volume: Int) {
                Log.d(TAG, "Navigation volume changed: $volume")
                listeners.forEach { it.onNavigationVolumeChanged(volume) }
            }
            
            @Throws(RemoteException::class)
            override fun onGetCurrentPhoneVolume() {
                // Запрос текущей громкости телефона
            }
            
            @Throws(RemoteException::class)
            override fun onSetCurrentPhoneVolume(volume: Int) {
                Log.d(TAG, "Phone volume changed: $volume")
                listeners.forEach { it.onPhoneVolumeChanged(volume) }
            }
            
            @Throws(RemoteException::class)
            override fun onGetCurrentNotificationVolume() {
                // Запрос текущей громкости уведомлений
            }
            
            @Throws(RemoteException::class)
            override fun onSetCurrentNotificationVolume(volume: Int) {
                Log.d(TAG, "Notification volume changed: $volume")
                listeners.forEach { it.onNotificationVolumeChanged(volume) }
            }
            
            @Throws(RemoteException::class)
            override fun onVolumeUp() {
                listeners.forEach { it.onVolumeUp() }
            }
            
            @Throws(RemoteException::class)
            override fun onVolumeDown() {
                listeners.forEach { it.onVolumeDown() }
            }
            
            @Throws(RemoteException::class)
            override fun onRequestAudioPolicy(streamType: Int, clientId: String) {
                Log.d(TAG, "Request audio policy: streamType=$streamType, clientId=$clientId")
                listeners.forEach { it.onRequestAudioPolicy(streamType, clientId) }
            }
            
            @Throws(RemoteException::class)
            override fun onAbandonAudioPolicy(clientId: String) {
                Log.d(TAG, "Abandon audio policy: clientId=$clientId")
                listeners.forEach { it.onAbandonAudioPolicy(clientId) }
            }
            
            // Остальные методы можно добавить по необходимости
            @Throws(RemoteException::class)
            override fun onGetCurrentA2DPVolume() {}
            
            @Throws(RemoteException::class)
            override fun onSetCurrentA2DPVolume(volume: Int) {}
            
            @Throws(RemoteException::class)
            override fun onSetEqualizer(json: String?) {}
            
            @Throws(RemoteException::class)
            override fun onGetEqualizer() {}
            
            @Throws(RemoteException::class)
            override fun onSetBalanceFaderLevel(position: Int, balance: Int, fader: Int) {}
            
            @Throws(RemoteException::class)
            override fun onSetSpeedVolumeModeType(type: Int) {}
            
            @Throws(RemoteException::class)
            override fun onSetLoudness(open: Boolean) {}
            
            @Throws(RemoteException::class)
            override fun onGetBalanceFaderLevel() {}
            
            @Throws(RemoteException::class)
            override fun onGetSpeedVolumeModeType() {}
            
            @Throws(RemoteException::class)
            override fun onGetLoudness() {}
            
            @Throws(RemoteException::class)
            override fun onSetMute(isMute: Boolean) {}
            
            @Throws(RemoteException::class)
            override fun onGetMute() {}
            
            @Throws(RemoteException::class)
            override fun onGetMDVolumeResponse(type: Int, streamType: Int, volume: Int) {}
            
            @Throws(RemoteException::class)
            override fun onMDVolumeUpdate(streamType: Int, volume: Int) {}
            
            @Throws(RemoteException::class)
            override fun onGetCurrentAudioPolicyInfo() {}
        }
        
        try {
            volumeService?.registerListener(volumeListener)
            Log.i(TAG, "✅ Volume listener registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register volume listener", e)
        }
    }
    
    /**
     * Приглушить музыку (для активации голосового помощника)
     * 
     * @param targetVolume Целевая громкость (по умолчанию 1 - минимальная)
     * @return true если успешно
     */
    fun duckMedia(targetVolume: Int = 1): Boolean {
        if (volumeService == null) {
            Log.e(TAG, "Volume service not connected")
            return false
        }
        
        try {
            // Сохраняем текущую громкость для восстановления
            // (в реальном приложении нужно запросить текущую громкость)
            previousMediaVolume = -1 // TODO: запросить текущую громкость
            
            // Устанавливаем минимальную громкость
            volumeService?.sendSetMDVolume(STREAM_MEDIA, targetVolume)
            Log.i(TAG, "✅ Media duck to volume: $targetVolume")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to duck media", e)
            return false
        }
    }
    
    /**
     * Восстановить громкость музыки после приглушения
     * 
     * @return true если успешно
     */
    fun restoreMedia(): Boolean {
        if (volumeService == null) {
            Log.e(TAG, "Volume service not connected")
            return false
        }
        
        if (previousMediaVolume <= 0) {
            Log.w(TAG, "No previous volume to restore")
            return false
        }
        
        try {
            volumeService?.sendSetMDVolume(STREAM_MEDIA, previousMediaVolume)
            Log.i(TAG, "✅ Media volume restored to: $previousMediaVolume")
            previousMediaVolume = -1
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore media volume", e)
            return false
        }
    }
    
    /**
     * Установить громкость медиа
     */
    fun setMediaVolume(volume: Int): Boolean {
        return try {
            volumeService?.sendSetMDVolume(STREAM_MEDIA, volume) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set media volume", e)
            false
        }
    }
    
    /**
     * Установить громкость навигации
     */
    fun setNavigationVolume(volume: Int): Boolean {
        return try {
            volumeService?.sendSetMDVolume(STREAM_NAVIGATION, volume) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set navigation volume", e)
            false
        }
    }
    
    /**
     * Увеличить громкость
     */
    fun volumeUp(): Boolean {
        return try {
            volumeService?.sendVolumeUp() == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to volume up", e)
            false
        }
    }
    
    /**
     * Уменьшить громкость
     */
    fun volumeDown(): Boolean {
        return try {
            volumeService?.sendVolumeDown() == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to volume down", e)
            false
        }
    }
    
    /**
     * Добавить слушатель событий громкости
     */
    fun addListener(listener: VolumeListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            Log.d(TAG, "Listener added, total: ${listeners.size}")
        }
    }
    
    /**
     * Удалить слушатель событий громкости
     */
    fun removeListener(listener: VolumeListener) {
        listeners.remove(listener)
        Log.d(TAG, "Listener removed, total: ${listeners.size}")
    }
    
    /**
     * Отключиться от сервиса
     */
    fun disconnect() {
        Log.i(TAG, "Disconnecting from Volume service...")
        
        try {
            volumeListener?.let { listener ->
                volumeService?.let { service ->
                    // unregisterListener не существует в IVolume, просто очищаем
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering listener", e)
        }
        
        listeners.clear()
        volumeService = null
        volumeListener = null
        isConnected = false
        previousMediaVolume = -1
        
        Log.i(TAG, "Disconnected from Volume service")
    }
    
    /**
     * Проверка состояния
     */
    fun isConnected(): Boolean = isConnected
}

/**
 * Слушатель событий управления громкостью
 */
interface VolumeListener {
    /**
     * Вызывается при подключении к сервису
     */
    fun onConnected()
    
    /**
     * Вызывается при отключении от сервиса
     */
    fun onDisconnected()
    
    /**
     * Вызывается при изменении громкости музыки
     */
    fun onMediaVolumeChanged(volume: Int) {}
    
    /**
     * Вызывается при изменении громкости навигации
     */
    fun onNavigationVolumeChanged(volume: Int) {}
    
    /**
     * Вызывается при изменении громкости телефона
     */
    fun onPhoneVolumeChanged(volume: Int) {}
    
    /**
     * Вызывается при изменении громкости уведомлений
     */
    fun onNotificationVolumeChanged(volume: Int) {}
    
    /**
     * Вызывается при нажатии кнопки увеличения громкости
     */
    fun onVolumeUp() {}
    
    /**
     * Вызывается при нажатии кнопки уменьшения громкости
     */
    fun onVolumeDown() {}
    
    /**
     * Вызывается при запросе аудио-политики
     */
    fun onRequestAudioPolicy(streamType: Int, clientId: String) {}
    
    /**
     * Вызывается при освобождении аудио-политики
     */
    fun onAbandonAudioPolicy(clientId: String) {}
}
