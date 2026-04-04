# 🎤 НОВАЯ АРХИТЕКТУРА AUDIO SOURCE

**Дата:** 2026-04-04
**Версия:** 14.0 (AudioSource Refactoring)
**Статус:** ✅ ГОТОВО К ТЕСТИРОВАНИЮ

---

## 📋 ЧТО ИЗМЕНИЛОСЬ

### Проблема
Прямая работа с `AudioRecord` вызывала нестабильность:
- `trackRecorder() failed, riid is -1`
- Падения микрофона при активации кнопкой
- Конфликты с другими приложениями (телефон, музыка)

### Решение
Разделили ответственность:
1. **AudioSource** — получение PCM данных из системы
2. **SpeechRecognitionModule** — распознавание через Vosk

---

## 🏗️ НОВАЯ АРХИТЕКТУРА

```
VoboostVoiceService
└── SpeechRecognitionModule (Vosk распознавание)
    └── AudioSource (абстрактный интерфейс)
        ├── RecorderManagerAudioSource (системный, с шумоподавлением) ← РЕКОМЕНДУЕТСЯ
        └── AndroidAudioSource (fallback, стандартный AudioRecord)
```

### Поток данных

```
Микрофон автомобиля
    ↓
RecorderManager (QGSpeechService) ← уже применено шумоподавление
    ↓
IDataListener.onData(byte[] data) ← PCM данные
    ↓
AudioSource.Listener ← наш слушатель
    ↓
SpeechRecognitionModule.accumulate() ← накопление буфера
    ↓
Vosk Recognizer.acceptWaveForm() ← распознавание
    ↓
Текст → NLU → Команда
```

---

## 📁 НОВЫЕ ФАЙЛЫ

| Файл | Описание |
|------|----------|
| `audio/AudioSource.kt` | Абстрактный интерфейс для аудио-источника |
| `audio/RecorderManagerAudioSource.kt` | Обёртка над системным RecorderManager (reflection) |
| `audio/AndroidAudioSource.kt` | Fallback на стандартный AudioRecord с эффектами |
| `audio/AudioSourceFactory.kt` | Фабрика для автоматического выбора источника |

### Изменённые файлы

| Файл | Изменения |
|------|-----------|
| `speech/SpeechRecognitionModule.kt` | Полностью переписан для работы через AudioSource |
| `engine/vosk/VoskRecognition.kt` | Добавлен параметр audioSourceType |
| `core/SpeechEngineFactory.kt` | Добавлен параметр audioSourceType |
| `VoboostVoiceService.kt` | Добавлена константа AUDIO_SOURCE_TYPE |

---

## 🔧 КАК ЭТО РАБОТАЕТ

### 1. RecorderManagerAudioSource (рекомендуется)

Подключается к системному `RecorderManager` из `QGSpeechService.apk` через reflection:

```kotlin
val audioSource = RecorderManagerAudioSource(context, recordType = "aosp")
audioSource.initialize()
audioSource.addListener { data, bytesRead ->
    // PCM данные уже с шумоподавлением!
    voskRecognizer.acceptWaveForm(data, bytesRead)
}
audioSource.start()
```

**Преимущества:**
- ✅ Уже применено шумоподавление (NoiseSuppressor)
- ✅ Уже применена эхокомпенсация (AcousticEchoCanceler)
- ✅ Уже применён автоматический гейн (AutomaticGainControl)
- ✅ Используется правильный аудио-источник для автомобиля
- ✅ Нет конфликтов с другими приложениями

**Как подключается:**
1. Находит класс `com.qinggan.audiorecord.record.RecorderManager`
2. Вызывает `getInstance()` для получения singleton
3. Вызывает `init(Context, "aosp")` для инициализации
4. Создаёт прокси для `IDataListener` через reflection
5. Вызывает `addListener(proxy)` и `startRecord()`

### 2. AndroidAudioSource (fallback)

Использует стандартный `AudioRecord` с применёнными аудио-эффектами:

```kotlin
val audioSource = AndroidAudioSource(context)
audioSource.initialize()
audioSource.addListener { data, bytesRead ->
    voskRecognizer.acceptWaveForm(data, bytesRead)
}
audioSource.start()
```

**Особенности:**
- Применяет NoiseSuppressor, AcousticEchoCanceler, AutomaticGainControl (если доступны)
- Использует `MediaRecorder.AudioSource.MIC` (совместимо с авто)
- Автоматический расчёт размера буфера

---

## 🎯 КАК ТЕСТИРОВАТЬ

### Проверка доступности RecorderManager

```bash
adb shell pm list packages | grep qinggan.speech
# Должно быть: package:com.qinggan.speech (QGSpeechService)
```

### Логи при запуске

