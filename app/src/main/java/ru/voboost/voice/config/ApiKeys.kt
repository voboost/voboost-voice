package ru.voboost.voice.config

/**
 * API ключи для внешних сервисов
 * 
 * ВАЖНО: Не коммитьте этот файл с заполненными ключами!
 * Добавьте его в .gitignore
 * 
 * Для получения ключей Yandex Cloud:
 * 1. https://cloud.yandex.ru/docs/speechkit/stt/
 * 2. https://cloud.yandex.ru/docs/speechkit/tts/
 */
object ApiKeys {
    // Yandex SpeechKit (STT - распознавание речи)
    const val YANDEX_SPEECH_API_KEY = ""  // Вставить API ключ
    const val YANDEX_FOLDER_ID = ""        // Вставить Folder ID из Yandex Cloud
    
    // Yandex TTS (синтез речи)
    const val YANDEX_TTS_API_KEY = ""      // Вставить API ключ
    
    // Флаг: использовать ли онлайн режим
    val isOnlineEnabled: Boolean
        get() = YANDEX_SPEECH_API_KEY.isNotEmpty() && 
                YANDEX_FOLDER_ID.isNotEmpty()
    
    // Проверка наличия ключа для TTS
    val isTtsOnlineEnabled: Boolean
        get() = YANDEX_TTS_API_KEY.isNotEmpty()
}


