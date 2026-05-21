package ru.voboost.voice.speech

/**
 * Универсальный интерфейс загрузчика моделей
 * Позволяет использовать любой движок (Vosk, Sherpa, и т.д.)
 */
interface IModelLoader {
    /**
     * Загрузить модель из указанного пути
     * @return Объект модели (зависит от движка)
     */
    fun loadModel(): Any
}


