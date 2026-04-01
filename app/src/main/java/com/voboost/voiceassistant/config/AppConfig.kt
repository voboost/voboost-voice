package com.voboost.voiceassistant.config

import com.google.gson.annotations.SerializedName

/**
 * Основная конфигурация приложения
 * ✅ Все поля имеют значения по умолчанию
 */
data class AppConfig(
    @SerializedName("version") val version: String = "1.0",
    @SerializedName("language") val language: String = "ru-RU",
    @SerializedName("activation") val activation: ActivationConfig = ActivationConfig(),
    @SerializedName("speech") val speech: SpeechConfig = SpeechConfig(),
    @SerializedName("tts") val tts: TtsConfig = TtsConfig(),
    @SerializedName("ui") val ui: UiConfig = UiConfig(),
    @SerializedName("confirmation") val confirmation: ConfirmationConfig = ConfirmationConfig(),
    @SerializedName("phrases") val phrases: DefaultPhrases = DefaultPhrases(),
    @SerializedName("commands") val commands: List<CommandConfig> = emptyList()
)

/**
 * Конфигурация активации
 */
data class ActivationConfig(
    @SerializedName("keyword") val keyword: String = "привет машина",
    @SerializedName("alternative_keywords") val alternativeKeywords: List<String> = listOf("окей вобуст", "привет вобуст"),
    @SerializedName("button_keycode") val buttonKeycode: String = "KEYCODE_HEADSETHOOK",
    @SerializedName("button_package") val buttonPackage: String = ""
)

/**
 * Конфигурация распознавания речи
 */
data class SpeechConfig(
    @SerializedName("offline") val offline: OfflineSpeechConfig = OfflineSpeechConfig(),
    @SerializedName("online") val online: OnlineSpeechConfig = OnlineSpeechConfig()
)

data class OfflineSpeechConfig(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("engine") val engine: String = "vosk",
    @SerializedName("model") val model: String = "vosk-model-small-ru-0.22",
    @SerializedName("sample_rate") val sampleRate: Int = 16000
)

data class OnlineSpeechConfig(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("engine") val engine: String = "yandex",
    @SerializedName("api_key") val apiKey: String = "",
    @SerializedName("folder_id") val folderId: String = ""
)

/**
 * Конфигурация TTS
 */
data class TtsConfig(
    @SerializedName("offline") val offline: OfflineTtsConfig = OfflineTtsConfig(),
    @SerializedName("online") val online: OnlineTtsConfig = OnlineTtsConfig()
)

data class OfflineTtsConfig(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("engine") val engine: String = "system",
    @SerializedName("voice") val voice: String = "",
    @SerializedName("rate") val rate: Float = 1.0f,
    @SerializedName("pitch") val pitch: Float = 1.0f
)

data class OnlineTtsConfig(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("engine") val engine: String = "yandex",
    @SerializedName("api_key") val apiKey: String = ""
)

/**
 * Конфигурация UI
 */
data class UiConfig(
    @SerializedName("overlay_position") val overlayPosition: String = "center",
    @SerializedName("overlay_offset_x_dp") val overlayOffsetXDp: Int = 50,
    @SerializedName("overlay_offset_y_dp") val overlayOffsetYDp: Int = 50,
    @SerializedName("show_animation") val showAnimation: Boolean = true,
    @SerializedName("animation_duration_ms") val animationDurationMs: Long = 3000L,
    @SerializedName("toast_duration_ms") val toastDurationMs: Long = 2000L
)

/**
 * Конфигурация подтверждения
 */
data class ConfirmationConfig(
    @SerializedName("default_timeout_sec") val defaultTimeoutSec: Int = 5,
    @SerializedName("require_confirmation") val requireConfirmation: Boolean = false
)

/**
 * Фразы по умолчанию
 * ✅ Все фразы имеют значения по умолчанию (никогда не null)
 */
data class DefaultPhrases(
    @SerializedName("success") val success: String = "Выполнено",
    @SerializedName("failure") val failure: String = "Произошла ошибка",
    @SerializedName("not_understood") val notUnderstood: String = "Не поняла команду",
    @SerializedName("confirm_question") val confirmQuestion: String = "Вы уверены?",
    @SerializedName("confirm_yes") val confirmYes: String = "Да",
    @SerializedName("confirm_no") val confirmNo: String = "Нет",
    @SerializedName("listening") val listening: String = "Слушаю вас"
)