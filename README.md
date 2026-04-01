# VoboostVoiceAssistant

Голосовой помощник для автомобилей с поддержкой русского языка.

## Возможности

- ✅ Голосовое управление автомобилем (лючки, режимы, климат)
- ✅ Управление телефоном (звонки)
- ✅ Офлайн распознавание речи (Vosk)
- ✅ Онлайн распознавание (Yandex SpeechKit - опционально)
- ✅ Активация по кнопке на руле или кодовой фразе
- ✅ Визуальные эффекты (анимация как в оригинале)
- ✅ TTS ответы (RhVoice/системный)
- ✅ Расширяемая система команд (JSON конфиг)

## Структура проекта

```
app/src/main/
├── java/com/voboost/voiceassistant/
│   ├── VoboostVoiceService.kt       # Главный сервис
│   ├── VoiceActivationService.kt    # Accessibility для кнопки
│   ├── VoiceCommandReceiver.kt      # Broadcast receiver
│   │
│   ├── config/
│   │   ├── AppConfig.kt             # Data классы конфига
│   │   ├── CommandConfig.kt         # Конфиг команд
│   │   ├── ApiKeys.kt               # API ключи (не коммить!)
│   │   └── ConfigManager.kt         # Загрузка конфига
│   │
│   ├── speech/
│   │   └── SpeechRecognitionModule.kt  # Vosk распознавание
│   │
│   ├── nlu/
│   │   ├── NLUEngine.kt             # Парсинг команд
│   │   └── Command.kt               # Data классы команд
│   │
│   ├── executor/
│   │   └── CommandExecutor.kt       # Выполнение команд
│   │
│   ├── tts/
│   │   └── TTSEngine.kt             # Синтез речи
│   │
│   └── ui/
│       ├── OverlayManager.kt        # Оверлеи
│       └── VoiceClickView.kt        # Анимация
│
├── assets/
│   ├── config.json                  # Главный конфиг
│   └── vosk/
│       └── model-ru/                # Модель Vosk (скачать отдельно)
│
└── res/
    ├── layout/
    │   └── toast_voice.xml
    ├── drawable/
    │   ├── ic_voice.xml
    │   └── toast_background.xml
    └── xml/
        └── accessibility_service_config.xml
```

## Установка

### 1. Сборка приложения

```bash
# Открыть проект в Android Studio
# Или собрать через командную строку:
./gradlew assembleDebug
```

### 2. Скачать модель Vosk

1. Скачать модель: https://alphacephei.com/vosk/models
   - **Рекомендуемая:** `vosk-model-small-ru-0.22` (50MB)
   
2. Распаковать и поместить в:
   ```
   app/src/main/assets/vosk/vosk-model-small-ru-0.22/
   ```

### 3. Установка на устройство

```bash
# Подключить устройство по USB
adb install app/build/outputs/apk/debug/app-debug.apk

# Или через Android Studio: Run → Run 'app'
```

### 4. Настройка разрешений

После установки:

1. **Разрешить микрофон:**
   - Настройки → Приложения → Voboost Voice → Разрешения → Микрофон

2. **Включить Accessibility Service:**
   - Настройки → Спе. возможности → Voboost Voice Button → ВКЛ

3. **Разрешить поверх других окон:**
   - Настройки → Приложения → Voboost Voice → Поверх других окон → РАЗРЕШИТЬ

## Настройка

### API ключи Yandex (опционально)

Для онлайн режима нужны ключи Yandex Cloud:

1. Зарегистрироваться: https://cloud.yandex.ru
2. Создать платежный аккаунт (есть бесплатный лимит)
3. Получить API ключ и Folder ID
4. Вставить в `app/src/main/java/com/voboost/voiceassistant/config/ApiKeys.kt`:

```kotlin
const val YANDEX_SPEECH_API_KEY = "your_api_key"
const val YANDEX_FOLDER_ID = "your_folder_id"
```

**Важно:** Не коммитьте файл с ключами в git!

### Конфигурация команд

Команды настраиваются в `app/src/main/assets/config.json`:

```json
{
  "commands": [
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
      "confirmation": {
        "required": false
      },
      "phrases": {
        "success": "Открываю лючок зарядки"
      }
    }
  ]
}
```

## Использование

### Активация

1. **Кодовая фраза:** "Привет, Вобуст" (или "Привет, машина")
2. **Кнопка на руле:** (требует настройки keycode)

### Примеры команд

#### Управление автомобилем:
- "Открой лючок зарядки"
- "Закрой бензобак"
- "Включи режим отдыха"
- "Включи детский режим"
- "Включи кондиционер"
- "Установи 22 градуса"

#### Телефон:
- "Позвони маме"
- "Набери 123-45-67"

#### Окна:
- "Открой окно"
- "Закрой все окна"

## Добавление новых команд

1. Открыть `app/src/main/assets/config.json`
2. Добавить новую команду:

```json
{
  "id": "my_custom_command",
  "enabled": true,
  "patterns": [
    "моя команда",
    "выполни действие"
  ],
  "action": {
    "target": "Target",
    "classify": 35,
    "command": 1,
    "intent_action": "pateo.dls.ivoka.vehicle.CONTROL",
    "params": {}
  },
  "confirmation": {
    "required": false
  },
  "phrases": {
    "success": "Выполняю команду",
    "failure": "Не получилось"
  }
}
```

3. Пересобрать и установить приложение

## Поиск keycode кнопки на руле

Для активации кнопкой нужно найти правильный keycode:

```bash
# Подключить устройство
adb logcat | grep -i "key\|button\|voice"

# Нажать кнопку на руле
# Смотреть вывод logcat

# Или использовать:
adb shell getevent -l
```

Найденный keycode вставить в `VoiceActivationService.kt`:
```kotlin
private val VOICE_BUTTON_KEYCODES = intArrayOf(
    219, // Заменить на найденный
)
```

## Архитектура

```
Пользователь говорит команду
        ↓
Vosk распознавание (офлайн)
        ↓
NLUEngine парсит команду
        ↓
Проверка подтверждения (если нужно)
        ↓
CommandExecutor отправляет Intent
        ↓
Система выполняет (QGSpeechService)
        ↓
TTS говорит результат + Overlay
```

## Статус

- ✅ Базовая функциональность
- ✅ Офлайн распознавание (Vosk)
- ✅ 13 команд из коробки
- ⚠️ Кнопка на руле (TODO - найти keycode)
- ⚠️ Онлайн режим (TODO - добавить ключи)
- ⚠️ Подтверждение команд (TODO - диалог)

## Лицензия

MIT License

## Авторы

Voboost Team
