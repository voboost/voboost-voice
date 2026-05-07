package ru.voboost.voiceassistant

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class SoundEffectManager(private val context: Context) {

    companion object {
        const val TAG = "SoundEffectManager"
        private const val SOUND_START = 1
        private const val SOUND_END = 2
        private const val SOUND_CANCEL = 3
        private const val TONE_DURATION_MS = 200L
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var toneGenerator: ToneGenerator? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            Log.d(TAG, "ToneGenerator initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ToneGenerator", e)
        }
    }

    // Теперь методы suspend и возвращают управление только после окончания звука
    suspend fun playStartSoundAsync() = playToneAsync(SOUND_START)
    suspend fun playEndSoundAsync() = playToneAsync(SOUND_END)
    suspend fun playCancelSoundAsync() = playToneAsync(SOUND_CANCEL)

    private suspend fun playToneAsync(type: Int) {
        requestAudioFocus()
        try {
            when (type) {
                SOUND_START -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS.toInt())
                    delay(TONE_DURATION_MS)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, TONE_DURATION_MS.toInt())
                    delay(TONE_DURATION_MS)
                }
                SOUND_END -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS.toInt())
                    delay(TONE_DURATION_MS + 50) // +50ms буфер на системные задержки
                }
                SOUND_CANCEL -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, TONE_DURATION_MS.toInt())
                    delay(TONE_DURATION_MS + 50)
                }
            }
        } catch (e: CancellationException) {
            // Если корутину отменили во время задержки, останавливаем звук
            toneGenerator?.stopTone()
            throw e // Обязательно пробрасываем исключение дальше
        } finally {
            // Гарантированно освобождаем AudioFocus даже при отмене или ошибке
            abandonAudioFocus()
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attributes)
                    .build()
                val result = audioManager.requestAudioFocus(audioFocusRequest!!)
                hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                val result = audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
                hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
            Log.d(TAG, "Audio focus requested: $hasAudioFocus")
            hasAudioFocus
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request audio focus", e)
            false
        }
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        hasAudioFocus = false
        Log.d(TAG, "Audio focus abandoned")
    }

    fun release() {
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
            abandonAudioFocus()
            Log.d(TAG, "SoundEffectManager released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release", e)
        }
    }
}