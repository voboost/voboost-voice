package com.voboost.voiceassistant.config

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.IOException

/**
 * Менеджер конфигурации
 * Загружает и кэширует конфигурацию с приоритетом:
 * 1. Внешний конфиг на SD-карте (/sdcard/Android/data/.../files/config.json)
 * 2. Встроенный конфиг из assets/config.json (fallback)
 */
class ConfigManager private constructor(private val context: Context) {
    private var config: AppConfig? = null
    private val gson = Gson()

    companion object {
        const val TAG = "ConfigManager"
        private const val CONFIG_PATH = "config.json"
        private const val EXTERNAL_CONFIG_PATH = "config.json"

        @Volatile
        private var instance: ConfigManager? = null

        fun getInstance(context: Context): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: ConfigManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Загрузить конфигурацию
     * Приоритет: внешний конфиг на SD-карте → assets
     */
    fun loadConfig(): AppConfig {
        config?.let { return it }

        // Пробуем загрузить внешний конфиг
        val externalConfig = loadExternalConfig()
        if (externalConfig != null) {
            Log.i(TAG, "Configuration loaded from external storage")
            config = externalConfig
            return externalConfig
        }

        // Fallback: загружаем из assets
        return try {
            context.assets.open(CONFIG_PATH).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                val loadedConfig = gson.fromJson(json, AppConfig::class.java)
                config = loadedConfig
                Log.i(TAG, "Configuration loaded from assets (fallback), version: ${loadedConfig.version}")
                loadedConfig
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load configuration from assets", e)
            // Возвращаем конфигурацию по умолчанию
            createDefaultConfig()
        }
    }

    /**
     * Загрузить внешний конфиг с SD-карты
     * @return AppConfig или null если файл не найден
     */
    private fun loadExternalConfig(): AppConfig? {
        try {
            val externalConfigDir = context.getExternalFilesDir(null)
            val configFile = File(externalConfigDir, EXTERNAL_CONFIG_PATH)

            if (!configFile.exists()) {
                Log.d(TAG, "External config not found: ${configFile.absolutePath}")
                return null
            }

            Log.i(TAG, "Found external config: ${configFile.absolutePath}")

            val json = configFile.readText()
            val loadedConfig = gson.fromJson(json, AppConfig::class.java)
            Log.i(TAG, "External config loaded successfully, version: ${loadedConfig.version}")
            return loadedConfig

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load external config", e)
            return null
        }
    }

    /**
     * Создать конфигурацию по умолчанию (если config.json не найден)
     */
    private fun createDefaultConfig(): AppConfig {
        Log.w(TAG, "Using default configuration")
        return AppConfig(
            version = "1.0",
            language = "ru-RU",
            activation = ActivationConfig(
                keyword = "привет машина",
                alternativeKeywords = listOf("окей вобуст", "привет вобуст"),
                buttonKeycode = 16,
            ),
            speech = SpeechConfig(
                offline = OfflineSpeechConfig(
                    enabled = true,
                    engine = "vosk"
                ),
                online = OnlineSpeechConfig(
                    enabled = false,
                    engine = "yandex",
                    apiKey = "",
                    folderId = ""
                )
            ),
            tts = TtsConfig(
                offline = OfflineTtsConfig(
                    enabled = true,
                    engine = "system",
                    voice = "",
                    rate = 1.0f,
                    pitch = 1.0f
                ),
                online = OnlineTtsConfig(
                    enabled = false,
                    engine = "yandex",
                    apiKey = ""
                )
            ),
            confirmation = ConfirmationConfig(),
            phrases = DefaultPhrases(
                listening = "Слушаю вас",
                success = "Выполнено",
                failure = "Произошла ошибка",
                notUnderstood = "Не понял команду",
                confirmQuestion = "Вы уверены?",
                confirmYes = "Да",
                confirmNo = "Нет"
            ),
            commands = emptyList()
        )
    }

    /**
     * Получить конфигурацию (кэшированную)
     */
    fun getConfig(): AppConfig {
        return config ?: loadConfig()
    }

    /**
     * Получить команду по ID
     */
    fun getCommandById(id: String): CommandConfig? {
        return getConfig().commands.find { it.id == id }
    }

    /**
     * Проверить, является ли текст кодовой фразой
     */
    fun isActivationKeyword(text: String): Boolean {
        try {
            val config = getConfig()
            val normalizedText = text.lowercase().trim()

            // Проверка основной фразы
            if (normalizedText == config.activation.keyword.lowercase()) {
                return true
            }

            // Проверка альтернативных фраз
            return config.activation.alternativeKeywords.any {
                normalizedText == it.lowercase()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking activation keyword", e)
            return false
        }
    }

    /**
     * Получить фразу по умолчанию
     * ВСЕГДА возвращает строку (никогда null)
     */
    fun getDefaultPhrase(type: PhraseType): String {
        return try {
            val config = getConfig()
            val phrases = config.phrases

            when (type) {
                PhraseType.SUCCESS ->
                    phrases.success.takeUnless { it.isNullOrEmpty() } ?: "Выполнено"
                PhraseType.FAILURE ->
                    phrases.failure.takeUnless { it.isNullOrEmpty() } ?: "Произошла ошибка"
                PhraseType.NOT_UNDERSTOOD ->
                    phrases.notUnderstood.takeUnless { it.isNullOrEmpty() } ?: "Не понял"
                PhraseType.CONFIRM_QUESTION ->
                    phrases.confirmQuestion.takeUnless { it.isNullOrEmpty() } ?: "Вы уверены?"
                PhraseType.LISTENING ->
                    phrases.listening.takeUnless { it.isNullOrEmpty() } ?: "Слушаю"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting default phrase for $type", e)
            // Возвращаем значение по умолчанию при любой ошибке
            when (type) {
                PhraseType.SUCCESS -> "Выполнено"
                PhraseType.FAILURE -> "Произошла ошибка"
                PhraseType.NOT_UNDERSTOOD -> "Не понял"
                PhraseType.CONFIRM_QUESTION -> "Вы уверены?"
                PhraseType.LISTENING -> "Слушаю"
            }
        }
    }

    enum class PhraseType {
        SUCCESS,
        FAILURE,
        NOT_UNDERSTOOD,
        CONFIRM_QUESTION,
        LISTENING
    }
}
