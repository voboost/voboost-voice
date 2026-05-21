# Voice Assistant for Car - Документация

**Последнее обновление:** 2026-05-15  
**Статус:** ✅ ВСЕ РАБОТАЕТ  
**Package:** ru.voboost.voice  

---

## 📖 Оглавление

- [Основные возможности](#-основные-возможности)
- [Структура проекта](#-структура-проекта)
- [Архитектура](#-архитектура)
- [Быстрый старт](#-быстрый-старт)
- [Установка](#-установка)
- [Настройка](#-настройка)
- [Решение проблем](#-решение-проблем)

---

## ✨ Основные возможности

| Функция | Статус | Описание |
|---------|--------|----------|
| 🎤 Голосовое управление | ✅ | Распознавание русской речи (офлайн + онлайн) |
| 🚗 Управление автомобилем | ✅ | **26 команд из коробки** (лючки, климат, режимы, звонки и др.) |
| 📞 Телефон | ✅ | Звонки по контактам и номерам |
| 🔊 TTS ответы | ✅ | Синтез речи (Sherpa-ONNX) |
| 👀 Визуальные эффекты | ✅ | Анимация микрофона |
| 🛣️ TSR (распознавание знаков) | ✅ | Предупреждения о превышении скорости |
| 🎯 Zone Detection | ✅ | Определение зоны говорящего |
| 🔑 Кнопка на руле | ✅ | KEYCODE=16 в config.json |
| 🚀 Автозапуск | ✅ | Запуск при загрузке системы |

**Команды из коробки:**
- Управление автомобилем (лючки, бензобак)
- Режимы умной системы (отдых, детский, романтика, мойка)
- Климат (вкл/выкл, температура ±1°)
- Телефон (звонки по имени и номеру)
- Окна (открыть/закрыть отдельно или все сразу)
- Режимы вождения (эко, комфорт, спорт, бездорожье, индивидуальный, снег)
- Режимы источника питания (электро, гибрид, сохранение)

---

## 🏗️ Структура проекта

```
VoboostVoiceAssistant/
├── app/src/main/
│   ├── java/ru/voboost/voiceassistant/
│   │   ├── VoboostVoiceService.kt          # Главный сервис
│   │   ├── VoiceActivationService.kt       # Accessibility для кнопки
│   │   ├── SoundEffectManager.kt           # Звуковые эффекты
│   │   ├── BootActivity.kt                 # Для автозапуска
│   │   ├── BootReceiver.kt                 # Обработчик BOOT_COMPLETED
│   │   └── VoiceCommandReceiver.kt         # Broadcast receiver
│   │
│   ├── audio/                              # Источники аудио (9 файлов)
│   │   ├── IAudioSource.kt                 # Интерфейс аудио
│   │   ├── AndroidAudioSource.kt           # Микрофон Android API
│   │   ├── MultiChannelAudioSource.kt      # Мультиканальный аудио (zone detection)
│   │   ├── RecorderManagerAudioSource.kt   # RecorderManager API
│   │   └── ...
│   │
│   ├── canbus/                             # CAN Bus интеграция (5 файлов)
│   │   ├── CanBusServiceManager.kt         # Управление сервисом
│   │   ├── ICanBusServiceConnectionCallback.kt
│   │   └── handlers/
│   │       ├── TSRSpeedLimitHandler.kt     # Превышение скорости
│   │       ├── VoiceButtonHandler.kt       # Кнопка на руле (KEYCODE=16)
│   │       └── TestCanBusServiceHandler.kt
│   │
│   ├── config/                             # Конфигурация (6 файлов)
│   │   ├── ApiKeys.kt                      # API ключи (не коммить!)
│   │   ├── AppConfig.kt                    # Data классы конфига
│   │   ├── CommandConfig.kt                # Конфиг команд
│   │   ├── ConfigManager.kt                # Загрузка конфига
│   │   └── ExternalStoragePaths.kt         # Пути к моделям
│   │
│   ├── core/                               # Ядро системы (7 файлов)
│   │   ├── ISpeechRecognizer.kt            # Интерфейс STT
│   │   ├── ISpeechSynthesis.kt             # Интерфейс TTS
│   │   └── SpeechEngineFactory.kt          # Фабрика движков
│   │
│   ├── engine/                             # Движки STT/TTS (10+ файлов)
│   │   ├── sherpa/                         # Sherpa-ONNX (TTS + STT)
│   │   ├── vosk/                           # Vosk (STT)
│   │   └── system/                         # Системный TTS (fallback)
│   │
│   ├── executor/                           # Обработчики команд (45+ файлов)
│   │   ├── CommandExecutor.kt              # Базовый класс
│   │   ├── IVehicleCommandExecutor.kt      # Интерфейс
│   │   └── handlers/
│   │       ├── aidl/                       # AIDL обработчики (30+ файлов)
│   │       └── intent/                     # Intent обработчики
│   │
│   ├── nlu/                                # Natural Language Understanding (6 файлов)
│   │   ├── NLUEngine.kt                    # Парсер команд
│   │   ├── Command.kt                      # Data классы
│   │   └── ...
│   │
│   ├── speech/                             # Модули распознавания (10+ файлов)
│   │   ├── KeywordChecker.kt               # Проверка ключевых слов
│   │   ├── SpeechRecognizer.kt             # Распознавание речи
│   │   └── state/                          # 9 состояний State Machine
│   │
│   └── ui/                                 # UI компоненты (2 файла)
│       ├── OverlayManager.kt               # Оверлеи и анимация
│       └── VoiceClickView.kt               # Анимация микрофона
│
├── assets/
│   └── config.json                         # Конфигурация команд (26 команд)
│
└── models/                                 # Модели распознавания
    ├── vosk/vosk-model-small-ru-0.22/     # Vosk STT модель (50MB)
    └── sherpa/
        ├── asr-ru-model/                   # Sherpa STT (в разработке)
        └── tts-ru-model/                   # Sherpa TTS (100MB)
```

**Всего:** 103 Kotlin файла, ~5000+ строк кода

---

## 🧠 Архитектура

### State Machine (9 состояний)

```
State Type               | Описание
------------------------|----------------------------------
IDLE                    | Ожидание активации
ACTIVATED              | Активирован (проигран звук)
LISTENING_COMMAND      | Слушание команды
RECOGNIZED_COMMAND     | Команда распознана
EXECUTING_COMMAND      | Выполнение команды
CONFIRMATION           | Подтверждение действия
COMMAND_ERROR          | Ошибка команды
KEYWORD_ERROR          | Ошибка ключевого слова
TIMEOUT                | Таймаут
```

### Speech Engine Factory

Выбор движков по приоритету:

1. **Sherpa TTS** → SherpaSpeechSynthesis (приоритет)
2. **Vosk STT** → VoskRecognition  
3. **System TTS** → SystemSpeechSynthesis (fallback)

### Executor Pattern для команд

```
CommandExecutor
    ↓ использует
IVehicleCommandExecutor (интерфейс)
    ↓ реализуют
├── IntentVehicleCommandExecutor      # Broadcast Intent
├── ShellVehicleCommandExecutor       # Shell CAN-команды
└── AutoVehicleCommandExecutor        # Автоматический выбор
```

---

## 🚀 Быстрый старт

### 1. Сборка проекта

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant

# Собрать APK
gradlew.bat assembleDebug

# Или собрать релизную версию
gradlew.bat assembleRelease
```

### 2. Установка на устройство

```bash
# Установить через ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Или установить в систему (требует root)
adb root && adb remount
adb push app/build/outputs/apk/release/app-release-unsigned.apk /system/priv-app/VoboostVoiceAssistant/
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### 3. Дача разрешений

```bash
# Основные разрешения
adb shell pm grant ru.voboost.voice android.permission.RECORD_AUDIO
adb shell pm grant ru.voboost.voice android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant ru.voboost.voice android.permission.FOREGROUND_SERVICE

# Дать разрешение на автозапуск
adb shell appops set ru.voboost.voice RECEIVE_BOOT_COMPLETED allow
```

### 4. Настройка Accessibility Service

```bash
adb shell settings put secure enabled_accessibility_services ru.voboost.voice/ru.voboost.voice.VoiceActivationService
adb shell settings put secure accessibility_enabled 1
```

---

## ⚙️ Установка

Полная инструкция установки доступна в документе: [SETUP/INSTALLATION.md](./SETUP/INSTALLATION.md)

### Двухэтапная установка:

**Этап 1:** Установить APK и библиотеки
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
scripts/install/VoboostVoiceAssistant-install.bat
```

**Этап 2:** После перезагрузки - модели и конфигурация
```bash
scripts/install/copy-models-to-sdcard.bat
scripts/install/copy-config-to-sdcard.bat
```

---

## ⚙️ Настройка

### Конфигурация команд

Команды настраиваются в `app/src/main/assets/config.json` или `/data/user/0/ru.voboost.voice/files/config.json`.

Пример команды:

```json
{
  "id": "charge_port_open",
  "enabled": true,
  "patterns": [
    "открой лючок зарядки",
    "открой порт зарядки"
  ],
  "action": {
    "target": "Chargport",
    "classify": 35,
    "command": 1
  },
  "phrases": {
    "success": "Открываю лючок зарядки"
  }
}
```

### API ключи (опционально)

Для онлайн-режима Yandex SpeechKit:

```kotlin
// app/src/main/java/ru/voboost/voiceassistant/config/ApiKeys.kt
const val YANDEX_SPEECH_API_KEY = "your_api_key"
const val YANDEX_FOLDER_ID = "your_folder_id"
```

---

## 🐛 Решение проблем

Полный раздел с решением проблем: [SETUP/TROUBLESHOOTING.md](./SETUP/TROUBLESHOOTING.md)

### Частые проблемы:

| Проблема | Решение |
|---------|---------|
| Сервис не запускается | Проверить разрешения и Accessibility Service |
| Голос не работает | Убедиться что модель скачана и установлена |
| Кнопка на руле не реагирует | Проверить Frida скрипт и KEYCODE |
| Звуки не слышны | Проверить разрешение SYSTEM_ALERT_WINDOW |

---

## 📚 Дополнительная документация

- [ARCHITECTURE/](./ARCHITECTURE/) - Архитектурные документы
- [FEATURES/](./FEATURES/) - Описание функций
- [SETUP/](./SETUP/) - Установка и настройка  
- [TECHNICAL_ANALYSIS/](./TECHNICAL_ANALYSIS/) - Технические детали

---

## 📞 Поддержка

**Author:** Voboost Team  
**License:** [License information to be added]
