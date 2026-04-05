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
     * Сохранить конфигурацию во внешнее хранилище
     * Используется для обновления конфига без переустановки APK
     */
    fun saveExternalConfig(appConfig: AppConfig): Boolean {
        return try {
            val externalConfigDir = context.getExternalFilesDir(null) ?: return false
            val configFile = File(externalConfigDir, EXTERNAL_CONFIG_PATH)

            // Создать директорию если не существует
            if (!externalConfigDir.exists()) {
                externalConfigDir.mkdirs()
            }

            val json = gson.toJson(appConfig)
            configFile.writeText(json)

            Log.i(TAG, "Configuration saved to external storage: ${configFile.absolutePath}")
            config = appConfig // Обновить кэш
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save external config", e)
            false
        }
    }

    /**
     * Проверить наличие внешнего конфига
     */
    fun hasExternalConfig(): Boolean {
        val externalConfigDir = context.getExternalFilesDir(null) ?: return false
        val configFile = File(externalConfigDir, EXTERNAL_CONFIG_PATH)
        return configFile.exists()
    }

    /**
     * Удалить внешний конфиг (вернётся к встроенному из assets)
     */
    fun clearExternalConfig(): Boolean {
        try {
            val externalConfigDir = context.getExternalFilesDir(null) ?: return false
            val configFile = File(externalConfigDir, EXTERNAL_CONFIG_PATH)

            if (configFile.exists()) {
                configFile.delete()
                config = null // Сбросить кэш, следующая загрузка будет из assets
                Log.i(TAG, "External config cleared, will use assets config")
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear external config", e)
            return false
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
                buttonKeycode = "KEYCODE_HEADSETHOOK",
                buttonPackage = ""
            ),
            speech = SpeechConfig(
                offline = OfflineSpeechConfig(
                    enabled = true,
                    engine = "vosk",
                    model = "vosk-model-small-ru-0.22",
                    sampleRate = 16000
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
            ui = UiConfig(),
            confirmation = ConfirmationConfig(),
            phrases = DefaultPhrases(
                listening = "Слушаю вас",
                success = "Выполнено",
                failure = "Произошла ошибка",
                notUnderstood = "Не поняла команду",
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
                    phrases.notUnderstood.takeUnless { it.isNullOrEmpty() } ?: "Не поняла команду"
                PhraseType.CONFIRM_QUESTION ->
                    phrases.confirmQuestion.takeUnless { it.isNullOrEmpty() } ?: "Вы уверены?"
                PhraseType.LISTENING ->
                    phrases.listening.takeUnless { it.isNullOrEmpty() } ?: "Слушаю вас"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting default phrase for $type", e)
            // Возвращаем значение по умолчанию при любой ошибке
            when (type) {
                PhraseType.SUCCESS -> "Выполнено"
                PhraseType.FAILURE -> "Произошла ошибка"
                PhraseType.NOT_UNDERSTOOD -> "Не поняла команду"
                PhraseType.CONFIRM_QUESTION -> "Вы уверены?"
                PhraseType.LISTENING -> "Слушаю вас"
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
