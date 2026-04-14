package ru.voboost.voiceassistant.config

import android.os.Environment
import java.io.File

/**
 * Пути к внешнему хранилищу для моделей и конфигурации
 * 
 * Используем стандартный Android путь:
 * /storage/emulated/0/Android/data/ru.voboost.voiceassistant/files/
 * 
 * Структура:
 * /storage/emulated/0/Android/data/ru.voboost.voiceassistant/files/
 * ├── config.json
 * └── models/
 *     ├── vosk/
 *     │   └── vosk-model-small-ru-0.22/
 *     └── sherpa/
 *         ├── asr-ru-model/
 *         └── tts-ru-model/
 */
object ExternalStoragePaths {
    
    // Имя пакета приложения
    private const val PACKAGE_NAME = "ru.voboost.voiceassistant"
    
    // Базовая директория на внешнем хранилище (стандартный Android путь)
    // /storage/emulated/0/Android/data/ru.voboost.voiceassistant/files/
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
    
    /**
     * Проверить существование всех необходимых файлов
     */
    fun checkAllPaths(): Map<String, Boolean> {
        return mapOf(
            "baseDir" to baseDir.exists(),
            "config.json" to configFile.exists(),
            "vosk-model" to voskModelDir.exists(),
            "sherpa-asr" to sherpaAsrModelDir.exists(),
            "sherpa-tts" to sherpaTtsModelDir.exists()
        )
    }
    
    /**
     * Создать все необходимые директории
     */
    fun createDirectories(): Boolean {
        return try {
            baseDir.mkdirs()
            modelsDir.mkdirs()
            File(modelsDir, "vosk").mkdirs()
            File(modelsDir, "sherpa").mkdirs()
            true
        } catch (e: Exception) {
            android.util.Log.e("ExternalStoragePaths", "Failed to create directories", e)
            false
        }
    }
}
