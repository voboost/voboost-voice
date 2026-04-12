package com.voboost.voiceassistant.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import com.voboost.voiceassistant.config.ConfigManager

/**
 * VoiceClickView - анимация голосового помощника
 * Копия оригинального VoiceClickView из системы
 */
class VoiceClickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val START_RADIUS = 13f
        private const val END_RADIUS = 28f
        private const val NARROW_POSITION = 25f
        private const val DEFAULT_STROKE_WIDTH = 4f
        private const val DEFAULT_COLOR = 0xFF757575.toInt() // Серый цвет как в оригинале
    }
    
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = DEFAULT_STROKE_WIDTH
        isAntiAlias = true
        color = DEFAULT_COLOR
    }
    
    private var radius = START_RADIUS
    private var strokeWidth = DEFAULT_STROKE_WIDTH
    
    init {
        // Устанавливаем размер 60x60 как в оригинале
        layoutParams = WindowManager.LayoutParams(60, 60)
        
        // Запускаем анимацию при прикреплении к окну
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Очистка анимаций
    }
    
    /**
     * Запустить анимацию
     */
    private fun startAnimation() {
        val duration = OverlayManager.ANIMATION_DURATION_MS
        
        val animator = ValueAnimator.ofFloat(START_RADIUS, END_RADIUS)
        animator.duration = duration
        
        animator.addUpdateListener { animation ->
            radius = animation.animatedValue as Float
            
            // Уменьшаем толщину линии когда радиус больше 25
            if (radius > NARROW_POSITION) {
                strokeWidth = (1f - (radius - NARROW_POSITION) / 5f) * DEFAULT_STROKE_WIDTH
            } else {
                strokeWidth = DEFAULT_STROKE_WIDTH
            }
            
            paint.strokeWidth = strokeWidth
            invalidate()
        }
        
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Анимация завершена
                // OverlayManager сам удалит view
            }
        })
        
        animator.start()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Фиксированный размер 60x60
        setMeasuredDimension(60, 60)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Рисуем круг по центру
        val centerX = width / 2f
        val centerY = height / 2f
        
        canvas.drawCircle(centerX, centerY, radius, paint)
    }
}
