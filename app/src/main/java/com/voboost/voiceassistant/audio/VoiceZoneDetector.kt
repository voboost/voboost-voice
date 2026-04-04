package com.voboost.voiceassistant.audio

import android.util.Log

/**
 * Детектор зоны говорящего
 *
 * Определяет кто говорит (водитель/пассажир) на основе:
 * 1. Режима микрофона (MASTER/SLAVE/BOTH/FRONT)
 * 2. Угла источника звука (0-360°)
 *
 * Карта зон по углу:
 *   0° (вперёд)
 *      |
 * 270°-┼-90°  (лево-право)
 *      |
 *   180° (назад)
 *
 * front_left:  315°-45°  (водитель — левый передний)
 * front_right: 45°-135°  (передний пассажир — правый передний)
 * second_left: 135°-225° (задний левый)
 * second_right: 225°-315° (задний правый)
 */
class VoiceZoneDetector(
    private val micphoneModeManager: MicphoneModeManager
) {
    companion object {
        private const val TAG = "VoiceZoneDetector"

        // Угловые пороги для определения зоны
        private const val ANGLE_FRONT_LEFT_MIN = 315
        private const val ANGLE_FRONT_LEFT_MAX = 45
        private const val ANGLE_FRONT_RIGHT_MIN = 45
        private const val ANGLE_FRONT_RIGHT_MAX = 135
        private const val ANGLE_SECOND_LEFT_MIN = 135
        private const val ANGLE_SECOND_LEFT_MAX = 225
        private const val ANGLE_SECOND_RIGHT_MIN = 225
        private const val ANGLE_SECOND_RIGHT_MAX = 315
    }

    private val zoneListeners = mutableListOf<ZoneListener>()

    /**
     * Определить зону говорящего
     * @return Зона: front_left, front_right, second_left, second_right, all_location
     */
    fun detectZone(): String {
        val mode = micphoneModeManager.getCurrentMicMode()
        val angle = micphoneModeManager.getCurrentAudioAngle()

        val zone = when (mode) {
            MicphoneModeManager.MODE_MASTER -> "front_left"
            MicphoneModeManager.MODE_SLAVE -> "front_right"
            MicphoneModeManager.MODE_BOTH -> "all_location"
            MicphoneModeManager.MODE_FRONT -> angleToZone(angle)
            else -> "front_left" // По умолчанию — водитель
        }

        Log.d(TAG, "🎯 Zone detected: mode=$mode, angle=$angle° → zone='$zone'")
        return zone
    }

    /**
     * Преобразовать угол в зону
     */
    private fun angleToZone(angle: Int): String {
        val normalizedAngle = ((angle % 360) + 360) % 360

        return when {
            normalizedAngle in ANGLE_FRONT_LEFT_MIN..360 || normalizedAngle in 0..ANGLE_FRONT_LEFT_MAX -> "front_left"
            normalizedAngle in ANGLE_FRONT_LEFT_MAX..ANGLE_FRONT_RIGHT_MAX -> "front_right"
            normalizedAngle in ANGLE_FRONT_RIGHT_MAX..ANGLE_SECOND_LEFT_MAX -> "second_left"
            normalizedAngle in ANGLE_SECOND_LEFT_MAX..ANGLE_SECOND_RIGHT_MAX -> "second_right"
            normalizedAngle in ANGLE_SECOND_RIGHT_MAX..360 -> "front_left"
            else -> "front_left"
        }
    }

    /**
     * Добавить слушатель зоны
     */
    fun addZoneListener(listener: ZoneListener) {
        zoneListeners.add(listener)
    }

    /**
     * Удалить слушатель зоны
     */
    fun removeZoneListener(listener: ZoneListener) {
        zoneListeners.remove(listener)
    }

    /**
     * Уведомить слушателей об изменении зоны
     */
    fun notifyZoneChanged(zone: String) {
        zoneListeners.forEach { it.onZoneDetected(zone) }
    }

    /**
     * Слушатель определения зоны
     */
    interface ZoneListener {
        fun onZoneDetected(zone: String)
    }
}
