package com.voboost.voiceassistant.engine.sherpa

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.speech.IModelLoader
import java.io.File

/**
 * Загрузчик моделей Sherpa-ONNX
 * Реализует универсальный интерфейс IModelLoader
 */
class SherpaModelLoader(
    private val context: Context
) : IModelLoader {
    
    companion object {
        const val TAG = "SherpaModelLoader"
    }
    
    private val configManager = ConfigManager.getInstance(context)
    
    /**
     * Загрузить модель Sherpa-ONNX из указанного пути
     * @param modelPath Путь к директории модели
     * @return Путь к модели (String)
     */
    override fun loadModel(modelPath: String): Any {
        val modelDir = File(modelPath)
        
        if (!modelDir.exists()) {
            throw IllegalStateException("Sherpa model not found at: $modelPath")
        }
        
        Log.i(TAG, "Sherpa model verified at: ${modelDir.absolutePath}")
        // Возвращаем путь к модели для SherpaStreamFactory
        return modelPath
    }
    
    /**
     * Получить путь к модели Sherpa-ONNX
     * @return Путь к директории модели
     */
    override fun getModelPath(): String {
        val internalModelDir = File(context.filesDir, "sherpa/asr-ru-model")

        if (internalModelDir.exists() && internalModelDir.isDirectory) {
            Log.i(TAG, "Using Sherpa model from internal storage: ${internalModelDir.absolutePath}")
            return internalModelDir.absolutePath
        }

        Log.w(TAG, "Sherpa model not found at: ${internalModelDir.absolutePath}")
        return ""
    }
}
