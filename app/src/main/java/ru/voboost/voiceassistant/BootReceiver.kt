package ru.voboost.voiceassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Приемник загрузки системы
 * Автоматически запускает сервис при включении устройства
 *
 * Запускаем СРАЗУ — система уже готова (BOOT_COMPLETED приходит после полной загрузки)
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        Log.i(TAG, "Received broadcast: $action")

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
     * Запустить невидимую BootActivity для получения foreground context.
     * 
     * Android 10+ запрещает запуск foreground service с микрофоном из фона.
     * BootActivity запускается с foreground context и стартует сервис,
     * получая полный доступ к микрофону.
     */
    private fun startVoiceService(context: Context) {
        try {
            val activityIntent = Intent(context, BootActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(activityIntent)

            Log.i(TAG, "✅ BootActivity launched — will start foreground service with mic access")

        }
        catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start BootActivity", e)
        }
    }
}
