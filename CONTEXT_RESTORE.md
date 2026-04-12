# 📋 КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата восстановления:** 2026-04-11
**Версия:** 19.1 (AudioRecord retry + Builder API)
**Статус:** ✅ Работает при ручном запуске | ❌ Не работает при автозапуске после BOOT

---

## 🎯 ОПИСАНИЕ ПРОЕКТА

**Оффлайн голосовой ассистент для автомобильных ГУ**
- **Платформа:** Android 11, API 30 (minSdk=26, targetSdk=33)
- **Package:** `ru.voboost.voiceassistant` (uid: u0_a68, НЕ system)
- **Build:** Gradle, Kotlin 2.1.0, AGP 8.13.2, compileSdk=36
- **Язык:** Kotlin 100%
- **Распознавание:** Vosk (offline, русский), TTS: Sherpa-ONNX (ru_RU-ruslan-medium)
- **CAN-шина:** через AIDL `com.qinggan.canbus.ICanBusService`

---

## 🏗 АРХИТЕКТУРА

### Главный сервис: `VoboostVoiceService.kt`
- **Foreground service** с типом `microphone`
- Запускается через `BootActivity` (foreground context) для получения доступа к микрофону
- Управляет State Machine (9 состояний)
- Координирует все компоненты системы

### State Machine (9 состояний):
```
IdleState → ActivatedState → ListeningCommandState → RecognizedCommandState → ExecutingCommandState
                                       ↓                        ↓
                                 KeywordErrorState        ConfirmationState
                                        ↓                         ↓
                                   CommandErrorState          TimeoutState
```

### Ключевые компоненты:

| Компонент | Файл | Назначение |
|-----------|------|------------|
| **BootReceiver** | `BootReceiver.kt` | Автозапуск при BOOT_COMPLETED → запускает BootActivity |
| **BootActivity** | `BootActivity.kt` | Невидимая Activity для foreground context при автозапуске |
| **State Machine** | `speech/state/*.kt` | Управление состояниями распознавания |
| **SpeechRecognizer** | `speech/SpeechRecognizer.kt` | Непрерывное распознавание речи |
| **NLUEngine** | `nlu/NLUEngine.kt` | Паттерн-матчинг команд из config.json |
| **CommandExecutor** | `executor/CommandExecutor.kt` | Выполнение команд через VehicleCommandExecutor |
| **TTS Engine** | `tts/TTSEngine.kt` | Синтез речи (Sherpa-ONNX) |
| **AudioSource** | `audio/*.kt` | Источники аудио (TransProxy, AndroidAudioSource) |
| **CanBusManager** | `canbus/CanBusServiceManager.kt` | AIDL обёртка к CAN-шине |
| **VoiceButtonHandler** | `canbus/VoiceButtonHandler.kt` | Кнопка на руле (keycode=16) |
| **TSRSpeedLimitHandler** | `canbus/TSRSpeedLimitHandler.kt` | Предупреждения о превышении скорости |
| **OverlayManager** | `ui/OverlayManager.kt` | UI overlay поверх всех окон |

---

## 🔊 АУДИО СИСТЕМА

### AudioSource типы:
- **TRANSPROXY** — системный источник с шумоподавлением (используется по умолчанию)
- **AndroidAudioSource** — стандартный AudioRecord
- **MicrophoneStreamAudioSource** — потоковый микрофон
- **RecorderManagerAudioSource** — через AudioManager

### Конфигурация:
```kotlin
ASR_ENGINE_TYPE = RecognitionEngine.VOSK
TTS_ENGINE_TYPE = SpeechEngineFactory.SynthesisEngine.SHERPA
AUDIO_SOURCE_TYPE = AudioSourceFactory.SourceType.TRANSPROXY
```

---

## 🚗 CAN-ШИНА (AIDL)

### Поддерживаемые команды (15):

