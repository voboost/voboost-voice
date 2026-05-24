package ru.voboost.voice.engine.vosk

import android.util.Log
import org.vosk.Model
import ru.voboost.voice.config.ExternalStoragePaths
import java.io.File

class VoskModelLoader {

    companion object {
        const val TAG = "VoskModelLoader"
    }

    /**
     * Загрузить модель Vosk из указанного пути
     * @param modelPath Путь к директории модели
     * @return Model объект Vosk
     */
    fun loadModel(): Any {
        val modelPath = getModelPath()
        val modelDir = File(modelPath)
        return Model(modelDir.absolutePath)
    }

    /**
     * Получить путь к модели Vosk из внешнего хранилища
     * @return Путь к директории модели
     */
    private fun getModelPath(): String {
        val externalModelDir = ExternalStoragePaths.voskModelDir

        if (externalModelDir.exists() && externalModelDir.isDirectory) {
            Log.i(TAG, "Using Vosk model from external storage: ${externalModelDir.absolutePath}")
            return externalModelDir.absolutePath
        }
        Log.w(TAG, "Vosk model not found at: ${externalModelDir.absolutePath}")
        return ""
    }
}


