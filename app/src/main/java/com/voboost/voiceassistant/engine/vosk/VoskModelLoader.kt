package com.voboost.voiceassistant.engine.vosk

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.speech.IModelLoader
import org.vosk.Model
import java.io.File

/**
 * Загрузчик моделей Vosk
 * Реализует универсальный интерфейс IModelLoader
 */
class VoskModelLoader(
    private val context: Context
) : IModelLoader {
    
    companion object {
        const val TAG = "VoskModelLoader"
        const val MODEL_NAME = "vosk-model-small-ru-0.22"
    }

    /**
     * Загрузить модель Vosk из указанного пути
     * @param modelPath Путь к директории модели
     * @return Model объект Vosk
     */
    override fun loadModel(modelPath: String): Any {
        val modelDir = File(modelPath)
        
        if (!modelDir.exists()) {
            throw IllegalStateException("Vosk model not found at: $modelPath")
        }
        
        Log.i(TAG, "Loading Vosk model from: ${modelDir.absolutePath}")
        return Model(modelDir.absolutePath)
    }
    
    /**
     * Получить путь к модели Vosk
     * @return Путь к директории модели
     */
    override fun getModelPath(): String {
        val internalModelDir = File(context.filesDir, "models/vosk/$MODEL_NAME")

        if (internalModelDir.exists() && internalModelDir.isDirectory) {
            Log.i(TAG, "Using Vosk model from internal storage: ${internalModelDir.absolutePath}")
            return internalModelDir.absolutePath
        }

        Log.w(TAG, "Vosk model not found at: ${internalModelDir.absolutePath}")
        return ""
    }
}