| ID команды | Действие | Классификатор | Command |
|------------|----------|---------------|---------|
| `charge_port_open` | Открыть зарядку | 35 | 1 |
| `fuel_tank_open` | Открыть бензобак | 19 | 0 |
| `smart_mode_leisure` | Режим отдыха | 22 | 0 (mode=18) |
| `smart_mode_child` | Детский режим | 22 | 0 (mode=22) |
| `smart_mode_romantic` | Романтический режим | 22 | 0 (mode=6) |
| `ac_open` | Включить кондиционер | 5 | 0 |
| `ac_close` | Выключить кондиционер | 5 | 1 |
| `ac_set_temp` | Установить температуру | 5 | 3 |
| `phone_call_contact` | Позвонить контакту | 1 | 1 |
| `phone_call_number` | Позвонить по номеру | 1 | 1 |
| `window_open` | Открыть окно | 2 | 0 |
| `window_close` | Закрыть окно | 2 | 1 |
| `window_all_open` | Открыть все окна | 3 | 0 |
| `window_all_close` | Закрыть все окна | 3 | 1 |

### Особенности CAN-шины:
- **Бензобак (IVI_FUEL_PORT_CAP):** toggle только с value=1
- **Окна:** ALL_WINDOW (3=OPEN, 1=CLOSE), DRIVER_WINDOW (97=CLOSE, 51=OPEN — инвертировано)
- **Кондиционер (AC_POWER_SWITCH):** toggle только с value=1, проверка статуса через `canBusManager.getAirCondition()?.airSWStatus` (1=ON, 0=OFF)

---

## ❌ ТЕКУЩАЯ ПРОБЛЕМА: AudioRecord при автозапуске

### Описание:
После **перезагрузки ММ** сервис запускается через `BootReceiver`, но **AudioRecord не создаётся**:
```
AudioFlinger E createRecord_l(1929445393): AudioFlinger could not create record track, status: -1
AudioRecord  E createRecord_l(0): AudioFlinger could not create record track, status: -1
```

### Диагностика:
- **dvr_service** (PID ~6124, uid root/1000) держит микрофон
- Session ID `1929445393` — заглушка ошибки AudioFlinger
- **dvr_service использует AUDIO_SOURCE_ECHO_REFERENCE (1997)** — это НЕ мешает нам при ручном запуске
- При ручном запуске (`am start-foreground-service`) — **всё работает идеально**

### Решение:
Нужно убить **ОБА процесса**:
1. `dvr_service` (root, держит микрофон)
2. `audioserver` (AudioFlinger, замороженные сессии)

После этого audioserver автоматически перезапустится через HIDL init.

### Рабочий тест:
Когда вручную убивали audioserver (`kill 429`), AudioRecord создавался с session ID `54` и работал.

---

## 📁 СТРУКТУРА ПРОЕКТА

```
VoboostVoiceAssistant/
├── app/
│   ├── src/main/
│   │   ├── java/com/voboost/voiceassistant/
│   │   │   ├── audio/                  # AudioSource, VolumeManager, VoiceZoneDetector
│   │   │   ├── canbus/                 # CanBusManager, VoiceButtonHandler, TSRHandler
│   │   │   ├── config/                 # ConfigManager, AppConfig, CommandConfig
│   │   │   ├── core/                   # SpeechEngineFactory, ISpeechRecognition, ISpeechSynthesis
│   │   │   ├── engine/
│   │   │   │   ├── sherpa/             # Sherpa STT + TTS интеграция
│   │   │   │   └── vosk/               # Vosk STT интеграция
│   │   │   ├── executor/               # CommandExecutor, VehicleCommandExecutor
│   │   │   │   └── handlers/           # AIDL, Shell, Intent handlers
│   │   │   ├── nlu/                    # NLUEngine, Command
│   │   │   ├── speech/                 # SpeechRecognizer, CommandHandler
│   │   │   │   └── state/              # State Machine (9 состояний)
│   │   │   ├── tts/                    # TTSEngine
│   │   │   ├── ui/                     # OverlayManager, VoiceClickView
│   │   │   ├── BootActivity.kt
│   │   │   ├── BootReceiver.kt
│   │   │   ├── SoundEffectManager.kt
│   │   │   ├── VoboostVoiceService.kt  # ГЛАВНЫЙ СЕРВИС
│   │   │   └── VoiceCommandReceiver.kt
│   │   ├── assets/
│   │   │   └── config.json             # Конфиг команд и фраз
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── libs/                               # sherpa-onnx JAR
```

