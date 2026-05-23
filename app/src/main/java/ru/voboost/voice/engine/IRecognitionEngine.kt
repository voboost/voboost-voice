package ru.voboost.voice.engine

import ru.voboost.voice.engine.RecognitionEngineResult

interface IRecognitionEngine {

    fun acceptWaveform(pcm: ByteArray, start: Int, end: Int): RecognitionEngineResult?
    fun getFinalResult(): RecognitionEngineResult?
    fun reset()
    fun release()
}