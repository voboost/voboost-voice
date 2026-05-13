package ru.voboost.voiceassistant.nlu

import NLUOrtEngine
import android.content.Context
import android.util.Log
import ru.voboost.voiceassistant.config.ConfigManager

object NLUEngineFactory {

    const val TAG = "NLUEngineFactory"

    enum class NLUEngineType {
        PARSER,
        LLM,
        ONNX
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
                 NLUEngineType.LLM
             }
         }

        return when (nluEngineType) {
             NLUEngineType.PARSER -> NLUEngine(configManager)
            NLUEngineType.LLM -> NULLLMEngine(context, configManager)
             NLUEngineType.ONNX -> NLUOrtEngine(context, configManager)
         }
    }
}