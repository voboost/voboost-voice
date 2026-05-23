package ru.voboost.voice

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
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.speech.SpeechServiceFactory
import ru.voboost.voice.executor.CommandExecutor
import ru.voboost.voice.ui.OverlayManager
import ru.voboost.voice.services.canbus.CanBusServiceManager
import ru.voboost.voice.services.canbus.handlers.TSRSpeedLimitHandler
import ru.voboost.voice.states.StateMachine
import ru.voboost.voice.states.StateContext
import ru.voboost.voice.states.state.IdleState
import kotlinx.coroutines.*
import android.Manifest
import android.content.pm.ServiceInfo
import ru.voboost.voice.services.audiopolicy.AudioPolicyServiceManager
import ru.voboost.voice.audio.IAudioSource
import ru.voboost.voice.audio.AudioSourceFactory
import ru.voboost.voice.audio.VolumeManager
import ru.voboost.voice.services.canbus.handlers.TestCanBusServiceHandler
import ru.voboost.voice.services.canbus.handlers.VoiceButtonHandler
import ru.voboost.voice.audio.PhoneCallPoller
import ru.voboost.voice.services.qgbus.QGBusServiceManager
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.executor.VehicleCommandExecutor
import ru.voboost.voice.nlu.INLUEngine
import ru.voboost.voice.nlu.NLUEngineFactory
import ru.voboost.voice.services.recognition.RecognitionServiceFactory
import ru.voboost.voice.services.recognition.RecognitionServiceFactory.RecognitionEngine
import ru.voboost.voice.services.speech.ISpeechService

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
        const val ACTION_CANCEL = "ru.voboost.voice.CANCEL"
        const val ACTION_ACTIVATE = "ru.voboost.voice.ACTIVATE"
        val ASR_ENGINE_TYPE = RecognitionEngine.VOSK
        val AUDIO_SOURCE_TYPE = AudioSourceFactory.SourceType.ANDROID
    }

    // Компоненты системы
    private lateinit var configManager: ConfigManager
    private lateinit var stateMachine: StateMachine  // < IState Machine
    private lateinit var recognitionService: IRecognitionService  // < Распознавание речи
    private lateinit var nluEngine: INLUEngine
    private lateinit var commandExecutor: CommandExecutor
    private lateinit var overlayManager: OverlayManager
    private lateinit var speechService: ISpeechService  // < Интерфейс
    private lateinit var soundEffectManager: SoundEffectManager
    // IAudioSource - единый источник аудио данных
    private lateinit var audioSource: IAudioSource
    // CanBus Manager - единая точка доступа к CAN шине
    private lateinit var canBusManager: CanBusServiceManager
    private lateinit var audioPolicyManager: AudioPolicyServiceManager
    // Voice Button Handler - обработка кнопки на руле
    private var voiceButtonHandler: VoiceButtonHandler? = null
    // TSR Speed Limit Handler - предупреждения о превышении скорости
    private var tsrSpeedLimitHandler: TSRSpeedLimitHandler? = null
    private var testCanBusServiceHandler: TestCanBusServiceHandler? = null
    // Phone Call Poller - поллинг состояния телефона через AudioPolicyManager
    private lateinit var phoneCallPoller: PhoneCallPoller
    // Volume Manager - управление громкостью
    private var volumeManager: VolumeManager? = null
    // QGBus Service Manager для отправки уведомлений через шину событий
    private lateinit var qgbusServiceManager: QGBusServiceManager
    // Coroutines
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    // Флаг остановки сервиса (для отмены retry в startKeywordSpotting)
    private var isDestroying = false
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

        // ? СРАЗУ становимся foreground сервисом (до 5 секунд на инициализацию)
        startForeground(NOTIFICATION_ID,
                        createNotification(),
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
        // TTS Engine — выбираем согласно конфигу
        val speechConfig = configManager.getConfig().tts
        // TTS Engine (используем системный как стабильный, можно заменить на Sherpa)
        try {
            speechService = SpeechServiceFactory.create(this, configManager)
            Log.i(TAG,"TTS engine initialized (engine=${speechConfig.offline.engine}, rate=${speechConfig.offline.rate}, pitch=${speechConfig.offline.pitch})")
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS engine", e)
        }

        nluEngine = NLUEngineFactory.create(this, configManager)
        overlayManager = OverlayManager(this, configManager)
        soundEffectManager = SoundEffectManager(this)
        // IAudioSource - создаётся ОДИН раз и передаётся в движок распознавания
        audioSource = AudioSourceFactory.create(this, AUDIO_SOURCE_TYPE)
        Log.i(TAG, "IAudioSource created: ${audioSource::class.simpleName}")
        // Volume Manager - управление громкостью
        volumeManager = VolumeManager(this)
        volumeManager?.connect()
        Log.i(TAG, "VolumeManager connected - can duck/restore media volume")
        // Voice Button Handler - регистрация обработчика кнопки на руле
        voiceButtonHandler = VoiceButtonHandler(serviceScope, configManager)
        // TSR Speed Limit Handler — регистрируем через callback подключения
        tsrSpeedLimitHandler = TSRSpeedLimitHandler(speechService)
        testCanBusServiceHandler = TestCanBusServiceHandler(speechService)

        canBusManager = CanBusServiceManager(this)
        Log.i(TAG, "CanBusServiceManager initialized")
        voiceButtonHandler?.let { canBusManager.registerConnectionCallback(it) }
        tsrSpeedLimitHandler?.let {  canBusManager.registerConnectionCallback(it)}
        testCanBusServiceHandler?.let {  canBusManager.registerConnectionCallback(it)}

        // Подключаемся к AudioPolicyService после инициализации CanBus
        audioPolicyManager = AudioPolicyServiceManager(this)


        // Создаём VehicleCommandExecutor через фабрику
        val vehicleCommandExecutor = VehicleCommandExecutor(this, canBusManager)
        commandExecutor = CommandExecutor(context = this,
                                          speechService = speechService,
                                          overlayManager = overlayManager,
                                          coroutineScope = serviceScope,
                                          vehicleCommandExecutor = vehicleCommandExecutor,
                                          configManager = configManager)
        Log.i(TAG, "CommandHandler initialized")
        // SpeechRecognizer - распознавание речи (утилита без состояний)
        // Зона определяется автоматически через MultiChannelAudioSource или TDOA
        recognitionService = RecognitionServiceFactory.create(engine = ASR_ENGINE_TYPE,
                                                              audioSource = audioSource,
                                                              configManager = configManager)
        Log.i(TAG, "SpeechRecognizer initialized")
        // Phone Call Poller - поллинг состояния телефона через AudioPolicyManager
        phoneCallPoller = PhoneCallPoller(recognitionService, audioPolicyManager)

        // QGBus Service Manager для отправки уведомлений через шину событий
        qgbusServiceManager = QGBusServiceManager(this)
        
        // State Machine - управление состояниями
        val context = StateContext(soundEffectManager = soundEffectManager,
                                   recognitionService = recognitionService,
                                   overlayManager = overlayManager,
                                   volumeManager = volumeManager,
                                   speechService = speechService,
                                   configManager = configManager,
                                   nluEngine = nluEngine,
                                   commandExecutor = commandExecutor)

        stateMachine = StateMachine(serviceScope, context)
        Log.i(TAG, "IState Machine initialized")
        voiceButtonHandler?.stateMachine = stateMachine

        canBusManager.connect()
        audioPolicyManager.connect()

        // Регистрируем receiver
        val filter = IntentFilter(ACTION_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cancelReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
        else {
            registerReceiver(cancelReceiver, filter)
        }
        Log.d(TAG, "CancelReceiver registered")

        // ? Запуск поллинга состояния телефона (до распознавания)
        phoneCallPoller.start()
        
        // Подключаем QGBus Service Manager к системной шине событий
        qgbusServiceManager.connect()
        Log.i(TAG, "QGBusServiceManager connected")
        
        // Передать менеджер в OverlayManager
        overlayManager.setQGBusManager(qgbusServiceManager)

        // ? Запуск распознавания (если permission есть)
        if (hasRecordPermission()) {
            startKeywordSpotting()
        }
        else {
            Log.w(TAG, "No RECORD_AUDIO permission")
        }
    }

    private fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
                                                  Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
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
        
        // ? Устанавливаем флаг ДО отмены scope, чтобы retry знал о остановке
        isDestroying = true
        serviceScope.cancel()

        try {
            unregisterReceiver(cancelReceiver)
            Log.d(TAG, "CancelReceiver unregistered")
        }
        catch (e: Exception) {
            Log.w(TAG, "Failed to unregister receiver", e)
        }

        // Отключаем Volume Manager
        volumeManager?.disconnect()
        volumeManager = null
        Log.d(TAG, "VolumeManager disconnected")

        // Освобождаем IAudioSource (если инициализирован)
        if (::audioSource.isInitialized) {
            audioSource.stop()
            audioSource.release()
            Log.d(TAG, "IAudioSource released")
        }
        else {
            Log.w(TAG, "IAudioSource not initialized, skipping release")
        }

        canBusManager.onDisconnected()
        // Отключаем Voice Button Handler
        voiceButtonHandler?.let { canBusManager.unregisterConnectionCallback(it) }
        voiceButtonHandler?.release()
        voiceButtonHandler = null
        // Отключаем TSR Speed Limit Handler
        tsrSpeedLimitHandler?.let {  canBusManager.unregisterConnectionCallback(it)}
        tsrSpeedLimitHandler?.release()
        tsrSpeedLimitHandler = null
        // Останавливаем Phone Call Poller
        phoneCallPoller.stop()
        Log.d(TAG, "PhoneCallPoller stopped")

        // Отключаем QGBus Service Manager от системной шины событий
        qgbusServiceManager.destroy()
        Log.d(TAG, "QGBusServiceManager destroyed")

        // Отключаем CanBus Manager
        try {
            canBusManager.unbind(this)
            Log.d(TAG, "CanBusServiceManager unbound")
        }
        catch (e: Exception) {
            Log.w(TAG, "Failed to unbind CanBusServiceManager", e)
        }

        audioPolicyManager.onDisconnected()

        try{
            audioPolicyManager.unbind(this)
            Log.d(TAG, "AudioPolicyManager unbound")
        }
        catch (e: Exception){
            Log.w(TAG, "Failed to unbind AudioPolicyManager", e)
        }

        stateMachine.stop()
        recognitionService.shutdown()
        
        // Освобождаем Phone Call Poller (после shutdown speechRecognizer)
        phoneCallPoller.release()
        Log.d(TAG, "PhoneCallPoller released")
        
        speechService.release()

        soundEffectManager.release()

        // Освобождаем NLU Engine (ONNX модель и токенайзер)
        nluEngine.release()
        Log.d(TAG, "NLU Engine released")

        instance = null  // < Очищаем instance ПОСЛЕ всех операций
        super.onDestroy()
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
            // Сохраняем ссылку на сервис для проверки в withContext
            val currentService = this@VoboostVoiceService
            try {
                Log.i(TAG, "Starting keyword spotting (waiting for activation phrase)...")
                recognitionService.start()  // Запускаем непрерывный поток распознавания
                stateMachine.start()      // Запускаем IState Machine
            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to start keyword spotting", e)
                // Retry с таймером и проверкой onStopping/onDestroy
                try {
                    withContext(Dispatchers.Default) {
                        delay(3000)
                        // Проверяем, что сервис не остановлен
                        if (!currentService.isDestroying) {
                            Log.i(TAG, "Retrying keyword spotting after exception...")
                            startKeywordSpotting()
                        } else {
                            Log.w(TAG, "Service is stopping, aborting retry")
                        }
                    }
                }
                catch (retryException: Exception) {
                    // delay() может быть отменён при onDestroy()
                    Log.w(TAG, "Retry cancelled", retryException)
                }
            }
        }
    }

    /**
     * Активировать голосовой помощник (после ключевой фразы или кнопки)
     */
    private fun activateVoiceAssistant() {}

    /**
     * Отменить распознавание (повторное нажатие кнопки или от Frida)
     */
    fun cancelRecognition() {
        Log.i(TAG, "? Cancelling recognition...")
        serviceScope.launch {
            try { // IState Machine сам обработает отмену в текущем IState
                stateMachine.onButtonPressed()
                Log.i(TAG, "? Recognition cancelled")
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
                            VoiceCommandReceiver::class.java).setAction("ru.voboost.voice.NOOP")

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


