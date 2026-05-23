package ru.voboost.voice.nlu

import android.util.Log
import ru.voboost.voice.config.ConfigManager

/**
 * Универсальная проверка ключевых слов для активации
 * Отвечает ТОЛЬКО за определение является ли текст ключевым словом
 *
 * НЕ зависит от движка распознавания (Vosk, Sherpa, и т.д.)
 */
class NLUKeyword(private val configManager: ConfigManager) {

    companion object {
        const val TAG = "KeywordChecker"
    }

    /**
     * Проверить является ли текст ключевым словом для активации
     * @param text Распознанный текст
     * @return true если текст содержит ключевое слово
     */
    fun isActivationKeyword(text: String): Boolean {
        val isKeyword = configManager.isActivationKeyword(text)
        if (isKeyword) {
            Log.i(TAG, "?? KEYWORD DETECTED: $text")
        }
        return isKeyword
    }

    /**
     * Обновить список ключевых слов
     * @param keywords Новый список ключевых слов
     */
    fun updateKeywords(keywords: List<String>) {
        Log.d(TAG, "Keywords updated: $keywords") // ConfigManager сам обновляет ключевые слова
    }
}