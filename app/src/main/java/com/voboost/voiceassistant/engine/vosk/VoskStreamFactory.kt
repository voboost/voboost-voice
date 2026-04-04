package com.voboost.voiceassistant.engine.vosk

import com.voboost.voiceassistant.speech.RecognitionEngine
import com.voboost.voiceassistant.speech.StreamFactory
import org.vosk.Model

/**
 * Фабрика потоков распознавания Vosk
 * Реализует универсальный интерфейс StreamFactory
 */
class VoskStreamFactory : StreamFactory {
    
    /**
     * Создать поток распознавания Vosk
     * @param model Объект модели Vosk
     * @return VoskStream который реализует RecognitionEngine
     */
    override fun create(model: Any): RecognitionEngine {
        if (model !is Model) {
            throw IllegalArgumentException("Expected Vosk Model, got ${model::class.simpleName}")
        }
        
        val recognizer = org.vosk.Recognizer(model, 16000f)
        return VoskStream(recognizer)
    }
}
