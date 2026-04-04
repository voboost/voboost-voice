package com.voboost.voiceassistant.audio

import android.content.Context
import android.util.Log
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

/**
 * AudioSource через системный RecorderManager из QGSpeechService
 * 
 * Преимущества:
 * - Уже применено шумоподавление (NoiseSuppressor)
 * - Уже применена эхокомпенсация (AcousticEchoCanceler)
 * - Уже применён автоматический增益 (AutomaticGainControl)
 * - Используется правильный аудио-источник для автомобиля
 * 
 * Подключается к com.qinggan.audiorecord.record.RecorderManager через reflection
 * т.к. этот класс находится в системном APK (QGSpeechService)
 */
class RecorderManagerAudioSource(
    private val context: Context,
    private val recordType: String = "aosp" // "aosp", "native", "primary"
) : AudioSource {
    
    companion object {
        private const val TAG = "RecorderManagerAudio"
        private const val RECORDER_MANAGER_CLASS = "com.qinggan.audiorecord.record.RecorderManager"
        private const val DATA_LISTENER_CLASS = "com.qinggan.audiorecord.output.IDataListener"
    }
    
    private val listeners = CopyOnWriteArrayList<AudioSource.Listener>()
    
    // Reflection объекты
    private var recorderManagerInstance: Any? = null
    private var dataListenerInstance: Any? = null
    private var initMethod: Method? = null
    private var startRecordMethod: Method? = null
    private var stopRecordMethod: Method? = null
    private var addListenerMethod: Method? = null
    private var removeListenerMethod: Method? = null
    private var isStartRecordMethod: Method? = null
    private var releaseMethod: Method? = null
    
    @Volatile
    private var isInitialized = false
    
    @Volatile
    private var isRecording = false
    
    override fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return true
        }
        
        return try {
            Log.d(TAG, "Initializing RecorderManager via reflection, type=$recordType")
            
            // Получаем класс RecorderManager
            val recorderManagerClass = Class.forName(RECORDER_MANAGER_CLASS)
            
            // Получаем singleton instance через getInstance()
            val getInstanceMethod = recorderManagerClass.getMethod("getInstance")
            recorderManagerInstance = getInstanceMethod.invoke(null)
            
            if (recorderManagerInstance == null) {
                Log.e(TAG, "Failed to get RecorderManager instance")
                return false
            }
            
            // Получаем метод init(Context, String)
            initMethod = recorderManagerClass.getMethod("init", Context::class.java, String::class.java)
            
            // Получаем методы управления записью
            startRecordMethod = recorderManagerClass.getMethod("startRecord")
            stopRecordMethod = recorderManagerClass.getMethod("stopRecord")
            isStartRecordMethod = recorderManagerClass.getMethod("isStartRecord")
            releaseMethod = recorderManagerClass.getMethod("release")
            
            // Создаём IDataListener через reflection
            val dataListenerClass = Class.forName(DATA_LISTENER_CLASS)
            dataListenerInstance = createDataListenerProxy(dataListenerClass)
            
            // Получаем методы добавления слушателя
            addListenerMethod = recorderManagerClass.getMethod("addListener", dataListenerClass)
            removeListenerMethod = recorderManagerClass.getMethod("removeListener", dataListenerClass)
            
            // Инициализируем RecorderManager
            initMethod?.invoke(recorderManagerInstance, context, recordType)
            
            isInitialized = true
            Log.i(TAG, "✅ RecorderManager initialized successfully")
            true
            
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "❌ RecorderManager class not found — QGSpeechService not installed", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize RecorderManager", e)
            false
        }
    }
    
    /**
     * Создаёт прокси для IDataListener интерфейса
     * Перехватывает onData() и передаёт PCM данные слушателям
     */
    private fun createDataListenerProxy(dataListenerClass: Class<*>): Any {
        return java.lang.reflect.Proxy.newProxyInstance(
            dataListenerClass.classLoader,
            arrayOf(dataListenerClass)
        ) { _, method, args ->
            when (method.name) {
                "onData" -> {
                    // onData(byte[] bytes, int offset, int channels)
                    @Suppress("UNCHECKED_CAST")
                    val data = args[0] as ByteArray
                    val bytesRead = args[1] as Int
                    // val channels = args[2] as Int // можно использовать для отладки
                    
                    // Передаём данные всем слушателям
                    for (listener in listeners) {
                        try {
                            listener.onAudioData(data, bytesRead)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in audio listener", e)
                        }
                    }
                    null
                }
                "onStart" -> {
                    Log.d(TAG, "RecorderManager onStart")
                    isRecording = true
                    null
                }
                "onStop" -> {
                    Log.d(TAG, "RecorderManager onStop")
                    isRecording = false
                    null
                }
                else -> {
                    // Для методов по умолчанию возвращаем null
                    null
                }
            }
        }
    }
    
    override fun start() {
        if (!isInitialized) {
            Log.e(TAG, "Not initialized, call initialize() first")
            return
        }
        
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }
        
        try {
            // Добавляем слушатель перед стартом
            val dataListener = dataListenerInstance ?: run {
                Log.e(TAG, "Data listener not created")
                return
            }
            
            addListenerMethod?.invoke(recorderManagerInstance, dataListener)
            
            // Запускаем запись
            startRecordMethod?.invoke(recorderManagerInstance)
            
            Log.i(TAG, "✅ Recording started via RecorderManager")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start recording", e)
        }
    }
    
    override fun stop() {
        if (!isRecording) {
            Log.w(TAG, "Not recording")
            return
        }
        
        try {
            // Останавливаем запись
            stopRecordMethod?.invoke(recorderManagerInstance)
            
            // Удаляем слушатель
            val dataListener = dataListenerInstance
            if (dataListener != null) {
                removeListenerMethod?.invoke(recorderManagerInstance, dataListener)
            }
            
            Log.i(TAG, "✅ Recording stopped via RecorderManager")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to stop recording", e)
        }
    }
    
    override fun release() {
        stop()
        
        try {
            releaseMethod?.invoke(recorderManagerInstance)
            isInitialized = false
            Log.i(TAG, "RecorderManager released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing RecorderManager", e)
        }
    }
    
    override fun addListener(listener: AudioSource.Listener) {
        listeners.add(listener)
    }
    
    override fun removeListener(listener: AudioSource.Listener) {
        listeners.remove(listener)
    }
    
    override fun isRecording(): Boolean {
        return isRecording
    }
}
