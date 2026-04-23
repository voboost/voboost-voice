package ru.voboost.voiceassistant.core

/**
 * Интерфейс обратных вызовов для синтеза речи
 */
interface ISpeechSynthesisCallback {
    /**
     * Вызывается при завершении воспроизведения фразы
     */
    fun handleSpeechFinished()
}