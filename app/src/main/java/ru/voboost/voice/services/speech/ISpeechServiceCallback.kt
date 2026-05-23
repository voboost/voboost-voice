package ru.voboost.voice.services.speech

/**
 * Интерфейс обратных вызовов для синтеза речи
 */
interface ISpeechServiceCallback {
    /**
     * Вызывается при завершении воспроизведения фразы
     */
    fun handleSpeechFinished()
}