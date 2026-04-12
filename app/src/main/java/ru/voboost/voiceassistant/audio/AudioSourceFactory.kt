package ru.voboost.voiceassistant.audio

import android.content.Context
import android.util.Log

/**
 * Фабрика IAudioSource
 * Автоматически выбирает лучший доступный аудио-источник
 * 
 * Приоритет:
 * 1. TransProxy (системный, с шумоподавлением) ← РЕКОМЕНДУЕТСЯ
 * 2. RecorderManager (системный, с шумоподавлением)
 * 3. Android AudioRecord (fallback)
 */
object AudioSourceFactory {
    
    const val TAG = "AudioSourceFactory"
    
    /**
     * Тип аудио-источника
     */
    enum class SourceType {
        /** RecorderManager через QGSpeechService (рекомендуется) */
        RECORDER_MANAGER,

        /** TransProxy через QGSpeechService (НЕ работает - только выход на телефон) */
        @Deprecated("TransProxy does NOT provide microphone input")
        TRANSPROXY,

        /** Стандартный Android AudioRecord (fallback) */
        ANDROID
    }
    
    /**
     * Создать IAudioSource с автоматическим выбором лучшего источника
     *
     * @param context Android Context
     * @param preferredType Предпочтительный тип (по умолчанию RECORDER_MANAGER)
     * @return IAudioSource (никогда null)
     */
    fun create(
        context: Context,
        preferredType: SourceType = SourceType.RECORDER_MANAGER
    ): IAudioSource {
        return when (preferredType) {
            SourceType.RECORDER_MANAGER -> {
                val recorderManagerSource = RecorderManagerAudioSource(context)
                if (recorderManagerSource.initialize()) {
                    Log.i(TAG, "✅ Using RecorderManagerAudioSource (system microphone)")
                    recorderManagerSource
                } else {
                    Log.w(TAG, "⚠️ RecorderManager unavailable, falling back to AndroidAudioSource")
                    createAndroidSource(context)
                }
            }

            SourceType.TRANSPROXY -> {
                // TransProxy НЕ работает для получения микрофона!
                Log.w(TAG, "⚠️ TransProxy deprecated - does NOT provide microphone input")
                Log.w(TAG, "⚠️ Falling back to AndroidAudioSource")
                createAndroidSource(context)
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
    
    private fun createAndroidSource(context: Context): IAudioSource {
        return AndroidAudioSource(context).apply {
            if (!initialize()) {
                throw IllegalStateException("Failed to initialize AndroidAudioSource")
            }
        }
    }
}
