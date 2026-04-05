package com.voboost.voiceassistant.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

/**
 * AudioSource на основе стандартного Android AudioRecord
 * 
 * Используется как fallback если системный RecorderManager недоступен.
 * Применяет аудио-эффекты (шумоподавление, эхокомпенсация, авто-гейн)
 * если они доступны на устройстве.
 */
class AndroidAudioSource(
    private val context: Context,
    private val sampleRate: Int = AudioSource.SAMPLE_RATE,
    // VOICE_RECOGNITION = 6, оптимизирован для распознавания речи (как в MyVoya)
    private val audioSource: Int = MediaRecorder.AudioSource.VOICE_RECOGNITION
) : AudioSource {
    
    companion object {
        private const val TAG = "AndroidAudioSource"
        private const val BUFFER_SIZE_MS = 20 // размер буфера в миллисекундах
    }
    
    private val listeners = CopyOnWriteArrayList<AudioSource.Listener>()
    private var audioRecord: AudioRecord? = null
    private var recordThread: Thread? = null
    
    @Volatile
    private var isRecording = false
    
    @Volatile
    private var isInitialized = false
    
    override fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return true
        }

        if (!hasRecordPermission()) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            return false
        }

        return try {
            val bufferSize = calculateBufferSize()

            // Пробуем VOICE_RECOGNITION, если не работает — fallback на MIC
            audioRecord = tryCreateAudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                bufferSize
            ) ?: tryCreateAudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize, state: ${audioRecord?.state}")
                audioRecord?.release()
                audioRecord = null
                return false
            }

            // Применяем аудио-эффекты (как в MyVoya)
            applyAudioEffects()

            isInitialized = true
            Log.i(TAG, "✅ AudioRecord initialized successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize AudioRecord", e)
            false
        }
    }

    /**
     * Безопасно создать AudioRecord с конкретным источником
     */
    private fun tryCreateAudioRecord(source: Int, sampleRate: Int, bufferSize: Int): AudioRecord? {
        return try {
            Log.d(TAG, "Trying AudioRecord source=$source, sampleRate=$sampleRate, bufferSize=$bufferSize")
            val record = AudioRecord(
                source,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (record.state == AudioRecord.STATE_INITIALIZED) {
                Log.i(TAG, "✅ AudioRecord initialized with source=$source")
                record
            } else {
                Log.w(TAG, "⚠️ AudioRecord not initialized (state=${record.state}), releasing")
                record.release()
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to create AudioRecord with source=$source: ${e.message}")
            null
        }
    }
    
    /**
     * Рассчитывает размер буфера на основе длительности
     */
    private fun calculateBufferSize(): Int {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        // Используем минимум 2x от системного минимума для стабильности
        return max(minBufferSize * 2, sampleRate * 2 * BUFFER_SIZE_MS / 1000)
    }
    
    /**
     * Применяет аудио-эффекты для улучшения качества записи
     * (как в MyVoya RealOverlayActivity)
     */
    private fun applyAudioEffects() {
        val sessionId = audioRecord?.audioSessionId ?: return
        
        if (NoiseSuppressor.isAvailable()) {
            val ns = NoiseSuppressor.create(sessionId)
            if (ns != null) {
                Log.i(TAG, "✅ NoiseSuppressor enabled")
            } else {
                Log.w(TAG, "NoiseSuppressor not supported")
            }
        } else {
            Log.d(TAG, "NoiseSuppressor not available")
        }
        
        if (AcousticEchoCanceler.isAvailable()) {
            val aec = AcousticEchoCanceler.create(sessionId)
            if (aec != null) {
                Log.i(TAG, "✅ AcousticEchoCanceler enabled")
            } else {
                Log.w(TAG, "AcousticEchoCanceler not supported")
            }
        } else {
            Log.d(TAG, "AcousticEchoCanceler not available")
        }
        
        if (AutomaticGainControl.isAvailable()) {
            val agc = AutomaticGainControl.create(sessionId)
            if (agc != null) {
                Log.i(TAG, "✅ AutomaticGainControl enabled")
            } else {
                Log.w(TAG, "AutomaticGainControl not supported")
            }
        } else {
            Log.d(TAG, "AutomaticGainControl not available")
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
        
        val recorder = audioRecord ?: run {
            Log.e(TAG, "AudioRecord is null")
            return
        }

        try {
            recorder.startRecording()
            isRecording = true

            // Запускаем поток чтения аудио с правильным приоритетом
            recordThread = Thread(AudioRecordRunnable(recorder), "AudioRecord-Thread").apply {
                // Thread.MAX_PRIORITY = 10, что соответствует высокому приоритету
                // Thread.MIN_PRIORITY = 1, NORM_PRIORITY = 5
                priority = Thread.MAX_PRIORITY
                start()
            }

            Log.i(TAG, "✅ Recording started")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start recording", e)
            isRecording = false
        }
    }
    
    override fun stop() {
        if (!isRecording) {
            Log.w(TAG, "Not recording")
            return
        }
        
        isRecording = false
        
        try {
            recordThread?.join(1000) // Ждём завершения потока
            recordThread = null
            
            audioRecord?.stop()
            Log.i(TAG, "✅ Recording stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }
    
    override fun release() {
        stop()
        
        try {
            audioRecord?.release()
            audioRecord = null
            isInitialized = false
            Log.i(TAG, "AudioRecord released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord", e)
        }
    }
    
    override fun addListener(listener: AudioSource.Listener) {
        listeners.add(listener)
    }
    
    override fun removeListener(listener: AudioSource.Listener) {
        listeners.remove(listener)
    }
    
    override fun isRecording(): Boolean = isRecording
    
    private fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Runnable для чтения аудио-данных в отдельном потоке
     */
    private inner class AudioRecordRunnable(private val recorder: AudioRecord) : Runnable {
        
        override fun run() {
            val bufferSize = recorder.bufferSizeInFrames
            val buffer = ByteArray(bufferSize)
            
            Log.d(TAG, "AudioRecord thread started, buffer size: $bufferSize")
            
            try {
                while (isRecording && !Thread.currentThread().isInterrupted) {
                    val bytesRead = recorder.read(buffer, 0, buffer.size)
                    
                    when (bytesRead) {
                        in 1..Int.MAX_VALUE -> {
                            // Успешное чтение — передаём данные слушателям
                            for (listener in listeners) {
                                try {
                                    listener.onAudioData(buffer, bytesRead)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error in listener", e)
                                }
                            }
                        }
                        
                        AudioRecord.ERROR_INVALID_OPERATION -> {
                            Log.e(TAG, "AudioRecord ERROR_INVALID_OPERATION")
                            break
                        }
                        
                        AudioRecord.ERROR_BAD_VALUE -> {
                            Log.e(TAG, "AudioRecord ERROR_BAD_VALUE")
                            break
                        }
                        
                        AudioRecord.ERROR_DEAD_OBJECT -> {
                            Log.e(TAG, "AudioRecord ERROR_DEAD_OBJECT")
                            // Пробуем продолжить
                            Thread.sleep(100)
                        }
                        
                        else -> {
                            Log.w(TAG, "AudioRecord read returned: $bytesRead")
                        }
                    }
                }
            } catch (e: InterruptedException) {
                Log.d(TAG, "AudioRecord thread interrupted")
            } catch (e: Exception) {
                Log.e(TAG, "AudioRecord thread exception", e)
            } finally {
                Log.d(TAG, "AudioRecord thread exited")
            }
        }
    }
}
