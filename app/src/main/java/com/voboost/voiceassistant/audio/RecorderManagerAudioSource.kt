package com.voboost.voiceassistant.audio

import android.content.Context
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * AudioSource через RecorderManager (системный микрофон QGSpeechService)
 * 
 * Использует тот же механизм что и внутренние компоненты QGSpeechService:
 * - SpeechPcmSource
 * - IflytekPcmSource  
 * - ArklitePcmRecorder
 * 
 * Преимущества:
 * - ✅ Системный доступ к микрофону с шумоподавлением
 * - ✅ Нет конфликтов с QGSpeechService
 * - ✅ PCM данные уже обработаны (AEC, NS, AGC)
 */
class RecorderManagerAudioSource(private val context: Context) : AudioSource {

    companion object {
        private const val TAG = "RecorderManagerAudio"
        private const val RECORD_TYPE_AOSP = "aosp"
        private const val RECORD_TYPE_NATIVE = "native"
        private const val RECORD_TYPE_HARDAEC = "hardaec"
    }

    private val listeners = CopyOnWriteArrayList<AudioSource.Listener>()
    private var isRecording = false
    private var isInitialized = false

    // JNI методы RecorderManager
    private var recorderManagerInstance: Any? = null

    override fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return true
        }

        return try {
            Log.d(TAG, "Initializing RecorderManagerAudioSource...")

            // Загружаем класс RecorderManager из QGSpeechService
            val recorderManagerClass = Class.forName("com.qinggan.audiorecord.record.RecorderManager")
            val getInstanceMethod = recorderManagerClass.getMethod("getInstance")
            recorderManagerInstance = getInstanceMethod.invoke(null)

            if (recorderManagerInstance == null) {
                Log.e(TAG, "❌ RecorderManager.getInstance() returned null")
                return false
            }

            // Инициализируем RecorderManager
            val initMethod = recorderManagerClass.getMethod("init", Context::class.java, String::class.java)
            initMethod.invoke(recorderManagerInstance, context, RECORD_TYPE_AOSP)
            Log.d(TAG, "✅ RecorderManager initialized with type: $RECORD_TYPE_AOSP")

            isInitialized = true
            Log.i(TAG, "✅ RecorderManagerAudioSource initialized successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize RecorderManagerAudioSource", e)
            false
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
            Log.d(TAG, "Starting recording via RecorderManager...")

            val recorderManagerClass = Class.forName("com.qinggan.audiorecord.record.RecorderManager")
            
            // Создаём IDataListener
            val dataListenerClass = Class.forName("com.qinggan.audiorecord.output.IDataListener")
            val dataListener = java.lang.reflect.Proxy.newProxyInstance(
                dataListenerClass.classLoader,
                arrayOf(dataListenerClass),
                java.lang.reflect.InvocationHandler { _, method, args ->
                    when (method.name) {
                        "onData" -> {
                            @Suppress("UNCHECKED_CAST")
                            val pcmData = args[0] as ByteArray
                            val length = args[1] as Int

                            if (length > 0) {
                                for (listener in listeners) {
                                    try {
                                        listener.onAudioData(pcmData, length)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error in audio listener", e)
                                    }
                                }
                            }
                        }
                        "onStart" -> {
                            Log.i(TAG, "✅ RecorderManager started recording")
                            isRecording = true
                        }
                        "onStop" -> {
                            Log.i(TAG, "✅ RecorderManager stopped recording")
                            isRecording = false
                        }
                    }
                    Unit
                }
            )

            // Добавляем listener
            val addListenerMethod = recorderManagerClass.getMethod("addListener", dataListenerClass)
            addListenerMethod.invoke(recorderManagerInstance, dataListener)
            Log.d(TAG, "✅ Data listener added")

            // Запускаем запись
            val startRecordMethod = recorderManagerClass.getMethod("startRecord")
            startRecordMethod.invoke(recorderManagerInstance)
            Log.i(TAG, "✅ Recording started via RecorderManager")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start recording", e)
            isRecording = false
        }
    }

    override fun stop() {
        if (!isRecording) {
            Log.w(TAG, "Not recording")
            return
        }

        try {
            val recorderManagerClass = Class.forName("com.qinggan.audiorecord.record.RecorderManager")
            val stopRecordMethod = recorderManagerClass.getMethod("stopRecord")
            stopRecordMethod.invoke(recorderManagerInstance)
            Log.i(TAG, "✅ Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to stop recording", e)
        }
    }

    override fun release() {
        stop()

        try {
            val recorderManagerClass = Class.forName("com.qinggan.audiorecord.record.RecorderManager")
            val releaseMethod = recorderManagerClass.getMethod("release")
            releaseMethod.invoke(recorderManagerInstance)
            Log.i(TAG, "✅ RecorderManager released")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to release RecorderManager", e)
        }

        recorderManagerInstance = null
        isInitialized = false
    }

    override fun addListener(listener: AudioSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AudioSource.Listener) {
        listeners.remove(listener)
    }

    override fun isRecording(): Boolean = isRecording
}
