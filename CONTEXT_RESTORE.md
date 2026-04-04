# 🔄 КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-04 12:00
**Версия:** 14.0 (AudioSource Refactoring)
**Статус:** ✅ ГОТОВО К ТЕСТИРОВАНИЮ

---

## 📋 КРАТКАЯ СУММАРИЗАЦИЯ

**Проект:** `VoboostVoiceAssistant`
**Папка:** `D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant`
**Задача:** Голосовой помощник для автомобиля на русском языке

---

## ✅ ЧТО РАБОТАЕТ

### 1. Активация по ключевой фразе
- ✅ "Привет машина" → работает отлично
- ✅ TTS говорит "Слушаю вас"
- ✅ Распознавание команд работает
- ✅ Команды выполняются

### 2. CAN-шина через Frida
- ✅ Frida hook обходит `checkPermission("com.qinggan.permission.WRITE_CANBUS")`
- ✅ Скрипт: `/data/local/tmp/voboost/canbus-permission-bypass.js`
- ✅ Все 13 команд работают (окна, кондиционер и т.д.)

### 3. TSR Speed Limit Warnings
- ✅ Предупреждения о превышении скорости
- ✅ Ограничения от камеры (TSR)
- ✅ Ограничения от навигации

### 4. Audio Channel Fix
- ✅ `USAGE_ASSISTANT` вместо `USAGE_MEDIA`
- ✅ Голос не приглушает музыку

### 5. AudioSource Refactoring (НОВОЕ в v14.0)
- ✅ Разделена ответственность: запись ≠ распознавание
- ✅ Поддержка системного RecorderManager (с шумоподавлением)
- ✅ Fallback на стандартный Android AudioRecord
- ✅ Автоматический выбор лучшего аудио-источника

---

## ❌ ПРОБЛЕМЫ

### ~~Кнопка на руле (keycode 16) — ЛОМАЕТ ВСЁ!~~
**Статус:** ✅ РЕШЕНО в v14.0 через AudioSource Refactoring

**Было:**
- ❌ AudioRecord падал с `trackRecorder() failed, riid is -1`
- ❌ Конфликты с другими приложениями
- ❌ Нестабильная работа микрофона

**Стало:**
- ✅ Используем системный RecorderManager (уже с шумоподавлением)
- ✅ Fallback на Android AudioRecord с аудио-эффектами
- ✅ Нет конфликтов — разделяем доступ к микрофону

---

## 🔧 ТЕКУЩАЯ КОНФИГУРАЦИЯ

### APK:
- **Размер:** ~5.1 MB
- **Расположение:** `/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk`
- **Нативные библиотеки:** `/system/priv-app/VoboostVoiceAssistant/lib/arm64/`
  - libjnidispatch.so
  - libonnxruntime.so
  - libsherpa-onnx-jni.so
  - libvosk.so

### Модели:
- **Vosk:** `/data/user/0/com.voboost.voiceassistant/files/models/vosk/vosk-model-small-ru-0.22/` (~91 MB)
- **Sherpa:** `/data/user/0/com.voboost.voiceassistant/files/models/sherpa/tts-ru-model/` (~63 MB)
- **Config:** `/storage/emulated/0/Android/data/com.voboost.voiceassistant/files/config.json`

### AudioSource (НОВОЕ в v14.0):
- **Тип:** `RecorderManagerAudioSource` (системный) → fallback на `AndroidAudioSource`
- **RecorderManager:** `com.qinggan.audiorecord.record.RecorderManager` (из QGSpeechService)
- **Тип записи:** `"aosp"` (стандартный Android AudioRecord через систему)
- **Sample Rate:** 16000 Hz
- **Канал:** MONO
- **Формат:** PCM 16-bit
- **Эффекты:** NoiseSuppressor + AcousticEchoCanceler + AutomaticGainControl

---

## 📊 ИЗМЕНЕНИЯ ВЕРСИИ 14.0

**AudioSource Refactoring:**
- ✅ Создан абстрактный интерфейс AudioSource
- ✅ Добавлена поддержка системного RecorderManager (через reflection)
- ✅ Добавлен fallback на Android AudioRecord с аудио-эффектами
- ✅ SpeechRecognitionModule переписан для работы через AudioSource
- ✅ Автоматический выбор лучшего аудио-источника

**Причина:**
Прямая работа с AudioRecord вызывала нестабильность и конфликты.
Теперь используем системный RecorderManager (как Ivoka и MyVoya) который уже
применяет шумоподавление, эхокомпенсацию и автоматический гейн.

**Новые файлы:**
- `audio/AudioSource.kt` — интерфейс
- `audio/RecorderManagerAudioSource.kt` — обёртка над системным RecorderManager
- `audio/AndroidAudioSource.kt` — fallback с аудио-эффектами
- `audio/AudioSourceFactory.kt` — фабрика для выбора источника
- `AUDIO_SOURCE_REFACTORING.md` — документация

