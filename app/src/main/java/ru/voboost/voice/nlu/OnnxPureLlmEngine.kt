//package ru.voboost.voice.nlu
//
//import ai.onnxruntime.genai.Model
//import ai.onnxruntime.genai.Tokenizer
//import ai.onnxruntime.genai.GeneratorParams
//import ai.onnxruntime.genai.Generator
//import android.util.Log
//import ru.voboost.voice.config.CommandConfig
//import ru.voboost.voice.config.ConfigManager
//import ru.voboost.voice.executor.CommandData
//import java.io.File
//
//class OnnxPureLlmEngine(private val configManager: ConfigManager,
//                        modelDirPath: String )
//    : INLUEngine {
//
//    companion object {
//        private const val TAG = "OnnxGenAiEngine"
//
//        // Промпт с /no_think для отключения thinking mode
//        private const val SYSTEM_PROMPT =
//                "Ты NLU ассистент авто. Твоя задача — строго перевести фразу в формат: ID|параметр. " +
//                "Если параметра нет, пиши none. Не пиши JSON, вводные слова и точки.\n" +
//                "/no_think\n" +
//                "Список ID: ac_set_temp, ac_temp_up, ac_temp_down, ac_open, ac_close, " +
//                "phone_call_contact, phone_call_number, unknown.\n" +
//                "Примеры:\n\"22 градуса\"->ac_set_temp|22\n\"позвони маме\"->phone_call_contact|маме\n" +
//                "\"мне холодно\"->ac_temp_up|none"
//    }
//
//    private val model: Model
//    private val tokenizer: Tokenizer
//    private val parser = LlmResultParser(configManager)
//
//    // 🔥 ПРЕ-ТОКЕНИЗИРОВАННЫЙ системный промпт (IntArray, готов к использованию)
//    private val systemPromptTokens: IntArray
//
//    init {
//        Log.i(TAG, "Loading Quantized LLM into Microsoft C++ GenAI Engine...")
//        val dir = File(modelDirPath)
//        if (!dir.exists() || dir.listFiles().isNullOrEmpty()) {
//            Log.e(TAG, "LLM Model directory is empty or missing: $modelDirPath")
//            throw IllegalStateException("Model directory invalid: $modelDirPath")
//        }
//
//        model = Model(modelDirPath)
//        tokenizer = Tokenizer(model)
//
//        // 🔥 Пре-токенизируем системный промпт ОДИН РАЗ при старте
//        // getSequence(0) уже возвращает IntArray — никаких конвертаций!
//        val systemFormatted = "<|im_start|>system\n$SYSTEM_PROMPT<|im_end|>\n"
//        systemPromptTokens = tokenizer.encode(systemFormatted).getSequence(0)
//
//        Log.i(TAG, "✅ GenAI LLM Engine loaded! System prompt pre-tokenized (${systemPromptTokens.size} tokens).")
//    }
//
//    override fun parseCommand(text: String): CommandData? {
//        return try {
//            // Форматируем ТОЛЬКО пользовательскую часть
//            val userFormatted = "<|im_start|>user\n${text.trim()}<|im_end|>\n<|im_start|>assistant\n"
//            val userTokens = tokenizer.encode(userFormatted).getSequence(0)  // Уже IntArray!
//
//            // Склеиваем: [системный промпт] + [пользователь]
//            val fullPrompt = systemPromptTokens + userTokens
//
//            // Настраиваем параметры поиска
//            val params = GeneratorParams(model)
//
//            // 🔥 FIX: max_length передаётся как Double (Java API требует double/boolean)
//            val maxLen = (fullPrompt.size + 20).toDouble()
//            params.setSearchOption("max_length", maxLen)
//            params.setSearchOption("temperature", 0.0)
//            params.setSearchOption("do_sample", false)  // Greedy для детерминированного ответа
//
//            // Создаём генератор
//            val generator = Generator(model, params)
//
//            // Скармливаем промпт (IntArray принимается напрямую)
//            generator.appendTokens(fullPrompt)
//
//            // Запоминаем длину промпта, чтобы потом обрезать ответ
//            val promptLength = fullPrompt.size
//
//            // Генерируем
//            while (!generator.isDone) {
//                generator.generateNextToken()
//            }
//
//            // 🔥 FIX: getSequence(0) уже возвращает IntArray — никаких конвертаций!
//            val fullTokens = generator.getSequence(0)
//
//            // Берём ТОЛЬКО сгенерированные токены (после промпта)
//            val responseTokens = if (fullTokens.size > promptLength) {
//                fullPrompt.sliceArray(promptLength until fullTokens.size)
//            } else {
//                intArrayOf()  // Пустой ответ
//            }
//
//            // Освобождаем C++ ресурсы
//            generator.close()
//
//            // Декодируем ТОЛЬКО ответ
//            val rawResponse = tokenizer.decode(responseTokens).trim()
//            Log.d(TAG, "Raw LLM Response: '$rawResponse'")
//
//            // Чистим от артефактов (на всякий случай)
//            val cleanResponse = rawResponse
//                .replace(Regex("<\\|im_end\\|>"), "")
//                .replace(Regex("<\\|im_start\\|>"), "")
//                .replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "")
//                .replace(Regex("<think>.*"), "")  // Незакрытый тег
//                .trim()
//
//            Log.d(TAG, "Clean Response: '$cleanResponse'")
//
//            // Отдаём в парсер
//            parser.parse(cleanResponse, text)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "LLM GenAI Inference failed for input: '$text'", e)
//            null
//        }
//    }
//
//    override fun release() {
//        try {
//            tokenizer.close()
//            model.close()
//            Log.i(TAG, "GenAI LLM resources released safely.")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error releasing LLM", e)
//        }
//    }
//
//    // === Методы интерфейса подтверждения (без изменений) ===
//    override fun isConfirmationYes(text: String, commandConfig: CommandConfig?): Boolean {
//        return ((commandConfig?.confirmation?.yesPatterns ?: emptyList()) + INLUEngine.DEFAULT_YES)
//            .any { text.lowercase().trim() == it.lowercase().trim() }
//    }
//
//    override fun isConfirmationNo(text: String, commandConfig: CommandConfig): Boolean {
//        return ((commandConfig.confirmation.noPatterns ?: emptyList()) + INLUEngine.DEFAULT_NO)
//            .any { text.lowercase().trim() == it.lowercase().trim() }
//    }
//
//    override fun requiresConfirmation(commandConfig: CommandConfig): Boolean {
//        return commandConfig.confirmation.required
//    }
//
//    override fun getConfirmationQuestion(commandConfig: CommandConfig?): String {
//        return commandConfig?.confirmation?.question ?: "Подтверждаете?"
//    }
//
//    override fun getConfirmationTimeout(commandConfig: CommandConfig): Int {
//        return commandConfig.confirmation.timeoutSec ?: 5
//    }
//}