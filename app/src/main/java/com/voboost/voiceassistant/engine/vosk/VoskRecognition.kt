package com.voboost.voiceassistant.engine.vosk

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechRecognition
import com.voboost.voiceassistant.speech.SpeechRecognitionModule
import java.io.File

/**
 * Обёртка над старым SpeechRecognitionModule (Vosk) для совместимости с новым интерфейсом
 * Используется как временное решение до полной миграции на Sherpa-ONNX
 * 
 * Поддерживает загрузку моделей с SD-карты (внешнего хранилища)
 */
class VoskRecognition(
    private val context: Context
) : SpeechRecognition {

    companion object {
        private const val TAG = "VoskRecognition"
        private const val MODEL_DIR = "models/vosk"
    }

    private val speechRecognition: SpeechRecognitionModule
    private val configManager = ConfigManager.getInstance(context)
    private val modelPath: String

    init {
        // Определяем путь к модели: SD-карта или внутренняя память
        modelPath = getModelPath()
        Log.i(TAG, "Vosk model path: $modelPath")
        
        speechRecognition = SpeechRecognitionModule(context, modelPath)
    }

    /**
     * Получить путь к модели Vosk
     * Модель хранится во внутренней памяти: /data/user/0/.../files/models/vosk/
     */
    private fun getModelPath(): String {
        val config = configManager.getConfig()
        val modelName = config.speech.offline.model

        // Путь во внутренней памяти
        val internalModelDir = File(context.filesDir, "models/vosk/$modelName")

        // Проверяем наличие директории модели
        if (internalModelDir.exists() && internalModelDir.isDirectory) {
            Log.i(TAG, "Using Vosk model from internal storage: ${internalModelDir.absolutePath}")
            Log.i(TAG, "Model directory contents: ${internalModelDir.list()?.joinToString()}")
            return internalModelDir.absolutePath
        }

        // Модели нет - выбрасываем исключение
        throw IllegalStateException(
            "Vosk model not found at: ${internalModelDir.absolutePath}\n" +
            "Please copy the model using copy-vosk-to-internal.bat script"
        )
    }

    /**
     * Проверить что модель полная (содержит необходимые файлы)
     */
    private fun isModelComplete(modelDir: File): Boolean {
        if (!modelDir.isDirectory) return false

        // Проверяем наличие ключевых файлов Vosk модели
        val requiredFiles = listOf("am/final.mdl", "conf/model_config.json", "graph/HCLG.fst", "graph/words.txt")
        return requiredFiles.all { File(modelDir, it).exists() }
    }

    override suspend fun initialize() {
        Log.d(TAG, "VoskRecognition already initialized in constructor")
    }

    override fun isReady(): Boolean {
        // Проверяем что Vosk инициализирован
        return true  // SpeechRecognitionModule инициализируется в конструкторе
    }

    override suspend fun startKeywordSpotting(
        onKeywordDetected: () -> Unit,
        onError: suspend (String) -> Unit
    ) {
        Log.d(TAG, "Starting keyword spotting (Vosk)")

        speechRecognition.startKeywordSpotting(
            onKeywordDetected = onKeywordDetected,
            onError = onError
        )
    }

    override fun startCommandListening(
        onCommandReceived: (String) -> Unit,
        onError: (String) -> Unit,
        onTimeout: suspend () -> Unit
    ) {
        Log.d(TAG, "Starting command listening (Vosk)")

        speechRecognition.startCommandListening(
            onCommandReceived = onCommandReceived,
            onError = onError,
            onTimeout = onTimeout
        )
    }

    override suspend fun listenForCommand(timeout: Long): String {
        Log.d(TAG, "listenForCommand called (Vosk), timeout: ${timeout}ms")
        return speechRecognition.listenForCommand(timeout)
    }

    override fun setActivationKeywords(keywords: List<String>) {
        Log.d(TAG, "setActivationKeywords not supported in Vosk, keywords are loaded from config")
        // Vosk загружает ключевые слова из config.json при инициализации
    }

    override fun stop() {
        Log.d(TAG, "Stopping recognition (Vosk)")
        speechRecognition.pause()
    }

    override fun shutdown() {
        Log.d(TAG, "Shutting down (Vosk)")
        speechRecognition.shutdown()
    }
}
