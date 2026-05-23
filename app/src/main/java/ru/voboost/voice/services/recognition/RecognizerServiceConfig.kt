package ru.voboost.voice.services.recognition

data class RecognizerServiceConfig(val sampleRate: Int = 16000,
                                   val bitsPerSample: Int = 16,
                                   val bufferSizeSec: Float = 8f, // размер буфера в секундах
                                   val silenceThresholdDb: Float = -45f,
                                   val minSilenceDurationMs: Int = 600,
                                   val rmsWindowSizeMs: Int = 50, // Таймауты (страховка)
                                   val keywordTimeoutMs: Long = 30_000,
                                   val commandTimeoutMs: Long = 5_000, // Интервал обработки
                                   val processingIntervalMs: Long = 20) {
    companion object {
        val Default = RecognizerServiceConfig()
    }
}