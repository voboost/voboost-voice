package ru.voboost.voiceassistant.speech

/**
 * Результат распознавания речи
 *
 * Универсальный data class для всех движков (Vosk, Sherpa, и т.д.)
 */
data class RecognitionResult(val text: String,
                             val isFinal: Boolean = false,
                             val isPartial: Boolean = false,
                             val zone: String = "")