```
AudioSourceFactory: ✅ Using RecorderManagerAudioSource (system)
SpeechRecognition: ✅ AudioSource initialized: RecorderManagerAudioSource
```

Или если RecorderManager недоступен:

```
AudioSourceFactory: ⚠️ RecorderManager unavailable, falling back to AndroidAudioSource
SpeechRecognition: ✅ AudioSource initialized: AndroidAudioSource
```

### Переключение на fallback

Если нужно принудительно использовать стандартный AudioRecord:

```kotlin
// VoboostVoiceService.kt
val AUDIO_SOURCE_TYPE = AudioSourceFactory.SourceType.ANDROID
```

---

## 📊 СРАВНЕНИЕ ВАРИАНТОВ

| Характеристика | RecorderManager | Android AudioRecord |
|----------------|-----------------|---------------------|
| **Шумоподавление** | ✅ Уже в системе | ✅ Применяем сами |
| **Эхокомпенсация** | ✅ Уже в системе | ✅ Применяем сами |
| **Авто-гейн** | ✅ Уже в системе | ✅ Применяем сами |
| **Стабильность** | ✅ Высокая (системный) | ⚠️ Зависит от устройства |
| **Конфликты** | ✅ Нет (разделяет доступ) | ⚠️ Возможны |
| **Зависимости** | Требует QGSpeechService | Стандартный Android |

---

## 🔍 ТИПЫ ЗАПИСИ RECORDERMANAGER

RecorderManager поддерживает разные типы записи:

| Тип | Описание | Каналы |
|-----|----------|--------|
| `"native"` | JNI библиотеки (SpeechRecord) | 4-6 (многомикрофонный) |
| `"aosp"` | Стандартный Android AudioRecord | 1 (моно) |
| `"primary"` | Локальный Android Recorder | 1 (моно) |
| `"nuance"` | Nuance Record (для их движка) | 1 (моно) |
| `"iflytek"` | iFlytek Record | 6 (многомикрофонный) |
| `"hardaec"` | С аппаратным AEC | 1 (моно) |

**Мы используем:** `"aosp"` — самый универсальный

---

## 🚀 СЛЕДУЮЩИЕ ШАГИ

### 1. Протестировать на устройстве

```bash
# Сборка
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleRelease

# Установка
adb root
adb remount
adb push app\build\outputs\apk\release\app-release-unsigned.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk

# Перезапуск
adb shell am force-stop com.voboost.voiceassistant
adb shell am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService

# Логи
adb logcat -s AudioSourceFactory:* SpeechRecognition:* VoboostVoiceService:*
```

### 2. Проверить работу кнопки

- Нажать кнопку на руле
- Убедиться что распознавание работает
- Проверить логи на отсутствие `trackRecorder() failed`

### 3. Проверить ключевую фразу

- Сказать "Привет машина"
- Убедиться что активация проходит
- Проверить распознавание команд

---

## 🐛 ВОЗМОЖНЫЕ ПРОБЛЕМЫ

### RecorderManager not found

**Симптомы:**
```
AudioSourceFactory: ❌ RecorderManager class not found — QGSpeechService not installed
```

**Решение:**
- Убедиться что `QGSpeechService.apk` установлен в системе
- Или использовать `AudioSourceFactory.SourceType.ANDROID`

### Permission denied

**Симптомы:**
```
AndroidAudioSource: RECORD_AUDIO permission not granted
```

**Решение:**
```bash
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
```

### Нет распознавания после активации

**Симптомы:**
- TTS говорит "Слушаю вас"
- Но команды не распознаются

**Проверить:**
```bash
adb logcat -s SpeechRecognition:*
# Должно быть: "Listening for command..."
# И потом: "Recognized: <текст>"
```

---

## 📝 АРХИТЕКТУРНЫЕ РЕШЕНИЯ

### Почему reflection для RecorderManager?

1. `RecorderManager` находится в системном APK (`QGSpeechService`)
2. Мы не можем скомпилироваться с ним напрямую
3. Reflection позволяет подключиться динамически
4. Если класс не найден — fallback на Android AudioRecord

### Почему буферизация в SpeechRecognitionModule?

1. `AudioSource` отдаёт данные асинхронно через callback
2. Vosk требует накопления данных для распознавания
3. Буферизация позволяет гибче управлять таймингами
4. Упрощает тестирование (можно подменить AudioSource)

### Почему интерфейс AudioSource?

1. **Разделение ответственности:** распознавание ≠ запись
2. **Тестируемость:** можно мокать AudioSource
3. **Расширяемость:** легко добавить новый источник (например, Bluetooth SCO)
4. **Fallback:** автоматический выбор лучшего источника

---

**Последнее обновление:** 2026-04-04
**Следующая задача:** Протестировать на устройстве и проверить стабильность
