package ru.voboost.voice.services.speech

import android.content.Context
import android.util.Log
import ru.voboost.voice.VoboostVoiceService
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.config.ExternalStoragePaths
import ru.voboost.voice.engine.sherpa.SherpaSpeechEngine
import ru.voboost.voice.engine.system.SystemSpeechEngine

object SpeechServiceFactory {

    const val TAG = "SpeechEngineFactory"

    enum class SpeechEngine {
        SYSTEM,
        SHERPA,
    }

    /**
     * Создать модуль синтеза речи
     * @param context Контекст приложения
     * @param engine Тип движка (по умолчанию System как доступный)
     * @param modelPath Путь к модели (опционально)
     * @param speakerId ID спикера для Sherpa (0-4)
     */
    suspend fun create(context: Context, configManager: ConfigManager): ISpeechService {
        val speechConfig = configManager.getConfig().tts
        val engine = when (speechConfig.offline.engine.lowercase()) {
            "sherpa" -> {
                Log.i(VoboostVoiceService.Companion.TAG, "TTS engine from config: Sherpa-ONNX")
                SpeechEngine.SHERPA
            }
            else -> {
                Log.i(VoboostVoiceService.Companion.TAG, "TTS engine from config: System TTS")
                SpeechEngine.SYSTEM
            }
        }
        val speakerId = speechConfig.offline.speaker
        val modelPath = ExternalStoragePaths.sherpaTtsModelDir.absolutePath

        val speechEngine = when (engine) {
            SpeechEngine.SYSTEM -> {
                Log.i(TAG, "Creating System TTS engine")
                SystemSpeechEngine(context)
            }

            SpeechEngine.SHERPA -> {
                Log.i(TAG, "Creating Sherpa TTS engine")
                SherpaSpeechEngine(modelPath, speakerId)
            }
        }

        speechEngine.initialize(speechConfig.offline.rate, speechConfig.offline.pitch)
        return SpeechService(speechEngine)
    }
}