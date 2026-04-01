package com.voboost.voiceassistant

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
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechRecognition
import com.voboost.voiceassistant.core.SpeechSynthesis
import com.voboost.voiceassistant.core.SpeechEngineFactory
import com.voboost.voiceassistant.nlu.NLUEngine
import com.voboost.voiceassistant.executor.CommandExecutor
import com.voboost.voiceassistant.executor.VehicleCommandExecutorFactory
import com.voboost.voiceassistant.ui.OverlayManager
import com.voboost.voiceassistant.engine.vosk.VoskRecognition
import com.voboost.voiceassistant.engine.system.SystemTtsSynthesis
import com.voboost.voiceassistant.canbus.CanBusServiceManager
import com.voboost.voiceassistant.canbus.VoiceButtonHandler
import com.voboost.voiceassistant.canbus.VoiceAssistantCallback
import com.voboost.voiceassistant.canbus.TSRSpeedLimitHandler
import com.voboost.voiceassistant.canbus.TTSCallback
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.Manifest

/**
 * Главный сервис голосового помощника
 */
class VoboostVoiceService : Service() {

    companion object {
        private const val TAG = "VoboostVoiceService"
        private const val NOTIFICATION_CHANNEL_ID = "voboost_voice_channel"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        private var instance: VoboostVoiceService? = null

        fun getInstance(): VoboostVoiceService? = instance

        const val ACTION_CANCEL = "com.voboost.voiceassistant.CANCEL"
        const val ACTION_ACTIVATE = "com.voboost.voiceassistant.ACTIVATE"
        const val ACTION_CONFIRMATION_RESPONSE = "com.voboost.voiceassistant.CONFIRMATION_RESPONSE"
        val ASR_ENGINE_TYPE = SpeechEngineFactory.RecognitionEngine.VOSK  // ← Vosk (стабильный)
        val TTS_ENGINE_TYPE = SpeechEngineFactory.SynthesisEngine.SHERPA  // ← Sherpa TTS (русский есть)
    }

    // Компоненты системы
    private lateinit var configManager: ConfigManager
    private lateinit var speechRecognition: SpeechRecognition  // ← Интерфейс
    private lateinit var nluEngine: NLUEngine
    private lateinit var commandExecutor: CommandExecutor
    private lateinit var overlayManager: OverlayManager
    private lateinit var ttsEngine: SpeechSynthesis  // ← Интерфейс
    private lateinit var soundEffectManager: SoundEffectManager

    // CanBus Manager - единая точка доступа к CAN шине
    private lateinit var canBusManager: CanBusServiceManager

    // Voice Button Handler - обработка кнопки на руле
    private var voiceButtonHandler: VoiceButtonHandler? = null
    
    // TSR Speed Limit Handler - предупреждения о превышении скорости
    private var tsrSpeedLimitHandler: TSRSpeedLimitHandler? = null

    // Coroutines
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Состояние
    @Volatile
    private var isListening = false

    @Volatile
    private var isOnline = false

    @Volatile
    private var isCommandMode = false  // Новый флаг: режим команды

    // Подтверждение команд
    @Volatile
    private var isWaitingConfirmation = false

    private var confirmationContinuation: kotlin.coroutines.Continuation<String>? = null

