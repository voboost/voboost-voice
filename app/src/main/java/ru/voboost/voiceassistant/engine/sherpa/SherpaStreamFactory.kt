package ru.voboost.voiceassistant.engine.sherpa

import ru.voboost.voiceassistant.speech.IRecognitionEngine
import ru.voboost.voiceassistant.speech.IStreamFactory

/**
 * Фабрика потоков распознавания Sherpa-ONNX
 * Реализует универсальный интерфейс IStreamFactory
 */
class SherpaStreamFactory : IStreamFactory {
    
    /**
     * Создать поток распознавания Sherpa-ONNX
     * @param model Путь к директории модели (String)
     * @return SherpaStream который реализует IRecognitionEngine
     */
    override fun create(model: Any): IRecognitionEngine {
        if (model !is String) {
            throw IllegalArgumentException("Expected model path (String), got ${model::class.simpleName}")
        }
        
        return SherpaStream.create(model)
    }
}
