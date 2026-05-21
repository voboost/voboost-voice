package ru.voboost.voice.engine.system

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import ru.voboost.voice.core.BaseSpeechSynthesis
import java.util.*

class SystemSpeechSynthesis(private val context: Context) : BaseSpeechSynthesis(),
        TextToSpeech.OnInitListener {
    companion object {
        const val TAG = "SystemTtsSynthesis"
        private const val RUSSIAN_LANG_TAG = "ru-RU"
    }

    private var tts: TextToSpeech? = null

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isSpeaking = false

    init { // Инициализация системного TTS
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag(RUSSIAN_LANG_TAG))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Russian language not supported by system TTS")
                isInitialized = false
            }
            else {
                isInitialized = true
                Log.i(TAG, "System TTS initialized successfully")
            }
        }
        else {
            Log.e(TAG, "TTS initialization failed")
            isInitialized = false
        }
    }

    override suspend fun initialize() { // Ждём пока onInit будет вызван
        var waitCount = 0
        while (!isInitialized && waitCount < 50) {  // Ждём до 5 секунд
            kotlinx.coroutines.delay(100)
            waitCount++
        }

        if (!isInitialized) {
            throw IllegalStateException("System TTS initialization timeout")
        }
    }

    override fun isReady(): Boolean = isInitialized

    override fun speak(text: String) {
        if (text.isBlank()) {
            Log.w(TAG, "Empty text, skipping TTS")
            return
        }

        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, adding to queue")
            return
        }

        Log.d(TAG, "Speaking: $text")

        // Запустить воспроизведение если не играет
        if (!isSpeaking) {
            playSpeech(text)
        }
    }

    override fun stop() {
        Log.d(TAG, "Stopping TTS playback")
        tts?.stop()
        isSpeaking = false
    }

    override fun setRate(rate: Float) {
        tts?.setSpeechRate(rate)
        Log.d(TAG, "Speech rate set to: $rate")
    }

    override fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
        Log.d(TAG, "Speech pitch set to: $pitch")
    }

    override fun isAvailable(): Boolean = isInitialized && tts != null

    override fun shutdown() {
        Log.i(TAG, "Shutting down...")
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.i(TAG, "Shutdown complete")
    }

    private fun playSpeech(text: String) {
        try {
            val utteranceId = UUID.randomUUID().toString()

            // Установить listener для отслеживания завершения
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "Speech started: $utteranceId")
                    isSpeaking = true
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "Speech completed: $utteranceId")
                    isSpeaking = false // Вызываем обратные вызовы
                    onSpeechFinish()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "Speech error: $utteranceId")
                    isSpeaking = false // Вызываем обратные вызовы при ошибке
                    onSpeechFinish()
                }
            })

            // Воспроизвести текст
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        }
        catch (e: Exception) {
            Log.e(TAG, "Error playing speech", e)
        }
    }
}


