package ru.voboost.voice.config

import com.google.gson.annotations.SerializedName

/**
 * Основная конфигурация приложения
 * ? Все поля имеют значения по умолчанию
 */
data class AppConfig(@SerializedName("version") val version: String = "1.0",
                     @SerializedName("language") val language: String = "ru-RU",
                     @SerializedName("activation") val activation: ActivationConfig = ActivationConfig(),
                     @SerializedName("speech") val speech: SpeechConfig = SpeechConfig(),
                     @SerializedName("tts") val tts: TtsConfig = TtsConfig(),
                     @SerializedName("nlu") val nlu: NLUConfig = NLUConfig(),
                     @SerializedName("confirmation") val confirmation: ConfirmationConfig = ConfirmationConfig(),
                     @SerializedName("phrases") val phrases: DefaultPhrases = DefaultPhrases(),
                     @SerializedName("commands") val commands: List<CommandConfig> = emptyList())

/**
 * Конфигурация активации
 */
data class ActivationConfig(
    @SerializedName("keyword") val keyword: String = "привет машина",
    @SerializedName("alternative_keywords") val alternativeKeywords: List<String> = listOf("окей вобуст",
                                                                                           "привет вобуст"),
    @SerializedName("button_keycode") val buttonKeycode: Int = 16,
                           )

/**
 * Конфигурация распознавания речи
 */
data class SpeechConfig(@SerializedName("offline") val offline: OfflineSpeechConfig = OfflineSpeechConfig(),
                        @SerializedName("online") val online: OnlineSpeechConfig = OnlineSpeechConfig())

data class OfflineSpeechConfig(@SerializedName("enabled") val enabled: Boolean = true,
                               @SerializedName("engine") val engine: String = "vosk",
                               @SerializedName("max_attempts") val maxAttempts: Int = 3)

data class OnlineSpeechConfig(@SerializedName("enabled") val enabled: Boolean = false,
                              @SerializedName("engine") val engine: String = "yandex",
                              @SerializedName("api_key") val apiKey: String = "",
                              @SerializedName("folder_id") val folderId: String = "")

/**
 * Конфигурация TTS
 */
data class TtsConfig(@SerializedName("offline") val offline: OfflineTtsConfig = OfflineTtsConfig(),
                     @SerializedName("online") val online: OnlineTtsConfig = OnlineTtsConfig())

/**
 * Конфигурация NLU
 */
data class NLUConfig(
    @SerializedName("engine") val engine: String = "onnx",
    @SerializedName("antonyms") val antonyms: List<AntonymPair> = emptyList(),
    @SerializedName("penalty_for_antonyms") val penaltyForAntonyms: Float = 0.5f
)


data class OfflineTtsConfig(@SerializedName("enabled") val enabled: Boolean = true,
                            @SerializedName("engine") val engine: String = "sherpa",
                            @SerializedName("voice") val voice: String = "",
                            @SerializedName("rate") val rate: Float = 1.0f,
                            @SerializedName("pitch") val pitch: Float = 1.0f,
                            @SerializedName("speaker") val speaker: Int = 0)

data class OnlineTtsConfig(@SerializedName("enabled") val enabled: Boolean = false,
                           @SerializedName("engine") val engine: String = "yandex",
                           @SerializedName("api_key") val apiKey: String = "")

/**
 * Конфигурация подтверждения
 */
data class ConfirmationConfig(@SerializedName("default_timeout_sec") val defaultTimeoutSec: Int = 5,
                              @SerializedName("require_confirmation") val requireConfirmation: Boolean = false)

/**
 * Фразы по умолчанию
 * ? Все фразы имеют значения по умолчанию (никогда не null)
 */
data class DefaultPhrases(@SerializedName("success") val success: String = "Выполнено",
                          @SerializedName("failure") val failure: String = "Произошла ошибка",
                          @SerializedName("not_understood") val notUnderstood: String = "Не понял",
                          @SerializedName("not_understood_retry") val notUnderstoodRetry: String = "Не понял, повторите",
                          @SerializedName("confirm_question") val confirmQuestion: String = "Вы уверены?",
                          @SerializedName("confirm_yes") val confirmYes: String = "Да",
                          @SerializedName("confirm_no") val confirmNo: String = "Нет",
                          @SerializedName("listening") val listening: String = "Слушаю",
                          @SerializedName("cancel") val cancel: String = "Отмена")

