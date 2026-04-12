# 🔊 AUDIO CHANNEL FIX — ИСПРАВЛЕНИЕ КАНАЛА ВОСПРОИЗВЕДЕНИЯ

**Дата:** 2026-04-01  
**Версия:** 13.3 (Audio Channel Fix)

---

## 📋 ПРОБЛЕМА

Голосовой помощник воспроизводился в **музыкальный канал** (`USAGE_MEDIA`), из-за чего:

1. ❌ Приглушалась музыка при воспроизведении голоса
2. ❌ Голос звучал тише чем нужно
3. ❌ Неправильная категоризация аудио

---

## 🔍 АНАЛИЗ ОРИГИНАЛЬНОГО TTS

Оригинальный `QGTtsService` использует правильный аудиоканал:

### MediaPlayerImpl.java (оригинал)

```java
// Строки 119-121
AudioAttributes audioAttributesBuild = new AudioAttributes.Builder()
    .setContentType(1)   // CONTENT_TYPE_SPEECH
    .setUsage(16)        // USAGE_ASSISTANT ← ПРАВИЛЬНО!
    .build();
this.mPlayer.setAudioAttributes(audioAttributesBuild);
```

### Числовые значения

| Параметр | Значение | Константа |
|----------|----------|-----------|
| `setContentType(1)` | 1 | `CONTENT_TYPE_SPEECH` |
| `setUsage(16)` | 16 | `USAGE_ASSISTANT` |

### AudioFocusController.java (оригинал)

```java
// Запрос аудиофокуса
this.mAudioManager.requestAudioFocus(this, 3, 2);
// streamType=3 (STREAM_MUSIC)
// durationHint=2 (AUDIOFOCUS_GAIN_TRANSIENT)
```

---

## ✅ РЕШЕНИЕ

### Изменения в SherpaSynthesis.kt

**Было (неправильно):**
```kotlin
val audioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_MEDIA)  // ❌ Музыкальный канал
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()
```

**Стало (правильно):**
```kotlin
// ✅ Используем USAGE_ASSISTANT как в оригинальном QGTtsService
// Это отдельный аудиоканал для голосового помощника
// Громкость не приглушает музыку
val audioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ASSISTANT)  // ✅ Голосовой канал
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()
```

---

## 📊 АУДИОКАНАЛЫ ANDROID

| Usage | Назначение | Приглушает музыку |
|-------|------------|-------------------|
| `USAGE_MEDIA` | Музыка, видео | ❌ Да |
| `USAGE_ASSISTANT` | Голосовой помощник | ✅ Нет |
| `USAGE_VOICE_COMMUNICATION` | VoIP звонки | ✅ Нет |
| `USAGE_NOTIFICATION` | Уведомления | ⚠️ Частично |
| `USAGE_ALARM` | Будильник | ✅ Нет |

---

## 🎯 ЭФФЕКТЫ ИСПРАВЛЕНИЯ

### До исправления

```
🎵 Музыка играет (громкость 80%)
🤖 "Привет машина" → музыка приглушается до 20%
🤖 Голос звучит через музыку
```

### После исправления

```
🎵 Музыка играет (громкость 80%)
🤖 "Привет машина" → музыка НЕ приглушается
🤖 Голос звучит отдельно в канале ASSISTANT
🎵 Музыка продолжает играть на 80%
```

---

## 🔧 НАСТРОЙКА ГРОМКОСТИ

Громкость канала `ASSISTANT` регулируется отдельно:

```
Настройки → Звук → Громкость → Голосовой помощник
```

Или через ADB:
```bash
# Получить текущую громкость
adb shell settings get global volume_settings_assistant

# Установить громкость (0-15)
adb shell settings put global volume_settings_assistant 12
```

---

## 📝 ДОПОЛНИТЕЛЬНЫЕ УЛУЧШЕНИЯ

### AudioFocus (опционально)

Можно добавить запрос AudioFocus как в оригинале:

```kotlin
private fun requestAudioFocus() {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.requestAudioFocus(
        { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Получили фокус
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Потеряли фокус временно
                }
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                    // Получили фокус на короткое время
                }
            }
        },
        AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
    )
}
```

---

## 🧪 ТЕСТИРОВАНИЕ

### 1. Запустить музыку
```bash
adb shell am start -a android.intent.action.VIEW \
  -d "https://music.yandex.ru/track/123456"
```

### 2. Активировать голосовой помощник
```bash
adb shell am broadcast -a ru.voboost.voiceassistant.ACTIVATE
```

### 3. Сказать команду
```
"Привет машина"
```

### 4. Проверить поведение

**Ожидаемо:**
- ✅ Музыка НЕ приглушается
- ✅ Голос звучит чисто
- ✅ Громкость голоса регулируется отдельно

---

## 📈 СРАВНЕНИЕ

| Параметр | До | После |
|----------|----|-------|
| **Audio Usage** | `USAGE_MEDIA` | `USAGE_ASSISTANT` |
| **Канал** | Музыкальный | Голосовой |
| **Приглушение музыки** | ❌ Да | ✅ Нет |
| **Отдельная громкость** | ❌ Нет | ✅ Да |
| **Как в оригинале** | ❌ Нет | ✅ Да |

---

## ⚠️ ОГРАНИЧЕНИЯ

### SystemTtsSynthesis.kt

Системный `TextToSpeech` не позволяет изменить аудиоканал — используется то, что установила система по умолчанию для TTS.

**Решение:** Использовать Sherpa-ONNX TTS для полного контроля.

---

## 🔄 ОТКАТ

Если нужно вернуть старое поведение:

```kotlin
val audioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_MEDIA)  // Вернуть как было
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()
```

---

## 📚 ИСТОЧНИКИ

- [Android AudioAttributes](https://developer.android.com/reference/android/media/AudioAttributes)
- [AudioAttributes.USAGE_ASSISTANT](https://developer.android.com/reference/android/media/AudioAttributes#USAGE_ASSISTANT)
- Оригинал: `QGTtsService/app/src/main/java/com/qinggan/ttsservice/system/MediaPlayerImpl.java`

---

**Готово! Голос теперь воспроизводится в правильном канале! 🎉**
