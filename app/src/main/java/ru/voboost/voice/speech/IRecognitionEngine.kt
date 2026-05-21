package ru.voboost.voice.speech

interface IRecognitionEngine {

    fun acceptWaveform(pcm: ByteArray, start: Int, end: Int): RecognitionResult?
    fun getFinalResult(): RecognitionResult?
    fun reset()
    fun release()
}


