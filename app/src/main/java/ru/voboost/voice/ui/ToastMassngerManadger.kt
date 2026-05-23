package ru.voboost.voice.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import ru.voboost.voice.services.qgbus.QGBusEvent
import ru.voboost.voice.services.qgbus.QGBusServiceManager

/**
 * Менеджер показа toast-уведомлений через QGBus
 */
class ToastMessengerManager(
    context: Context,
    private val qgbusServiceManager: QGBusServiceManager
) {
    companion object {
        const val TAG = "ToastMessengerManager"
        private const val SCREEN_ID = 0
        private const val DURATION_MS = 3000L  // Short duration (3s)
        private const val EVENT_TYPE = "showToast"
        private const val DESTINATION = "com.qinggan.app.launcher"

        // Ключи Bundle
        private const val KEY_PACKAGE = "package"
        private const val KEY_CONTENT = "content"
        private const val KEY_SCREEN_ID = "screenId"
        private const val KEY_DURATION = "duration"
    }

    private val componentName: String = context.packageName

    /**
     * Показать toast через QGBus
     *
     * @param message Текст уведомления
     */
    fun show(message: String) {
        if (!qgbusServiceManager.isConnected()) {
            Log.w(TAG, "QGBus not connected, skipping toast")
            return
        }

        val bundle = Bundle().apply {
            putString(KEY_PACKAGE, componentName)
            putCharSequence(KEY_CONTENT, message)
            putInt(KEY_SCREEN_ID, SCREEN_ID)
            putLong(KEY_DURATION, DURATION_MS)
        }

        val event = QGBusEvent().apply {
            this.eventType = EVENT_TYPE
            this.source = componentName
            this.data = bundle
            this.destination = DESTINATION
            setSticky(false)
        }

        qgbusServiceManager.publish(event)
        Log.d(TAG, "Toast shown via QGBus: $message")
    }
}

