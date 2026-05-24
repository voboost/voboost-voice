package ru.voboost.voice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Невидимая Activity для получения foreground context при автозапуске.
 * 
 * Android 10+ запрещает запуск foreground service с доступом к микрофону из фона.
 * BootReceiver работает в фоне, поэтому мы запускаем эту Activity,
 * она получает foreground context, запускает сервис и закрывается.
 */
class BootActivity : Activity() {

    companion object {
        const val TAG = "BootActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate — starting VoboostVoiceService with foreground context")
        // Запускаем сервис из foreground context Activity
        val serviceIntent = Intent(this, VoboostVoiceService::class.java)
        startForegroundService(serviceIntent)
        Log.i(TAG, "? startForegroundService called from Activity context")
        // Ждём 5 секунд чтобы сервис успел вызвать startForeground() и инициализировать AudioRecord
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i(TAG, "BootActivity finishing — service should have microphone access now")
            finish()
        }, 5000)
    }
}


