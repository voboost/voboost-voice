package ru.voboost.voiceassistant.engine.vosk

import android.util.Log
import org.vosk.Model
import ru.voboost.voiceassistant.config.ExternalStoragePaths
import ru.voboost.voiceassistant.speech.IModelLoader
import java.io.File

public class VoskModelLoader() : IModelLoader {

    companion object {
        const val TAG = "VoskModelLoader"
    }

    /**
     * Загрузить модель Vosk из указанного пути
     * @param modelPath Путь к директории модели
     * @return Model объект Vosk
     */
    override fun loadModel(): Any {
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
