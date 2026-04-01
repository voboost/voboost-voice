package com.voboost.voiceassistant.core

/**
 * Интерфейс для модуля распознавания речи (STT)
 * Позволяет легко менять реализации (Vosk, Sherpa-ONNX, Google Speech API)
 */
interface SpeechRecognition {

    /**
     * Инициализировать модуль распознавания
     * Должен быть вызван перед использованием
     */
    suspend fun initialize()

    /**
     * Проверить, готов ли модуль к работе
     */
    fun isReady(): Boolean

    /**
     * Запустить постоянное прослушивание ключевой фразы
     * @param onKeywordDetected Вызывается когда обнаружена ключевая фраза
     * @param onError Вызывается при ошибке распознавания
     */
    suspend fun startKeywordSpotting(
        onKeywordDetected: () -> Unit,
        onError: suspend (String) -> Unit
    )

    /**
     * Запустить прослушивание команды после активации
     * @param onCommandReceived Вызывается когда команда распознана
     * @param onError Вызывается при ошибке распознавания
     * @param onTimeout Вызывается при таймауте (если команда не сказана)
     */
    fun startCommandListening(
        onCommandReceived: (String) -> Unit,
        onError: (String) -> Unit,
        onTimeout: suspend () -> Unit
    )

    /**
     * Однократное прослушивание команды (для подтверждений и других сценариев)
     * @param timeout Таймаут в миллисекундах
     * @return Распознанный текст или пустая строка при таймауте/ошибке
     */
    suspend fun listenForCommand(timeout: Long = 3000): String

    /**
     * Установить ключевые фразы для активации
     * @param keywords Список фраз для детекции
     */
    fun setActivationKeywords(keywords: List<String>)

    /**
     * Остановить текущее распознавание
     */
    fun stop()

    /**
     * Освободить ресурсы
     */
    fun shutdown()
}
