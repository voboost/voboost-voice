package ru.voboost.voice.ui

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Менеджер UI оверлеев
 * Показывает анимацию и уведомления поверх других приложений
 */
class VoceAnimationManager(private val context: Context) {
    companion object {
        const val TAG = "VoceAnimationManager"
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var voiceClickView: View? = null
    private var isAnimationShowing = false

    /**
     * Показать анимацию голосового помощника
     * Анимация крутится бесконечно пока ассистент активен.
     * Вызвать hideAnimation() для остановки.
     */
    fun show() {
        if (isAnimationShowing) {
            Log.w(TAG, "Animation already showing")
            return
        }

        handler.post {
            try {
                // Создаем VoiceClickView с frame-by-frame анимацией
                voiceClickView = VoiceClickView(context)
                // Параметры окна
                val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                                        PixelFormat.TRANSLUCENT)
                // Позиция: верх, X=0 (центрируем после layout)
                params.gravity = Gravity.TOP or Gravity.START
                params.x = 0
                params.y = 0
                // Добавляем view
                windowManager.addView(voiceClickView, params)
                (voiceClickView as? VoiceClickView)?.startAnimation()
                isAnimationShowing = true
                // Центрируем после того как View измерен
                voiceClickView?.post {
                    val viewWidth = voiceClickView?.width ?: 0
                    val screenWidth = context.resources.displayMetrics.widthPixels
                    val centerX = (screenWidth - viewWidth) / 2
                    val newParams = voiceClickView?.layoutParams as? WindowManager.LayoutParams
                    newParams?.x = centerX
                    newParams?.y = 0
                    voiceClickView?.let { windowManager.updateViewLayout(it, newParams) }
                    Log.d(TAG, "Centered: screen=$screenWidth, view=$viewWidth, x=$centerX")
                }
                Log.d(TAG, "Animation shown at top-center")
            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to show animation", e)
            }
        }
    }

    /**
     * Скрыть анимацию (останавливает цикл и удаляет View)
     */
    fun hide() {
        handler.post {
            try {
                (voiceClickView as? VoiceClickView)?.stopAnimation()
                voiceClickView?.let { view ->
                    windowManager.removeView(view)
                    voiceClickView = null
                    isAnimationShowing = false
                    Log.d(TAG, "Animation hidden")
                }
            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to hide animation", e)
            }
        }
    }

    /**
     * Очистить все оверлеи
     */
    fun clear() {
        hide()
        handler.post {
            try {
                voiceClickView?.let { view ->
                    windowManager.removeView(view)
                    voiceClickView = null
                }
            }
            catch (e: Exception) {
                Log.e(TAG, "Error clearing overlays", e)
            }
        }
    }
}