    // Receiver для отмены распознавания
    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_CANCEL) {
                Log.i(TAG, "Cancel request received from Frida")
                cancelRecognition()
            }
        }
    }

    // Receiver для подтверждения команд
    private val confirmationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_CONFIRMATION_RESPONSE) {
                val response = intent.getStringExtra("recognized_text") ?: ""
                Log.d(TAG, "Confirmation response received: '$response'")
                confirmationContinuation?.resume(response)
                confirmationContinuation = null
                isWaitingConfirmation = false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        instance = this

        // ✅ СРАЗУ становимся foreground сервисом (до 5 секунд на инициализацию)
        startForeground(NOTIFICATION_ID, createNotification())
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

        // TTS Engine (используем системный как стабильный, можно заменить на Sherpa)
        try {
            ttsEngine = SpeechEngineFactory.createSynthesisEngine(
                context = this,
                engine = TTS_ENGINE_TYPE
            )

            // Инициализация TTS (обязательно для Sherpa)
            ttsEngine.initialize()
            Log.i(TAG, "TTS engine initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS engine", e)
            ttsEngine = SystemTtsSynthesis(this)  // Fallback
        }

        nluEngine = NLUEngine(this)
        overlayManager = OverlayManager(this)
        soundEffectManager = SoundEffectManager(this)

        // CanBus Manager - инициализация (автоматически подключается к CanBusService)
        canBusManager = CanBusServiceManager(this)
        Log.i(TAG, "CanBusServiceManager initialized")

        // Voice Button Handler - регистрация обработчика кнопки на руле
        voiceButtonHandler = VoiceButtonHandler(canBusManager, object : VoiceAssistantCallback {
            override fun onVoiceButtonPressed() {
                Log.i(TAG, "Voice button pressed on steering wheel - activating assistant")
                // Активация по кнопке на руле
                serviceScope.launch {
                    activateVoiceAssistant()
                }
            }
        })

        // Регистрируем callback для кнопки на руле
        if (voiceButtonHandler?.register() == true) {
            Log.i(TAG, "Voice button handler registered")
        } else {
            Log.w(TAG, "Failed to register voice button handler")
        }
        
        // TSR Speed Limit Handler - предупреждения о превышении скорости
        tsrSpeedLimitHandler = TSRSpeedLimitHandler(canBusManager, object : TTSCallback {
            override fun playWarning(text: String) {
                // Воспроизводим предупреждение через TTS
                serviceScope.launch {
                    Log.i(TAG, "🔊 TSR Warning: $text")
                    ttsEngine.speak(text)
                }
            }
        })
        
        if (tsrSpeedLimitHandler?.register() == true) {
            Log.i(TAG, "TSR speed limit handler registered")
        } else {
            Log.w(TAG, "Failed to register TSR speed limit handler")
        }

        // Создаём VehicleCommandExecutor через фабрику
        // Используем HYBRID режим (телефон=Intent, остальное=AIDL)
        val vehicleCommandExecutor = VehicleCommandExecutorFactory.createFromString(
            context = this,
            canBusManager = canBusManager,  // ← Передаем CanBusManager
            modeString = "hybrid"  // "intent", "shell", "aidl", "auto", "hybrid"
        )

        commandExecutor = CommandExecutor(
            context = this,
            ttsEngine = ttsEngine,
            nluEngine = nluEngine,
            overlayManager = overlayManager,
            coroutineScope = serviceScope,
            vehicleCommandExecutor = vehicleCommandExecutor
        )

        // Speech Recognition (используем Vosk как стабильный, можно заменить на Sherpa)
        try {
            speechRecognition = SpeechEngineFactory.createRecognitionEngine(
                context = this,
                engine = ASR_ENGINE_TYPE
            )

            // Инициализация движка (обязательно для Sherpa)
            speechRecognition.initialize()
            Log.i(TAG, "Speech recognition engine initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize speech recognition", e)
            speechRecognition = VoskRecognition(this)  // Fallback
        }

        // Регистрируем receiver
        val filter = IntentFilter(ACTION_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cancelReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
        else {
            registerReceiver(cancelReceiver, filter)
        }
        Log.d(TAG, "CancelReceiver registered")

        // Регистрируем receiver для подтверждений
        val confirmationFilter = IntentFilter(ACTION_CONFIRMATION_RESPONSE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(confirmationReceiver, confirmationFilter, Context.RECEIVER_NOT_EXPORTED)
        }
        else {
            registerReceiver(confirmationReceiver, confirmationFilter)
        }
        Log.d(TAG, "ConfirmationReceiver registered")

        // ✅ Запуск распознавания (если permission есть)
        if (hasRecordPermission()) {
            startKeywordSpotting()
        } else {
            Log.w(TAG, "No RECORD_AUDIO permission, requesting...")
            requestRecordPermission()  // ← Запросить разрешение
        }
    }

    private fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordPermission() {
        // Нужно вызвать из Activity, не из Service!
        // Или использовать системный диалог через notification
        Log.d(TAG, "requestRecordPermission")
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

        try {
            unregisterReceiver(confirmationReceiver)
            Log.d(TAG, "ConfirmationReceiver unregistered")
        }
        catch (e: Exception) {
            Log.w(TAG, "Failed to unregister confirmation receiver", e)
        }

        // Отключаем Voice Button Handler
        voiceButtonHandler?.unregister()
        voiceButtonHandler = null
        Log.d(TAG, "VoiceButtonHandler unregistered")
        
        // Отключаем TSR Speed Limit Handler
        tsrSpeedLimitHandler?.unregister()
        tsrSpeedLimitHandler = null
        Log.d(TAG, "TSRSpeedLimitHandler unregistered")

        // Отключаем CanBus Manager
        try {
            canBusManager.unbind(this)
            Log.d(TAG, "CanBusServiceManager unbound")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unbind CanBusServiceManager", e)
        }

        speechRecognition.shutdown()
        ttsEngine.shutdown()
        soundEffectManager.release()

        super.onDestroy()
    }

    /**
     * Запустить детекцию кодовой фразы (постоянное прослушивание)
     */
    private fun startKeywordSpotting() {
        Log.d(TAG, "startKeywordSpotting called")
        Log.d(TAG, "  isListening=$isListening")
        Log.d(TAG, "  isCommandMode=$isCommandMode")

        if (isListening) {
            Log.w(TAG, "Already running, skipping keyword spotting")
            return
        }

        serviceScope.launch {
            try {
                Log.i(TAG, "Starting keyword spotting (waiting for activation phrase)...")
                
                speechRecognition.startKeywordSpotting(
                    onKeywordDetected = {
                        Log.i(TAG, "🎯 Keyword detected!")
                        activateVoiceAssistant()
                    },
                    onError = { error ->
                        Log.e(TAG, "Keyword spotting error: $error")
                        withContext(Dispatchers.Main) {
                            ttsEngine.speak(configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE))
                        }
                        // Возврат в keyword spotting после ошибки
                        serviceScope.launch {
                            delay(2000)
                            if (serviceScope.isActive) {
                                Log.i(TAG, "Returning to keyword spotting after error...")
                                startKeywordSpotting()
                            }
                        }
                    }
                )
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
        if (isCommandMode) {
            // Уже активен — отменяем команду и возвращаемся к ожиданию
            Log.i(TAG, "Already in command mode - CANCEL command")
            cancelCurrentCommand()
            return
        }

        activateVoiceAssistantInternal()
    }
    
    /**
     * Отменить текущую команду и вернуться к ожиданию ключевого слова
     */
    private fun cancelCurrentCommand() {
        serviceScope.launch {
            try {
                // 🗣️ Сказать "Отмена"
                withContext(Dispatchers.Main) {
                    ttsEngine.speak("Отмена")
                }
                
                // Пауза для воспроизведения
                delay(500)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during cancel", e)
            } finally {
                // Сброс всех флагов
                isCommandMode = false
                isListening = false
                isWaitingConfirmation = false
                
                // Отменить подтверждение если есть
                confirmationContinuation?.cancel()
                confirmationContinuation = null
                
                // Остановить распознавание
                cancelRecognition()
                
                // Скрыть анимацию
                withContext(Dispatchers.Main) {
                    overlayManager.hideAnimation()
                }
                
                // Очистить очередь TTS
                ttsEngine.clearQueue()
                
                // 🎵 Звук окончания
                soundEffectManager.playEndSound()
                
                Log.i(TAG, "✅ Command cancelled, returning to keyword spotting")
            }
        }
    }
    
    /**
     * Внутренняя активация (без проверок)
     */
    private fun activateVoiceAssistantInternal() {
        isCommandMode = true
        isListening = true

        serviceScope.launch {
            try { // 🎵 Звук начала распознавания
                soundEffectManager.playStartSound()

                // Показать анимацию
                withContext(Dispatchers.Main) {
                    overlayManager.showAnimation()
                }

                // Сказать что слушаем (опционально)
                val listeningPhrase = configManager.getConfig().phrases.listening
                if (!listeningPhrase.isNullOrEmpty()) {
                    ttsEngine.speak(listeningPhrase)
                }
                else {
                    Log.w(TAG, "Listening phrase is null or empty")
                }

                // 🎤 Запустить прослушивание команды
                // После получения команды модуль автоматически вернётся к keyword spotting
                speechRecognition.startCommandListening(
                    onCommandReceived = { command ->
                        Log.i(TAG, "📝 Command received: $command")
                        processVoiceCommand(command)

                        // После обработки команды - возвращаемся к ожиданию ключевой фразы
                        isCommandMode = false
                        isListening = false

                        serviceScope.launch {
                            withContext(Dispatchers.Main) {
                                soundEffectManager.playEndSound()
                                overlayManager.hideAnimation()
                            }

                            // Небольшая пауза перед возвратом к keyword spotting
                            delay(1000)

                            if (serviceScope.isActive) {
                                Log.i(TAG, "✅ Command completed - returning to keyword spotting...")
                                startKeywordSpotting()
                            }
                        }
                    }, onError = { error ->
                        Log.e(TAG, "❌ Command listening error: $error")
                        isCommandMode = false
                        isListening = false
                        serviceScope.launch {
                            withContext(Dispatchers.Main) {
                                val failurePhrase = configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)
                                if (!failurePhrase.isNullOrEmpty()) {
                                    ttsEngine.speak(failurePhrase)
                                }
                                soundEffectManager.playEndSound()
                                overlayManager.hideAnimation()
                            }

                            delay(500)
                            if (serviceScope.isActive) {
                                Log.i(TAG, "🔄 Returning to keyword spotting after error...")
                                startKeywordSpotting()
                            }
                        }
                    }, onTimeout = {  // ← Обработка таймаута
                        Log.w(TAG, "⏱️ Command timeout - returning to keyword spotting")

                        isCommandMode = false
                        isListening = false

                        serviceScope.launch {
                            withContext(Dispatchers.Main) {
                                soundEffectManager.playEndSound()
                                overlayManager.hideAnimation()
                            }
                            delay(500)

                            if (serviceScope.isActive) {
                                Log.i(TAG, "🔄 Returning to keyword spotting after timeout...")
                                startKeywordSpotting()
                            }
                        }
                    })

            }
            catch (e: Exception) {
                Log.e(TAG, "Error during voice recognition", e)
                isCommandMode = false
                isListening = false

                withContext(Dispatchers.Main) {
                    val failurePhrase = configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE)
                    if (!failurePhrase.isNullOrEmpty()) {
                        ttsEngine.speak(failurePhrase)
                    }
                    soundEffectManager.playEndSound()
                    overlayManager.hideAnimation()
                }

                delay(500)
                if (serviceScope.isActive) {
                    startKeywordSpotting()
                }
            }
        }
    }

    /**
     * Отменить распознавание (повторное нажатие кнопки или от Frida)
     */
    fun cancelRecognition() {
        if (!isListening) {
            Log.w(TAG, "Not listening, ignoring cancel request")
            return
        }

        Log.i(TAG, "❌ Cancelling recognition...")

        serviceScope.launch {
            try {
                // 🎵 Звук отмены
                soundEffectManager.playCancelSound()

                // Скрыть анимацию
                withContext(Dispatchers.Main) {
                    overlayManager.hideAnimation()
                }

                // Сброс состояния
                isCommandMode = false
                isListening = false

                Log.i(TAG, "✅ Recognition cancelled")

                // Пауза перед возвратом к keyword spotting
                delay(1000)

                if (serviceScope.isActive) {
                    Log.i(TAG, "🔄 Returning to keyword spotting after cancel...")
                    startKeywordSpotting()
                }

            }
            catch (e: Exception) {
                Log.e(TAG, "Error cancelling recognition", e)
            }
        }
    }

    /**
     * Обработать голосовую команду
     */
    fun processVoiceCommand(text: String) {
        serviceScope.launch {
            try { // Парсинг команды
                val recognizedCommand = nluEngine.parseCommand(text)

                if (recognizedCommand != null) {
                    Log.i(TAG, "Command parsed: ${recognizedCommand.id}")
                    commandExecutor.executeCommand(recognizedCommand)
                }
                else {
                    Log.w(TAG, "Command not recognized: $text")
                    commandExecutor.handleUnrecognizedCommand(text)
                }

            }
            catch (e: Exception) {
                Log.e(TAG, "Error processing command", e)
                withContext(Dispatchers.Main) {
                    ttsEngine.speak(configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE))
                    overlayManager.showToast(configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE))
                }
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
                            VoiceCommandReceiver::class.java).setAction("com.voboost.voiceassistant.NOOP")

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

    /**
     * Обновить состояние сети
     */
    fun updateNetworkState(isOnline: Boolean) {
        this.isOnline = isOnline
        Log.d(TAG, "Network state updated: isOnline=$isOnline")
    }

    /**
     * Запросить голосовое подтверждение (для CommandExecutor)
     * @param question Текст вопроса (например, "Подтверждаете открытие окна?")
     * @param timeout Таймаут в миллисекундах
     * @return Ответ пользователя ("да", "нет" или пустая строка при таймауте)
     */
    suspend fun requestConfirmation(question: String, timeout: Long = 3000): String {
        Log.d(TAG, "Requesting confirmation: '$question', timeout: ${timeout}ms")

        // Сказать вопрос
        ttsEngine.speak(question)

        // Запустить распознавание ответа
        return kotlinx.coroutines.withTimeoutOrNull(timeout) {
            suspendCoroutine<String> { continuation ->
                confirmationContinuation = continuation
                isWaitingConfirmation = true

                // Запустить короткое прослушивание
                serviceScope.launch {
                    try {
                        val response = speechRecognition.listenForCommand(timeout = timeout)
                        Log.d(TAG, "Confirmation response: '$response'")
                        confirmationContinuation = null
                        isWaitingConfirmation = false
                        continuation.resume(response)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during confirmation", e)
                        confirmationContinuation = null
                        isWaitingConfirmation = false
                        continuation.resume("")
                    }
                }
            }
        } ?: run {
            Log.w(TAG, "Confirmation timeout")
            confirmationContinuation = null
            isWaitingConfirmation = false
            ""
        }
    }
}