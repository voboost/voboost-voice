package ru.voboost.voiceassistant.core

import android.content.Context
import android.util.Log
import ru.voboost.voiceassistant.VoboostVoiceService
import ru.voboost.voiceassistant.audio.IAudioSource
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.config.ExternalStoragePaths
import ru.voboost.voiceassistant.engine.sherpa.SherpaModelLoader
import ru.voboost.voiceassistant.engine.sherpa.SherpaStreamFactory
import ru.voboost.voiceassistant.engine.sherpa.SherpaSpeechSynthesis
import ru.voboost.voiceassistant.engine.system.SystemSpeechSynthesis
import ru.voboost.voiceassistant.engine.vosk.VoskModelLoader
import ru.voboost.voiceassistant.engine.vosk.VoskStreamFactory
import ru.voboost.voiceassistant.speech.KeywordChecker
import ru.voboost.voiceassistant.speech.SpeechRecognizer

object SpeechEngineFactory {

    const val TAG = "SpeechEngineFactory"

    enum class RecognitionEngine {
        VOSK,
        SHERPA,
    }

    enum class SynthesisEngine {
        SYSTEM,
        SHERPA,
    }

    /**
     * Создать распознаватель речи (утилита без состояний)
     * @param context Контекст приложения
     * @param engine Тип движка (по умолчанию Vosk как стабильный)
     * @param IAudioSource Источник аудио (создаётся через AudioSourceFactory)
     * @param defaultZone Зона по умолчанию (опционально, зона теперь определяется в AudioSource callback)
     * @return SpeechRecognizer для управления распознаванием
     */
    fun createSpeechRecognizer(engine: RecognitionEngine,
                               audioSource: IAudioSource,
                               configManager: ConfigManager): ISpeechRecognizer {
        val keywordChecker = KeywordChecker(configManager)

        val recognitionEngine = when (engine) {
            RecognitionEngine.VOSK -> {
                Log.i(TAG, "Creating Vosk recognition engine")
                val IModelLoader = VoskModelLoader()
                val model = IModelLoader.loadModel()
                VoskStreamFactory().create(model)
            }

            RecognitionEngine.SHERPA -> {
                Log.i(TAG, "Creating Sherpa recognition engine")
                val IModelLoader = SherpaModelLoader()
                val model = IModelLoader.loadModel()
                SherpaStreamFactory().create(model)
            }
        }

        return SpeechRecognizer(audioSource = audioSource,
                                recognitionEngine = recognitionEngine,
                                keywordChecker = keywordChecker)
    }

    /**
     * Создать модуль синтеза речи
     * @param context Контекст приложения
     * @param engine Тип движка (по умолчанию System как доступный)
     * @param modelPath Путь к модели (опционально)
     * @param speakerId ID спикера для Sherpa (0-4)
     */
    fun createSynthesisEngine(context: Context,
                              configManager: ConfigManager): ISpeechSynthesis {

        val speechConfig = configManager.getConfig().tts
        val engine = when (speechConfig.offline.engine.lowercase()) {
            "sherpa" -> {
                Log.i(VoboostVoiceService.Companion.TAG, "TTS engine from config: Sherpa-ONNX")
                SynthesisEngine.SHERPA
            }
            else -> {
                Log.i(VoboostVoiceService.Companion.TAG, "TTS engine from config: System TTS")
                SynthesisEngine.SYSTEM
            }
        }
        val speakerId = speechConfig.offline.speaker
        val modelPath = ExternalStoragePaths.sherpaTtsModelDir.absolutePath

        return when (engine) {
            SynthesisEngine.SYSTEM -> {
                Log.i(TAG, "Creating System TTS engine")
                SystemSpeechSynthesis(context)
            }

            SynthesisEngine.SHERPA -> {
                Log.i(TAG, "Creating Sherpa TTS engine")
                SherpaSpeechSynthesis(modelPath, speakerId)
            }
        }
    }
}
