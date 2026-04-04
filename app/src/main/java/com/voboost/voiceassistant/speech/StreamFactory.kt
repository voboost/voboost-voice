package com.voboost.voiceassistant.speech

/**
 * Универсальный интерфейс фабрики потоков распознавания
 * Позволяет использовать любой движок (Vosk, Sherpa, и т.д.)
 */
interface StreamFactory {
    /**
     * Создать поток распознавания
     * @param model Объект модели (зависит от движка)
     * @return RecognitionEngine поток распознавания
     */
    fun create(model: Any): RecognitionEngine
}