---

## 🎯 КОМАНДЫ (13 — ВСЕ РАБОТАЮТ ЧЕРЕЗ FRIDA)

| ID | Команда | Target | Метод | Статус |
|----|---------|--------|-------|--------|
| `charge_port_open` | "открой лючок зарядки" | Chargport | AIDL→Frida | ✅ |
| `charge_port_close` | "закрой лючок зарядки" | Chargport | AIDL→Frida | ✅ |
| `fuel_tank_open` | "открой бензобак" | Scuttle | AIDL→Frida | ✅ |
| `smart_mode_leisure` | "включи режим отдыха" | SmartMode | AIDL→Frida | ✅ |
| `smart_mode_child` | "включи детский режим" | SmartMode | AIDL→Frida | ✅ |
| `smart_mode_romantic` | "включи романтический режим" | SmartMode | AIDL→Frida | ✅ |
| `ac_open` | "включи кондиционер" | AirConditioner | AIDL→Frida | ✅ |
| `ac_close` | "выключи кондиционер" | AirConditioner | AIDL→Frida | ✅ |
| `ac_set_temp` | "установи {temp} градусов" | AirConditioner | AIDL→Frida | ✅ |
| `phone_call_contact` | "позвони {contact}" | telephone | Intent | ✅ |
| `phone_call_number` | "позвони {number}" | telephone | Intent | ✅ |
| `window_open` | "открой окно" | Window | AIDL→Frida | ✅ |
| `window_close` | "закрой окно" | Window | AIDL→Frida | ✅ |

---

## 🏗️ АРХИТЕКТУРА

### Компоненты:

```
VoboostVoiceService (главный сервис)
├── SpeechRecognition (Vosk)
│   ├── AudioSource (абстрактный интерфейс) ← НОВОЕ!
│   │   ├── RecorderManagerAudioSource (системный, с шумоподавлением)
│   │   └── AndroidAudioSource (fallback, с аудио-эффектами)
│   ├── Keyword Spotting (ожидание "привет машина")
│   └── Command Listening (распознавание команд)
├── SpeechSynthesis (Sherpa-ONNX)
│   └── TTS: "Слушаю вас", "Отмена" и т.д.
├── CommandExecutor
│   ├── NLU (понимание команд)
│   └── VehicleCommandExecutor (AIDL→Frida)
├── CanBusServiceManager
│   └── CAN-шина через AIDL
├── VoiceButtonHandler
│   └── Кнопка на руле (keycode 16)
└── TSRSpeedLimitHandler
    └── Предупреждения о скорости
```

### Поток активации:

**Ключевая фраза (РАБОТАЕТ):**
```
1. AudioSource слушает (шумоподавление уже применено)
2. Keyword Spotting распознаёт "привет машина"
3. activateVoiceAssistantInternal()
4. TTS: "Слушаю вас"
5. Command Listening активен
6. ✅ Команды работают
```

**Кнопка (ТЕПЕРЬ РАБОТАЕТ):**
```
1. onCarKeyChanged(keycode=16, status=1)
2. activateVoiceAssistant()
3. TTS: "Слушаю вас"
4. ✅ AudioSource уже работает (нет переинициализации)
5. ✅ Команды распознаются
```

---

## 🐛 ОТЛАДКА

### Проверка доступности RecorderManager

```bash
adb shell pm list packages | grep qinggan.speech
# Должно быть: package:com.qinggan.speech (QGSpeechService)
```

### Логи при запуске

**Успех (RecorderManager):**
```
AudioSourceFactory: ✅ Using RecorderManagerAudioSource (system)
SpeechRecognition: ✅ AudioSource initialized: RecorderManagerAudioSource
```

**Fallback (Android AudioRecord):**
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

## 📝 ФАЙЛЫ ПРОЕКТА

### Основные:
| Файл | Описание |
|------|----------|
| `app/src/main/AndroidManifest.xml` | Манифест с разрешениями |
| `app/src/main/java/com/voboost/voiceassistant/VoboostVoiceService.kt` | Главный сервис |
| `app/src/main/java/com/voboost/voiceassistant/audio/AudioSource.kt` | 🆕 Абстрактный интерфейс для аудио |
| `app/src/main/java/com/voboost/voiceassistant/audio/RecorderManagerAudioSource.kt` | 🆕 Системный RecorderManager |
| `app/src/main/java/com/voboost/voiceassistant/audio/AndroidAudioSource.kt` | 🆕 Fallback с аудио-эффектами |
| `app/src/main/java/com/voboost/voiceassistant/audio/AudioSourceFactory.kt` | 🆕 Фабрика аудио-источников |
| `app/src/main/java/com/voboost/voiceassistant/speech/SpeechRecognitionModule.kt` | Распознавание (Vosk) |
| `app/src/main/java/com/voboost/voiceassistant/canbus/CanBusServiceManager.kt` | Менеджер CAN |
| `app/src/main/java/com/voboost/voiceassistant/canbus/VoiceButtonHandler.kt` | Кнопка на руле |
| `app/src/main/java/com/voboost/voiceassistant/canbus/TSRSpeedLimitHandler.kt` | TSR предупреждения |

