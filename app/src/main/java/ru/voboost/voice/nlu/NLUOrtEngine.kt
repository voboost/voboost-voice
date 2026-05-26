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
        private const val SIMILARITY_THRESHOLD = 0.55 // Порог срабатывания
        private const val MARGIN_THRESHOLD = 0.08
        private const val EMBEDDING_DIM = 384 // Размерность MiniLM-L12
    }

    private val ortEnv = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val tokenizer: HuggingFaceTokenizer
    private val lock = ReentrantLock()
    private val commandPatterns = mutableMapOf<String, List<FloatArray>>()

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
        val tokenizerStream = tokenizerFile.inputStream() //System.setProperty("ai.djl.huggingface.tokenizers.skip_init", "true") //        System.loadLibrary("tokenizers")
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
                    val normalizePattern = normalizePhrase(pattern)
                    if (normalizePattern.isEmpty()) null
                    else embedText(normalizePattern)
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
            val normalized = normalizePhrase(text)
            val queryEmbedding = embedText(normalized)

            // Собираем сырые скоры и запоминаем лучший паттерн для extractParams
            val cmdAggregatedScores = mutableMapOf<String, Double>()

            for ((cmdId, patterns) in commandPatterns) {
                val rawScores = mutableListOf<Double>()
                var bestPatScore = -1.0

                for (cmdEmbedding in patterns) {
                    val score = cosineSimilarity(queryEmbedding, cmdEmbedding)
                    rawScores.add(score)
                    if (score > bestPatScore) {
                        bestPatScore = score
                    }
                }
                // Агрегируем по Softmax
                cmdAggregatedScores[cmdId] = computeSoftmaxScore(rawScores, temperature = 5.0)
            }

            if (cmdAggregatedScores.isEmpty()) {
                Log.d(TAG, "❌ No match for: '$text'")
                return null
            }

            // Сортируем команды по агрегированному скору
            val sorted = cmdAggregatedScores.entries.sortedByDescending { it.value }
            val bestId = sorted[0].key
            val bestScore = sorted[0].value

            val secondBestScore = if (sorted.size > 1) sorted[1].value else 0.0
            val secondBestId = if (sorted.size > 1) sorted[1].key else "none"

            val margin = bestScore - secondBestScore

            if (bestScore >= SIMILARITY_THRESHOLD && margin >= MARGIN_THRESHOLD) {
                Log.d(TAG,"✅ Matched: '$text' → $bestId (agg: ${String.format("%.3f", bestScore)}, margin vs $secondBestId: ${String.format("%.3f", margin)})")
                val commandConfig = configManager.getCommandById(bestId)
                if(commandConfig == null) {
                    null
                }
                else {
                    CommandData(data = commandConfig, phrase = text)
                }
            }
            else {
                Log.d(TAG,"❌ Ambiguous/No match: '$text' (agg: $bestId=${String.format("%.3f", bestScore)}, 2nd: $secondBestId=${String.format("%.3f", secondBestScore)})")
                null
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "NLU inference failed", e)
            null
        }
    }

    /**
     * Softmax-взвешенная агрегация скоров паттернов одной команды.
     * temperature: 3.0–5.0 → мягкое усреднение, 7.0–10.0 → ближе к max
     */
    private fun computeSoftmaxScore(scores: List<Double>, temperature: Double): Double {
        if (scores.isEmpty()) return 0.0
        val maxScore = scores.maxOrNull() ?: 0.0 // Численная стабилизация: вычитаем max, чтобы exp() не уходил в Infinity
        val expScores = scores.map { kotlin.math.exp((it - maxScore) * temperature) }
        val sumExp = expScores.sum()
        if (sumExp == 0.0) return maxScore // Взвешенное среднее
        return scores.zip(expScores).sumOf { (s, w) -> s * (w / sumExp) }
    }

    private fun embedText(text: String): FloatArray {
        lock.lock()
        return try {
            val normalize = normalizePhrase(text)
            val encoding = tokenizer.encode(normalize)

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

    // Добавьте в класс NLUOrtEngine
    private fun normalizePhrase(text: String): String {
        return text.lowercase()
            .replace(Regex("\\{temp\\}"), "<NUM>")
            .replace(Regex("\\{contact\\}"), "<NAME>")
            .replace(Regex("\\{number\\}"), "<PHONE>")
            .replace(Regex("[^\\w\\s<>/\\-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        var dot = 0.0
        for (i in a.indices) dot += a[i] * b[i]
        return dot // Вектора уже нормализованы, поэтому dot product == cosine similarity
    }

    // === Интерфейс подтверждения (без изменений) ===
    override fun isConfirmationYes(text: String, commandConfig: CommandConfig?): Boolean {
        return ((commandConfig?.confirmation?.yesPatterns
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

    override fun requiresConfirmation(commandConfig: CommandConfig) =
            commandConfig.confirmation.required

    override fun getConfirmationQuestion(commandConfig: CommandConfig?) =
            commandConfig?.confirmation?.question?: "Подтверждаете?"

    override fun getConfirmationTimeout(commandConfig: CommandConfig) =
            commandConfig.confirmation.timeoutSec?: 5

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

