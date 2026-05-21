package ru.voboost.voice.speech

/**
 * Результат распознавания
 */
sealed class SpeechResult {
    data class KeywordDetected(val text: String, val zone: String = "front_left") : SpeechResult()
    data class CommandReceived(val text: String, val zone: String = "front_left") : SpeechResult()
    object Timeout : SpeechResult()
    data class Error(val message: String) : SpeechResult()
}

