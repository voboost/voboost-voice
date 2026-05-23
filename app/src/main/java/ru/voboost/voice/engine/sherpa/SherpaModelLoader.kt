package ru.voboost.voice.engine.sherpa

import android.util.Log
import ru.voboost.voice.config.ExternalStoragePaths

/**
 * Загрузчик моделей Sherpa-ONNX
 * Реализует универсальный интерфейс IModelLoader
 *
 * Модель загружается из внешнего хранилища:
 * /storage/emulated/0/voboost/models/sherpa/asr-ru-model/
 */
class SherpaModelLoader {

    companion object {
        const val TAG = "SherpaModelLoader"
    }

    /**
     * Загрузить модель Sherpa-ONNX из указанного пути
     * @param modelPath Путь к директории модели
     * @return Путь к модели (String)
     */
    fun loadModel(): Any {
        return getModelPath()
    }

    /**
     * Получить путь к модели Sherpa-ONNX из внешнего хранилища
     * @return Путь к директории модели
     */
    private fun getModelPath(): String {
        val externalModelDir = ExternalStoragePaths.sherpaAsrModelDir

        if (externalModelDir.exists() && externalModelDir.isDirectory) {
            Log.i(TAG, "Using Sherpa model from external storage: ${externalModelDir.absolutePath}")
            return externalModelDir.absolutePath
        }

        Log.w(TAG, "Sherpa model not found at: ${externalModelDir.absolutePath}")
        return ""
    }
}


