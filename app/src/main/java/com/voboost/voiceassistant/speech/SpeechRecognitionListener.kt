package com.voboost.voiceassistant.speech

/**
 * Слушатель событий распознавания речи
 * 
 * Универсальный интерфейс для всех движков (Vosk, Sherpa, и т.д.)
 * 
 * Преимущества перед callback-ами:
 * - ✅ Можно реализовать только нужные методы
 * - ✅ Легко расширять (добавлять новые методы)
 * - ✅ Более идиоматично для Kotlin/Java
 */
interface SpeechRecognitionListener {
    
    /**
     * Вызывается когда распознано ключевое слово для активации
     */
    fun onKeywordDetected() {}
    
    /**
     * Вызывается когда распознана команда
     * @param text Распознанный текст команды
     */
    fun onCommandReceived(text: String) {}
    
    /**
     * Вызывается при ошибке распознавания
     * @param error Описание ошибки
     */
    fun onError(error: String) {}
    
    /**
     * Вызывается при таймауте ожидания команды
     */
    suspend fun onTimeout() {}
    
    /**
     * Вызывается при изменении состояния распознавания
     * @param state Новое состояние
     */
    fun onStateChanged(state: SpeechState) {}
}
