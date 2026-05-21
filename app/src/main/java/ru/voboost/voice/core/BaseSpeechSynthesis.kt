package ru.voboost.voice.core

import android.util.Log

abstract class BaseSpeechSynthesis() : ISpeechSynthesis
{
    private val callbacks = mutableSetOf<ISpeechSynthesisCallback>()

    override fun addCallback(callback: ISpeechSynthesisCallback) {
        callbacks.add(callback)
        Log.d("SpeechSynthesis", "Callback added. Total callbacks: ${callbacks.size}")
    }

    override fun removeCallback(callback: ISpeechSynthesisCallback) {
        callbacks.remove(callback)
        Log.d("SpeechSynthesis", "Callback removed. Total callbacks: ${callbacks.size}")
    }

    protected fun onSpeechFinish()
    {
        callbacks.forEach { it.handleSpeechFinished() }
    }
}

