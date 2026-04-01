package com.voboost.voiceassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Приемник загрузки системы
 * Автоматически запускает сервис при включении устройства
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        Log.d(TAG, "Received broadcast: $action")

        // Проверяем тип загрузки
        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "System boot completed, starting service...")
                startVoiceService(context)
            }

            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.i(TAG, "Quick boot detected, starting service...")
                startVoiceService(context)
            }

            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                Log.i(TAG, "HTC Quick boot detected, starting service...")
                startVoiceService(context)
            }

            Intent.ACTION_REBOOT -> {
                Log.i(TAG, "System reboot detected, starting service...")
                startVoiceService(context)
            }
        }
    }

    /**
     * Запустить голосовой сервис
     */
    private fun startVoiceService(context: Context) {
        try {
            val serviceIntent = Intent(context, VoboostVoiceService::class.java)

            // Для Android 8.0+ нужен foreground service
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.i(TAG, "Voice service started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start voice service", e)
        }
    }
}
