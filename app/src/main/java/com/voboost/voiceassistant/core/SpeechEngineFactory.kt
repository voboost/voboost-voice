package com.voboost.voiceassistant.core

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.audio.AudioSource
import com.voboost.voiceassistant.audio.AudioSourceFactory
import com.voboost.voiceassistant.engine.sherpa.SherpaModelLoader
import com.voboost.voiceassistant.engine.sherpa.SherpaStreamFactory
import com.voboost.voiceassistant.engine.sherpa.SherpaSynthesis
import com.voboost.voiceassistant.engine.system.SystemTtsSynthesis
import com.voboost.voiceassistant.engine.vosk.VoskModelLoader
import com.voboost.voiceassistant.engine.vosk.VoskStreamFactory
import com.voboost.voiceassistant.speech.KeywordChecker
import com.voboost.voiceassistant.speech.SpeechRecognizer
import com.voboost.voiceassistant.ui.OverlayManager
import java.io.File

/**
 * Фабрика для создания модулей распознавания и синтеза речи
 * Позволяет легко переключаться между реализациями (Vosk, Sherpa, System)
 */
object SpeechEngineFactory {

    private const val TAG = "SpeechEngineFactory"

    enum class RecognitionEngine {
        VOSK,       // Текущая реализация (стабильная)
        SHERPA,     // Новая реализация (требует правильной версии API)
    }

    enum class SynthesisEngine {
        SYSTEM,     // Системный TTS (простой, но может не поддерживать русский)
        SHERPA,     // Sherpa-ONNX TTS (качественный, офлайн)
    }

    /**
     * Создать распознаватель речи (утилита без состояний)
     * @param context Контекст приложения
     * @param engine Тип движка (по умолчанию Vosk как стабильный)
     * @param audioSource Источник аудио (создаётся через AudioSourceFactory)
     * @return SpeechRecognizer для управления распознаванием
     */
    fun createSpeechRecognizer(
        context: Context,
        engine: RecognitionEngine = RecognitionEngine.VOSK,
        audioSource: AudioSource
    ): SpeechRecognizer {
        val configManager = com.voboost.voiceassistant.config.ConfigManager.getInstance(context)
        val keywordChecker = KeywordChecker(configManager)

        val recognitionEngine = when (engine) {
            RecognitionEngine.VOSK -> {
                Log.i(TAG, "Creating Vosk recognition engine")
                val modelLoader = VoskModelLoader(context)
                val modelPath = modelLoader.getModelPath()
                val model = modelLoader.loadModel(modelPath)
                VoskStreamFactory().create(model)
            }

            RecognitionEngine.SHERPA -> {
                Log.i(TAG, "Creating Sherpa recognition engine")
                val modelLoader = SherpaModelLoader(context)
                val modelPath = modelLoader.getModelPath()
                val model = modelLoader.loadModel(modelPath)
                SherpaStreamFactory().create(model)
            }
        }

        return SpeechRecognizer(
            audioSource = audioSource,
            recognitionEngine = recognitionEngine,
            keywordChecker = keywordChecker
        )
    }

    /**
     * Создать модуль синтеза речи
     * @param context Контекст приложения
     * @param engine Тип движка (по умолчанию System как доступный)
     * @param modelPath Путь к модели (опционально)
     * @param speakerId ID спикера для Sherpa (0-4)
     */
    fun createSynthesisEngine(
        context: Context,
        engine: SynthesisEngine = SynthesisEngine.SYSTEM,
        modelPath: String? = null,
        speakerId: Int = 0
    ): SpeechSynthesis {
        return when (engine) {
            SynthesisEngine.SYSTEM -> {
                Log.i(TAG, "Creating System TTS engine")
                SystemTtsSynthesis(context)
            }

            SynthesisEngine.SHERPA -> {
                Log.i(TAG, "Creating Sherpa TTS engine")
                val path = modelPath ?: getDefaultSherpaTtsPath(context)
                SherpaSynthesis(context, path, speakerId)
            }
        }
    }

    /**
     * Проверить, доступна ли модель Sherpa-ONNX для синтеза
     */
    fun isSherpaSynthesisAvailable(context: Context): Boolean {
        val modelPath = getDefaultSherpaTtsPath(context)
        return File(modelPath).exists()
    }

    /**
     * Получить путь к модели Sherpa-ONNX TTS по умолчанию
     */
    private fun getDefaultSherpaTtsPath(context: Context): String {
        // Модель будет копироваться из assets или загружаться отдельно
        return context.filesDir.resolve("sherpa/tts-ru-model").absolutePath
    }

    /**
     * Рекомендованная конфигурация для текущего устройства
     */
    fun getRecommendedConfig(context: Context): Pair<RecognitionEngine, SynthesisEngine> {
        // Проверить системный TTS на поддержку русского
        val systemTtsAvailable = isSystemTtsRussianAvailable(context)

        return when {
            // Если System TTS доступен - использовать его
            systemTtsAvailable -> {
                Log.i(TAG, "Recommended: Vosk ASR + System TTS")
                Pair(RecognitionEngine.VOSK, SynthesisEngine.SYSTEM)
            }

            // Fallback - только Vosk
            else -> {
                Log.i(TAG, "Recommended: Vosk ASR only (no TTS available)")
                Pair(RecognitionEngine.VOSK, SynthesisEngine.SYSTEM)
            }
        }
    }

    /**
     * Проверить поддержку русского языка системным TTS
     */
    private fun isSystemTtsRussianAvailable(context: Context): Boolean {
        return try {
            val tts = android.speech.tts.TextToSpeech(context) { /* dummy */ }
            val result = tts.isLanguageAvailable(java.util.Locale.forLanguageTag("ru-RU"))
            tts.shutdown()
            result != android.speech.tts.TextToSpeech.LANG_MISSING_DATA &&
            result != android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check system TTS", e)
            false
        }
    }
}
