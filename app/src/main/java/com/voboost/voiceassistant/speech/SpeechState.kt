package com.voboost.voiceassistant.speech

/**
 * Состояния распознавания речи
 * 
 * Универсальный enum для всех движков (Vosk, Sherpa, и т.д.)
 */
enum class SpeechState {
    /** Ожидание ключевого слова */
    LISTENING_KEYWORD,
    
    /** Ключевое слово распознано, активация */
    ACTIVATED,
    
    /** Ожидание команды */
    LISTENING_COMMAND,
    
    /** Распознавание остановлено */
    STOPPED
}
