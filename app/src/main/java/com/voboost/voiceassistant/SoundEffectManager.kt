package com.voboost.voiceassistant

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Звуковые эффекты голосового помощника
 * Копирует поведение оригинальной системы:
 * - Звук начала распознавания
 * - Звук окончания распознавания
 * - Звук отмены (повторное нажатие кнопки)
 */
class SoundEffectManager(private val context: Context) {

    companion object {
        private const val TAG = "SoundEffectManager"

        // Типы звуков
        private const val SOUND_START = 1
        private const val SOUND_END = 2
        private const val SOUND_CANCEL = 3

        // Длительность звуков (мс)
        private const val TONE_DURATION_MS = 200
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())

    // ToneGenerator для системных звуков
    private var toneGenerator: ToneGenerator? = null

    // Аудио фокус
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    init {
        // Инициализация ToneGenerator
        // STREAM_RING - звонок (высокий приоритет, воспроизводится даже когда TTS активен)
        // STREAM_MUSIC заглушается когда TTS говорит, поэтому используем STREAM_RING
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_RING, 80)
            Log.d(TAG, "ToneGenerator initialized with STREAM_RING")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ToneGenerator with STREAM_RING, trying STREAM_ALARM", e)
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 80)
                Log.d(TAG, "ToneGenerator initialized with STREAM_ALARM")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to create ToneGenerator with STREAM_ALARM", e2)
            }
        }
    }

    /**
     * Воспроизвести звук начала распознавания
     */
    fun playStartSound() {
        Log.d(TAG, "Playing start recognition sound")
        playTone(SOUND_START)
    }

    /**
     * Воспроизвести звук окончания распознавания
     */
    fun playEndSound() {
        Log.d(TAG, "Playing end recognition sound")
        playTone(SOUND_END)
    }

    /**
     * Воспроизвести звук отмены (повторное нажатие кнопки)
     */
    fun playCancelSound() {
        Log.d(TAG, "Playing cancel sound")
        playTone(SOUND_CANCEL)
    }

    /**
     * Воспроизвести тон
     */
    private fun playTone(type: Int) {
        try {
            toneGenerator?.let { tg ->
                when (type) {
                    SOUND_START -> {
                        // Двойной сигнал: высокий + низкий
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)
                        handler.postDelayed({
                            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, TONE_DURATION_MS)
                        }, TONE_DURATION_MS.toLong())
                    }

                    SOUND_END -> {
                        // Одиночный высокий сигнал
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)
                    }

                    SOUND_CANCEL -> {
                        // Низкий сигнал отмены
                        tg.startTone(ToneGenerator.TONE_PROP_NACK, TONE_DURATION_MS)
                    }
                }

                Log.d(TAG, "Tone played: type=$type")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play tone", e)
        }
    }

    /**
     * Очистить ресурсы
     */
    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
            handler.removeCallbacksAndMessages(null)
            Log.d(TAG, "SoundEffectManager released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release", e)
        }
    }
}
