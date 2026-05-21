package ru.voboost.voice.audio

import android.util.Log
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min

/**
 * TDOA (Time Difference of Arrival) детектор зоны говорящего
 *
 * Использует разницу времени прихода звукового сигнала к разным микрофонам
 * для определения направления на говорящего.
 *
 * Алгоритм:
 * 1. Cross-correlation между парами микрофонов
 * 2. Нахождение задержки (delay) с максимальной корреляцией
 * 3. Вычисление угла: θ = arcsin((c * Δt) / d)
 * 4. Маппинг угла на зону: front_left, front_right, second_left, second_right
 *
 * @param micSpacing Расстояние между микрофонами в метрах (по умолчанию 15 см)
 * @param sampleRate Частота дискретизации (по умолчанию 16000 Гц)
 */
class TdoaZoneDetector(
    private val micSpacing: Float = 0.15f,  // 15 см между микрофонами
    private val sampleRate: Int = 16000
) {
    companion object {
        const val TAG = "TdoaZoneDetector"
        private const val SPEED_OF_SOUND = 343.0f  // м/с при 20°C
        
        // Максимальная ожидаемая задержка в сэмплах (~1.25мс для 15см)
        private const val MAX_DELAY_SAMPLES = 10
        
        // Порог энергии для детектирования активности речи
        private const val ENERGY_THRESHOLD = 10000.0f
    }

    /**
     * Определить зону по данным с 4 микрофонов
     * @param channels 4 массива PCM данных (16-bit signed)
     * @return Зона: front_left, front_right, second_left, second_right
     */
    fun detectZone(channels: Array<ShortArray>): String {
        if (channels.size < 4) {
            Log.w(TAG, "Not enough channels: ${channels.size}, need 4")
            return "front_left"
        }

        // Вычисляем энергию каждого канала для проверки активности
        val energies = channels.map { calculateEnergy(it) }
        Log.d(TAG, "Energies: ${energies.map { "%.0f".format(it) }}")

        // Проверяем, есть ли вообще звук
        val maxEnergy = energies.maxOrNull() ?: 0f
        if (maxEnergy < ENERGY_THRESHOLD) {
            Log.d(TAG, "Low energy, no speech detected")
            return "front_left"  // По умолчанию
        }

        // Находим канал с максимальной энергией
        val maxEnergyIndex = energies.indexOfMaxOrNull() ?: 0

        // Вычисляем задержки между парами микрофонов
        val delay12 = crossCorrelationDelay(channels[0], channels[1])  // передние
        val delay34 = crossCorrelationDelay(channels[2], channels[3])  // задние
        val delay13 = crossCorrelationDelay(channels[0], channels[2])  // левые
        val delay24 = crossCorrelationDelay(channels[1], channels[3])  // правые

        Log.d(TAG, "Delays: 1-2=$delay12, 3-4=$delay34, 1-3=$delay13, 2-4=$delay24")

        // Определяем зону по комбинации задержек и энергий
        val zone = determineZone(delay12, delay34, delay13, delay24, energies, maxEnergyIndex)
        
        Log.d(TAG, "🎯 Zone detected: $zone (maxEnergyIndex=$maxEnergyIndex)")
        return zone
    }

    /**
     * Вычислить энергию сигнала (сумма квадратов сэмплов)
     */
    private fun calculateEnergy(signal: ShortArray): Float {
        var sum = 0.0f
        for (sample in signal) {
            sum += sample * sample
        }
        return sum / signal.size
    }

    /**
     * Найти задержку с максимальной кросс-корреляцией между двумя сигналами
     * @return Задержка в сэмплах (положительная = сигнал 2 запаздывает, отрицательная = опережает)
     */
    private fun crossCorrelationDelay(signal1: ShortArray, signal2: ShortArray): Int {
        var bestLag = 0
        var maxCorrelation = Float.NEGATIVE_INFINITY

        // Ищем задержку в диапазоне [-MAX_DELAY_SAMPLES, +MAX_DELAY_SAMPLES]
        for (lag in -MAX_DELAY_SAMPLES..MAX_DELAY_SAMPLES) {
            var correlation = 0.0f
            var count = 0

            for (i in 0 until signal1.size) {
                val j = i + lag
                if (j >= 0 && j < signal2.size) {
                    correlation += signal1[i] * signal2[j]
                    count++
                }
            }

            if (count > 0) {
                correlation /= count

                if (correlation > maxCorrelation) {
                    maxCorrelation = correlation
                    bestLag = lag
                }
            }
        }

        return bestLag
    }

    /**
     * Определить зону по задержкам и энергиям
     */
    private fun determineZone(
        delay12: Int,  // между передними микрофонами
        delay34: Int,  // между задними микрофонами
        delay13: Int,  // между левыми микрофонами
        delay24: Int,  // между правыми микрофонами
        energies: List<Float>,
        maxEnergyIndex: Int
    ): String {
        // Если максимальная энергия на переднем левом (водитель)
        if (maxEnergyIndex == 0) {
            return if (delay12 > 2) "front_right" else "front_left"
        }
        
        // Если максимальная энергия на переднем правом (пассажир)
        if (maxEnergyIndex == 1) {
            return if (delay12 < -2) "front_left" else "front_right"
        }
        
        // Если максимальная энергия на заднем левом
        if (maxEnergyIndex == 2) {
            return if (delay34 > 2) "second_right" else "second_left"
        }
        
        // Если максимальная энергия на заднем правом
        if (maxEnergyIndex == 3) {
            return if (delay34 < -2) "second_left" else "second_right"
        }

        // Альтернативный алгоритм по задержкам
        val isLeft = delay13 < -2  // Звук пришёл слева
        val isFront = delay12 < 2  // Звук пришёл спереди

        return when {
            isLeft && isFront -> "front_left"
            !isLeft && isFront -> "front_right"
            isLeft && !isFront -> "second_left"
            else -> "second_right"
        }
    }

    /**
     * Вычислить угол прихода звука (в градусах)
     * @param delay Задержка между микрофонами в сэмплах
     * @return Угол от -90° (слева) до +90° (справа)
     */
    fun calculateAngle(delay: Int): Float {
        // Время задержки
        val timeDelay = delay / sampleRate.toFloat()
        
        // Расстояние, которое прошёл звук
        val distance = timeDelay * SPEED_OF_SOUND

        // Ограничиваем, чтобы не вышло за пределы физики
        val ratio = min(1.0f, max(-1.0f, distance / micSpacing))

        // θ = arcsin((c * Δt) / d)
        val angleRad = acos(ratio) - (Math.PI / 2).toFloat()

        return Math.toDegrees(angleRad.toDouble()).toFloat()
    }

    /**
     * Расширить ShortArray до нужной длины (заполнить нулями)
     */
    fun ensureLength(signal: ShortArray, minLength: Int): ShortArray {
        if (signal.size >= minLength) return signal
        
        return signal.copyOf(minLength)
    }

    /**
     * Обрезать все каналы до минимальной длины
     */
    fun trimToMinLength(channels: Array<ShortArray>): Array<ShortArray> {
        val minLength = channels.minOf { it.size }
        return channels.map { it.copyOf(minLength) }.toTypedArray()
    }
}

// Extension функции
private fun List<Float>.indexOfMaxOrNull(): Int? {
    if (isEmpty()) return null
    var maxIndex = 0
    var maxValue = this[0]
    for (i in 1 until size) {
        if (this[i] > maxValue) {
            maxValue = this[i]
            maxIndex = i
        }
    }
    return maxIndex
}


