package ru.voboost.voice.nlu

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import ru.voboost.voice.R
import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.config.ExternalStoragePaths

/**
 * LLM-based NLU Engine (MediaPipe) тАФ ╨в╨Ю╨Ы╨м╨Ъ╨Ю ╨┐╨░╤А╤Б╨╕╨╜╨│ ╤З╨╡╤А╨╡╨╖ ╨╝╨╛╨┤╨╡╨╗╤М.
 *
 * ╨Ъ╨╛╨╜╤В╤А╨░╨║╤В:
 * - ╨Х╤Б╨╗╨╕ ╨║╨╛╨╝╨░╨╜╨┤╨░ ╤А╨░╤Б╨┐╨╛╨╖╨╜╨░╨╜╨░ тЖТ ╨▓╨╛╨╖╨▓╤А╨░╤Й╨░╨╡╤В RecognizedCommand
 * - ╨Х╤Б╨╗╨╕ ╨╜╨╡ ╤А╨░╤Б╨┐╨╛╨╖╨╜╨░╨╜╨░ / ╨╛╤И╨╕╨▒╨║╨░ / ╨╜╨╡╨▓╨░╨╗╨╕╨┤╨╜╤Л╨╣ JSON тЖТ ╨▓╨╛╨╖╨▓╤А╨░╤Й╨░╨╡╤В null
 * - ╨Э╨╡ ╨┤╨╡╨╗╨░╨╡╤В ╤Д╨╛╨╗╨╗╨▒╤Н╨║, ╨╜╨╡ ╨╖╨╜╨░╨╡╤В ╨╛ regex-╨┐╨░╤А╤Б╨╡╤А╨╡
 */
