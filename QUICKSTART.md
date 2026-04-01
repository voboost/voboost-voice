# Быстрый старт VoboostVoiceAssistant

## Шаг 1: Открыть проект в Android Studio

1. Запустить Android Studio
2. File → Open → Выбрать папку `VoboostVoiceAssistant`
3. Дождаться синхронизации Gradle

## Шаг 2: Скачать модель Vosk

```bash
# Перейти в папку assets
cd app/src/main/assets

# Скачать модель (Windows PowerShell)
Invoke-WebRequest -Uri "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip" -OutFile "vosk-model-small-ru-0.22.zip"

# Распаковать (нужен 7-Zip или встроенный архиватор)
# Или вручную через проводник

# Переименовать папку
Rename-Item -Path "vosk-model-small-ru-0.22" -NewName "vosk-model-small-ru-0.22"
```

**Или вручную:**
1. Скачать: https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip
2. Распаковать в: `app/src/main/assets/vosk/`

## Шаг 3: Собрать приложение

```bash
# В Android Studio:
Build → Build Bundle(s) / APK(s) → Build APK(s)

# Или через командную строку:
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew assembleDebug
```

## Шаг 4: Установить на устройство

```bash
# Подключить устройство по USB
# Включить отладку по USB

# Установить APK:
adb install app/build/outputs/apk/debug/app-debug.apk

# Или через Android Studio:
Run → Run 'app' (Shift+F10)
```

## Шаг 5: Настроить разрешения

На устройстве:

1. **Настройки → Приложения → Voboost Voice**
2. **Разрешения:**
   - ✅ Микрофон
   - ✅ Поверх других окон
3. **Спец. возможности:**
   - ✅ Voboost Voice Button → ВКЛ

## Шаг 6: Проверить работу

1. Запустить приложение (оно автоматически запускает сервис)
2. Сказать: **"Привет, Вобуст"**
3. После активации сказать: **"Открой лючок зарядки"**
4. Проверить результат в логах:

```bash
adb logcat | grep -i "voboost\|voice"
```

## Проверка команд

Попробуйте команды:

```
# Зарядка
"Открой лючок зарядки"
"Закрой порт зарядки"

# Бензобак
"Открой бензобак"

# Режимы
"Включи режим отдыха"
"Включи детский режим"
"Включи романтику"

# Климат
"Включи кондиционер"
"Выключи кондиционер"
"Установи 22 градуса"

# Окна
"Открой окно"
"Закрой окно"

# Телефон
"Позвони маме"
```

## Отладка

### Логи приложения:
```bash
adb logcat -s VoboostVoiceService
adb logcat -s NLUEngine
adb logcat -s SpeechRecognition
```

### Если не работает кнопка на руле:
```bash
# Найти keycode:
adb logcat | grep -i "keycode\|keyevent"

# Нажать кнопку на руле, смотреть вывод
```

### Если не распознается речь:
```bash
# Проверить модель Vosk:
adb shell "ls -la /data/data/com.voboost.voiceassistant/files/"

# Проверить микрофон:
adb shell "dumpsys audio"
```

## Следующие шаги

1. ✅ Проверить работу базовых команд
2. ⚠️ Найти keycode кнопки на руле
3. ⚠️ Добавить API ключи Yandex (для онлайн режима)
4. ⚠️ Настроить подтверждение команд
5. ⚠️ Добавить свои команды в config.json

## Проблемы и решения

### Ошибка: "Vosk model not found"
- Проверить, что модель скачана и лежит в `app/src/main/assets/vosk/`
- Пересобрать приложение

### Ошибка: "Permission denied"
- Дать все разрешения в настройках приложения
- Проверить SYSTEM_ALERT_WINDOW

### Не слышно TTS
- Проверить громкость медиа
- Установить RhVoice или другой русский TTS
- Проверить настройки TTS в системе

### Не работает кнопка
- Включить Accessibility Service
- Найти правильный keycode (см. выше)

## Контакты

Вопросы и предложения: [ваш email]
