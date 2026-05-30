package ru.voboost.voice.config

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.Gson

/**
 * Менеджер конфигурации
 * Загружает и кэширует конфигурацию из:
 * /storage/emulated/0/voboost/config.json (внешнее хранилище)
 * Если не найден — создаёт конфигурацию по умолчанию.
 *
 * Данные на внешнем хранилище не удаляются при перезагрузке или очистке данных приложения.
 */
class ConfigManager private constructor(context: Context) {
    private var config: AppConfig? = null
    private val gson = Gson()

    companion object {
        const val TAG = "ConfigManager"

        @SuppressLint("StaticFieldLeak")
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
     * Путь: /storage/emulated/0/voboost/config.json (внешнее хранилище)
     * Если не найден — конфигурация по умолчанию.
     */
    fun loadConfig(): AppConfig {
        config?.let { return it }

        // Пробуем загрузить конфиг из внешнего хранилища
        val loadedConfig = loadConfigFile()
        if (loadedConfig != null) {
            config = loadedConfig
            return loadedConfig
        }
        // Не найден — используем дефолтный
        return createDefaultConfig()
    }

    /**
     * Загрузить config.json из внешнего хранилища
     * @return AppConfig или null если файл не найден
     */
    private fun loadConfigFile(): AppConfig? {
        try {
            val configFile = ExternalStoragePaths.configFile

            if (!configFile.exists()) {
                Log.d(TAG, "Config not found: ${configFile.absolutePath}")
                return null
            }

            Log.i(TAG, "Found config: ${configFile.absolutePath}")

            val json = configFile.readText()
            val loadedConfig = gson.fromJson(json, AppConfig::class.java)
            Log.i(TAG, "Config loaded successfully, version: ${loadedConfig.version}")
            return loadedConfig

        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to load config", e)
            return null
        }
    }

    /**
     * Создать конфигурацию по умолчанию (если config.json не найден)
     */
    private fun createDefaultConfig(): AppConfig {
        Log.w(TAG, "Using default configuration")
        return AppConfig(version = "1.0",
                         language = "ru-RU",
                         activation = ActivationConfig(
                             keyword = "привет машина",
                             alternativeKeywords = listOf("окей вобуст", "привет вобуст"),
                             buttonKeycode = 16,
                         ),
                         speech = SpeechConfig(offline = OfflineSpeechConfig(enabled = true,
                                                                             engine = "vosk"),
                                               online = OnlineSpeechConfig(enabled = false,
                                                                           engine = "yandex",
                                                                           apiKey = "",
                                                                           folderId = "")),
                         tts = TtsConfig(offline = OfflineTtsConfig(enabled = true,
                                                                    engine = "system",
                                                                    voice = "",
                                                                    rate = 1.0f,
                                                                    pitch = 1.0f),
                                         online = OnlineTtsConfig(enabled = false,
                                                                  engine = "yandex",
                                                                  apiKey = "")),
                         confirmation = ConfirmationConfig(),
                         phrases = DefaultPhrases(listening = "Слушаю вас",
                                                  success = "Выполнено",
                                                  failure = "Произошла ошибка",
                                                  notUnderstood = "Не понял команду",
                                                  confirmQuestion = "Вы уверены?",
                                                  confirmYes = "Да",
                                                  confirmNo = "Нет"),
                         commands = emptyList())
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
        }
        catch (e: Exception) {
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
                PhraseType.SUCCESS -> phrases.success.takeUnless { it.isNullOrEmpty() }
                    ?: "Выполнено"
                PhraseType.FAILURE -> phrases.failure.takeUnless { it.isNullOrEmpty() }
                    ?: "Произошла ошибка"
                PhraseType.NOT_UNDERSTOOD -> phrases.notUnderstood.takeUnless { it.isNullOrEmpty() }
                    ?: "Не понял"
                PhraseType.NOT_UNDERSTOOD_RETRY -> phrases.notUnderstoodRetry.takeUnless { it.isNullOrEmpty() }
                    ?: "Не понял, повторите команду"
                PhraseType.CONFIRM_QUESTION -> phrases.confirmQuestion.takeUnless { it.isNullOrEmpty() }
                    ?: "Вы уверены?"
                PhraseType.LISTENING -> phrases.listening.takeUnless { it.isNullOrEmpty() }
                    ?: "Слушаю"
                PhraseType.CANCEL -> phrases.cancel.takeUnless { it.isNullOrEmpty() } ?: "Отмена"
            }
        }
        catch (e: Exception) {
            Log.e(TAG,
                  "Error getting default phrase for $type",
                  e) // Возвращаем значение по умолчанию при любой ошибке
            when (type) {
                PhraseType.SUCCESS -> "Выполнено"
                PhraseType.FAILURE -> "Произошла ошибка"
                PhraseType.NOT_UNDERSTOOD -> "Не понял"
                PhraseType.NOT_UNDERSTOOD_RETRY -> "Не понял, повторите команду"
                PhraseType.CONFIRM_QUESTION -> "Вы уверены?"
                PhraseType.LISTENING -> "Слушаю"
                PhraseType.CANCEL -> "Отмена"
            }
        }
    }

    enum class PhraseType {
        SUCCESS,
        FAILURE,
        NOT_UNDERSTOOD,
        NOT_UNDERSTOOD_RETRY,
        CONFIRM_QUESTION,
        LISTENING,
        CANCEL
    }
}


