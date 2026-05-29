package ru.voboost.voice.config

import com.google.gson.annotations.SerializedName

/**
 * Конфигурация отдельной команды
 */
data class CommandConfig(@SerializedName("id") val id: String,
                         @SerializedName("enabled") val enabled: Boolean,
                         @SerializedName("patterns") val patterns: List<String>,
                         @SerializedName("show_notification") val showNotification: Boolean = false,
                         @SerializedName("confirmation") val confirmation: ConfirmationCommandConfig,
                         @SerializedName("phrases") val phrases: CommandPhrases?) {
    companion object {
        val Empty: CommandConfig = CommandConfig("",
                                                 false,
                                                 emptyList(),
                                                 false,
                                                 ConfirmationCommandConfig.Empty,
                                                 CommandPhrases.Empty)
    }
}

/**
 * Конфигурация подтверждения для команды
 */
data class ConfirmationCommandConfig(@SerializedName("required") val required: Boolean,
                                     @SerializedName("timeout_sec") val timeoutSec: Int? = null,
                                     @SerializedName("question") val question: String? = null,
                                     @SerializedName("yes_patterns") val yesPatterns: List<String>? = null,
                                     @SerializedName("no_patterns") val noPatterns: List<String>? = null) {
    companion object {
        val Empty: ConfirmationCommandConfig = ConfirmationCommandConfig(false,
                                                                         0,
                                                                         null,
                                                                         emptyList(),
                                                                         emptyList())
    }
}

/**
 * Фразы для команды
 */
data class CommandPhrases(@SerializedName("success") val success: String,
                          @SerializedName("failure") val failure: String,
                          @SerializedName("confirmed") val confirmed: String? = null,
                          @SerializedName("cancelled") val cancelled: String? = null) {
    companion object {
        val Empty: CommandPhrases = CommandPhrases("",
                                                   "",
                                                   "",
                                                   "")

    }
}

