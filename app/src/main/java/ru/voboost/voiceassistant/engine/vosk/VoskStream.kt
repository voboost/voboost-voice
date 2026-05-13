package ru.voboost.voiceassistant.engine.vosk

import android.util.Log
import ru.voboost.voiceassistant.speech.IRecognitionEngine
import ru.voboost.voiceassistant.speech.RecognitionResult
import org.json.JSONObject
import org.vosk.Recognizer

/**
 * Поток распознавания речи Vosk
 * Отвечает ТОЛЬКО за преобразование PCM данных в текст
 *
 * Реализует IRecognitionEngine напрямую — без лишних обёрток
 *
 * НЕ управляет состоянием, НЕ работает с IAudioSource
 * Только PCM → Text
 */
class VoskStream(private val recognizer: Recognizer) : IRecognitionEngine {
    companion object {
        const val TAG = "VoskStream"
    }

    @Volatile
    private var needsRecreation = false

    // Блокировка для защиты от race condition между reset() и acceptWaveform()
    private val recognizerLock = Any()

    /**
     * Принять порцию PCM данных и распознать
     * @param pcm PCM данные (16-bit, mono, 16000 Hz)
     * @return Результат распознавания или null если ничего не распознано
     */
    override fun acceptWaveform(pcm: ByteArray, start: Int, end: Int): RecognitionResult? {
        if (pcm.isEmpty()) return null

        // Синхронизация чтобы избежать race condition с reset()
        synchronized(recognizerLock) { // Если нужен сброс — выполняем его безопасно
            if (needsRecreation) {
                needsRecreation = false
                Log.d(TAG, "Resetting recognizer after reset() was called")
                try {
                    recognizer.reset()
                }
                catch (e: Exception) {
                    Log.w(TAG, "Reset during acceptWaveform failed (expected): ${e.message}")
                }
            }

            val chunkSize = end - start
            val chunk = pcm.copyOfRange(start, end)
            return try {
                if (recognizer.acceptWaveForm(chunk, chunkSize)) { // Финальный результат
                    val text = extractText(recognizer.result)
                    if (text.isNotEmpty()) {
                        Log.d(TAG, "Final result: $text")
                        RecognitionResult(text = text, isFinal = true)
                    }
                    else null
                }
                else { // Частичный результат (для логов/UI)
                    val partialText = extractText(recognizer.partialResult)
                    if (partialText.isNotEmpty()) {
                        Log.v(TAG, "Partial result: $partialText")
                        RecognitionResult(text = partialText, isPartial = true)
                    }
                    else null
                }
            }
            catch (e: Exception) {
                Log.e(TAG, "Error recognizing waveform", e)
                null
            }
        }
    }

    /**
     * Получить финальный результат (для завершения распознавания)
     */
    override fun getFinalResult(): RecognitionResult? {
        synchronized(recognizerLock) {
            return try {
                val text = extractText(recognizer.finalResult)
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
    }

    /**
     * Сбросить распознавание (начать заново)
     */
    override fun reset() { // НЕ вызываем reset() на активном декодере — это вызывает race condition в C++ коде Vosk!
        // Просто устанавливаем флаг — сброс произойдёт при следующем acceptWaveform
        synchronized(recognizerLock) {
            Log.d(TAG, "reset() called — will reset on next acceptWaveform")
            needsRecreation = true
        }
    }

    /**
     * Освободить ресурсы
     */
    override fun release() {
        synchronized(recognizerLock) {
            try {
                recognizer.close()
            }
            catch (e: Exception) {
                Log.e(TAG, "Error releasing recognizer", e)
            }
        }
    }

    private fun extractText(jsonResult: String): String {
        return try {
            val json = JSONObject(jsonResult)
            json.optString("text", "")
        }
        catch (e: Exception) {
            Log.w(TAG, "Failed to parse JSON result", e)
            ""
        }
    }
}
