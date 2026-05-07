package ru.voboost.voiceassistant.audio

/**
 * Слушатель событий управления громкостью
 */
interface IVolumeListener {
    /**
     * Вызывается при подключении к сервису
     */
    fun onConnected()

    /**
     * Вызывается при отключении от сервиса
     */
    fun onDisconnected()

    /**
     * Вызывается при изменении громкости музыки
     */
    fun onMediaVolumeChanged(volume: Int) {}

    /**
     * Вызывается при изменении громкости навигации
     */
    fun onNavigationVolumeChanged(volume: Int) {}

    /**
     * Вызывается при изменении громкости телефона
     */
    fun onPhoneVolumeChanged(volume: Int) {}

    /**
     * Вызывается при изменении громкости уведомлений
     */
    fun onNotificationVolumeChanged(volume: Int) {}

    /**
     * Вызывается при нажатии кнопки увеличения громкости
     */
    fun onVolumeUp() {}

    /**
     * Вызывается при нажатии кнопки уменьшения громкости
     */
    fun onVolumeDown() {}

    /**
     * Вызывается при запросе аудио-политики
     */
    fun onRequestAudioPolicy(streamType: Int, clientId: String) {}

    /**
     * Вызывается при освобождении аудио-политики
     */
    fun onAbandonAudioPolicy(clientId: String) {}
}