class NULLLMEngine(private val context: Context, private val configManager: ConfigManager) :
        INLUEngine {

    companion object {
        const val TAG = "LLMNLUEngine"
        private const val MAX_TOKENS = 512

    }


    private val llmInference: LlmInference
    private val gson = Gson()

    // ╨Ч╨░╨│╤А╤Г╨╢╨░╨╡╨╝ ╨┐╤А╨╛╨╝╨┐╤В ╨╕╨╖ res/raw/nlu_system_prompt.txt
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
                  e) // Fallback-╨┐╤А╨╛╨╝╤В ╨╜╨░ ╤Б╨╗╤Г╤З╨░╨╣ ╨╛╤И╨╕╨▒╨║╨╕ ╨╖╨░╨│╤А╤Г╨╖╨║╨╕
            """╨в╤Л тАФ ╨│╨╛╨╗╨╛╤Б╨╛╨▓╨╛╨╣ ╨░╤Б╤Б╨╕╤Б╤В╨╡╨╜╤В ╨▓ ╨░╨▓╤В╨╛╨╝╨╛╨▒╨╕╨╗╨╡. ╨а╨░╤Б╨┐╨╛╨╖╨╜╨░╨╣ ╨║╨╛╨╝╨░╨╜╨┤╤Г ╨╕ ╨▓╨╡╤А╨╜╨╕ ╤Б╤В╤А╨╛╨│╨╛ JSON: {"id": "command_id", "params": {...}}. ╨Ф╨╛╤Б╤В╤Г╨┐╨╜╤Л╨╡ ╨║╨╛╨╝╨░╨╜╨┤╤Л: ${configManager.getConfig().commands.joinToString { it.id }}""".trimIndent()
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
        Log.i(TAG, "тЬУ LLMNLUEngine initialized")
    }

    /**
     * ╨Я╨░╤А╤Б╨╕╤В ╨║╨╛╨╝╨░╨╜╨┤╤Г ╤З╨╡╤А╨╡╨╖ LLM.
     * @return RecognizedCommand ╨╡╤Б╨╗╨╕ ╤Г╤Б╨┐╨╡╤И╨╜╨╛, null ╨╡╤Б╨╗╨╕ ╨╜╨╡ ╤А╨░╤Б╨┐╨╛╨╖╨╜╨░╨╜╨╛ ╨╕╨╗╨╕ ╨╛╤И╨╕╨▒╨║╨░
     */
    override fun parseCommand(text: String): RecognizedCommand? {
        return try {
            val json = generateJsonResponse(text)

            // ╨Я╤А╨╛╨▓╨╡╤А╤П╨╡╨╝, ╤З╤В╨╛ ╨╛╤В╨▓╨╡╤В ╨┐╨╛╤Е╨╛╨╢ ╨╜╨░ ╨▓╨░╨╗╨╕╨┤╨╜╤Л╨╣ ╨╛╨▒╤К╨╡╨║╤В
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
            null // ╨з╨╡╤Б╤В╨╜╨╛ ╨▓╨╛╨╖╨▓╤А╨░╤Й╨░╨╡╨╝ null, ╨┐╤Г╤Б╤В╤М ╤А╨╡╤И╨░╨╡╤В ╨▓╤Л╤И╨╡╤Б╤В╨╛╤П╤Й╨╕╨╣ ╤Б╨╗╨╛╨╣
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
            append("\n\n╨Я╨╛╨╗╤М╨╖╨╛╨▓╨░╤В╨╡╨╗╤М ╤Б╨║╨░╨╖╨░╨╗: \"")
            append(userText)
            append("\"\nJSON ╨╛╤В╨▓╨╡╤В:")
        }
    }

    private fun cleanJsonResponse(raw: String): String { // 1. ╨г╨▒╨╕╤А╨░╨╡╨╝ markdown-╨╛╨▒╤С╤А╤В╨║╨╕
        var cleaned = raw.trim().replace(Regex("```(?:json)?\\s*|```"), "").trim()

        // 2. ╨Ш╤Й╨╡╨╝ ╨┐╨╡╤А╨▓╤Г╤О '{'
        val start = cleaned.indexOf('{')
        if (start == -1) {
            Log.w(TAG, "No JSON object found in: $raw")
            return "{}"
        }

        // 3. ╨б╤З╨╕╤В╨░╨╡╨╝ ╨│╨╗╤Г╨▒╨╕╨╜╤Г ╨▓╨╗╨╛╨╢╨╡╨╜╨╜╨╛╤Б╤В╨╕ ╨┤╨╗╤П ╨┐╤А╨░╨▓╨╕╨╗╤М╨╜╨╛╨╣ ╨╖╨░╨║╤А╤Л╨▓╨░╤О╤Й╨╡╨╣ ╤Б╨║╨╛╨▒╨║╨╕
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

        // 4. ╨Х╤Б╨╗╨╕ ╨╜╨╡ ╨╜╨░╤И╨╗╨╕ ╨┐╨░╤А╨╜╤Г╤О ╤Б╨║╨╛╨▒╨║╤Г тАФ ╨╗╨╛╨│╨╕╤А╤Г╨╡╨╝ ╨╕ ╨▓╨╛╨╖╨▓╤А╨░╤Й╨░╨╡╨╝ ╤З╤В╨╛ ╨╡╤Б╤В╤М
        Log.w(TAG, "Unmatched braces in LLM response: $raw")
        return cleaned.substring(start)
    }

    private fun buildRecognizedCommand(llm: LLMOutput, originalText: String): RecognizedCommand? {
        val config = configManager.getConfig().commands.find { it.id == llm.id } ?: run {
            Log.w(TAG, "Config not found for id: '${llm.id}'")
            return null
        }
        return RecognizedCommand(id = llm.id,
                                 config = config,
                                 matchedPattern = "llm:${llm.id}",
                                 extractedParams = llm.params.mapValues { it.value.toString() })
    }

    // === ╨Я╨╛╨┤╤В╨▓╨╡╤А╨╢╨┤╨╡╨╜╨╕╤П тАФ ╤В╨╛╨╗╤М╨║╨╛ ╨▒╤Л╤Б╤В╤А╤Л╨╡ ╨║╨╗╤О╤З╨╡╨▓╤Л╨╡ ╤Б╨╗╨╛╨▓╨░, ╨С╨Х╨Ч LLM ===

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
        return commandConfig.confirmation.question ?: "╨Я╨╛╨┤╤В╨▓╨╡╤А╨╢╨┤╨░╨╡╤В╨╡?"
    }

    override fun getConfirmationTimeout(commandConfig: CommandConfig): Int {
        return commandConfig.confirmation.timeoutSec ?: 5
    }

    private data class LLMOutput(val id: String = "", val params: Map<String, Any> = emptyMap()) {
        fun isValid(): Boolean = id.isNotEmpty()
    }

    /**
     * ╨Ю╤Б╨▓╨╛╨▒╨╛╨┤╨╕╤В╤М ╤А╨╡╤Б╤Г╤А╤Б╤Л (╤А╨╡╨░╨╗╨╕╨╖╨░╤Ж╨╕╤П ╨╕╨╜╤В╨╡╤А╤Д╨╡╨╣╤Б╨░ INLUEngine)
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

