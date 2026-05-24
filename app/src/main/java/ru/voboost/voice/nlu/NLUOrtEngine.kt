package ru.voboost.voice.nlu

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import android.util.Log
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ru.voboost.voice.config.CommandConfig
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.config.ExternalStoragePaths
import ru.voboost.voice.executor.CommandData
import java.io.FileNotFoundException
import java.nio.LongBuffer
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.sqrt

class NLUOrtEngine(private val configManager: ConfigManager) : INLUEngine {

    companion object {
        private const val TAG = "OnnxNluEngine"
        private const val SIMILARITY_THRESHOLD = 0.62f // Порог срабатывания
        private const val EMBEDDING_DIM = 384 // Размерность MiniLM-L12
    }

    private val ortEnv = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val tokenizer: HuggingFaceTokenizer
    private val lock = ReentrantLock()
    private val commandPatterns = mutableMapOf<String, List<Pair<String, FloatArray>>>()

    init {
        Log.i(TAG, "Initializing ONNX NLU Engine...")

        // Загрузка модели из внешнего хранилища
        val modelFile = ExternalStoragePaths.nluModelFile
        if (!modelFile.exists()) {
            throw FileNotFoundException("NLU model not found: ${modelFile.absolutePath}")
        }
        val modelBytes = modelFile.readBytes()
        session = ortEnv.createSession(modelBytes, OrtSession.SessionOptions())

        // Загрузка токенайзера из внешнего хранилища
        val tokenizerFile = ExternalStoragePaths.tokenizerFile
        if (!tokenizerFile.exists()) {
            throw FileNotFoundException("Tokenizer not found: ${tokenizerFile.absolutePath}")
        }
        val tokenizerStream = tokenizerFile.inputStream() //        System.setProperty("ai.djl.huggingface.tokenizers.skip_init", "true") //        System.loadLibrary("tokenizers")
        this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerStream, mapOf())
        tokenizerStream.close()

