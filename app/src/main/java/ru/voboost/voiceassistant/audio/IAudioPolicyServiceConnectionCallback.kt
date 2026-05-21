package ru.voboost.voiceassistant.audio

interface IAudioPolicyServiceConnectionCallback {
    fun handlerConnected(audioPolicyServiceManager: AudioPolicyServiceManager) {}
    fun handlerDisconnected(audioPolicyServiceManager: AudioPolicyServiceManager) {}
    fun handlerConnectionFailed(audioPolicyServiceManager: AudioPolicyServiceManager, error: String) {
    }
}