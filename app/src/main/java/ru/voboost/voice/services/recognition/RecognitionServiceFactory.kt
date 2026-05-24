package ru.voboost.voice.services.recognition

import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import ru.voboost.voice.audio.IAudioSource
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.engine.sherpa.SherpaModelLoader
import ru.voboost.voice.engine.sherpa.SherpaRecognitionEngine
import ru.voboost.voice.engine.vosk.VoskModelLoader
import ru.voboost.voice.engine.vosk.VoskRecognitionEngine
import ru.voboost.voice.nlu.NLUKeyword

object RecognitionServiceFactory {

    const val TAG = "RecognitionEngineFactory"

    enum class RecognitionEngine {
        VOSK,
        SHERPA,
    }

    /**
     * Создать распознаватель речи (утилита без состояний)
     * @param context Контекст приложения
     * @param engine Тип движка (по умолчанию Vosk как стабильный)
     * @param ru.voboost.voice.audio.IAudioSource Источник аудио (создаётся через AudioSourceFactory)
     * @param defaultZone Зона по умолчанию (опционально, зона теперь определяется в AudioSource callback)
     * @return SpeechRecognizer для управления распознаванием
     */
    fun create(engine: RecognitionEngine,
               audioSource: IAudioSource,
               configManager: ConfigManager): IRecognitionService {
        val keywordChecker = NLUKeyword(configManager)

        val recognitionEngine = when (engine) {
            RecognitionEngine.VOSK -> {
                Log.i(TAG, "Creating Vosk recognition engine")
                val modelLoader = VoskModelLoader()
                val model = modelLoader.loadModel()
                if (model !is Model) {
                    throw IllegalArgumentException("Expected Vosk Model, got ${model::class.simpleName}")
                }
                val recognizer = Recognizer(model, 16000f)
                VoskRecognitionEngine(recognizer)
            }

            RecognitionEngine.SHERPA -> {
                Log.i(TAG, "Creating Sherpa recognition engine")
                val modelLoader = SherpaModelLoader()
                val model = modelLoader.loadModel()
                if (model !is String) {
                    throw IllegalArgumentException("Expected model path (String), got ${model::class.simpleName}")
                }
                SherpaRecognitionEngine.Companion.create(model)
            }
        }
        return RecognitionService(audioSource = audioSource,
                                  recognitionEngine = recognitionEngine,
                                  keywordChecker = keywordChecker)
    }
}