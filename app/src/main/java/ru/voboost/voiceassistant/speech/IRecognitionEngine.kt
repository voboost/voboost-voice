package ru.voboost.voiceassistant.speech

interface IRecognitionEngine {

    fun acceptWaveform(pcm: ByteArray): RecognitionResult?
    fun getFinalResult(): RecognitionResult?
    fun reset()
    fun release()
}
