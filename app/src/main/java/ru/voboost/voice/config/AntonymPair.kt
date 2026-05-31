package ru.voboost.voice.config

import com.google.gson.annotations.SerializedName

/**
 * Пара антонимов для NLU
 */
data class AntonymPair(
    @SerializedName("word") val word: String,
    @SerializedName("opposite") val opposite: String
)