        precomputeCommandEmbeddings()
        Log.i(TAG, "✅ ONNX NLU initialized. Commands indexed: ${commandPatterns.size}")
    }

    private fun precomputeCommandEmbeddings() {
        val commands = configManager.getConfig().commands.filter { it.enabled }
        for (cmd in commands) {
            val embeddings = cmd.patterns.mapNotNull { pattern ->
                try {
                    val cleanPattern = pattern.replace(Regex("\\{[^}]+\\}"), "").trim()
                    if (cleanPattern.isEmpty()) null
                    else Pair(pattern, embedText(cleanPattern))
                }
                catch (e: Exception) {
                    Log.w(TAG, "Failed to embed pattern: $pattern", e)
                    null
                }
            }
            if (embeddings.isNotEmpty()) {
                commandPatterns[cmd.id] = embeddings
            }
        }
    }

    override fun parseCommand(text: String): CommandData? {
        return try {
            val queryEmbedding = embedText(text)
            var bestId: String? = null
            var bestScore = 0f
            var bestPattern: String? = null

            // Поиск максимального косинусного сходства
            for ((cmdId, patterns) in commandPatterns) {
                for ((pattern, cmdEmbedding) in patterns) {
                    val score = cosineSimilarity(queryEmbedding, cmdEmbedding)
                    if (score > bestScore) {
                        bestScore = score
                        bestId = cmdId
                        bestPattern = pattern
                    }
                }
            }

            if (bestScore >= SIMILARITY_THRESHOLD && bestId != null) {
                Log.d(TAG,
                      "✅ Matched: '$text' → $bestId (score: ${String.format("%.3f", bestScore)})")
                val params = extractParams(bestPattern!!, text)
                buildRecognizedCommand(bestId, params)
            }
            else {
                Log.d(TAG, "❌ No match for: '$text' (best: ${String.format("%.3f", bestScore)})")
                null
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "NLU inference failed", e)
            null
        }
    }

    private fun embedText(text: String): FloatArray {
        lock.lock()
        return try {
            val encoding = tokenizer.encode(text)

            // Создаём тензоры
            val inputIds = encoding.ids.map { it }.toLongArray()
            val attentionMask = encoding.attentionMask.map { it }.toLongArray()
            val tokenTypeIds = encoding.typeIds.map { it }.toLongArray()

            val inputIdsTensor = OnnxTensor.createTensor(ortEnv,
                                                         LongBuffer.wrap(inputIds),
                                                         longArrayOf(1, inputIds.size.toLong()))
            val attentionMaskTensor = OnnxTensor.createTensor(ortEnv,
                                                              LongBuffer.wrap(attentionMask),
                                                              longArrayOf(1,
                                                                          attentionMask.size.toLong()))
            val tokenTypeIdsTensor = OnnxTensor.createTensor(ortEnv,
                                                             LongBuffer.wrap(tokenTypeIds),
                                                             longArrayOf(1,
                                                                         tokenTypeIds.size.toLong()))

            val inputs = mapOf("input_ids" to inputIdsTensor,
                               "attention_mask" to attentionMaskTensor,
                               "token_type_ids" to tokenTypeIdsTensor)

            var result: OrtSession.Result? = null
            var outputTensor: OnnxTensor? = null

            try {
                result = session.run(inputs)
                outputTensor = result.get(0) as OnnxTensor

                require(outputTensor.info.type == OnnxJavaType.FLOAT) { "Expected FLOAT output" }

                val shape = outputTensor.info.shape
                val embedding = FloatArray(EMBEDDING_DIM)

                // Вариант 1: Если модель выдает 3D-тензор [1, sequence_length, hidden_size] (Большинство BERT/MiniLM)
                if (shape.size == 3) {
                    require(shape[2] == EMBEDDING_DIM.toLong()) {
                        "Expected embedding dim $EMBEDDING_DIM, got ${shape[2]}"
                    }

                    // Извлекаем данные как трехмерный массив Float
                    val outputs = outputTensor.value as Array<Array<FloatArray>>
                    val tokenEmbeddings = outputs[0] // Массив векторов для каждого токена [sequence_length][EMBEDDING_DIM]

                    // Делаем Mean Pooling (усредняем только значащие токены по attention_mask)
                    var validTokensCount = 0
                    for (i in tokenEmbeddings.indices) {
                        if (i < attentionMask.size && attentionMask[i] == 1L) {
                            validTokensCount++
                            for (dim in 0 until EMBEDDING_DIM) {
                                embedding[dim] += tokenEmbeddings[i][dim]
                            }
                        }
                    }

                    // Делим сумму на количество значащих токенов
                    if (validTokensCount > 0) {
                        for (dim in 0 until EMBEDDING_DIM) {
                            embedding[dim] /= validTokensCount.toFloat()
                        }
                    }
                } // Вариант 2: Если модель уже имеет встроенный пулинг и сразу выдает 2D [1, EMBEDDING_DIM]
                else if (shape.size == 2) {
                    require(shape[1] == EMBEDDING_DIM.toLong()) {
                        "Expected embedding dim $EMBEDDING_DIM, got ${shape[1]}"
                    }
                    val buffer = outputTensor.floatBuffer
                    buffer.get(embedding)
                }
                else {
                    throw IllegalArgumentException("Unexpected output tensor shape: ${shape.joinToString()}")
                }

                normalize(embedding)
                embedding
            }
            finally {
                outputTensor?.close()
                result?.close()
                inputIdsTensor.close()
                attentionMaskTensor.close()
                tokenTypeIdsTensor.close()
            }
        }
        finally {
            lock.unlock()
        }
    }

    private fun normalize(v: FloatArray) {
        val norm = sqrt(v.sumOf { (it * it).toDouble() }).toFloat()
        if (norm > 0) {
            for (i in v.indices) v[i] /= norm
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        for (i in a.indices) dot += a[i] * b[i]
        return dot // Вектора уже нормализованы, поэтому dot product == cosine similarity
    }

    private fun extractParams(pattern: String,
                              text: String): Map<String, String> { // 1. Извлекаем имена параметров
        val paramNames = "\\{(\\w+)\\}".toRegex()
            .findAll(pattern)
            .map { it.groupValues[1] }
            .toList()

        // 2. Экранируем ВСЕ спецсимволы regex, кроме наших {параметров}
        var regexPattern = Regex.escape(pattern) // экранируем всё
        for (param in paramNames) { // Восстанавливаем наши группы: \{param\} → (?<param>[^}]+)
            regexPattern = regexPattern.replace("\\\\{\\Q$param\\E\\\\}", "(?<${param}>[^}]+)")
        }

        val regex = regexPattern.toRegex()
        val match = regex.find(text) ?: return emptyMap()

        return paramNames.mapNotNull { name ->
            match.groups[name]?.value?.let { name to it.trim() }
        }.toMap()
    }

    private fun buildRecognizedCommand(id: String, params: Map<String, String>): CommandData? {
        val config = configManager.getConfig().commands.find { it.id == id } ?: return null
        return CommandData(id = id,
                           config = config,
                           matchedPattern = "onnx:${id}",
                           extractedParams = params)
    }

    // === Интерфейс подтверждения (без изменений) ===
    override fun isConfirmationYes(text: String, commandConfig: CommandConfig): Boolean {
        return ((commandConfig.confirmation.yesPatterns
                 ?: emptyList()) + INLUEngine.DEFAULT_YES).any {
            text.lowercase().trim() == it.lowercase().trim()
        }
    }

    override fun isConfirmationNo(text: String, commandConfig: CommandConfig): Boolean {
        return ((commandConfig.confirmation.noPatterns
                 ?: emptyList()) + INLUEngine.DEFAULT_NO).any {
            text.lowercase().trim() == it.lowercase().trim()
        }
    }

    override fun requiresConfirmation(commandConfig: CommandConfig) = commandConfig.confirmation.required
    override fun getConfirmationQuestion(commandConfig: CommandConfig) = commandConfig.confirmation.question
                                                                         ?: "Подтверждаете?"

    override fun getConfirmationTimeout(commandConfig: CommandConfig) = commandConfig.confirmation.timeoutSec
                                                                        ?: 5

    /**
     * Освободить ресурсы (реализация интерфейса INLUEngine)
     */
    override fun release() {
        try {
            session.close()
            tokenizer.close()
        }
        catch (e: Exception) {
            Log.e(TAG, "Release error", e)
        }
    }
}

