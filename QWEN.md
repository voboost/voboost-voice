# КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-26
**Статус:** ✅ ВСЕ РАБОТАЕТ -- State Machine (9 состояний), Sherpa TTS, Vosk STT, CAN-bus, 17 команд, кнопка, TSR, phone calls, температура, русские числа

---

## ОПИСАНИЕ ПРОЕКТА

**Оффлайн голосовой ассистент для автомобильных ГУ**
- **Платформа:** Android 11, API 30 (minSdk=26, targetSdk=33)
- **Package:** `ru.voboost.voiceassistant` (uid: u0_a68, НЕ system)
- **Build:** Gradle, Kotlin 100%, AGP 8.13.2, compileSdk=36
- **Структура:** 85 Kotlin файлов, паттерн Executor, State Machine (9 состояний)
- **Распознавание:** Vosk (offline, русский, small model 50MB)
- **TTS:** Sherpa-ONNX (ru_RU-ruslan-medium) — выбирается из config.json
- **CAN-шина:** через AIDL `com.qinggan.canbus.ICanBusService`
- **Установка:** 2 этапа — APK+libs → reboot → models+config+permissions

---

## 🏗️ АРХИТЕКТУРА (2026-04-26)

### State Machine (Event-driven)

```
StateMachine (202 строк)
├── StateType.IDLE              → Ожидание активации
├── StateType.ACTIVATED         → Активирован (проигран звук)
├── StateType.LISTENING_COMMAND → Слушание команды
├── StateType.RECOGNIZED_COMMAND → Команда распознана
├── StateType.EXECUTING_COMMAND → Выполнение команды
├── StateType.CONFIRMATION      → Подтверждение действия
├── StateType.COMMAND_ERROR     → Ошибка команды
├── StateType.KEYWORD_ERROR     → Ошибка ключевого слова
└── StateType.TIMEOUT           → Таймаут
```

**Принцип работы:**
- Все состояния создаются один раз при инициализации
- Состояния сами вызывают `finish()` или `cancelled()` когда готовы
- StateMachine переключает по типу состояния
- `reset()` вызывается перед переходом для сброса внутреннего состояния

### Speech Engine Factory

```
ru.voboost.voiceassistant/
├── core/
│   ├── ISpeechRecognizer.kt        # Интерфейс STT
│   ├── ISpeechSynthesis.kt         # Интерфейс TTS
│   └── SpeechEngineFactory.kt      # Фабрика движков
├── engine/
│   ├── sherpa/
│   │   ├── SherpaModelLoader.kt
│   │   ├── SherpaStream.kt
│   │   ├── SherpaStreamFactory.kt
│   │   └── SherpaSpeechSynthesis.kt  # Sherpa TTS
│   ├── vosk/
│   │   ├── VoskModelLoader.kt
│   │   ├── VoskStream.kt
│   │   ├── VoskStreamFactory.kt
│   │   └── VoskRecognition.kt        # Vosk STT (обёртка)
│   └── system/
│       └── SystemSpeechSynthesis.kt  # Системный TTS
└── speech/
    ├── SpeechRecognizer.kt
    ├── KeywordChecker.kt
    ├── state/                      # 9 состояний StateMachine
    └── IRecognitionEngine.kt
```

**Алгоритм выбора движка:**
1. Sherpa TTS →SherpaSpeechSynthesis (приоритет)
2. Vosk STT → VoskRecognition
3. System TTS → SystemSpeechSynthesis (fallback)

### Executor Pattern для команд

```
CommandExecutor (основная логика)
    ↓ использует
IVehicleCommandExecutor (интерфейс)
    ↓ реализуют
├── IntentVehicleCommandExecutor      # Broadcast Intent
├── ShellVehicleCommandExecutor       # Shell CAN-команды
└── AutoVehicleCommandExecutor        # Автоматический выбор (Intent → Shell fallback)
```

### Audio Sources

```
IAudioSource (интерфейс)
├── AndroidAudioSource               # Микрофон через Android API
├── MultiChannelAudioSource          # Мультиканальный аудио (зона говорящего)
└── RecorderManagerAudioSource       # RecorderManager API
```

---

## 🔧 ПОСЛЕДНИЕ ИЗМЕНЕНИЯ (git HEAD: 18e67db)

### 18e67db — refactor: архитектура Service и executorPattern
- Полная рефакторинг архитектуры VoboostVoiceService
- Паттерн Executor для команд
- Обновление StateMachine на event-driven версию

### 73bf8f3 — refactoring
- Оптимизация потоков данных
- Улучшение обработки ошибок

