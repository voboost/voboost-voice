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
        private const val EMBEDDING_DIM = 384
        private const val TEMPERATURE = 3.0
        private const val ANTONYMS_PENALTY = 0.5
        private const val CONTEXT_BONUS = 0.1
        // Динамические пороги: (scoreThreshold, marginThreshold)
        private val THRESHOLDS = mapOf(1 to Pair(0.75, 0.10),   // 1 слово: высокий score, маленький margin
                                       2 to Pair(0.65, 0.10),   // 2-3 слова: средний
                                       3 to Pair(0.60, 0.08),
                                       4 to Pair(0.55, 0.05))    // 4+ слова: низкий score, маленький margin
        // Антонимы: word -> opposite
        private val ANTONYMS = mapOf("открой" to "закрой",
                                     "закрой" to "открой",
                                     "подними" to "опусти",
                                     "опусти" to "подними",
                                     "включи" to "выключи",
                                     "выключи" to "включи",
                                     "отключи" to "включи" )
    }

    private val ortEnv = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val tokenizer: HuggingFaceTokenizer
    private val lock = ReentrantLock()

    // Храним пары: (cmdId, patternText, embedding)
    private val patternData = mutableListOf<Triple<String, String, FloatArray>>()

    init {
        Log.i(TAG, "Initializing ONNX NLU Engine...")

        val modelFile = ExternalStoragePaths.nluModelFile
        if (!modelFile.exists()) {
            throw FileNotFoundException("NLU model not found: ${modelFile.absolutePath}")
        }
        val modelBytes = modelFile.readBytes()
        session = ortEnv.createSession(modelBytes, OrtSession.SessionOptions())

        val tokenizerFile = ExternalStoragePaths.tokenizerFile
        if (!tokenizerFile.exists()) {
            throw FileNotFoundException("Tokenizer not found: ${tokenizerFile.absolutePath}")
        }
        val tokenizerStream = tokenizerFile.inputStream()
        this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerStream, mapOf())
        tokenizerStream.close()

        precomputeCommandEmbeddings()
        Log.i(TAG, "✅ ONNX NLU initialized. Commands indexed: ${
            patternData.map { it.first }.toSet().size}")
    }

    private fun precomputeCommandEmbeddings() {
        val commands = configManager.getConfig().commands.filter { it.enabled }
        for (cmd in commands) {
            for (pattern in cmd.patterns) {
                try {
                    val normalized = normalizeTemplatePattern(pattern)
                    if (normalized.isEmpty()) continue

                    val embedding = embedText(normalized)
                    patternData.add(Triple(cmd.id, normalized, embedding))
                }
                catch (e: Exception) {
                    Log.w(TAG, "Failed to embed pattern: $pattern", e)
                }
            }
        }
    }

    override fun parseCommand(text: String,
                              contextCmd: List<String>)
    : CommandData? {

        return try {
            val normalized = normalizeUserPhrase(text)

            for ((cmdId, patternText, _) in patternData) {
                // Проверяем exact match
                if (patternText == normalized) {
                    val commandConfig = configManager.getCommandById(cmdId)
                    return commandConfig?.let { CommandData(data = it, phrase = text) }
                }
            }

            val queryEmbedding = embedText(normalized)
            val wordCount = normalized.split(' ').size

            // Динамический порог
            val (threshold, marginThreshold) = getThresholds(wordCount)

            // Сравниваем со всеми паттернами
            val allMatches = mutableListOf<PatternMatch>()

            for ((cmdId, patternText, patternEmbedding) in patternData) {

                var score = cosineSimilarity(queryEmbedding, patternEmbedding)
                // Штраф за антонимы
                if (hasAntonymConflict(normalized, patternText)) {
                    score *= ANTONYMS_PENALTY
                }

                allMatches.add(PatternMatch(cmdId, patternText, score))
            }

            // Агрегация по командам
            val cmdScores = aggregateByCommand(allMatches, wordCount)

            if (cmdScores.isEmpty()) {
                Log.d(TAG, "❌ No match for: '$text'")
                return null
            }

            // === МЯГКИЙ КОНТЕКСТ: бонус для команд из вопроса ===
            val adjustedScores = cmdScores.mapValues { (cmdId, score) ->
                if (cmdId in contextCmd) score + CONTEXT_BONUS else score
            }

            val sorted = adjustedScores.entries.sortedByDescending { it.value }
            val bestId = sorted[0].key
            val bestScore = sorted[0].value
            val secondId = if (sorted.size > 1) sorted[1].key else "none"
            val secondScore = if (sorted.size > 1) sorted[1].value else 0.0
            val margin = bestScore - secondScore

            // Для 1 слова без exact match — проверяем глобальную неоднозначность
            var ambiguous = false
            if (wordCount == 1) {
                val top2Global = allMatches.sortedByDescending { it.score }.take(2)
                if (top2Global.size > 1) {
                    val globalMargin = top2Global[0].score - top2Global[1].score
                    if (globalMargin < marginThreshold) {
                        ambiguous = true
                    }
                }
            }

            val matched = (bestScore >= threshold && margin >= marginThreshold && !ambiguous)

            if (matched) {
                Log.d(TAG,
                      "✅ MATCH: '$text' → $bestId (score: ${"%.3f".format(bestScore)}, margin vs $secondId: ${
                          "%.3f".format(margin)
                      })")
                val commandConfig = configManager.getCommandById(bestId)
                commandConfig?.let { CommandData(data = it, phrase = text) }
            }
            else if (bestScore >= threshold && margin < marginThreshold && secondScore >= threshold) { // Неоднозначность — нужно уточнение
                CommandData(data = CommandConfig.Empty, phrase = text, listOf(bestId, secondId))
            }
            else {
                Log.d(TAG,
                      "❌ REJECT: '$text' (best: $bestId=${"%.3f".format(bestScore)}, 2nd: $secondId=${
                          "%.3f".format(secondScore)
                      }, margin=${"%.3f".format(margin)}, threshold=$threshold)")
                null
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "NLU inference failed", e)
            null
        }
    }

    private fun aggregateByCommand(matches: List<PatternMatch>,
                                   wordCount: Int): Map<String, Double> { // Группируем по командам
        val cmdPatterns = mutableMapOf<String, MutableList<Double>>()
        for (match in matches) {
            cmdPatterns.getOrPut(match.cmdId) { mutableListOf() }.add(match.score)
        }

        val result = mutableMapOf<String, Double>()
        for ((cmdId, scores) in cmdPatterns) {
            result[cmdId] = when {
                wordCount == 1 -> { // Exact: просто максимум
                    scores.maxOrNull() ?: 0.0
                }

                wordCount in 2..3 -> { // Mean top-3
                    val top3 = scores.sortedDescending().take(3)
                    top3.sum() / top3.size
                }

                else -> { // Softmax с temperature
                    computeSoftmaxScore(scores)
                }
            }
        }
        return result
    }

    private fun computeSoftmaxScore(scores: List<Double>): Double {
        if (scores.isEmpty()) return 0.0
        val maxScore = scores.maxOrNull() ?: 0.0
        val expScores = scores.map { kotlin.math.exp((it - maxScore) * TEMPERATURE) }
        val sumExp = expScores.sum()
        if (sumExp == 0.0) return maxScore
        return scores.zip(expScores).sumOf { (s, w) -> s * (w / sumExp) }
    }

    private data class PatternMatch(val cmdId: String, val pattern: String, val score: Double)

    private fun getThresholds(wordCount: Int): Pair<Double, Double> {
        val key = if (wordCount > 4) 4 else wordCount
        return THRESHOLDS[key] ?: THRESHOLDS[4]!!
    }

    private fun hasAntonymConflict(query: String, pattern: String): Boolean {
        val qWords = query.split(' ').toSet()
        val pWords = pattern.split(' ').toSet()

        for ((word, opposite) in ANTONYMS) {
            if (qWords.contains(word) && pWords.contains(opposite)) return true
            if (qWords.contains(opposite) && pWords.contains(word)) return true
        }
        return false
    }

    private fun embedText(text: String): FloatArray {
        lock.lock()
        return try {
            val encoding = tokenizer.encode(text)

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

                if (shape.size == 3) {
                    require(shape[2] == EMBEDDING_DIM.toLong()) {
                        "Expected embedding dim $EMBEDDING_DIM, got ${shape[2]}"
                    }

                    val outputs = outputTensor.value as Array<Array<FloatArray>>
                    val tokenEmbeddings = outputs[0]

                    var validTokensCount = 0
                    for (i in tokenEmbeddings.indices) {
                        if (i < attentionMask.size && attentionMask[i] == 1L) {
                            validTokensCount++
                            for (dim in 0 until EMBEDDING_DIM) {
                                embedding[dim] += tokenEmbeddings[i][dim]
                            }
                        }
                    }

                    if (validTokensCount > 0) {
                        for (dim in 0 until EMBEDDING_DIM) {
                            embedding[dim] /= validTokensCount.toFloat()
                        }
                    }
                }
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

    private fun normalizeTemplatePattern(pattern: String): String {
        val replaced = pattern.lowercase()
            .replace("{temp}", "двадцать два")
            .replace("{contact}", "иван")
            .replace("{number}", "одиннадцать")
        return cleanText(replaced)
    }

    private fun normalizeUserPhrase(text: String): String {
        return cleanText(text.lowercase())
    }

    private fun cleanText(text: String): String {
        return text.replace(Regex("[^a-zа-я0-9\\s\\-]"), " ").replace(Regex("\\s+"), " ").trim()
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        var dot = 0.0
        for (i in a.indices) dot += a[i] * b[i]
        return dot
    }

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