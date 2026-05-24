package ru.voboost.voice.audio

import android.content.Context
import android.util.Log

/**
 * Фабрика IAudioSource
 * Автоматически выбирает лучший доступный аудио-источник
 *
 * Приоритет:
 * 1. MultiChannel (4 микрофона + TDOA определение зоны) < РЕКОМЕНДУЕТСЯ
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
                 MultiChannelAudioSource(sampleRate = IAudioSource.SAMPLE_RATE,
                                                                 micSpacing = 0.15f,
                                                                 channelCount = 6)
            }
            SourceType.ANDROID -> {
                Log.i(TAG, "Using AndroidAudioSource (fallback, zone=front_left)")
                createAndroidSource(context)
            }
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


