package ru.voboost.voice.ui

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.widget.ImageView
import ru.voboost.voice.R

/**
 * VoiceClickView - анимация голосового помощника
 * Использует оригинальные кадры из Ivoka (voice_right000..040)
 * Зацикленная frame-by-frame анимация
 */
class VoiceClickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private var animationDrawable: AnimationDrawable? = null

    init {
        // Устанавливаем ресурс анимации
        setBackgroundResource(R.drawable.anim_voice_effect)
        animationDrawable = background as? AnimationDrawable
    }

    /**
     * Запустить анимацию (зацикленную)
     */
    fun startAnimation() {
        animationDrawable?.stop()
        animationDrawable?.start()
    }

    /**
     * Остановить анимацию
     */
    fun stopAnimation() {
        animationDrawable?.stop()
    }
}


