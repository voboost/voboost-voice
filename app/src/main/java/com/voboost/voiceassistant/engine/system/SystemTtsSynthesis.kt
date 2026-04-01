package com.voboost.voiceassistant.engine.system

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.voboost.voiceassistant.core.SpeechSynthesis
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Обёртка над системным TextToSpeech для совместимости с новым интерфейсом
 * Используется как запасной вариант если Sherpa-ONNX не доступен
 */
class SystemTtsSynthesis(private val context: Context) : SpeechSynthesis,
        TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "SystemTtsSynthesis"
        private const val RUSSIAN_LANG_TAG = "ru-RU"
    }

    private var tts: TextToSpeech? = null

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isSpeaking = false

    private val speechQueue = ConcurrentLinkedQueue<SpeechItem>()

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

                // Обработать очередь если есть
                processQueue()
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

    override fun speak(text: String, onCompletion: (() -> Unit)?) {
        if (text.isBlank()) {
            Log.w(TAG, "Empty text, skipping TTS")
            onCompletion?.invoke()
            return
        }

        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, adding to queue")
            speechQueue.add(SpeechItem(text, onCompletion))
            return
        }

        Log.d(TAG, "Speaking: $text")

        // Добавить в очередь
        speechQueue.add(SpeechItem(text, onCompletion))

        // Запустить воспроизведение если не играет
        if (!isSpeaking) {
            processQueue()
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

    override fun clearQueue() {
        Log.d(TAG, "Clearing speech queue")
        speechQueue.clear()
        stop()
    }

    override fun shutdown() {
        Log.i(TAG, "Shutting down...")
        stop()
        clearQueue()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.i(TAG, "Shutdown complete")
    }

    // ==================== ВНУТРЕННИЕ МЕТОДЫ ====================

    private fun processQueue() {
        if (isSpeaking) return  // Уже играет

        isSpeaking = true

        try {
            while (speechQueue.isNotEmpty()) {
                val item = speechQueue.poll() ?: break
                playSpeech(item.text, item.onCompletion)
            }
        }
        finally {
            isSpeaking = false
        }
    }

    private fun playSpeech(text: String, onCompletion: (() -> Unit)?) {
        try {
            val utteranceId = UUID.randomUUID().toString()

            // Установить listener для отслеживания завершения
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "Speech started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "Speech completed: $utteranceId")
                    onCompletion?.invoke() // Продолжить очередь
                    processQueue()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "Speech error: $utteranceId")
                    onCompletion?.invoke()
                    processQueue()
                }
            })

            // Воспроизвести текст
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        }
        catch (e: Exception) {
            Log.e(TAG, "Error playing speech", e)
            onCompletion?.invoke()
            processQueue()
        }
    }

    private data class SpeechItem(val text: String, val onCompletion: (() -> Unit)?)
}
