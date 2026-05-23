package ru.voboost.voice.engine

/**
 * Результат распознавания речи
 *
 * Универсальный data class для всех движков (Vosk, Sherpa, и т.д.)
 */
data class RecognitionEngineResult(val text: String,
                                   val isFinal: Boolean = false,
                                   val isPartial: Boolean = false,
                                   val zone: String = "")