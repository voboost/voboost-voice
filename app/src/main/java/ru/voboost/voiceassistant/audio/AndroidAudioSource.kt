package ru.voboost.voiceassistant.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

/**
 * IAudioSource на основе стандартного Android AudioRecord
 *
 * Используется как fallback если системный RecorderManager недоступен.
 * Применяет аудио-эффекты (шумоподавление, эхокомпенсация, авто-гейн)
 * если они доступны на устройстве.
 */
class AndroidAudioSource(private val context: Context,
                         private val sampleRate: Int = IAudioSource.SAMPLE_RATE, // VOICE_RECOGNITION = 6, оптимизирован для распознавания речи (как в MyVoya)
                         private val audioSource: Int = MediaRecorder.AudioSource.VOICE_RECOGNITION) :
        IAudioSource {

    companion object {
        const val TAG = "AndroidAudioSource"
        private const val BUFFER_SIZE_MS = 20 // размер буфера в миллисекундах
    }

    private val listeners = CopyOnWriteArrayList<IAudioSource.Listener>()
    private var audioRecord: AudioRecord? = null
    private var recordThread: Thread? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var aec: AcousticEchoCanceler? = null
    private var agc: AutomaticGainControl? = null

    @Volatile
    private var isRecording = false

    @Volatile
    private var isInitialized = false

    override fun initialize(): Boolean {

        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return true
        }

        return try { // Создаём AudioRecord с retry
            audioRecord = tryCreateAudioRecordWithRetry()

            if (audioRecord == null) {
                Log.e(TAG, "❌ AudioRecord failed to initialize after retries")
                return false
            }

            Log.i(TAG, "✅ AudioRecord state=${audioRecord?.state}")

            // Применяем аудио-эффекты (как в MyVoya)
            applyAudioEffects()

            isInitialized = true
            Log.i(TAG, "✅ AudioRecord initialized successfully")
            true

        }
        catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize AudioRecord", e)
            false
        }
    }

    /**
     * Создать AudioRecord с retry каждые 1 сек
     * Пробуем БЕСКОНЕЧНО, чтобы сервис не упал, пока микрофон занят
     */
    private fun tryCreateAudioRecordWithRetry(): AudioRecord? {
        var attempts = 1

        // Ждём 3 минуты чтобы dvr_service освободил микрофон после BOOT
       // Log.i(TAG, "⏳ Waiting 3 minutes before first attempt (letting dvr_service release mic)...")
      //  Thread.sleep(1000L * 60 * 3)

        while (true) { // Бесконечный цикл

            Log.d(TAG, "Attempt #$attempts: creating AudioRecord...")

            if (checkPermission()) {

                val record = tryCreateAudioRecord()

                if (record != null) {
                    Log.i(TAG, "✅ AudioRecord created on attempt #$attempts, state=${record.state}")
                    return record
                }

                attempts++
            }
            else {
                Log.w(TAG, "⏳ No permission yet, waiting...")
            }

            Log.w(TAG, "⏳ Waiting before retry (AudioFlinger needs time to release session)...")
            Thread.sleep(minOf(1000L * attempts, 60000L))
        }
    }

    /**
     * Безопасно создать AudioRecord с конкретным источником (через Builder)
     */
    @SuppressLint("MissingPermission")
    private fun tryCreateAudioRecord(): AudioRecord? {
        try {

            var bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                                                          AudioFormat.CHANNEL_IN_MONO,
                                                          AudioFormat.ENCODING_PCM_16BIT)

            Log.d(TAG, "AudioRecord created: bufferSize=$bufferSize")
            if (bufferSize <= 0) {
                return null
            }

            // Используем минимум 2x от системного минимума для стабильности
            bufferSize = max(bufferSize * 2, sampleRate * 2 * BUFFER_SIZE_MS / 1000)

            Log.d(TAG,
                  "Creating AudioRecord via Builder: source=${MediaRecorder.AudioSource.VOICE_RECOGNITION}, sampleRate=$sampleRate, bufferSize=$bufferSize")

            val format = AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_IN_MONO).build()

            val record =
                AudioRecord.Builder().setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                    .setAudioFormat(format).setBufferSizeInBytes(bufferSize).build()

            val state = record.state
            Log.d(TAG, "AudioRecord created: state=$state (0=UNINITIALIZED, 1=INITIALIZED)")

            if (state == AudioRecord.STATE_INITIALIZED) {
                Log.i(TAG,
                      "✅ AudioRecord initialized with source=${MediaRecorder.AudioSource.VOICE_RECOGNITION} (via Builder)")

                return record
            }
            else {
                Log.w(TAG, "⚠️ AudioRecord state=$state after build, releasing")
                record?.release()
                return null
            }
        }
        catch (e: UnsupportedOperationException) {
            Log.w(TAG, "⚠️ AudioRecord.Builder UnsupportedOperationException: ${e.message}")
            return null
        }
        catch (e: RuntimeException) {
            Log.w(TAG, "⚠️ AudioRecord.Builder RuntimeException: ${e.message}")
            return null
        }
        catch (e: Exception) {
            Log.w(TAG, "⚠️ Unexpected error: ${e.message}")
            return null
        }
    }

    /**
     * Применяет аудио-эффекты для улучшения качества записи
     * (как в MyVoya RealOverlayActivity)
     */
    private fun applyAudioEffects() {
        val sessionId = audioRecord?.audioSessionId ?: return

        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(sessionId)
            if (noiseSuppressor != null) {
                Log.i(TAG, "✅ NoiseSuppressor enabled")
            }
            else {
                Log.w(TAG, "NoiseSuppressor not supported")
            }
        }
        else {
            Log.d(TAG, "NoiseSuppressor not available")
        }

        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(sessionId)
            if (aec != null) {
                Log.i(TAG, "✅ AcousticEchoCanceler enabled")
            }
            else {
                Log.w(TAG, "AcousticEchoCanceler not supported")
            }
        }
        else {
            Log.d(TAG, "AcousticEchoCanceler not available")
        }

        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(sessionId)
            if (agc != null) {
                Log.i(TAG, "✅ AutomaticGainControl enabled")
            }
            else {
                Log.w(TAG, "AutomaticGainControl not supported")
            }
        }
        else {
            Log.d(TAG, "AutomaticGainControl not available")
        }
    }

    private fun checkPermission(): Boolean {
        val status = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        return status == PackageManager.PERMISSION_GRANTED
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

            // Запускаем поток чтения аудио
            recordThread = Thread(AudioRecordRunnable(recorder), "AudioRecord-Thread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }

            Log.i(TAG, "✅ Recording started")

        }
        catch (e: Exception) {
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

        }
        catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }

    override fun release() {
        stop()

        try {
            noiseSuppressor?.release()
            aec?.release()
            agc?.release()

            audioRecord?.release()
            audioRecord = null
            isInitialized = false

            Log.i(TAG, "AudioRecord released")
        }
        catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord", e)
        }
    }

    override fun addListener(listener: IAudioSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IAudioSource.Listener) {
        listeners.remove(listener)
    }

    override fun isRecording(): Boolean = isRecording

    // Runnable для чтения аудио-данных в отдельном потоке
    private inner class AudioRecordRunnable(initialRecorder: AudioRecord) : Runnable {

        @Volatile
        private var recorder: AudioRecord = initialRecorder

        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            val bufferSize = recorder.bufferSizeInFrames * 2
            val buffer = ByteArray(bufferSize)

            Log.d(TAG, "AudioRecord thread started, buffer size: $bufferSize")

            try {
                while (isRecording && !Thread.currentThread().isInterrupted) {
                    val bytesRead = recorder.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        val dataCopy = buffer.copyOf(bytesRead)
                        for (listener in listeners) {
                            try {
                                CoroutineScope(Dispatchers.IO).launch {
                                    // AndroidAudioSource всегда возвращает front_left (водитель)
                                    listener.onAudioData(dataCopy, bytesRead, "front_left")
                                }
                            }
                            catch (e: Exception) {
                                Log.e(TAG, "Listener error", e)
                            }
                        }
                    }
                    else if (bytesRead < 0) {
                        Log.e(TAG,
                              "AudioRecord read error: $bytesRead")
                    }
                }
            }
            catch (e: InterruptedException) {
                Log.d(TAG, "AudioRecord thread interrupted")
            }
            catch (e: Exception) {
                Log.e(TAG, "AudioRecord thread exception", e)
            }
        }
    }
}
