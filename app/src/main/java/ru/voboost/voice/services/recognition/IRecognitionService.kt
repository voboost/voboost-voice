package ru.voboost.voice.services.recognition

import kotlinx.coroutines.flow.MutableSharedFlow

interface IRecognitionService {
    val results: MutableSharedFlow<RecognitionServiceResult>

    fun setMode(newMode: RecognitionService.Mode)
    fun getMode(): RecognitionService.Mode
    fun setModeSafe(newMode: RecognitionService.Mode)
    fun start()
    fun stop()
    fun shutdown()
}