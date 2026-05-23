package ru.voboost.voice.engine

import android.util.Log
import ru.voboost.voice.services.speech.ISpeechServiceCallback

abstract class BaseSpeechEngine : ISpeechEngine {
    private val callbacks = mutableSetOf<ISpeechServiceCallback>()

    override fun addCallback(callback: ISpeechServiceCallback) {
        callbacks.add(callback)
        Log.d("SpeechSynthesis", "Callback added. Total callbacks: ${callbacks.size}")
    }

    override fun removeCallback(callback: ISpeechServiceCallback) {
        callbacks.remove(callback)
        Log.d("SpeechSynthesis", "Callback removed. Total callbacks: ${callbacks.size}")
    }

    protected fun onSpeechFinish() {
        callbacks.forEach { it.handleSpeechFinished() }
    }
}