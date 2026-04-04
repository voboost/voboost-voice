package com.voboost.voiceassistant.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.qinggan.qinglink.transProxy.api.IPcmListener
import com.qinggan.qinglink.transProxy.api.IPcmModule
import java.util.concurrent.CopyOnWriteArrayList

/**
 * AudioSource через TransProxy (QGSpeechService)
 *
 * Получает PCM данные из системного сервиса через AIDL интерфейс IPcmModule.
 * Преимущества:
 * - ✅ PCM данные уже с шумоподавлением (NoiseSuppressor)
 * - ✅ PCM данные уже с эхокомпенсацией (AcousticEchoCanceler)
 * - ✅ PCM данные уже с автоматическим гейном (AutomaticGainControl)
 * - ✅ Нет конфликтов с другими приложениями
 */
class MicrophoneStreamAudioSource(private val context: Context) : AudioSource {

    companion object {
        private const val TAG = "MicrophoneStreamAudio"
        private const val INTENT_TRANSPROXY = "com.qinggan.qinglink.transproxy"
        private const val QGSPEECH_PACKAGE = "com.qinggan.sttservice"
    }

    private val listeners = CopyOnWriteArrayList<AudioSource.Listener>()
    private var pcmModule: IPcmModule? = null
    private var pcmListener: IPcmListener.Stub? = null

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isRecording = false

    override fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return true
        }

        return try {
            Log.d(TAG, "Initializing MicrophoneStreamAudioSource...")

            // Создаём слушатель для получения PCM данных
            pcmListener = object : IPcmListener.Stub() {
                @Throws(RemoteException::class)
                override fun onPcm(pcm: ByteArray) {
                    // Передаём PCM данные всем внешним слушателям
                    for (listener in listeners) {
                        try {
                            listener.onAudioData(pcm, pcm.size)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in audio listener", e)
                        }
                    }
                }

                @Throws(RemoteException::class)
                override fun onConnected() {
                    Log.d(TAG, "✅ TransProxy connected")
                    isRecording = true
                }

                @Throws(RemoteException::class)
                override fun onDisconnected() {
                    Log.w(TAG, "❌ TransProxy disconnected")
                    isRecording = false
                }
            }

            isInitialized = true
            Log.i(TAG, "✅ MicrophoneStreamAudioSource initialized successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize MicrophoneStreamAudioSource", e)
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

        try {
            Log.d(TAG, "Connecting to TransProxy service...")

            val intent = Intent(INTENT_TRANSPROXY)
            intent.setPackage(QGSPEECH_PACKAGE)

            context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    Log.i(TAG, "✅ TransProxy connected")
                    pcmModule = IPcmModule.Stub.asInterface(service)

                    // Регистрируем слушатель
                    try {
                        pcmModule?.registerPcmListener(pcmListener)
                        Log.i(TAG, "✅ PCM listener registered")
                        isRecording = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to register PCM listener", e)
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    Log.w(TAG, "❌ TransProxy disconnected")
                    pcmModule = null
                    isRecording = false
                }
            }, Context.BIND_AUTO_CREATE)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start recording", e)
        }
    }

    override fun stop() {
        if (!isRecording) {
            Log.w(TAG, "Not recording")
            return
        }

        try {
            pcmListener?.let { listener ->
                pcmModule?.unregisterPcmListener(listener)
            }
            isRecording = false
            Log.i(TAG, "✅ Recording stopped via TransProxy")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to stop recording", e)
        }
    }

    override fun release() {
        stop()

        try {
            pcmModule = null
            pcmListener = null
            isInitialized = false
            Log.i(TAG, "MicrophoneStreamAudioSource released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MicrophoneStreamAudioSource", e)
        }
    }

    override fun addListener(listener: AudioSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AudioSource.Listener) {
        listeners.remove(listener)
    }

    override fun isRecording(): Boolean {
        return isRecording
    }
}
