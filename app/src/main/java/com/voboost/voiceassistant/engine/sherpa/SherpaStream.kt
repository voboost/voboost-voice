package com.voboost.voiceassistant.engine.sherpa

import android.util.Log
import com.k2fsa.sherpa.onnx.*
import com.voboost.voiceassistant.speech.RecognitionEngine
import com.voboost.voiceassistant.speech.RecognitionResult
import java.io.File

/**
 * Поток распознавания речи Sherpa-ONNX
 * Реализует универсальный интерфейс RecognitionEngine
 * 
 * Использует Zipformer модель для распознавания
 */
class SherpaStream private constructor(
    private val recognizer: OfflineRecognizer
) : RecognitionEngine {
    
    companion object {
        private const val TAG = "SherpaStream"
        private const val SAMPLE_RATE = 16000
        
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
            return SherpaStream(recognizer)
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
    override fun acceptWaveform(pcm: ByteArray): RecognitionResult? {
        if (pcm.isEmpty()) return null
        
        val stream = currentStream ?: run {
            Log.w(TAG, "No active stream, creating one")
            createStream()
            currentStream ?: return null
        }
        
        return try {
            // Преобразовать PCM bytes в FloatArray
            val floatSamples = pcmBytesToFloats(pcm)
            
            // Передать данные в Sherpa
            stream.acceptWaveform(floatSamples, SAMPLE_RATE)
            
            // Распознать
            recognizer.decode(stream)
            
            // Получить результат
            val result = recognizer.getResult(stream)
            val text = result.text.trim()
            
            if (text.isNotEmpty()) {
                Log.d(TAG, "Recognized: $text")
                RecognitionResult(text = text, isFinal = true)
            } else null
            
        } catch (e: Exception) {
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
            } else null
        } catch (e: Exception) {
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
     * Преобразовать PCM bytes (16-bit) в FloatArray (-1.0 to 1.0)
     */
    private fun pcmBytesToFloats(buffer: ByteArray): FloatArray {
        val samples = ShortArray(buffer.size / 2)
        for (i in samples.indices) {
            samples[i] = ((buffer[i * 2].toInt() and 0xFF) or (buffer[i * 2 + 1].toInt() shl 8)).toShort()
        }
        return FloatArray(samples.size) { samples[it].toFloat() / Short.MAX_VALUE }
    }
}
