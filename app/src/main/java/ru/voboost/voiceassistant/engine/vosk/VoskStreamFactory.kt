package ru.voboost.voiceassistant.engine.vosk

import ru.voboost.voiceassistant.speech.IRecognitionEngine
import ru.voboost.voiceassistant.speech.IStreamFactory
import org.vosk.Model

/**
 * Фабрика потоков распознавания Vosk
 * Реализует универсальный интерфейс IStreamFactory
 */
class VoskStreamFactory : IStreamFactory {
    
    /**
     * Создать поток распознавания Vosk
     * @param model Объект модели Vosk
     * @return VoskStream который реализует IRecognitionEngine
     */
    override fun create(model: Any): IRecognitionEngine {
        if (model !is Model) {
            throw IllegalArgumentException("Expected Vosk Model, got ${model::class.simpleName}")
        }
        
        val recognizer = org.vosk.Recognizer(model, 16000f)
        return VoskStream(recognizer)
    }
}
