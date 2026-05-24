package ru.voboost.voice.engine

import ru.voboost.voice.services.speech.ISpeechServiceCallback

/**
 * Интерфейс для модуля синтеза речи (TTS)
 * Позволяет легко менять реализации (Sherpa-ONNX, Vosk TTS, System TTS, Yandex SpeechKit)
 */
interface ISpeechEngine {
    suspend fun initialize(rate: Float, pitch: Float)
    fun isReady(): Boolean
    fun isAvailable(): Boolean
    fun speak(text: String)
    fun stop()
    fun shutdown()
    fun addCallback(callback: ISpeechServiceCallback)
    fun removeCallback(callback: ISpeechServiceCallback)
}