package ru.voboost.voiceassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Прием широковещательных команд активации
 */
class VoiceCommandReceiver : BroadcastReceiver() {
    
    companion object {
        const val TAG = "VoiceCommandReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")
        
        when (intent.action) {
            "ru.voboost.voiceassistant.ACTIVATE" -> {
                Log.i(TAG, "Activation request received")
                
                // Запускаем сервис если еще не запущен
                val serviceIntent = Intent(context, VoboostVoiceService::class.java)
                
                try {
                    context.startForegroundService(serviceIntent)
                    Log.i(TAG, "Service started")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start service", e)
                }
            }
        }
    }
}
