package ru.voboost.voice.canbus

/**
 * Callback для уведомления о подключении к CanBusService
 */
interface ICanBusServiceConnectionCallback {

    fun handlerConnected(canBusServiceManager: CanBusServiceManager) {}
    fun handlerDisconnected(canBusServiceManager: CanBusServiceManager) {}
    fun handlerConnectionFailed(canBusServiceManager: CanBusServiceManager, error: String) {}
}

