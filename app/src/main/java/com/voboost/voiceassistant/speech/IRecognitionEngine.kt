package com.voboost.voiceassistant.speech

/**
 * Универсальный интерфейс движка распознавания
 * Позволяет использовать любой движок (Vosk, Sherpa, и т.д.)
 */
interface IRecognitionEngine {
    /**
     * Принять порцию PCM данных и распознать
     * @param pcm PCM данные (16-bit, mono, 16000 Hz)
     * @return Результат распознавания или null
     */
    fun acceptWaveform(pcm: ByteArray): RecognitionResult?

    /**
     * Получить финальный результат
     */
    fun getFinalResult(): RecognitionResult?

    /**
     * Сбросить распознавание
     */
    fun reset()

    /**
     * Освободить ресурсы
     */
    fun release()
}
