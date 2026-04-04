package com.voboost.voiceassistant.speech

/**
 * Универсальный интерфейс загрузчика моделей
 * Позволяет использовать любой движок (Vosk, Sherpa, и т.д.)
 */
interface ModelLoader {
    /**
     * Загрузить модель из указанного пути
     * @param modelPath Путь к директории модели
     * @return Объект модели (зависит от движка)
     */
    fun loadModel(modelPath: String): Any
    
    /**
     * Получить путь к модели
     * @return Путь к директории модели
     */
    fun getModelPath(): String
}
