# 🔄 КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-05
**Версия:** 18.0 (Voice Zone Detection)
**Статус:** ✅ ГОТОВО К ТЕСТИРОВАНИЮ

---

## 📋 КРАТКАЯ СУММАРИЗАЦИЯ

**Проект:** `VoboostVoiceAssistant`
**Папка:** `D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant`
**Задача:** Голосовой помощник для автомобиля на русском языке с офлайн распознаванием

---

## 🎯 ОСНОВНАЯ ФУНКЦИОНАЛЬНОСТЬ

### Что умеет:
- ✅ **Распознавание речи (офлайн)** — Vosk, русская модель
- ✅ **Синтез речи (TTS)** — Sherpa-ONNX, русский язык
- ✅ **Активация по ключевому слову** — "Привет машина" или "Привет, Вобуст"
- ✅ **Активация кнопкой на руле** — через CAN-шину
- ✅ **Выполнение команд автомобиля** — лючки, окна, кондиционер, режимы
- ✅ **TSR Speed Limit Warnings** — предупреждения о превышении скорости
- ✅ **Определение зоны говорящего** — через IMicphoneMode AIDL
- ✅ **State Machine архитектура** — 9 состояний

---

## 🏗️ АРХИТЕКТУРА

### Главный сервис: `VoboostVoiceService.kt` (495 строк)

**Компоненты:**
```kotlin
- StateMachine                    // State machine с 9 состояниями
- SpeechRecognizer                // Распознавание речи (SharedFlow-based)
- CommandHandler                  // NLU → CommandExecutor
- ConfigManager                   // Загрузка config.json
- NLUEngine                       // Парсинг команд
- CommandExecutor                 // Выполнение команд
- OverlayManager                  // UI оверлеи
- SpeechSynthesis (Sherpa TTS)    // Синтез речи
- SoundEffectManager              // Звуковые эффекты
- AudioSource (TransProxy)        // Аудио поток
- CanBusServiceManager            // CAN-шина
- VoiceButtonHandler              // Кнопка на руле
- TSRSpeedLimitHandler            // TSR предупреждения
- VolumeManager                   // Громкость
- VoiceZoneDetector               // Зона говорящего
- MicphoneModeManager             // AIDL IMicphoneMode
```

### State Machine (9 состояний):

```
IdleState
    ↓ (ключевое слово / кнопка)
ActivatedState
    ↓
ListeningCommandState
    ↓ (распознана команда)
RecognizedCommandState
    ↓ (нужно подтверждение?)
ConfirmationState / ExecutingCommandState
    ↓
ExecutingCommandState
    ↓ (команда выполнена)
IdleState (цикл повторяется)
```

**Дополнительные состояния:**
- `TimeoutState` — таймаут ожидания
- `CommandErrorState` — ошибка выполнения команды
- `KeywordErrorState` — ошибка распознавания ключевого слова

---

## 📁 СТРУКТУРА ПРОЕКТА

