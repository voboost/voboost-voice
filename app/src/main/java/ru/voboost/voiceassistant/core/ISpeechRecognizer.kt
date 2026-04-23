package ru.voboost.voiceassistant.core

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.voboost.voiceassistant.speech.SpeechRecognizer.Mode
import ru.voboost.voiceassistant.speech.SpeechResult

interface ISpeechRecognizer {
    val results: MutableSharedFlow<SpeechResult>

    fun setMode(newMode: Mode)
    fun start()
    fun stop()
    fun shutdown()
}
