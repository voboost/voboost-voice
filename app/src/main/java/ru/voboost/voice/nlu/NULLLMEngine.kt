package ru.voboost.voice.nlu

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import ru.voboost.voice.R
import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.config.ExternalStoragePaths
import ru.voboost.voice.executor.CommandData

/**
 * LLM-based NLU Engine (MediaPipe) — ТОЛЬКО парсинг через модель.
 *
 * Контракт:
 * - Если команда распознана → возвращает RecognizedCommand
 * - Если не распознана / ошибка / невалидный JSON → возвращает null
 * - Не делает фоллбэк, не знает о regex-парсере
 */
class NULLLMEngine(private val context: Context, private val configManager: ConfigManager) :
        INLUEngine {

    companion object {
        const val TAG = "LLMNLUEngine"
        private const val MAX_TOKENS = 512

    }


    private val llmInference: LlmInference
    private val gson = Gson()

    // Загружаем промпт из res/raw/nlu_system_prompt.txt
    private val systemPrompt: String by lazy {
        try {
            context.resources.openRawResource(R.raw.nlu_system_prompt)
                .bufferedReader()
                .use { it.readText() }
                .trim()
        }
        catch (e: Exception) {
            Log.e(TAG,
                  "Failed to load system prompt",
                  e) // Fallback-промт на случай ошибки загрузки
            """Ты — голосовой ассистент в автомобиле. Распознай команду и верни строго JSON: {"id": "command_id", "params": {...}}. Доступные команды: ${configManager.getConfig().commands.joinToString { it.id }}""".trimIndent()
        }
    }

    init {
        Log.i(TAG, "Initializing LLMNLUEngine")
        val modelFile = ExternalStoragePaths.llmModelFile
        if (!modelFile.exists()) {
            throw IllegalStateException("LLM model not found: ${modelFile.absolutePath}")
        }

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(MAX_TOKENS)
            //.setTemperature(0f)
            .setMaxTopK(40)
            //.setPreferredBackend(LlmInference.Backend.GPU)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
        Log.i(TAG, "✓ LLMNLUEngine initialized")
    }

    /**
     * Парсит команду через LLM.
     * @return RecognizedCommand если успешно, null если не распознано или ошибка
     */
    override fun parseCommand(text: String): CommandData? {
        return try {
            val json = generateJsonResponse(text)

            // Проверяем, что ответ похож на валидный объект
            if (!json.startsWith("{") || !json.endsWith("}")) {
                Log.w(TAG, "Invalid JSON structure: $json")
                return null
            }

            val output = gson.fromJson(json, LLMOutput::class.java)

            if (output?.isValid() == true && output.id != "unknown") {
                buildRecognizedCommand(output, text)
            }
            else {
                Log.d(TAG, "LLM returned unknown/invalid for: '$text'")
                null
            }
        }
        catch (e: Exception) {
            Log.w(TAG, "LLM parsing failed for: '$text'", e)
            null // Честно возвращаем null, пусть решает вышестоящий слой
        }
    }

    private fun generateJsonResponse(userText: String): String {
        val prompt = buildPrompt(userText)
        val raw = llmInference.generateResponse(prompt)
        Log.w(TAG, "LLM responce raw: $raw")
        return cleanJsonResponse(raw)
    }

    private fun buildPrompt(userText: String): String {
        return buildString {
            append(systemPrompt)
            append("\n\nПользователь сказал: \"")
            append(userText)
            append("\"\nJSON ответ:")
        }
    }

    private fun cleanJsonResponse(raw: String): String { // 1. Убираем markdown-обёртки
        var cleaned = raw.trim().replace(Regex("```(?:json)?\\s*|```"), "").trim()

        // 2. Ищем первую '{'
        val start = cleaned.indexOf('{')
        if (start == -1) {
            Log.w(TAG, "No JSON object found in: $raw")
            return "{}"
        }

        // 3. Считаем глубину вложенности для правильной закрывающей скобки
        var depth = 0
        for (i in start until cleaned.length) {
            when (cleaned[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return cleaned.substring(start, i + 1)
                    }
                }
            }
        }

        // 4. Если не нашли парную скобку — логируем и возвращаем что есть
        Log.w(TAG, "Unmatched braces in LLM response: $raw")
        return cleaned.substring(start)
    }

    private fun buildRecognizedCommand(llm: LLMOutput, originalText: String): CommandData? {
        val config = configManager.getConfig().commands.find { it.id == llm.id } ?: run {
            Log.w(TAG, "Config not found for id: '${llm.id}'")
            return null
        }
        return CommandData(id = llm.id,
                           config = config,
                           matchedPattern = "llm:${llm.id}",
                           extractedParams = llm.params.mapValues { it.value.toString() })
    }

    // === Подтверждения — только быстрые ключевые слова, БЕЗ LLM ===

    override fun isConfirmationYes(text: String, commandConfig: CommandConfig): Boolean {
        val normalized = text.lowercase().trim()
        val patterns = commandConfig.confirmation.yesPatterns ?: emptyList()
        return (patterns + INLUEngine.DEFAULT_YES).any { normalized == it.lowercase().trim() }
    }

    override fun isConfirmationNo(text: String, commandConfig: CommandConfig): Boolean {
        val normalized = text.lowercase().trim()
        val patterns = commandConfig.confirmation.noPatterns ?: emptyList()
        return (patterns + INLUEngine.DEFAULT_NO).any { normalized == it.lowercase().trim() }
    }

    override fun requiresConfirmation(commandConfig: CommandConfig): Boolean {
        return commandConfig.confirmation.required
    }

    override fun getConfirmationQuestion(commandConfig: CommandConfig): String {
        return commandConfig.confirmation.question ?: "Подтверждаете?"
    }

    override fun getConfirmationTimeout(commandConfig: CommandConfig): Int {
        return commandConfig.confirmation.timeoutSec ?: 5
    }

    private data class LLMOutput(val id: String = "", val params: Map<String, Any> = emptyMap()) {
        fun isValid(): Boolean = id.isNotEmpty()
    }

    /**
     * Освободить ресурсы (реализация интерфейса INLUEngine)
     */
    override fun release() {
        try {
            llmInference.close()
        }
        catch (e: Exception) {
            Log.e(TAG, "Release error", e)
        }
    }
}

