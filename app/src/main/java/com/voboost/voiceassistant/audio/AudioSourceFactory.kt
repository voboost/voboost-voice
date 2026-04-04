package com.voboost.voiceassistant.audio

import android.content.Context
import android.util.Log

/**
 * Фабрика AudioSource
 * Автоматически выбирает лучший доступный аудио-источник
 * 
 * Приоритет:
 * 1. TransProxy (системный, с шумоподавлением) ← РЕКОМЕНДУЕТСЯ
 * 2. RecorderManager (системный, с шумоподавлением)
 * 3. Android AudioRecord (fallback)
 */
object AudioSourceFactory {
    
    private const val TAG = "AudioSourceFactory"
    
    /**
     * Тип аудио-источника
     */
    enum class SourceType {
        /** TransProxy через QGSpeechService (рекомендуется) */
        TRANSPROXY,
        
        /** Системный RecorderManager */
        RECORDER_MANAGER,
        
        /** Стандартный Android AudioRecord (fallback) */
        ANDROID
    }
    
    /**
     * Создать AudioSource с автоматическим выбором лучшего источника
     * 
     * @param context Android Context
     * @param preferredType Предпочтительный тип (по умолчанию TRANSPROXY)
     * @return AudioSource (никогда null)
     */
    fun create(
        context: Context,
        preferredType: SourceType = SourceType.TRANSPROXY
    ): AudioSource {
        return when (preferredType) {
            SourceType.TRANSPROXY -> {
                val transProxySource = MicrophoneStreamAudioSource(context)
                if (transProxySource.initialize()) {
                    Log.i(TAG, "✅ Using MicrophoneStreamAudioSource (TransProxy)")
                    transProxySource
                } else {
                    Log.w(TAG, "⚠️ TransProxy unavailable, falling back to AndroidAudioSource")
                    createAndroidSource(context)
                }
            }
            
            SourceType.RECORDER_MANAGER -> {
                val recorderManagerSource = RecorderManagerAudioSource(context)
                if (recorderManagerSource.initialize()) {
                    Log.i(TAG, "✅ Using RecorderManagerAudioSource (system)")
                    recorderManagerSource
                } else {
                    Log.w(TAG, "⚠️ RecorderManager unavailable, falling back to AndroidAudioSource")
                    createAndroidSource(context)
                }
            }
            
            SourceType.ANDROID -> {
                Log.i(TAG, "Using AndroidAudioSource (fallback)")
                createAndroidSource(context)
            }
        }
    }
    
    /**
     * Проверить доступен ли TransProxy
     */
    fun isTransProxyAvailable(context: Context): Boolean {
        return try {
            val clazz = Class.forName("com.qinggan.qinglink.transProxy.api.IPcmModule")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Проверить доступен ли RecorderManager
     */
    fun isRecorderManagerAvailable(context: Context): Boolean {
        return try {
            val clazz = Class.forName("com.qinggan.audiorecord.record.RecorderManager")
            val getInstance = clazz.getMethod("getInstance")
            getInstance.invoke(null) != null
        } catch (e: Exception) {
            false
        }
    }
    
    private fun createAndroidSource(context: Context): AudioSource {
        return AndroidAudioSource(context).apply {
            if (!initialize()) {
                throw IllegalStateException("Failed to initialize AndroidAudioSource")
            }
        }
    }
}
