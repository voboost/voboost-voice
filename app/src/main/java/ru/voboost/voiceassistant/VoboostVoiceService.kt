package ru.voboost.voiceassistant

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.core.SpeechEngineFactory
import ru.voboost.voiceassistant.core.SpeechEngineFactory.RecognitionEngine
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.ui.OverlayManager
import ru.voboost.voiceassistant.engine.system.SystemTtsSynthesis
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.canbus.VoiceButtonHandler
import ru.voboost.voiceassistant.canbus.VoiceAssistantCallback
import ru.voboost.voiceassistant.canbus.TSRSpeedLimitHandler
import ru.voboost.voiceassistant.canbus.TTSCallback
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.speech.state.StateMachine
import ru.voboost.voiceassistant.speech.state.StateContext
import ru.voboost.voiceassistant.speech.state.IdleState
import ru.voboost.voiceassistant.speech.CommandHandler
import kotlinx.coroutines.*
import android.Manifest
import android.content.pm.ServiceInfo
import ru.voboost.voiceassistant.audio.IAudioSource
import ru.voboost.voiceassistant.audio.AudioSourceFactory
import ru.voboost.voiceassistant.audio.VolumeManager
import ru.voboost.voiceassistant.config.ExternalStoragePaths
import ru.voboost.voiceassistant.executor.VehicleCommandExecutor

/**
 * Главный сервис голосового помощника
 */
class VoboostVoiceService : Service() {

    companion object {
        const val TAG = "VoboostVoiceService"
        private const val NOTIFICATION_CHANNEL_ID = "voboost_voice_channel"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        private var instance: VoboostVoiceService? = null

        fun getInstance(): VoboostVoiceService? = instance

        const val ACTION_CANCEL = "ru.voboost.voiceassistant.CANCEL"
        const val ACTION_ACTIVATE = "ru.voboost.voiceassistant.ACTIVATE"
        val ASR_ENGINE_TYPE = RecognitionEngine.VOSK  // ← Vosk (стабильный)
        val AUDIO_SOURCE_TYPE = AudioSourceFactory.SourceType.ANDROID  // ← TransProxy (системный, с шумоподавлением)
    }

    // Компоненты системы
    private lateinit var configManager: ConfigManager
    private lateinit var stateMachine: StateMachine  // ← IState Machine
    private lateinit var speechRecognizer: SpeechRecognizer  // ← Распознавание речи
    private lateinit var commandHandler: CommandHandler  // ← Обработка команд
    private lateinit var nluEngine: NLUEngine
    private lateinit var commandExecutor: CommandExecutor
    private lateinit var overlayManager: OverlayManager
    private lateinit var ttsEngine: ISpeechSynthesis  // ← Интерфейс
    private lateinit var soundEffectManager: SoundEffectManager
    
    // IAudioSource - единый источник аудио данных
    private lateinit var audioSource: IAudioSource

    // CanBus Manager - единая точка доступа к CAN шине
    private lateinit var canBusManager: CanBusServiceManager

    // Voice Button Handler - обработка кнопки на руле
    private var voiceButtonHandler: VoiceButtonHandler? = null

    // TSR Speed Limit Handler - предупреждения о превышении скорости
    private var tsrSpeedLimitHandler: TSRSpeedLimitHandler? = null

    // Volume Manager - управление громкостью
    private var volumeManager: VolumeManager? = null

