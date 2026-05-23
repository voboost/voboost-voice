package ru.voboost.voice.services.speech

import ru.voboost.voice.services.speech.SpeechService.Companion.PRIOR_LOW

interface ISpeechService{
    fun enqueue(text: String, priority: Int = PRIOR_LOW)
    suspend fun enqueueAsync(text: String, priority: Int = PRIOR_LOW): Boolean
    fun release()
}