```
app/src/main/java/com/voboost/voiceassistant/
├── VoboostVoiceService.kt           # Главный сервис (495 строк)
├── VoiceCommandReceiver.kt          # Broadcast receiver
├── BootReceiver.kt                  # Автозапуск
├── SoundEffectManager.kt            # Звуковые эффекты
│
├── speech/
│   ├── SpeechRecognizer.kt          # Распознавание (SharedFlow)
│   ├── CommandHandler.kt            # NLU → CommandExecutor
│   ├── KeywordChecker.kt            # Проверка ключевых слов
│   ├── RecognitionEngine.kt         # Интерфейс движка
│   ├── RecognitionResult.kt         # Результат
│   ├── SpeechResult.kt              # sealed class (+zone)
│   ├── ModelLoader.kt               # Интерфейс загрузчика
│   ├── StreamFactory.kt             # Фабрика потоков
│   │
│   └── state/
│       ├── State.kt                 # Интерфейс состояния
│       ├── StateMachine.kt          # Главный цикл
│       ├── StateContext.kt          # Передача данных (+zone)
│       ├── BaseState.kt             # Базовый класс
│       ├── StateResult.kt           # Результат состояния
│       ├── IdleState.kt             # Ожидание ключевого слова
│       ├── ActivatedState.kt        # Активация
│       ├── ListeningCommandState.kt # Слушаем команду
│       ├── RecognizedCommandState.kt
│       ├── ConfirmationState.kt
│       ├── ExecutingCommandState.kt
│       ├── TimeoutState.kt
│       ├── CommandErrorState.kt
│       └── KeywordErrorState.kt
│
├── audio/
│   ├── AudioSource.kt               # Интерфейс
│   ├── AudioSourceFactory.kt        # Фабрика
│   ├── MicrophoneStreamAudioSource.kt  # TransProxy
│   ├── AndroidAudioSource.kt        # Fallback
│   ├── RecorderManagerAudioSource.kt
│   ├── VolumeManager.kt             # Громкость
│   ├── MicphoneModeManager.kt       # AIDL IMicphoneMode
│   └── VoiceZoneDetector.kt         # Зона говорящего
│
├── engine/
│   ├── vosk/
│   │   ├── VoskModelLoader.kt
│   │   ├── VoskStream.kt
│   │   └── VoskStreamFactory.kt
│   ├── sherpa/
│   │   ├── SherpaModelLoader.kt
│   │   ├── SherpaStream.kt
│   │   ├── SherpaStreamFactory.kt
│   │   └── SherpaSynthesis.kt
│   └── system/
│       └── SystemTtsSynthesis.kt
│
├── config/
│   ├── AppConfig.kt                 # Data классы
│   ├── CommandConfig.kt             # Конфиг команд
│   ├── ApiKeys.kt                   # API ключи
│   └── ConfigManager.kt             # Загрузка конфига
│
├── nlu/
│   ├── NLUEngine.kt                 # Парсинг команд
│   └── Command.kt                   # Data классы
│
├── executor/
│   ├── CommandExecutor.kt           # Главный executor
│   ├── VehicleCommandExecutor.kt    # Интерфейс
│   ├── VehicleCommandExecutorFactory.kt
│   ├── IntentVehicleCommandExecutor.kt
│   ├── AIDLVehicleCommandExecutor.kt
│   ├── ShellVehicleCommandExecutor.kt
│   └── AutoVehicleCommandExecutor.kt
│
├── canbus/
│   ├── CanBusServiceManager.kt      # CAN-шина
│   ├── VoiceButtonHandler.kt        # Кнопка на руле
│   └── TSRSpeedLimitHandler.kt      # TSR warnings
│
├── ui/
│   ├── OverlayManager.kt            # UI оверлеи
│   └── VoiceClickView.kt            # Анимация
│
└── tts/
    └── TTSEngine.kt                 # TTS движок
```

---

## 🔧 ТЕХНОЛОГИИ

### Build конфигурация:
- **Gradle:** 8.13.2
- **Kotlin:** 2.1.0
- **Compile SDK:** 36
- **Min SDK:** 26
- **Target SDK:** 33
- **JVM Target:** 1.8

### Зависимости:
- **Vosk:** `com.alphacephei:vosk-android:0.3.45` (офлайн STT)
- **Sherpa-ONNX:** `libs/sherpa-onnx-v1.12.34-java8.jar` (STT + TTS)
- **JNA:** `net.java.dev.jna:jna:5.13.0@aar`
- **Coroutines:** `kotlinx-coroutines-android:1.7.1`
- **Gson:** `com.google.code.gson:gson:2.10.1`
- **AndroidX:** Core, AppCompat, Lifecycle

### Модели:
- **Vosk STT:** `vosk-model-small-ru-0.22` (50MB, русский язык)
- **Sherpa TTS:** Piper Russian model

---

## 🚀 ЗАПУСК

### Сборка:
```batch
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleRelease
```

### Установка:
```batch
adb root
adb remount
adb push app\build\outputs\apk\release\app-release-unsigned.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Разрешения:
```batch
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
```

### Запуск:
```batch
adb shell am force-stop com.voboost.voiceassistant
adb shell am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService
```

### Логи:
```batch
adb logcat -s StateMachine:* SpeechRecognizer:* VoboostVoiceService:*
```

---

## 📊 КОМАНДЫ (из config.json)

| ID | Команды | Действие |
|----|---------|----------|
| charge_port_open | "Открой лючок зарядки" | Открыть порт зарядки |
| charge_port_close | "Закрой лючок зарядки" | Закрыть порт зарядки |
| fuel_tank_open | "Открой бензобак" | Открыть бензобак |
| smart_mode_leisure | "Включи режим отдыха" | Режим LEISURE |
| smart_mode_child | "Включи детский режим" | Режим CHILD |
| smart_mode_romantic | "Включи романтику" | Режим ROMANTIC |
| ac_open | "Включи кондиционер" | Включить AC |
| ac_close | "Выключи кондиционер" | Выключить AC |
| ac_set_temp | "Установи 22 градуса" | Установка температуры |
| phone_call_contact | "Позвони маме" | Звонок контакту |
| phone_call_number | "Набери 123-45-67" | Звонок номеру |
| window_open | "Открой окно" | Открыть окно |
| window_close | "Закрой окно" | Закрыть окно |

---

## ✅ СТАТУС КОМПОНЕНТОВ

| Компонент | Статус | Примечание |
|-----------|--------|------------|
| **Keyword Spotting** | ✅ Работает | "Привет машина" |
| **Command Recognition** | ✅ Работает | После ключевой фразы И кнопки |
| **Кнопка на руле** | ✅ Работает | Через CAN-шину |
| **TTS (Sherpa)** | ✅ Работает | Говорит "Слушаю вас" |
| **CAN-шина** | ✅ Работает | CanBusServiceManager |
| **TSR Speed Limit** | ✅ Работает | Предупреждения |
| **Audio Channel** | ✅ Работает | TransProxy (USAGE_ASSISTANT) |
| **State Machine** | ✅ Работает | 9 состояний |
| **SpeechRecognizer** | ✅ Работает | SharedFlow-based |
| **VoiceZoneDetector** | ✅ Работает | IMicphoneMode AIDL |
| **ConfirmationState** | ⚠️ Заглушка | Не ждёт ответ пользователя |

---

## 📝 ИЗВЕСТНЫЕ ПРОБЛЕМЫ

1. **ConfirmationState** — не ждёт ответ пользователя (заглушка)
2. **Модели не в APK** — загружаются вручную на SD-карту
3. **Библиотеки вынесены** — загружаются через `copy-libs-to-device.bat`
4. **Frida CAN-bypass** — опционально, если нет системных разрешений

---

## 🔄 ЦЕПОЧКА ВЫПОЛНЕНИЯ

```
Пользователь говорит "Привет машина"
    ↓
