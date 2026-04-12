# 🎤 ИНТЕГРАЦИЯ TRANSPROXY ЧЕРЕЗ AudioSourceFactory

**Дата:** 2026-04-04
**Версия:** 15.1 (AudioSourceFactory Integration)
**Статус:** ✅ ГОТОВО К ТЕСТИРОВАНИЮ

---

## 📋 ЧТО ИЗМЕНИЛОСЬ

### Было (v15.0):
- ❌ `MicrophoneStreamManager` отдельный класс
- ❌ `SpeechRecognitionModule` имел свою логику для TransProxy
- ❌ Не использовалась `AudioSourceFactory`

### Стало (v15.1):
- ✅ **`MicrophoneStreamAudioSource`** — реализует интерфейс `AudioSource`
- ✅ **`AudioSourceFactory`** — автоматически выбирает лучший источник
- ✅ **Единый интерфейс** — все аудио-источники через `AudioSource`
- ✅ **Автоматический fallback** — если TransProxy недоступен

---

## 🏗️ АРХИТЕКТУРА

```
AudioSourceFactory.create(type)
├── TRANSPROXY → MicrophoneStreamAudioSource
│   └── Подключается к TransProxy (QGSpeechService)
│       └── Получает PCM с шумоподавлением
│
├── RECORDER_MANAGER → RecorderManagerAudioSource
│   └── Подключается к RecorderManager (QGSpeechService)
│       └── Получает PCM с шумоподавлением
│
└── ANDROID → AndroidAudioSource
    └── Создаёт свой AudioRecord
        └── Применяет аудио-эффекты сам
```

### Поток данных:

```
Микрофон автомобиля (4-6 микрофонов)
    ↓
QGSpeechService (шумоподавление + эхокомпенсация)
    ↓
TransProxy (IPcmModule)
    ↓
MicrophoneStreamAudioSource.onPcm(byte[] pcm)
    ↓
AudioSource.Listener.onAudioData(pcm, size)
    ↓
SpeechRecognitionModule → Vosk Recognizer
    ↓
Текст → NLU → Команда → CAN-шина
```

---

## 📁 НОВЫЕ ФАЙЛЫ

| Файл | Описание |
|------|----------|
| `audio/MicrophoneStreamAudioSource.kt` | 🆕 AudioSource через TransProxy |
| `audio/AudioSourceFactory.kt` | 🆕 Фабрика аудио-источников |

### Изменённые файлы:

| Файл | Изменения |
|------|-----------|
| `speech/SpeechRecognitionModule.kt` | Использует AudioSourceFactory |
| `engine/vosk/VoskRecognition.kt` | Принимает AudioSourceType |
| `core/SpeechEngineFactory.kt` | Передаёт AudioSourceType |
| `VoboostVoiceService.kt` | Использует AudioSourceFactory |

---

## 🔧 КАК ЭТО РАБОТАЕТ

### 1. Создание AudioSource через фабрику

```kotlin
// Создаём AudioSource (автоматически выберет лучший)
val audioSource = AudioSourceFactory.create(
    context = this,
    preferredType = AudioSourceFactory.SourceType.TRANSPROXY
)

// AudioSource реализует единый интерфейс:
audioSource.addListener { data, bytesRead ->
    // PCM данные (уже с шумоподавлением!)
    recognizer.acceptWaveForm(data, bytesRead)
}

audioSource.start()  // Начать запись
audioSource.stop()   // Остановить
audioSource.release() // Освободить ресурсы
```

### 2. Автоматический fallback

```kotlin
// Если TransProxy недоступен → RecorderManager
// Если RecorderManager недоступен → Android AudioRecord
val audioSource = AudioSourceFactory.create(context)
// Логи покажут что используется:
// "✅ Using MicrophoneStreamAudioSource (TransProxy)"
// или
// "⚠️ TransProxy unavailable, falling back to AndroidAudioSource"
```

### 3. Использование в SpeechRecognitionModule

```kotlin
// Создаём модуль распознавания
val speechModule = SpeechRecognitionModule(
    context = this,
    modelPath = "/path/to/vosk/model",
    audioSourceType = AudioSourceFactory.SourceType.TRANSPROXY
)

// Запускаем распознавание
speechModule.startKeywordSpotting(
    onKeywordDetected = { /* Ключевая фраза распознана */ },
    onError = { error -> /* Обработка ошибки */ }
)
```

---

## 📊 СРАВНЕНИЕ АУДИО-ИСТОЧНИКОВ

