package ru.voboost.voiceassistant.ui

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import ru.voboost.voiceassistant.R
import ru.voboost.voiceassistant.config.ConfigManager

/**
 * Менеджер UI оверлеев
 * Показывает анимацию и уведомления поверх других приложений
 */
class OverlayManager(
    private val context: Context
) {
    companion object {
        const val TAG = "OverlayManager"
        const val OVERLAY_POSITION = "top_left"
        const val OVERLAY_OFFSET_X_DP = 50
        const val OVERLAY_OFFSET_Y_DP = 50
        const val SHOW_ANIMATION =  true
        const val ANIMATION_DURATION_MS = 1000L
        const val TOAST_DURATION_MS = 3000
    }
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val configManager = ConfigManager.getInstance(context)
    private val handler = Handler(Looper.getMainLooper())
    
    private var voiceClickView: View? = null
    private var isAnimationShowing = false
    
    /**
     * Показать анимацию голосового помощника
     */
    fun showAnimation() {
        if (isAnimationShowing) {
            Log.w(TAG, "Animation already showing")
            return
        }
        
        handler.post {
            try {
                val config = configManager.getConfig()
                
                if (!SHOW_ANIMATION) {
                    Log.d(TAG, "Animation disabled in config")
                    return@post
                }
                
                // Создаем VoiceClickView
                voiceClickView = VoiceClickView(context)
                
                // Параметры окна
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
                )
                
                // Позиция (верхний левый угол)
                params.gravity = Gravity.TOP or Gravity.START
                
                // Смещение в DP
                val displayMetrics = context.resources.displayMetrics
                val offsetX = (OVERLAY_OFFSET_X_DP * displayMetrics.density).toInt()
                val offsetY = (OVERLAY_OFFSET_Y_DP * displayMetrics.density).toInt()
                
                params.x = offsetX
                params.y = offsetY
                
                // Добавляем view
                windowManager.addView(voiceClickView, params)
                isAnimationShowing = true
                
                Log.d(TAG, "Animation shown at ($offsetX, $offsetY)")
                
                // Автоматическое скрытие через duration
                handler.postDelayed({
                    hideAnimation()
                }, ANIMATION_DURATION_MS + 500)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show animation", e)
            }
        }
    }
    
    /**
     * Скрыть анимацию
     */
    fun hideAnimation() {
        handler.post {
            try {
                voiceClickView?.let { view ->
                    windowManager.removeView(view)
                    voiceClickView = null
                    isAnimationShowing = false
                    Log.d(TAG, "Animation hidden")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to hide animation", e)
            }
        }
    }
    
    /**
     * Показать Toast уведомление
     */
    @Suppress("DEPRECATION")
    fun showToast(message: String) {
        handler.post {
            try {
                val config = configManager.getConfig()

                // Создаем кастомный Toast
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.toast_voice, null)

                val textView = view.findViewById<android.widget.TextView>(R.id.toast_text)
                textView.text = message

                val toast = Toast(context).apply {
                    duration = Toast.LENGTH_SHORT
                    setGravity(Gravity.TOP or Gravity.START,
                        (OVERLAY_OFFSET_X_DP * context.resources.displayMetrics.density).toInt(),
                        (OVERLAY_OFFSET_Y_DP * context.resources.displayMetrics.density).toInt() + 200)
                    this.view = view
                }
                toast.show()

                Log.d(TAG, "Toast shown: $message")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to show toast", e)

                // Fallback к стандартному Toast
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Показать уведомление о выполнении команды
     */
    fun showCommandResult(message: String) {
        showToast(message)

        // Анимация уже показана во время распознавания
        // Здесь только текст
    }
    
    /**
     * Очистить все оверлеи
     */
    fun clearAll() {
        hideAnimation()
        
        handler.post {
            try {
                voiceClickView?.let { view ->
                    windowManager.removeView(view)
                    voiceClickView = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing overlays", e)
            }
        }
    }
}