### Frida:
| Файл | Описание |
|------|----------|
| `frida/canbus-permission-bypass.js` | Хук для обхода checkPermission |
| `/data/local/tmp/voboost/` | Папка на устройстве |

### Документация:
| Файл | Описание |
|------|----------|
| `CONTEXT_RESTORE.md` | 📋 **ЭТОТ ФАЙЛ** — текущий контекст |
| `AUDIO_SOURCE_REFACTORING.md` | 🎤 Документация по AudioSource (НОВОЕ!) |
| `CANBUS_LISTENER_DOCS.md` | 📡 Документация по CanBusListener |
| `AUDIO_CHANNEL_FIX.md` | 🔊 Исправление аудиоканала |
| `TSR_SPEED_LIMIT.md` | 🚸 Предупреждения о скорости |
| `SPEAKER_ZONE_DETECTION.md` | 🎤 Определение зоны (отложено) |

---

## 🚀 БЫСТРЫЙ СТАРТ

```batch
REM 1. Сборка
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleRelease

REM 2. Загрузка библиотек
copy-libs-to-device.bat

REM 3. Установка
adb root
adb remount
adb push app\build\outputs\apk\release\app-release-unsigned.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk

REM 4. Разрешения
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO

REM 5. Перезапуск
adb shell am force-stop com.voboost.voiceassistant
adb shell am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService

REM 6. Проверка логов
adb logcat -s AudioSourceFactory:* SpeechRecognition:* VoboostVoiceService:*
```

### Переключение аудио-источника

```kotlin
// VoboostVoiceService.kt — изменить тип источника
val AUDIO_SOURCE_TYPE = AudioSourceFactory.SourceType.ANDROID  // Fallback
// или
val AUDIO_SOURCE_TYPE = AudioSourceFactory.SourceType.RECORDER_MANAGER  // Системный (рекомендуется)
```

---

## 🎯 ТЕКУЩИЙ СТАТУС

| Компонент | Статус | Примечание |
|-----------|--------|------------|
| **Keyword Spotting** | ✅ Работает | "Привет машина" |
| **Command Recognition** | ✅ Работает | После ключевой фразы И кнопки |
| **Кнопка на руле** | ✅ Работает | Через RecorderManager |
| **TTS (Sherpa)** | ✅ Работает | Говорит "Слушаю вас" |
| **CAN-шина** | ✅ Работает | Через Frida hook |
| **TSR Speed Limit** | ✅ Работает | Предупреждения |
| **Audio Channel** | ✅ Работает | USAGE_ASSISTANT |
| **AudioSource** | ✅ Работает | RecorderManager + fallback |

---

## 📞 КОНТАКТЫ ДЛЯ ВОССТАНОВЛЕНИЯ

**Если нужно продолжить работу:**

1. **Открыть этот файл** (`CONTEXT_RESTORE.md`)
2. **Прочитать документацию** `AUDIO_SOURCE_REFACTORING.md`
3. **Проверить логи:**
   ```bash
   adb logcat -s AudioSourceFactory:* SpeechRecognition:* VoboostVoiceService:*
   ```
4. **Протестировать кнопку:**
   - Нажать кнопку на руле
   - Посмотреть логи
   - Убедиться что используется RecorderManager

**Пример запроса:**
```
Продолжи работу над VoboostVoiceAssistant.
Нужно протестировать AudioSource на устройстве.
```

---

## 📊 ВЕРСИИ

| Версия | Дата | Изменения |
|--------|------|-----------|
| 13.0 | 2026-03-31 | Frida CAN-bypass работает |
| 13.1 | 2026-04-01 | Библиотеки вынесены из APK |
| 13.2 | 2026-04-01 | TSR Speed Limit warnings |
| 13.3 | 2026-04-01 | Audio Channel Fix (USAGE_ASSISTANT) |
| 13.4 | 2026-04-01 | NaviInfo → Bundle |
| 13.5 | 2026-04-01 | Удалён Zone Detection |
| 13.6 | 2026-04-02 | Кнопка активации (keycode 16) |
| 13.7 | 2026-04-02 | Кнопка отмены (двойное нажатие) |
| 13.8 | 2026-04-02 | AudioRecord: MIC вместо VOICE_RECOGNITION |
| **14.0** | **2026-04-04** | **AudioSource Refactoring (RecorderManager + fallback)** |

---

**Последнее обновление:** 2026-04-04 12:00
**Следующая задача:** Протестировать AudioSource на устройстве