| Характеристика | TransProxy | RecorderManager | Android |
|----------------|-----------|-----------------|---------|
| **Шумоподавление** | ✅ Уже применено | ✅ Уже применено | ❌ Нужно применять |
| **Эхокомпенсация** | ✅ Уже применено | ✅ Уже применено | ❌ Нужно применять |
| **Авто-гейн** | ✅ Уже применено | ✅ Уже применено | ❌ Нужно применять |
| **Конфликты** | ✅ Нет | ✅ Нет | ⚠️ Возможны |
| **Стабильность** | ✅ Высокая | ✅ Высокая | ⚠️ Зависит от устройства |
| **Определение зоны** | ✅ Через IMicphoneMode | ✅ Через SSE | ❌ Нет |

---

## 🚀 КАК ТЕСТИРОВАТЬ

### 1. Проверить что QGSpeechService запущен

```bash
adb shell ps | grep sttservice
# Должно быть: com.qinggan.sttservice
```

### 2. Сборка и установка

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleRelease

adb root && adb remount
adb push app\build\outputs\apk\release\app-release-unsigned.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk

adb shell am force-stop ru.voboost.voiceassistant
adb shell am start-foreground-service -n ru.voboost.voiceassistant/.VoboostVoiceService
```

### 3. Проверить логи

```bash
adb logcat -s AudioSourceFactory:* MicrophoneStreamAudio:* SpeechRecognition:* VoboostVoiceService:*
```

**Ожидаемые логи:**

```
AudioSourceFactory: ✅ Using MicrophoneStreamAudioSource (TransProxy)
MicrophoneStreamAudio: ✅ MicrophoneStreamAudioSource initialized successfully
SpeechRecognition: ✅ AudioSource initialized: MicrophoneStreamAudioSource

VoboostVoiceService: VolumeManager connected - can duck/restore media volume
```

---

## 🐛 ВОЗМОЖНЫЕ ПРОБЛЕМЫ

### TransProxy не подключается

**Симптомы:**
```
AudioSourceFactory: ⚠️ TransProxy unavailable, falling back to AndroidAudioSource
```

**Причины:**
1. QGSpeechService не запущен
2. AIDL файлы не скомпилированы
3. Нет разрешения BIND_SERVICE

**Решение:**
```bash
# Проверить что QGSpeechService запущен
adb shell ps | grep sttservice

# Если не запущен — запустить
adb shell am startservice -n com.qinggan.sttservice/.vui.VuiService
```

### PCM данные не приходят

**Симптомы:**
```
MicrophoneStreamAudio: ✅ MicrophoneStreamAudioSource initialized
# Но onAudioData() не вызывается
```

**Причины:**
1. QGSpeechService не захватывает аудио
2. TransProxy не раздаёт данные

**Решение:**
```bash
# Проверить логи QGSpeechService
adb logcat -s TransProxy:* RecorderManager:*

# Должно быть:
# TransProxy: registerPcmListener
# RecorderManager: startRecord
```

---

## 📝 ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ

### Базовое использование

```kotlin
// Создать AudioSource
val audioSource = AudioSourceFactory.create(context)

// Добавить слушатель
audioSource.addListener { data, bytesRead ->
    // Обработать PCM данные
    processAudio(data, bytesRead)
}

// Начать запись
audioSource.start()

// ... работа ...

// Остановить
audioSource.stop()
audioSource.release()
```

### С выбором типа

```kotlin
// Принудительно использовать Android AudioRecord
val audioSource = AudioSourceFactory.create(
    context = context,
    preferredType = AudioSourceFactory.SourceType.ANDROID
)

// Или TransProxy
val audioSource = AudioSourceFactory.create(
    context = context,
    preferredType = AudioSourceFactory.SourceType.TRANSPROXY
)
```

### Проверка доступности

```kotlin
if (AudioSourceFactory.isTransProxyAvailable(context)) {
    Log.i(TAG, "TransProxy available")
} else {
    Log.w(TAG, "TransProxy unavailable, using fallback")
}
```

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

### 1. Протестировать на устройстве

- ✅ Проверить подключение TransProxy
- ✅ Проверить получение PCM данных
- ✅ Проверить распознавание команд
- ✅ Проверить fallback при недоступности

### 2. Добавить определение зоны водителя

```kotlin
// Подключиться к IMicphoneMode
val intent = Intent("com.qinggan.qinglink.hu.MICPHONEMODE")
intent.setPackage("com.qinggan.sttservice")

bindService(intent, object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val micModeService = IMicphoneMode.Stub.asInterface(service)
        
        micModeService.registerListener(object : IMicphoneModeListener.Stub() {
            override fun onSetMicMode(mode: Int) {
                when (mode) {
                    1537 -> Log.d("Mic", "🎤 Говорит ВОДИТЕЛЬ")
                    1538 -> Log.d("Mic", "🎤 Говорит ПАССАЖИР")
                }
            }
        })
    }
}, Context.BIND_AUTO_CREATE)
```

### 3. Оптимизировать буферизацию

- Настроить размер буфера
- Добавить обработку ошибок
- Логирование статистики

---

**Последнее обновление:** 2026-04-04
**Следующая задача:** Протестировать AudioSourceFactory на устройстве