---

## 🚀 УСТАНОВКА

### Путь установки:
```
/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Конфиг:
- **Приоритет:** `/sdcard/Android/data/ru.voboost.voiceassistant/files/config.json`
- **Fallback:** `assets/config.json` (в APK)

### Модели:
- **Vosk:** `/data/user/0/ru.voboost.voiceassistant/files/models/vosk/`
- **Sherpa:** `/data/user/0/ru.voboost.voiceassistant/files/models/sherpa/`

---

## 🔧 ADB КОМАНДЫ

### Установка APK:
```batch
adb root
adb shell "mount -o remount,rw /"
adb push <apk> /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Перезапуск:
```batch
adb shell "am force-stop ru.voboost.voiceassistant && am broadcast -a android.intent.action.BOOT_COMPLETED -n ru.voboost.voiceassistant/.BootReceiver"
```

### Ручной запуск (РАБОТАЕТ):
```batch
adb shell am force-stop ru.voboost.voiceassistant
adb shell am start-foreground-service --user 0 -n ru.voboost.voiceassistant/.VoboostVoiceService
```

### Убийство dvr_service и audioserver:
```batch
adb shell "ps -A | grep dvr_service"
adb shell "ps -A | grep audioserver"
adb shell "kill -9 <PID_dvr_service>"
adb shell "kill -9 <PID_audioserver>"
```

---

## 📝 РЕШЁННЫЕ ПРОБЛЕМЫ

### 1. Микрофон в фоне (Android 10+)
- **Проблема:** ActivityManager блокирует FGS с микрофоном из background
- **Решение:** BootReceiver → BootActivity (foreground context) → startForegroundService → сервис получает микрофон

### 2. sharedUserId="android.uid.system" вызывает bootloop
- **Причина:** APK подписан debug key, а не platform key
- **Без platform подписи** система не может загрузить пакет как system UID

### 3. Privapp-permissions НЕ нужны
- `privapp-permissions-voboost.xml` удалён — работает без него
- RECORD_AUDIO выдаётся как runtime permission

### 4. AudioRecord Builder API
- Замена конструктора на Builder для большей надёжности
- Бесконечный retry каждые 3 секунды при неудаче

---

## 🔍 ЗАВИСИМОСТИ

### Speech Engines:
- **Vosk:** `com.alphacephei:vosk-android:0.3.45` (исключены native libs)
- **Sherpa-ONNX:** `libs/sherpa-onnx-v1.12.34-java8.jar`
- **JNA:** `net.java.dev.jna:jna:5.13.0@aar`

### Основные:
- Kotlin Coroutines: 1.7.1
- AndroidX Core: 1.10.1
- Material Design: 1.9.0
- Gson: 2.10.1
- ZXing: 3.5.3

---

## 📊 СОСТОЯНИЕ НА ДАННЫЙ МОМЕНТ

### ✅ РАБОТАЕТ:
- TTS (Sherpa) — "Слушаю вас", "Открываю окно"
- Vosk STT — распознаёт команды
- Кнопка на руле — keycode=16 через CAN-шину
- State Machine — 9 состояний
- CAN-bus — AIDL команды
- AudioRecord — при ручном запуске

### ❌ НЕ РАБОТАЕТ:
- AudioRecord — при автозапуске после BOOT (зависшая сессия AudioFlinger)

### СЛЕДУЮЩИЕ ШАГИ:
1. Попробовать перезапуск audioserver перед созданием AudioRecord
2. Использовать MIC (source=1) вместо VOICE_RECOGNITION (source=6)
3. Найти и освободить зависшую сессию через dumpsys media.audio_flinger

---

**Последнее обновление:** 2026-04-11
**Build:** assembleDebug SUCCESS
**Git:** требует коммита изменений
