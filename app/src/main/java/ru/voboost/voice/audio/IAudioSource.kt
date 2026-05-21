package ru.voboost.voice.audio

/**
 * Абстрактный интерфейс для получения аудио-потока
 *
 * Позволяет использовать разные источники аудио:
 * - Системный RecorderManager (с шумоподавлением и эхокомпенсацией)
 * - Стандартный Android AudioRecord (fallback)
 * - MultiChannel AudioRecord (4 микрофона + определение зоны)
 *
 * Поток данных:
 * 1. Вызвать start() для начала записи
 * 2. Подписаться через addListener() для получения PCM данных + зоны
 * 3. Вызвать stop() для остановки
 */
interface IAudioSource {

    companion object {
        const val SAMPLE_RATE = 16000  // Vosk модель ожидает 16000
        const val CHANNELS = 1
        const val BITS_PER_SAMPLE = 16
    }

    /**
     * Слушатель аудио-данных
     * @param data PCM данные (16-bit, mono, 16000 Hz)
     * @param bytesRead количество прочитанных байт
     * @param zone Зона говорящего: front_left, front_right, second_left, second_right, all_location
     */
    fun interface Listener {
        fun onAudioData(data: ByteArray, bytesRead: Int, zone: String)
    }
    
    /**
     * Инициализация аудио-источника
     * @return true если успешно
     */
    fun initialize(): Boolean
    
    /**
     * Начать запись аудио
     */
    fun start()
    
    /**
     * Остановить запись аудио
     */
    fun stop()
    
    /**
     * Освободить ресурсы
     */
    fun release()
    
    /**
     * Добавить слушатель аудио-данных
     */
    fun addListener(listener: Listener)
    
    /**
     * Удалить слушатель аудио-данных
     */
    fun removeListener(listener: Listener)
    
    /**
     * Проверка состояния
     * @return true если запись активна
     */
    fun isRecording(): Boolean
}


