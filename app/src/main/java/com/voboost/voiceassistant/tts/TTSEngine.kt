package com.voboost.voiceassistant.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.voboost.voiceassistant.config.ConfigManager
import java.util.*

/**
 * TTS Engine - синтез речи
 * Использует системный TTS (RhVoice если установлен)
 */
class TTSEngine(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TTSEngine"
    }

    private var tts: TextToSpeech? = null
    private val configManager = ConfigManager.getInstance(context)
    private var isInitialized = false
    private val speechQueue = mutableListOf<String>()

    init { // Инициализация системного TTS
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("ru-RU"))

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Russian language not supported by TTS")
                isInitialized = false
            }
            else { // Настройки голоса
                val config = configManager.getConfig().tts.offline
                tts?.setPitch(config.pitch)
                tts?.setSpeechRate(config.rate)

                isInitialized = true
                Log.i(TAG, "TTS initialized successfully")

                // Проиграть сообщения из очереди
                processQueue()
            }
        }
        else {
            Log.e(TAG, "TTS initialization failed")
            isInitialized = false
        }
    }

    /**
     * Произнести текст
     */
    fun speak(text: String?) {
        if (text.isNullOrEmpty()) {  // ← Проверка
            Log.w(TAG, "Empty text, skipping TTS")
            return
        }
        Log.d(TAG, "Speaking: $text")

        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, adding to queue")
            speechQueue.add(text)
            return
        }

        // Остановить текущее воспроизведение
        stop()

        // Воспроизвести новый текст
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Остановить воспроизведение
     */
    fun stop() {
        tts?.stop()
    }

    /**
     * Обработать очередь сообщений
     */
    private fun processQueue() {
        if (speechQueue.isEmpty()) return

        val text = speechQueue.removeAt(0)
        speak(text)
    }

    /**
     * Установить скорость речи
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    /**
     * Установить тон голоса
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    /**
     * Проверить, доступен ли TTS
     */
    fun isAvailable(): Boolean {
        return isInitialized && tts != null
    }

    /**
     * Очистить очередь
     */
    fun clearQueue() {
        speechQueue.clear()
    }

    /**
     * Завершить работу
     */
    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.i(TAG, "TTS shutdown complete")
    }
}
