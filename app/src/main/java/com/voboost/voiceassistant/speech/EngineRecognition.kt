package com.voboost.voiceassistant.speech

import android.content.Context
import android.util.Log
import com.voboost.voiceassistant.audio.AudioSource
import com.voboost.voiceassistant.config.ConfigManager
import com.voboost.voiceassistant.core.SpeechRecognition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Универсальный движок распознавания речи
 * 
 * Работает с ЛЮБЫМ движком (Vosk, Sherpa, и т.д.) через зависимости:
 * - ModelLoader → загрузка модели
 * - StreamFactory → создание потока распознавания
 * 
 * Преимущества:
 * - ✅ Нет дублирования кода для разных движков
 * - ✅ Легко добавить новый движок
 * - ✅ Вся логика в одном месте
 */
class EngineRecognition(
    private val context: Context,
    private val audioSource: AudioSource,
    private val modelLoader: ModelLoader,
    private val streamFactory: StreamFactory
) : SpeechRecognition {

    companion object {
        private const val TAG = "EngineRecognition"
    }

    private val configManager = ConfigManager.getInstance(context)
    private lateinit var recognitionEngine: RecognitionEngine
    private lateinit var stateMachine: SpeechStateMachine
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    @Volatile
    private var isInitialized = false

    init {
        Log.d(TAG, "EngineRecognition created with ${modelLoader::class.simpleName}")
    }

    // ==================== ИНТЕРФЕЙС SpeechRecognition ====================

    override suspend fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return
        }

        try {
            val modelPath = modelLoader.getModelPath()
            Log.i(TAG, "Loading model from: $modelPath")
            
            // Загружаем модель через универсальный интерфейс
            val model = modelLoader.loadModel(modelPath)
            Log.i(TAG, "Model loaded successfully")
            
            // Создаём поток распознавания
            recognitionEngine = streamFactory.create(model)
            
            // Создаём универсальную State Machine
            stateMachine = SpeechStateMachine(
                audioSource = audioSource,
                recognitionEngine = recognitionEngine,
                keywordChecker = KeywordChecker(configManager)
            )
            
            isInitialized = true
            Log.i(TAG, "EngineRecognition initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize", e)
            throw e
        }
    }

    override fun isReady(): Boolean = isInitialized

    override suspend fun startKeywordSpotting(onKeywordDetected: () -> Unit,
                                              onError: suspend (String) -> Unit) {
        if (!isInitialized) {
            Log.e(TAG, "Not initialized, call initialize() first")
            onError("Not initialized")
            return
        }

        Log.i(TAG, "Starting keyword spotting...")
        
        val listener = object : SpeechRecognitionListener {
            override fun onKeywordDetected() {
                scope.launch { onKeywordDetected() }
            }
            
            override fun onError(error: String) {
                scope.launch { onError(error) }
            }
            
            override fun onStateChanged(state: SpeechState) {
                Log.d(TAG, "State changed: $state")
            }
        }
        
        stateMachine.start(listener)
    }

    override fun startCommandListening(onCommandReceived: (String) -> Unit,
                                       onError: (String) -> Unit,
                                       onTimeout: suspend () -> Unit) {
        if (!isInitialized) {
            Log.e(TAG, "Not initialized, call initialize() first")
            onError("Not initialized")
            return
        }

        Log.i(TAG, "Starting command listening...")
        
        val listener = object : SpeechRecognitionListener {
            override fun onCommandReceived(text: String) {
                onCommandReceived(text)
            }
            
            override fun onError(error: String) {
                onError(error)
            }
            
            override suspend fun onTimeout() {
                onTimeout()
            }
            
            override fun onStateChanged(state: SpeechState) {
                Log.d(TAG, "State changed: $state")
            }
        }
        
        // Активируем и начинаем слушать команду
        stateMachine.activate()
        stateMachine.startListeningCommand(listener)
    }

    override suspend fun listenForCommand(timeout: Long): String {
        if (!isInitialized) {
            Log.e(TAG, "Not initialized, call initialize() first")
            return ""
        }

        Log.d(TAG, "listenForCommand called, timeout: ${timeout}ms")
        
        return withTimeoutOrNull(timeout) {
            suspendCoroutine<String> { continuation ->
                var resultReceived = false
                
                val listener = object : SpeechRecognitionListener {
                    override fun onCommandReceived(text: String) {
                        if (!resultReceived) {
                            resultReceived = true
                            Log.i(TAG, "listenForCommand: received '$text'")
                            continuation.resume(text)
                        }
                    }
                    
                    override fun onError(error: String) {
                        if (!resultReceived) {
                            resultReceived = true
                            Log.e(TAG, "listenForCommand: error '$error'")
                            continuation.resume("")
                        }
                    }
                    
                    override suspend fun onTimeout() {
                        if (!resultReceived) {
                            resultReceived = true
                            Log.d(TAG, "listenForCommand: timeout")
                            continuation.resume("")
                        }
                    }
                }
                
                // Активируем и начинаем слушать команду
                stateMachine.activate()
                stateMachine.startListeningCommand(listener)
            }
        } ?: run {
            Log.w(TAG, "listenForCommand: withTimeoutOrNull returned null")
            ""
        }
    }

    override fun setActivationKeywords(keywords: List<String>) {
        Log.d(TAG, "setActivationKeywords not supported, keywords are loaded from config")
    }

    override fun stop() {
        Log.i(TAG, "Stopping recognition...")
        stateMachine.stop()
    }

    override fun shutdown() {
        Log.i(TAG, "Shutting down...")
        stateMachine.shutdown()
        isInitialized = false
        Log.i(TAG, "Shutdown complete")
    }
}