SpeechRecognizer распознаёт ключевое слово
    ↓
State Machine: Idle → Activated → ListeningCommand
    ↓
Пользователь говорит команду "Открой окно"
    ↓
SpeechRecognizer распознаёт команду
    ↓
State Machine: RecognizedCommand → ExecutingCommand
    ↓
NLUEngine парсит команду → CommandExecutor
    ↓
CommandExecutor отправляет Intent / AIDL / Shell
    ↓
Автомобиль выполняет команду
    ↓
TTS говорит "Открываю окно" + Overlay
    ↓
State Machine: Idle (цикл повторяется)
```

---

## 📞 ВОССТАНОВЛЕНИЕ КОНТЕКСТА

**Если нужно продолжить работу:**

1. **Открыть этот файл** (`CONTEXT_RESTORE.md`)
2. **Проверить текущий статус компонентов** (таблица выше)
3. **Проверить логи:**
   ```bash
   adb logcat -s StateMachine:* SpeechRecognizer:* VoboostVoiceService:*
   ```
4. **Протестировать:**
   - Сказать "привет машина"
   - Проверить переходы состояний
   - Проверить выполнение команд

---

## 📚 ДОКУМЕНТАЦИЯ

| Файл | Описание |
|------|----------|
| `CONTEXT_RESTORE.md` | 📋 **ЭТОТ ФАЙЛ** — текущий контекст |
| `ARCHITECTURE_V2.md` | 🏗️ State Machine архитектура v16.0 |
| `README.md` | 📖 Основная документация |
| `QUICKSTART.md` | 🚀 Быстрый старт |
| `PROJECT_SUMMARY.md` | 📦 Обзор проекта |
| `READY_TO_USE.md` | ✅ Готовые классы из decompile |
| `BUILD_INSTRUCTIONS.md` | 🔨 Сборка и установка |
| `AUDIO_SOURCE_REFACTORING.md` | 🎤 AudioSource рефакторинг |
| `TRANSPROXY_INTEGRATION.md` | 🔌 TransProxy интеграция |
| `CANBUS_LISTENER_DOCS.md` | 🚗 CAN-шина документация |
| `TSR_SPEED_LIMIT.md` | ⚡ TSR warnings |
| `SPEAKER_ZONE_DETECTION.md` | 🎤 Зона говорящего |
| `FRIDA_VOICE_ASSISTANT.md` | 🔧 Frida CAN-bypass |

---

## 📊 ВЕРСИИ ПРОЕКТА

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
| 14.0 | 2026-04-04 | AudioSource Refactoring |
| 15.0 | 2026-04-04 | State Machine Pattern |
| 16.0 | 2026-04-04 | Упрощение SpeechStateMachine |
| 17.0 | 2026-04-04 | SpeechRecognizer (Channel-based) |
| **18.0** | **2026-04-04** | **Voice Zone Detection (IMicphoneMode AIDL)** |

---

## 🎯 СЛЕДУЮЩИЕ ЗАДАЧИ

1. ⚠️ **Реализовать ConfirmationState** — ждать ответ пользователя ("Да"/"Нет")
2. ⚠️ **Тестирование на устройстве** — полная проверка всех команд
3. ⚠️ **Оптимизация моделей** — возможно заменить на более точные
4. ⚠️ **Онлайн режим** — Yandex SpeechKit (опционально)

---

**Последнее обновление:** 2026-04-05
**Следующая задача:** Реализовать ConfirmationState для ожидания ответа пользователя
