package ru.voboost.voiceassistant

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
 */
class SoundEffectManager(private val context: Context) {

    companion object {
        const val TAG = "SoundEffectManager"
        private const val SOUND_START = 1
        private const val SOUND_END = 2
        private const val SOUND_CANCEL = 3
        private const val TONE_DURATION_MS = 200
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
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

    fun playStartSound() {
        Log.d(TAG, "Playing start recognition sound")
        requestAudioFocus()
        playTone(SOUND_START)
    }

    fun playEndSound() {
        Log.d(TAG, "Playing end recognition sound")
        playTone(SOUND_END)
        handler.postDelayed({ abandonAudioFocus() }, 500)
    }

    fun playCancelSound() {
        Log.d(TAG, "Playing cancel sound")
        playTone(SOUND_CANCEL)
        abandonAudioFocus()
    }

    private fun playTone(type: Int) {
        try {
            toneGenerator?.let { tg ->
                when (type) {
                    SOUND_START -> {
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)
                        handler.postDelayed({
                            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, TONE_DURATION_MS)
                        }, TONE_DURATION_MS.toLong())
                    }
                    SOUND_END -> {
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)
                    }
                    SOUND_CANCEL -> {
                        tg.startTone(ToneGenerator.TONE_PROP_NACK, TONE_DURATION_MS)
                    }
                }
                Log.d(TAG, "Tone played: type=$type")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play tone", e)
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
            toneGenerator?.release()
            toneGenerator = null
            abandonAudioFocus()
            handler.removeCallbacksAndMessages(null)
            Log.d(TAG, "SoundEffectManager released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release", e)
        }
    }
}
