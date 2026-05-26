package ru.voboost.voice.services.recognition

import ru.voboost.voice.audio.MultiChannelAudioSource

/**
 * Результат распознавания
 */
sealed class RecognitionServiceResult {
    data class KeywordDetected(val text: String, val zone: String = MultiChannelAudioSource.ZONE_FRONT_LEFT)
        : RecognitionServiceResult()
    data class CommandReceived(val text: String, val zone: String = MultiChannelAudioSource.ZONE_FRONT_LEFT)
        : RecognitionServiceResult()
    object Timeout : RecognitionServiceResult()
    data class Error(val message: String) : RecognitionServiceResult()
}