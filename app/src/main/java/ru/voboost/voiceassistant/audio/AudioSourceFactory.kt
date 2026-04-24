package ru.voboost.voiceassistant.audio

import android.content.Context
import android.util.Log
import com.qinggan.audiorecord.record.NativeRecord

/**
 * Фабрика IAudioSource
 * Автоматически выбирает лучший доступный аудио-источник
 *
 * Приоритет:
 * 1. MultiChannel (4 микрофона + TDOA определение зоны) ← РЕКОМЕНДУЕТСЯ
 * 2. RecorderManager (системный, с шумоподавлением)
 * 3. Android AudioRecord (fallback, зона = front_left)
 */
object AudioSourceFactory {

    const val TAG = "AudioSourceFactory"

    /**
     * Тип аудио-источника
     */
    enum class SourceType {
        /** MultiChannel с 4 микрофонами + определение зоны через TDOA */
        MULTI_CHANNEL,

        /** RecorderManager через QGSpeechService (системный, 1-2 микрофона) */
        RECORDER_MANAGER,

        /** Стандартный Android AudioRecord (fallback, зона = front_left) */
        ANDROID
    }

    /**
     * Создать IAudioSource с автоматическим выбором лучшего источника
     *
     * @param context Android Context
     * @param preferredType Предпочтительный тип (по умолчанию MULTI_CHANNEL)
     * @return IAudioSource (никогда null)
     */
    fun create(context: Context,
               preferredType: SourceType): IAudioSource {
        return when (preferredType) {
            SourceType.MULTI_CHANNEL -> {
                val multiChannelSource = MultiChannelAudioSource(sampleRate = IAudioSource.SAMPLE_RATE,
                                                                 micSpacing = 0.15f,
                                                                 channelCount = 6)
                if (multiChannelSource.initialize()) {
                    Log.i(TAG, "✅ Using MultiChannelAudioSource (4 mics + TDOA zone detection)")
                    multiChannelSource
                }
                else {
                    Log.w(TAG, "⚠️ Multi-channel unavailable, falling back to RecorderManager")
                    createRecorderManagerSource(context)
                }
            }

            SourceType.RECORDER_MANAGER -> {
                createRecorderManagerSource(context)
            }

            SourceType.ANDROID -> {
                Log.i(TAG, "Using AndroidAudioSource (fallback, zone=front_left)")
                createAndroidSource(context)
            }
        }
    }

    /**
     * Проверить поддержку многоканальной записи
     */
    fun isMultiChannelSupported(context: Context): Boolean {
        return try { // Проверяем доступна ли системная библиотека
            val nativeRecord =
                NativeRecord.getInstance() // Если getInstance() не выбросил — библиотека загрузилась
            Log.d(TAG, "NativeRecord4Mic is available")
            true
        }
        catch (e: Exception) {
            Log.w(TAG, "Multi-channel not supported: ${e.message}")
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
        }
        catch (e: Exception) {
            false
        }
    }

    private fun createRecorderManagerSource(context: Context): IAudioSource {
            val recorderManagerSource = RecorderManagerAudioSource(context)
        if (recorderManagerSource.initialize()) {
            Log.i(TAG, "✅ Using RecorderManagerAudioSource (system microphone)")
            return recorderManagerSource
        }
        else {
            Log.w(TAG, "⚠️ RecorderManager unavailable, falling back to AndroidAudioSource")
            return createAndroidSource(context)
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
