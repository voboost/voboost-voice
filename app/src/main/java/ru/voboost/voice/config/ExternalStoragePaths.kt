package ru.voboost.voice.config

import android.os.Environment
import java.io.File

/**
 * Пути к внешнему хранилищу для моделей и конфигурации
 *
 * Используем стандартный Android путь:
 * /storage/emulated/0/Android/data/ru.voboost.voice/files/
 *
 * Структура:
 * /storage/emulated/0/Android/data/ru.voboost.voice/files/
 * ├── config.json
 * └── models/
 *     ├── nlu/
 *     │   ├── model.onnx          # ONNX NLU модель для распознавания команд
 *     │   └── tokenizer.json      # Токенайзер для HuggingFace
 *     ├── vosk/
 *     │   └── vosk-model-small-ru-0.22/
 *     ├── sherpa/
 *     │   ├── asr-ru-model/       # Sherpa STT модель
 *     │   └── tts-ru-model/       # Sherpa TTS модель
 *     └── llm/
 *         └── Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task  # MediaPipe LLM
 */
object ExternalStoragePaths {
    
    // Имя пакета приложения
    private const val PACKAGE_NAME = "ru.voboost.voice"
    
    // Базовая директория на внешнем хранилище (стандартный Android путь)
    // /storage/emulated/0/Android/data/ru.voboost.voice/files/
    val baseDir: File
        get() {
            val externalStorage = Environment.getExternalStorageDirectory()
            return File(externalStorage, "Android/data/$PACKAGE_NAME/files")
        }
    
    // Директория моделей
    val modelsDir: File
        get() = File(baseDir, "models")
    
    // Vosk модель
    val voskModelDir: File
        get() = File(modelsDir, "vosk/vosk-model-small-ru-0.22")
    
    // Sherpa ASR модель
    val sherpaAsrModelDir: File
        get() = File(modelsDir, "sherpa/asr-ru-model")
    
    // Sherpa TTS модель
    val sherpaTtsModelDir: File
        get() = File(modelsDir, "sherpa/tts-ru-model")
    
    // Файл конфигурации
    val configFile: File
        get() = File(baseDir, "config.json")

    // Директория LLM моделей
    val llmModelDir: File
        get() = File(modelsDir, "llm")

    // MediaPipe LLM модель (в формате .task)
    val llmModelFile: File
        get() = File(llmModelDir, "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task")

    // NLU ONNX модель для распознавания команд
    val nluModelDir: File
        get() = File(modelsDir, "nlu")
    
    val nluModelFile: File
        get() = File(nluModelDir, "model.onnx")
    
    val tokenizerFile: File
        get() = File(nluModelDir, "tokenizer.json")

    /**
     * Проверить существование всех необходимых файлов
     */
    fun checkAllPaths(): Map<String, Boolean> {
        return mapOf(
            "baseDir" to baseDir.exists(),
            "config.json" to configFile.exists(),
            "nlu-model" to nluModelFile.exists(),
            "tokenizer" to tokenizerFile.exists(),
            "vosk-model" to voskModelDir.exists(),
            "sherpa-asr" to sherpaAsrModelDir.exists(),
            "sherpa-tts" to sherpaTtsModelDir.exists(),
            "llm-model" to llmModelFile.exists()
        )
    }
    
    /**
     * Создать все необходимые директории
     */
    fun createDirectories(): Boolean {
        return try {
            baseDir.mkdirs()
            modelsDir.mkdirs()
            File(modelsDir, "nlu").mkdirs()
            File(modelsDir, "vosk").mkdirs()
            File(modelsDir, "sherpa").mkdirs()
            llmModelDir.mkdirs()
            true
        } catch (e: Exception) {
            android.util.Log.e("ExternalStoragePaths", "Failed to create directories", e)
            false
        }
    }
}


