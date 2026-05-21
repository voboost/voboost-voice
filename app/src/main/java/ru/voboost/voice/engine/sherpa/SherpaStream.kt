package ru.voboost.voice.engine.sherpa

import android.util.Log
import com.k2fsa.sherpa.onnx.*
import ru.voboost.voice.speech.IRecognitionEngine
import ru.voboost.voice.speech.RecognitionResult
import java.io.File
import kotlin.math.min

/**
 * Поток распознавания речи Sherpa-ONNX
 * Реализует универсальный интерфейс IRecognitionEngine
 *
 * Использует Zipformer модель для распознавания
 */
class SherpaStream private constructor(private val recognizer: OfflineRecognizer,
                                       private val reusableBuffer: FloatArray) : IRecognitionEngine {

    companion object {
        const val TAG = "SherpaStream"
        private const val SAMPLE_RATE = 16000
        const val MAX_CHUNK_SAMPLES = 3200

        /**
         * Создать SherpaStream из пути к модели
         */
        fun create(modelPath: String): SherpaStream {
            Log.i(TAG, "Creating SherpaStream from: $modelPath")

            val encoderPath = File(modelPath, "encoder.onnx").absolutePath
            val decoderPath = File(modelPath, "decoder.onnx").absolutePath
            val joinerPath = File(modelPath, "joiner.onnx").absolutePath
            val tokensPath = File(modelPath, "tokens.txt").absolutePath

            Log.d(TAG, "Encoder: $encoderPath")
            Log.d(TAG, "Decoder: $decoderPath")
            Log.d(TAG, "Joiner: $joinerPath")
            Log.d(TAG, "Tokens: $tokensPath")

            // Создаём конфигурацию транседера
            val transducerConfig = OfflineTransducerModelConfig.Builder()
                .setEncoder(encoderPath)
                .setDecoder(decoderPath)
                .setJoiner(joinerPath)
                .build()

            // Создаём конфигурацию модели
            val modelConfig = OfflineModelConfig.Builder()
                .setTransducer(transducerConfig)
                .setTokens(tokensPath)
                .setNumThreads(2)
                .setProvider("cpu")
                .setDebug(true)
                .build()

            // Создаём конфигурацию фичей
            val featureConfig = FeatureConfig.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setFeatureDim(80)
                .build()

            // Создаём основную конфигурацию распознавания
            val recognizerConfig = OfflineRecognizerConfig.Builder()
                .setFeatureConfig(featureConfig)
                .setOfflineModelConfig(modelConfig)
                .setMaxActivePaths(4)
                .build()

            val recognizer = OfflineRecognizer(recognizerConfig)
            val reusableBuffer = FloatArray(MAX_CHUNK_SAMPLES) // запас на большие чанки

            return SherpaStream(recognizer, reusableBuffer)
        }
    }

    private var currentStream: OfflineStream? = null

    /**
     * Создать новый поток для распознавания
     */
    fun createStream() {
        currentStream = recognizer.createStream()
        Log.d(TAG, "New stream created")
    }

    /**
     * Принять порцию PCM данных и распознать
     * @param pcm PCM данные (16-bit, mono, 16000 Hz)
     * @return Результат распознавания или null
     */
    override fun acceptWaveform(pcm: ByteArray, start: Int, end: Int): RecognitionResult? {

        if (start < 0 || end > pcm.size || start >= end) {
            Log.w(TAG, "Invalid range: start=$start, end=${pcm.size}, size=${pcm.size}")
            return null
        }

        val stream = currentStream ?: run {
            Log.w(TAG, "No active stream, creating one")
            createStream()
            currentStream ?: return null
        }

        return try {
            val samplesCount = pcmToFloats(pcm, start, end, reusableBuffer)

            val chunk = reusableBuffer.copyOf(samplesCount)
            stream.acceptWaveform(chunk,  SAMPLE_RATE)

            // Распознать
            recognizer.decode(stream)

            // Получить результат
            val result = recognizer.getResult(stream)
            val text = result.text.trim()

            if (text.isNotEmpty()) {
                Log.d(TAG, "Recognized: $text")
                RecognitionResult(text = text, isFinal = true)
            }
            else null

        }
        catch (e: Exception) {
            Log.e(TAG, "Error recognizing waveform", e)
            null
        }
    }

    /**
     * Получить финальный результат
     */
    override fun getFinalResult(): RecognitionResult? {
        return try {
            val stream = currentStream ?: return null
            val result = recognizer.getResult(stream)
            val text = result.text.trim()

            if (text.isNotEmpty()) {
                RecognitionResult(text = text, isFinal = true)
            }
            else null
        }
        catch (e: Exception) {
            Log.e(TAG, "Error getting final result", e)
            null
        }
    }

    /**
     * Сбросить распознавание
     */
    override fun reset() {
        createStream()
        Log.d(TAG, "Stream reset")
    }

    /**
     * Освободить ресурсы
     */
    override fun release() {
        currentStream = null
        Log.d(TAG, "Stream released")
    }

    /**
     * Конвертировать PCM bytes > FloatArray [-1.0, 1.0] БЕЗ аллокаций
     *
     * @param src исходный байтовый массив
     * @param srcStart начальный индекс в src (включительно)
     * @param srcEnd конечный индекс в src (исключительно)
     * @param dst буфер для результата (должен быть достаточно большим)
     * @return количество записанных сэмплов
     */
    private fun pcmToFloats(src: ByteArray, srcStart: Int, srcEnd: Int, dst: FloatArray): Int {
        val bytesCount = srcEnd - srcStart
        val samplesCount = bytesCount / 2 // 2 байта на 16-bit sample

        if (samplesCount > dst.size) {
            Log.w(TAG, "Buffer too small: need $samplesCount, have ${dst.size}. Truncating.")
            // Обработаем только то, что влезает
            return pcmToFloats(src, srcStart, srcStart + dst.size * 2, dst)
        }

        var srcIdx = srcStart
        var dstIdx = 0

        // ?? Little-endian: младший байт первый
        while (dstIdx < samplesCount) {
            val low = src[srcIdx].toInt() and 0xFF
            val high = src[srcIdx + 1].toInt()
            val sample = (low or (high shl 8)).toShort() // Нормализация в диапазон [-1.0, 1.0]
            dst[dstIdx] = sample.toFloat() / Short.MAX_VALUE
            srcIdx += 2
            dstIdx++
        }

        return samplesCount
    }
}


