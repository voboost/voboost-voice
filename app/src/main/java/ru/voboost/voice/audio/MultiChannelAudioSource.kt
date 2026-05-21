package ru.voboost.voice.audio

import android.util.Log
import com.qinggan.audiorecord.record.NativeRecord
import com.qinggan.audiorecord.record.api.IRecordListener
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Многоканальный IAudioSource для записи с 4 микрофонов
 *
 * Использует системную библиотеку libSpeechRecord4Mic.so через NativeRecord JNI wrapper:
 * - Захват с 4 микрофонов + 2 reference канала для AEC
 * - TDOA анализ для определения зоны говорящего
 *
 * @param sampleRate Частота дискретизации (по умолчанию 16000 Гц)
 * @param micSpacing Расстояние между микрофонами в метрах (по умолчанию 15 см)
 * @param channelCount Желаемое количество каналов (4 или 6 с AEC)
 */
class MultiChannelAudioSource(private val sampleRate: Int,
                              private val micSpacing: Float,
                              private val channelCount: Int) : IAudioSource {

    companion object {
        const val TAG = "MultiChannelAudioSource"
        const val ZONE_FRONT_LEFT = "front_left"
        const val ZONE_FRONT_RIGHT = "front_right"
        const val ZONE_SECOND_LEFT = "second_left"
        const val ZONE_SECOND_RIGHT = "second_right"
        const val ZONE_ALL_LOCATION = "all_location"
        private const val MIC_COUNT = 4
    }

    private val listeners = CopyOnWriteArrayList<IAudioSource.Listener>()
    private val nativeRecord = NativeRecord.getInstance()
    private val tdoaDetector = TdoaZoneDetector(micSpacing, sampleRate)

    @Volatile
    private var isRecording = false

    @Volatile
    private var isInitialized = false

    @Volatile
    private var lastDetectedZone: String = ZONE_FRONT_LEFT

    @Volatile
    private var actualChannelCount: Int = channelCount

    // Слушатель для NativeRecord — сигнатура: onData(byte[], int)
    private val recordListener = object : IRecordListener {
        override fun onData(data: ByteArray, length: Int) {
            if (length <= 0 || data.isEmpty()) {
                Log.w(TAG, "Empty audio data received")
                return
            }

            try { // Демультиплексируем interleaved PCM
                val channels = demuxChannels(data, length, actualChannelCount)

                if (channels.size >= 2) {
                    val zone = tdoaDetector.detectZone(channels)
                    lastDetectedZone = zone
                    val interleavedData = muxChannels(channels)

                    for (listener in listeners) {
                        try {
                            listener.onAudioData(interleavedData, interleavedData.size, zone)
                        }
                        catch (e: Exception) {
                            Log.e(TAG, "Listener callback error", e)
                        }
                    }
                }
                else {
                    Log.w(TAG, "Single channel audio, using default zone")
                    for (listener in listeners) {
                        try {
                            listener.onAudioData(data, length, ZONE_ALL_LOCATION)
                        }
                        catch (e: Exception) {
                            Log.e(TAG, "Listener callback error", e)
                        }
                    }
                }
            }
            catch (e: Exception) {
                Log.e(TAG, "Error processing audio data", e)
                for (listener in listeners) {
                    try {
                        listener.onAudioData(data, length, ZONE_ALL_LOCATION)
                    }
                    catch (ex: Exception) {
                        Log.e(TAG, "Fallback listener error", ex)
                    }
                }
            }
        }

        override fun onStart() {
            Log.i(TAG, "✅ Recording started (channels: $actualChannelCount)")
            isRecording = true
        }

        override fun onStop() {
            Log.i(TAG, "✅ Recording stopped")
            isRecording = false
        }
    }

    override fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return true
        }

        return try {
            Log.d(TAG, "Initializing: channels=$channelCount, sampleRate=$sampleRate")

            // ✅ Правильный вызов: только количество каналов
            nativeRecord.setChannelNumb(channelCount)

            // NativeRecord не принимает sampleRate — используем дефолт из библиотеки
            actualChannelCount = channelCount.coerceIn(2, 6)

            isInitialized = true
            Log.i(TAG, "✅ Initialized (channels=$actualChannelCount)")
            true
        }
        catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize", e)
            false
        }
    }

    override fun start() {
        if (!isInitialized) {
            Log.e(TAG, "Not initialized, call initialize() first")
            return
        }
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }

        Log.d(TAG, "Starting recording...")

        // ✅ 1. Сначала регистрируем слушателя через базовый класс
        nativeRecord.addRecordListener(recordListener)

        // ✅ 2. Затем запускаем запись (без параметров!)
        nativeRecord.startRecord()

        Log.i(TAG, "✅ Recording started via libSpeechRecord4Mic.so")
    }

    override fun stop() {
        if (!isRecording) {
            Log.w(TAG, "Not recording")
            return
        }
        Log.d(TAG, "Stopping recording...")
        nativeRecord.stopRecord()
    }

    override fun release() {
        stop()
        try { // ✅ Удаляем слушателя перед release()
            nativeRecord.removeRecordListener(recordListener)
            nativeRecord.release()
            isInitialized = false
            Log.i(TAG, "✅ Released")
        }
        catch (e: Exception) {
            Log.e(TAG, "Error releasing", e)
        }
    }

    override fun addListener(listener: IAudioSource.Listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            Log.d(TAG, "Listener added, total: ${listeners.size}")
        }
    }

    override fun removeListener(listener: IAudioSource.Listener) {
        if (listeners.remove(listener)) {
            Log.d(TAG, "Listener removed, total: ${listeners.size}")
        }
    }

    override fun isRecording(): Boolean = isRecording

    fun getLastDetectedZone(): String = lastDetectedZone
    fun getActualChannelCount(): Int = actualChannelCount

    private fun demuxChannels(buffer: ByteArray,
                              bytesRead: Int,
                              totalChannels: Int): Array<ShortArray> {
        val bytesPerSample = 2
        val frameSize = totalChannels * bytesPerSample
        if (frameSize <= 0 || bytesRead < frameSize) return emptyArray()

        val framesCount = bytesRead / frameSize
        if (framesCount <= 0) return emptyArray()

        val outputChannels = totalChannels.coerceIn(1, MIC_COUNT)
        val channels = Array(outputChannels) { ShortArray(framesCount) }

        for (frame in 0 until framesCount) {
            for (ch in 0 until outputChannels) {
                val offset = (frame * frameSize) + (ch * bytesPerSample)
                if (offset + 1 < bytesRead) {
                    val sample =
                        ((buffer[offset].toInt() and 0xFF) or (buffer[offset + 1].toInt() shl 8)).toShort()
                    channels[ch][frame] = sample
                }
            }
        }
        return channels
    }

    private fun muxChannels(channels: Array<ShortArray>): ByteArray {
        if (channels.isEmpty()) return ByteArray(0)
        val framesCount = channels.minOfOrNull { it.size } ?: 0
        if (framesCount <= 0) return ByteArray(0)

        val channelCount = channels.size
        val output = ByteArray(framesCount * channelCount * 2)

        for (frame in 0 until framesCount) {
            for (ch in 0 until channelCount) {
                val offset = (frame * channelCount + ch) * 2
                val sample = channels[ch].getOrNull(frame) ?: 0
                output[offset] = (sample.toInt() and 0xFF).toByte()
                output[offset + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
            }
        }
        return output
    }

    fun isMultiChannelSupported(): Boolean = true

    fun getDebugInfo(): Map<String, Any> = mapOf("requestedChannels" to channelCount,
                                                 "actualChannels" to actualChannelCount,
                                                 "sampleRate" to sampleRate,
                                                 "isInitialized" to isInitialized,
                                                 "isRecording" to isRecording,
                                                 "lastZone" to lastDetectedZone)
}

