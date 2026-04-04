package com.voboost.voiceassistant.engine.sherpa

import com.voboost.voiceassistant.speech.RecognitionEngine
import com.voboost.voiceassistant.speech.StreamFactory

/**
 * Фабрика потоков распознавания Sherpa-ONNX
 * Реализует универсальный интерфейс StreamFactory
 */
class SherpaStreamFactory : StreamFactory {
    
    /**
     * Создать поток распознавания Sherpa-ONNX
     * @param model Путь к директории модели (String)
     * @return SherpaStream который реализует RecognitionEngine
     */
    override fun create(model: Any): RecognitionEngine {
        if (model !is String) {
            throw IllegalArgumentException("Expected model path (String), got ${model::class.simpleName}")
        }
        
        return SherpaStream.create(model)
    }
}
