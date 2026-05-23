package ru.voboost.voice.services.recognition

/**
 * Результат распознавания
 */
sealed class RecognitionServiceResult {
    data class KeywordDetected(val text: String, val zone: String = "front_left") : RecognitionServiceResult()
    data class CommandReceived(val text: String, val zone: String = "front_left") : RecognitionServiceResult()
    object Timeout : RecognitionServiceResult()
    data class Error(val message: String) : RecognitionServiceResult()
}