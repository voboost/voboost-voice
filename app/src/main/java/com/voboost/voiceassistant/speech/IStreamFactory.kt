package com.voboost.voiceassistant.speech

/**
 * Универсальный интерфейс фабрики потоков распознавания
 * Позволяет использовать любой движок (Vosk, Sherpa, и т.д.)
 */
interface IStreamFactory {
    /**
     * Создать поток распознавания
     * @param model Объект модели (зависит от движка)
     * @return IRecognitionEngine поток распознавания
     */
    fun create(model: Any): IRecognitionEngine
}
