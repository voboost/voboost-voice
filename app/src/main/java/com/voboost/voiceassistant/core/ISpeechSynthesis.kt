package com.voboost.voiceassistant.core

/**
 * Интерфейс для модуля синтеза речи (TTS)
 * Позволяет легко менять реализации (Sherpa-ONNX, Vosk TTS, System TTS, Yandex SpeechKit)
 */
interface ISpeechSynthesis {

    /**
     * Инициализировать модуль синтеза
     * Должен быть вызван перед использованием
     */
    suspend fun initialize()

    /**
     * Проверить, готов ли модуль к работе
     */
    fun isReady(): Boolean

    /**
     * Произнести текст вслух
     * @param text Текст для синтеза
     * @param onCompletion Вызывается когда синтез завершён
     */
    fun speak(text: String, onCompletion: (() -> Unit)? = null)

    /**
     * Остановить текущее воспроизведение
     */
    fun stop()

    /**
     * Установить скорость речи
     * @param rate Скорость (0.5 = медленно, 1.0 = нормально, 2.0 = быстро)
     */
    fun setRate(rate: Float)

    /**
     * Установить тон голоса
     * @param pitch Тон (0.5 = низкий, 1.0 = нормально, 2.0 = высокий)
     */
    fun setPitch(pitch: Float)

    /**
     * Проверить, доступен ли модуль
     * @return true если модуль готов к работе
     */
    fun isAvailable(): Boolean

    /**
     * Очистить очередь воспроизведения
     */
    fun clearQueue()

    /**
     * Освободить ресурсы
     */
    fun shutdown()
}
