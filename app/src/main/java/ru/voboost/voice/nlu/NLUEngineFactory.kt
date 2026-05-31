package ru.voboost.voice.nlu

import android.content.Context
import android.util.Log
import ru.voboost.voice.config.ConfigManager

object NLUEngineFactory {

    const val TAG = "NLUEngineFactory"

    enum class NLUEngineType {
        PARSER, ONNX
    }

    fun create(context: Context, configManager: ConfigManager): INLUEngine {
        val nluConfig = configManager.getConfig().nlu
        val nluEngineType = when (nluConfig.engine.lowercase()) {
            "parser" -> {
                Log.i(TAG, "NLU engine from config: PARSER")
                NLUEngineType.PARSER
            }

            "onnx" -> {
                Log.i(TAG, "NLU engine from config: ONNX")
                NLUEngineType.ONNX
            }

            else -> {
                Log.i(TAG, "NLU engine from config: LLM")
                NLUEngineType.PARSER
            }
        }

        return when (nluEngineType) {
            NLUEngineType.PARSER -> NLUParserEngine(configManager)
            NLUEngineType.ONNX -> NLUOrtEngine(configManager)
        }
    }
}

