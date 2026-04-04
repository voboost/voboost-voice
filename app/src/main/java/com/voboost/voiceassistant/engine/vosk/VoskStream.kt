package com.voboost.voiceassistant.engine.vosk

import android.util.Log
import com.voboost.voiceassistant.speech.RecognitionEngine
import com.voboost.voiceassistant.speech.RecognitionResult
import org.json.JSONObject
import org.vosk.Recognizer

/**
 * Поток распознавания речи Vosk
 * Отвечает ТОЛЬКО за преобразование PCM данных в текст
 * 
 * Реализует RecognitionEngine напрямую — без лишних обёрток
 * 
 * НЕ управляет состоянием, НЕ работает с AudioSource
 * Только PCM → Text
 */
class VoskStream(
    private val recognizer: Recognizer
) : RecognitionEngine {
    companion object {
        private const val TAG = "VoskStream"
    }
    
    /**
     * Принять порцию PCM данных и распознать
     * @param pcm PCM данные (16-bit, mono, 16000 Hz)
     * @return Результат распознавания или null если ничего не распознано
     */
    override fun acceptWaveform(pcm: ByteArray): RecognitionResult? {
        if (pcm.isEmpty()) return null
        
        return try {
            if (recognizer.acceptWaveForm(pcm, pcm.size)) {
                // Финальный результат
                val text = extractText(recognizer.result)
                if (text.isNotEmpty()) {
                    Log.d(TAG, "Final result: $text")
                    RecognitionResult(text = text, isFinal = true)
                } else null
            } else {
                // Частичный результат (для логов/UI)
                val partialText = extractText(recognizer.partialResult)
                if (partialText.isNotEmpty()) {
                    Log.v(TAG, "Partial result: $partialText")
                    RecognitionResult(text = partialText, isPartial = true)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recognizing waveform", e)
            null
        }
    }
    
    /**
     * Получить финальный результат (для завершения распознавания)
     */
    override fun getFinalResult(): RecognitionResult? {
        return try {
            val text = extractText(recognizer.finalResult)
            if (text.isNotEmpty()) {
                RecognitionResult(text = text, isFinal = true)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting final result", e)
            null
        }
    }
    
    /**
     * Сбросить распознавание (начать заново)
     */
    override fun reset() {
        recognizer.reset()
    }
    
    /**
     * Освободить ресурсы
     */
    override fun release() {
        try {
            recognizer.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recognizer", e)
        }
    }
    
    private fun extractText(jsonResult: String): String {
        return try {
            val json = JSONObject(jsonResult)
            json.optString("text", "")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse JSON result", e)
            ""
        }
    }
}
