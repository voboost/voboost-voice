package ru.voboost.voice.core

/**
 * Интерфейс для модуля синтеза речи (TTS)
 * Позволяет легко менять реализации (Sherpa-ONNX, Vosk TTS, System TTS, Yandex SpeechKit)
 */
interface ISpeechSynthesis {
    suspend fun initialize()
    fun isReady(): Boolean
    fun isAvailable(): Boolean
    fun speak(text: String)
    fun stop()
    fun setRate(rate: Float)
    fun setPitch(pitch: Float)
    fun shutdown()
    fun addCallback(callback: ISpeechSynthesisCallback)
    fun removeCallback(callback: ISpeechSynthesisCallback)
}