    // Coroutines
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Receiver для отмены распознавания
    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_CANCEL) {
                Log.i(TAG, "Cancel request received from Frida")
                cancelRecognition()
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        instance = this

        // ✅ СРАЗУ становимся foreground сервисом (до 5 секунд на инициализацию)
        startForeground(NOTIFICATION_ID, createNotification(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        Log.d(TAG, "Foreground service started")

        configManager = ConfigManager.getInstance(this)

        try {
            val config = configManager.loadConfig()
            Log.i(TAG, "Config loaded: version ${config.version}")
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to load config", e)
        }

        // Инициализация модулей в фоне (не блокируем main thread)
        serviceScope.launch {
            initializeEngines()
        }
    }

    /**
     * Инициализация движков в фоне
     */
    private suspend fun initializeEngines() {
        // Используем фабрику для создания движков (легко переключаться между Vosk/Sherpa)
        val recommendedConfig = SpeechEngineFactory.getRecommendedConfig(this)
        Log.i(TAG, "Recommended config: ASR=${recommendedConfig.first}, TTS=${recommendedConfig.second}")

        // TTS Engine — выбираем согласно конфигу
        val ttsConfig = configManager.getConfig().tts
        val ttsEngineType = when (ttsConfig.offline.engine.lowercase()) {
            "sherpa" -> {
                Log.i(TAG, "TTS engine from config: Sherpa-ONNX")
                SpeechEngineFactory.SynthesisEngine.SHERPA
            }
            else -> {
                Log.i(TAG, "TTS engine from config: System TTS")
                SpeechEngineFactory.SynthesisEngine.SYSTEM
            }
        }

        // TTS Engine (используем системный как стабильный, можно заменить на Sherpa)
        try {
            val sherpaTtsModePath = ExternalStoragePaths.sherpaTtsModelDir.absolutePath
            val speaker = ttsConfig.offline.speaker
            ttsEngine = SpeechEngineFactory.createSynthesisEngine(
                this, ttsEngineType,
                sherpaTtsModePath,speaker
            )

            // Инициализация TTS (обязательно для Sherpa)
            ttsEngine.initialize()

            // Настройки rate/pitch из конфига
            ttsEngine.setRate(ttsConfig.offline.rate)
            ttsEngine.setPitch(ttsConfig.offline.pitch)

            Log.i(TAG, "TTS engine initialized (engine=${ttsConfig.offline.engine}, rate=${ttsConfig.offline.rate}, pitch=${ttsConfig.offline.pitch})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS engine", e)
            ttsEngine = SystemTtsSynthesis(this)  // Fallback
        }

        nluEngine = NLUEngine(this)
        overlayManager = OverlayManager(this)
        soundEffectManager = SoundEffectManager(this)
        
        // IAudioSource - создаётся ОДИН раз и передаётся в движок распознавания
        audioSource = AudioSourceFactory.create(this, AUDIO_SOURCE_TYPE)
        Log.i(TAG, "IAudioSource created: ${audioSource::class.simpleName}")
        
        // Volume Manager - управление громкостью
        volumeManager = VolumeManager(this)
        volumeManager?.connect()
        Log.i(TAG, "VolumeManager connected - can duck/restore media volume")

        // CanBus Manager - инициализация (автоматически подключается к CanBusService)
        canBusManager = CanBusServiceManager(this)
        Log.i(TAG, "CanBusServiceManager initialized")

        // Voice Button Handler - регистрация обработчика кнопки на руле
        // Voice Button Handler — регистрируем через callback подключения
        voiceButtonHandler = VoiceButtonHandler(this,canBusManager, object : VoiceAssistantCallback {
            override fun onVoiceButtonPressed() {
                Log.i(TAG, "Voice button pressed on steering wheel - activating assistant")
                serviceScope.launch {
                    activateVoiceAssistant()
                }
            }
        })

        // TSR Speed Limit Handler — регистрируем через callback подключения
        tsrSpeedLimitHandler = TSRSpeedLimitHandler(canBusManager, object : TTSCallback {
            override fun playWarning(text: String) {
                serviceScope.launch {
                    Log.i(TAG, "🔊 TSR Warning: $text")
                    ttsEngine.speak(text)
                }
            }
        })

        // Регистрируем callback подключения — обработчики регистрируются когда CanBusService готов
        canBusManager.addConnectionCallback(object : ru.voboost.voiceassistant.canbus.ConnectionCallback {
            override fun onConnected() {
                Log.i(TAG, "CanBusService connected — registering handlers")
                registerCanBusHandlers()
            }

            override fun onDisconnected() {
                Log.w(TAG, "CanBusService disconnected")
            }
        })

        // Если уже подключён — регистрируем сразу
        if (canBusManager.isConnected()) {
            Log.i(TAG, "CanBusService already connected — registering handlers immediately")
            registerCanBusHandlers()
        }

        // Создаём VehicleCommandExecutor через фабрику
        // Все 15 команд (включая телефонные) выполняются через единый executeByCommandId()
        val canBusManager = CanBusServiceManager(this)
        val vehicleCommandExecutor = VehicleCommandExecutor(this, canBusManager)

        commandExecutor = CommandExecutor(
            context = this,
            ttsEngine = ttsEngine,
            nluEngine = nluEngine,
            overlayManager = overlayManager,
            coroutineScope = serviceScope,
            vehicleCommandExecutor = vehicleCommandExecutor
        )

        // Command Handler - обработка команд
        commandHandler = CommandHandler(nluEngine, commandExecutor)
        Log.i(TAG, "CommandHandler initialized")

        // SpeechRecognizer - распознавание речи (утилита без состояний)
        // Зона определяется автоматически через MultiChannelAudioSource или TDOA
        speechRecognizer = SpeechEngineFactory.createSpeechRecognizer(
            context = this,
            engine = ASR_ENGINE_TYPE,
            audioSource = audioSource
        )
        Log.i(TAG, "SpeechRecognizer initialized")

        // State Machine - управление состояниями
        val context = StateContext()
        context.soundEffectManager = soundEffectManager
        context.speechRecognizer = speechRecognizer
        context.overlayManager = overlayManager
        context.volumeManager = volumeManager
        context.ttsEngine = ttsEngine
        context.configManager = configManager
        context.nluEngine = nluEngine
        context.commandExecutor = commandExecutor

        stateMachine = StateMachine(
            speechRecognizer = speechRecognizer,
            overlayManager = overlayManager,
            volumeManager = volumeManager,
            ttsEngine = ttsEngine,
            configManager = configManager,
            nluEngine = nluEngine,
            commandExecutor = commandExecutor,
            scope = serviceScope,
            context = context
        )

        Log.i(TAG, "IState Machine initialized")

        // Регистрируем receiver
        val filter = IntentFilter(ACTION_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cancelReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
        else {
            registerReceiver(cancelReceiver, filter)
        }
        Log.d(TAG, "CancelReceiver registered")

        // ✅ Запуск распознавания (если permission есть)
        if (hasRecordPermission()) {
            startKeywordSpotting()
        } else {
            Log.w(TAG, "No RECORD_AUDIO permission")
        }
    }

    private fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        startForeground(NOTIFICATION_ID, createNotification())

        when (intent?.action) {
            ACTION_ACTIVATE -> {
                Log.i(TAG, "Activate request received from button")
                activateVoiceAssistant()
            }

            ACTION_CANCEL -> {
                Log.i(TAG, "Cancel request received from button")
                cancelRecognition()
            }

            else -> {
                Log.d(TAG, "Service started, keyword spotting active")
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")

        instance = null
        serviceScope.cancel()

        try {
            unregisterReceiver(cancelReceiver)
            Log.d(TAG, "CancelReceiver unregistered")
        }
        catch (e: Exception) {
            Log.w(TAG, "Failed to unregister receiver", e)
        }

        // Отключаем Voice Button Handler
        voiceButtonHandler?.unregister()
        voiceButtonHandler = null
        Log.d(TAG, "VoiceButtonHandler unregistered")
        
        // Отключаем TSR Speed Limit Handler
        tsrSpeedLimitHandler?.unregister()
        tsrSpeedLimitHandler = null
        Log.d(TAG, "TSRSpeedLimitHandler unregistered")
        
        // Отключаем Volume Manager
        volumeManager?.disconnect()
        volumeManager = null
        Log.d(TAG, "VolumeManager disconnected")

        // Освобождаем IAudioSource (если инициализирован)
        if (::audioSource.isInitialized) {
            audioSource.stop()
            audioSource.release()
            Log.d(TAG, "IAudioSource released")
        } else {
            Log.w(TAG, "IAudioSource not initialized, skipping release")
        }

        // Отключаем CanBus Manager
        try {
            canBusManager.unbind(this)
            Log.d(TAG, "CanBusServiceManager unbound")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unbind CanBusServiceManager", e)
        }

        stateMachine.stop()
        speechRecognizer.shutdown()
        ttsEngine.shutdown()
        soundEffectManager.release()

        super.onDestroy()
    }

    /**
     * Зарегистрировать CAN-bus обработчики (кнопка, TSR)
     * Вызывается когда CanBusService подключён
     */
    private fun registerCanBusHandlers() {
        if (voiceButtonHandler?.register() == true) {
            Log.i(TAG, "✅ Voice button handler registered")
        } else {
            Log.w(TAG, "❌ Failed to register voice button handler")
        }

        if (tsrSpeedLimitHandler?.register() == true) {
            Log.i(TAG, "✅ TSR speed limit handler registered")
        } else {
            Log.w(TAG, "❌ Failed to register TSR speed limit handler")
        }
    }

    /**
     * Запустить детекцию кодовой фразы (постоянное прослушивание)
     */
    private fun startKeywordSpotting() {
        Log.d(TAG, "startKeywordSpotting called")
        Log.d(TAG, "  IState=${stateMachine.getCurrentState()::class.simpleName}")

        if (stateMachine.getCurrentState() !is IdleState) {
            Log.w(TAG, "Not in IdleState, skipping keyword spotting")
            return
        }

        serviceScope.launch {
            try {
                Log.i(TAG, "Starting keyword spotting (waiting for activation phrase)...")
                speechRecognizer.start()  // Запускаем непрерывный поток распознавания
                stateMachine.start()      // Запускаем IState Machine
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start keyword spotting", e)
                // Попытка перезапуска через 3 секунды
                serviceScope.launch {
                    delay(3000)
                    if (serviceScope.isActive) {
                        Log.i(TAG, "Retrying keyword spotting after exception...")
                        startKeywordSpotting()
                    }
                }
            }
        }
    }

    /**
     * Активировать голосовой помощник (после ключевой фразы или кнопки)
     */
    private fun activateVoiceAssistant() {
        Log.i(TAG, "Activating voice assistant...")
        
        serviceScope.launch {
            try {
                // IState Machine сам обработает активацию в текущем IState
                stateMachine.activate()
            } catch (e: Exception) {
                Log.e(TAG, "Error activating voice assistant", e)
            }
        }
    }

    /**
     * Отменить распознавание (повторное нажатие кнопки или от Frida)
     */
    fun cancelRecognition() {
        Log.i(TAG, "❌ Cancelling recognition...")

        serviceScope.launch {
            try {
                // IState Machine сам обработает отмену в текущем IState
                stateMachine.onButtonPressed()
                Log.i(TAG, "✅ Recognition cancelled")

            }
            catch (e: Exception) {
                Log.e(TAG, "Error cancelling recognition", e)
            }
        }
    }

    /**
     * Создать notification для foreground сервиса
     */
    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification(): Notification {
        createNotificationChannel()

        val intent = Intent(this,
                            VoiceCommandReceiver::class.java).setAction("ru.voboost.voiceassistant.NOOP")

        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text)).setSmallIcon(R.drawable.ic_voice)
            .setContentIntent(pendingIntent).setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE).build()
    }

    /**
     * Создать notification канал
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                                              getString(R.string.notification_channel_name),
                                              NotificationManager.IMPORTANCE_LOW).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}