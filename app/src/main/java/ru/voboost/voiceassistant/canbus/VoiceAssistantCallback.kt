package ru.voboost.voiceassistant.canbus

/**
 * Callback для уведомления о нажатии кнопки голосового помощника
 */
interface VoiceAssistantCallback {
    /**
     * Вызывается при нажатии кнопки на руле
     */
    fun onVoiceButtonPressed()
}