### 18e0bae — feat: multi-channel audio, new models, cleanup
- Multi-channel audio source с зоной говорящего
- Обновление моделей (Sherpa v1.12.34)
- Удаление неиспользуемого кода

---

## 📊 КОМАНДЫ И ОПОВЕЩЕНИЯ

### Реализованные команды (17 AIDL обработчиков):

| ID | Команда | Действие |
|----|---------|---------|
| `charge_port_open` | "Открой лючок зарядки" | OpenChargportHandler |
| `charge_port_close` | "Закрой лючок зарядки" | OpenChargportHandler |
| `fuel_tank_open` | "Открой бензобак" | FuelTankOpenHandler |
| `smart_mode_leisure` | "Включи режим отдыха" | SmartModeLeisureHandler |
| `smart_mode_child` | "Включи детский режим" | SmartModeChildHandler |
| `smart_mode_romantic` | "Включи романтику" | SmartModeRomanticHandler |
| `ac_open` | "Включи кондиционер" | AirConditionerOpenHandler |
| `ac_close` | "Выключи кондиционер" | AirConditionerCloseHandler |
| `ac_set_temp` | "Установи 22 градуса" | AirConditionerSetTempHandler |
| `ac_temp_up` | "Мне холодно" | AirConditionerTempOffsetHandler |
| `ac_temp_down` | "Мне жарко" | AirConditionerTempOffsetHandler |
| `phone_call_contact` | "Позвони Сынок" | PhoneCallContactIntentHandler |
| `phone_call_number` | "Набери 123-45-67" | PhoneCallNumberIntentHandler |
| `window_open_driver` | "Открой окно водителя" | WindowOpenDriverHandler |
| `window_close_driver` | "Закрой окно водителя" | WindowCloseDriverHandler |
| `window_open_all` | "Открой все окна" | WindowOpenAllHandler |
| `window_close_all` | "Закрой все окна" | WindowCloseAllHandler |

### Возможные оповещения (анализ QGTtsService):

Из декомпилированных APK (`QGTtsService-release-signed/`) выявлены **500+ ACTION_ID**.  
Наиболее полезные для Voboost:

**Музыка и аудио:**
- `ACTION_MUSIC_PLAY` — Воспроизведение музыки
- `ACTION_CMD_PLAY/PAUSE/NEXT/PREV` — Управление треками
- `ACTION_RADIO_LAUNCH` — Радио
- `ACTION_NEWS_PLAY` — Новости

**Навигация:**
- `ACTION_DCS_POI_NAV` — POI навигация
- `ACTION_DCS_ROUTELINE_NAV` — Маршрут следования
- `ACTION_DCS_PARKING_NAV` — Парковка

**Такси/Доставка:**
- `ACTION_VOICE_CALL_TAXI_QUERY` — Запрос такси
- `ACTION_VOICE_EXPRESS` — Экспресс-доставка
- `ACTION_VOICE_TAKEOUT_FOOD_*` — Доставка еды

**Системные:**
- `ACTION_WEATHER_QUERY` — Погода
- `ACTION_DCS_COMMON_ALARM_QUERY` — Будильник
- `ACTION_DCS_CAR_SET_BRIGHTNESS` — Яркость

**Полный список:**
- `TTS_OPPORTUNITIES.md` — Подробный анализ 100+ оповещений


### Дополнительные фичи:

| Фича | Описание |
|--|-|
| **TSR Speed Limit** | Предупреждения о превышении скорости (через CAN) |
| **Zone Detection** | Определение зоны говорящего (мультиканальный аудио) |
| **Temperature Offset** | "мне холодно" / "мне жарко" → текущая +2 / -2°C |
| **Russian Numbers** | Парсинг "двадцать четыре" → 24 |
| **Sound Effects** | Двойной сигнал (старт), одиночный (финиш), низкий (отмена) |

---

## 📊 СТРУКТУРА ДАННЫХ НА УСТРОЙСТВЕ

| Что | Путь |
|-----|-|
| **APK** | `/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk` |
| **Нативные библиотеки** | `/system/priv-app/VoboostVoiceAssistant/lib/arm64/` |
| **config.json** | `/data/user/0/ru.voboost.voiceassistant/files/config.json` |
| **Vosk модель** | `/storage/emulated/0/Android/data/.../files/models/vosk/vosk-model-small-ru-0.22/` |
| **Sherpa ASR** | `/storage/emulated/0/Android/data/.../files/models/sherpa/asr-ru-model/` |
| **Sherpa TTS** | `/storage/emulated/0/Android/data/.../files/models/sherpa/tts-ru-model/` |
| **TTS eSpeak-ng-data** | `/storage/emulated/0/Android/data/.../files/models/sherpa/tts-ru-model/espeak-ng-data/` ( права: 755) |

---

## ⚙️ КОНФИГУРАЦИЯ (config.json)

### Локация:
- **Путь:** `/data/user/0/ru.voboost.voiceassistant/files/config.json`
- **НЕ в assets** — APK облегчённый, конфиг только в data-папке
- Если не найден → дефолтная конфигурация

### Параметры:
- `temp` — температура для кондиционера (число или русское число)
- `contact` — имя контакта для звонков
- `number` — номер телефона
- `zone` — зона говорящего (driver/passenger/.../all)

---

## 🎯 АКТИВАЦИЯ

### Способы активации:

**1. Кодовая фраза:** "Привет, Вобуст" / "Привет, машина"

**2. Кнопка на руле:** KEYCODE_IVOKA (130) через Frida перехват
- Frida скрипт: `frida-voice-button.js`
- Перехват `KeyManager.inputKeyEvent(130)`
- Отправка Broadcast `ACTION_ACTIVATE` → Voboost
- Повторное нажатие → `ACTION_CANCEL` (отмена)

---

## 🔌 ИНТЕГРАЦИЯ С IVOKA

### Intent Actions (точно совпадают с оригиналом):
- `pateo.dls.ivoka.vehicle.CONTROL` — Управление машиной
- `pateo.dls.ivoka.air_control.OPEN/CLOSE/ADJUST` — Климат
- `pateo.dls.ivoka.telephone.CALL` — Телефон
- `pateo.dls.ivoka.SET_SMART_MODE` — Режимы

### Intent Parameters:
- `voice.param.vehicle.target/classify/command`
- `voice.param.air.target/classify/command`
- `voice.param.telephone.target/classify/command`
- `voice.param.contact` — Имя контакта

---

## 🔧 КОМАНДЫ ДЛЯ ОТЛАДКИ

```bash
# Собрать проект
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleDebug

# Установить
adb install app/build/outputs/apk/debug/app-debug.apk

# Дать разрешения
adb shell pm grant ru.voboost.voiceassistant android.permission.RECORD_AUDIO
adb shell pm grant ru.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant ru.voboost.voiceassistant android.permission.FOREGROUND_SERVICE

# Запустить сервис
adb shell am startservice ru.voboost.voiceassistant/.VoboostVoiceService

# Логи состояний
adb logcat -s StateMachine:* VoboostVoiceService:*

# Логи команд
adb logcat | grep -i "voboost\|CommandExecutor\|NLUEngine"
```

---

## ⚠️ ИЗВЕСТНЫЕ ПРОБЛЕМЫ

### Vosk small модель иногда путает числа (20 ↔ 22)
- **Ограничение модели** — ограничение Vosk small model (50MB)
- **Решение:** Используется `parseTemperature()` для русских числительных

---

## 📈 СТАТУС РАЗРАБОТКИ

- ✅ **State Machine** — Реализована (9 состояний, event-driven)
- ✅ **Speech Engine Factory** — Реализована (Vosk STT + Sherpa TTS)
- ✅ **Executor Pattern** — Реализован (Intent/Shell/Auto)
- ✅ **17 команд** — Из коробки (AIDL обработчики)
- ✅ **Multi-channel audio** — Реализована (зона говорящего)
- ✅ **KEYCODE кнопки** — Найден и работает (через Frida)
- ✅ **Модель Vosk** — Скачана и установлена
- ✅ **Sherpa-ONNX** — Интегрирована (v1.12.34)
- ✅ **TSR Speed Limit** — Реализован
- ✅ **2-этапная установка** — Реализована (APK → reboot → данные)

---

## 🎊 КЛЮЧЕВЫЕ ФАЙЛЫ

**Архитектура:**
- `ARCHITECTURE_V2.md` — State Machine refactoring (актуальна!)
- `ARCHITECTURE_SPEECH_ENGINE.md` — Модульные движки
- `COMPLETE_SOLUTION.md` — Frida интеграция кнопки
- `COMMAND_EXECUTOR_ARCHITECTURE.md` — Executor паттерн

**Код:**
- `VoboostVoiceService.kt` — Главный сервис (438 строк)
- `StateMachine.kt` — 9 состояний (202 строки)
- `SpeechRecognizer.kt` — Vosk STT
- `SherpaStream.kt` — Sherpa TTS
- `CommandExecutor.kt` — Базовая логика выполнения
- `config.json` — Команды и параметры

---

**Последнее обновление:** 2026-04-26  
**Git HEAD:** `18e67db` — refactor: архитектура Service и executorPattern  
**Build:** Gradle 8.13.2, Kotlin 2.1.0, compileSdk=36
