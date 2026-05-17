"# Установка Voice Assistant\n\n**Дата:** 2026-05-15  \n**Статус:** ✅ Актуальная версия  \n**Package:** ru.voboost.voiceassistant  \n\n---\n\n## 📦 Полная установка (первая)\n\n### Шаг 1: Сборка проекта\n\n```bash\ncd D:\\Projects\\Android\\MM\\6.11.1\\export\\VoboostVoiceAssistant\n\n# Собрать Debug APK (для тестирования)\ngradlew.bat assembleDebug\n```\n\n### Шаг 2: Запуск скрипта установки\n\n```bash\nscripts/install/VoboostVoiceAssistant-install.bat\n```\n\nЭтот скрипт автоматически:\n1. Отключает стандартные ассистенты (IVoka, QGSpeechService)\n2. Устанавливает APK в системную папку `/system/priv-app/`\n3. Копирует модели (Vosk + Sherpa TTS) на устройство\n4. Выдает все необходимые разрешения\n5. Перезагружает устройство\n6. Проверяет запуск сервиса\n\n**Продолжительность:** ~2-3 минуты (включая перезагрузку)\n\n---\n\n## 🔄 Обновление кода (без перезагрузки)\n\n```bash\n# Собрать новый APK\ngradlew.bat assembleDebug\n\n# Быстрое обновление\nscripts/install/install-update.bat\n```\n\n**Продолжительность:** ~15 секунд\n\n---\n
<|im_start|>## 📁 Что устанавливается

### APK:
```
/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Нативные библиотеки:
```
/system/priv-app/VoboostVoiceAssistant/lib/arm64/
├── libvosk.so
├── libonnxruntime.so
├── libjnidispatch.so
└── libsherpa-onnx-jni.so
```

### Модели:
```
/data/user/0/ru.voboost.voiceassistant/files/models/
├── vosk/vosk-model-small-ru-0.22/       # STT (распознавание)
└── sherpa/tts-ru-model/                 # TTS (синтез)
    ├── ru_RU-ruslan-medium.onnx
    ├── tokens.txt
    └── espeak-ng-data/
```

### Конфигурация:
```
/data/user/0/ru.voboost.voiceassistant/files/config.json
```

---\n\n## 🔧 Настройка разрешений\n\n```bash\n# Основные разрешения\nadb shell pm grant ru.voboost.voiceassistant android.permission.RECORD_AUDIO\nadb shell pm grant ru.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW\nadb shell pm grant ru.voboost.voiceassistant android.permission.FOREGROUND_SERVICE\n\n# Дать разрешение на автозапуск\nadb shell appops set ru.voboost.voiceassistant RECEIVE_BOOT_COMPLETED allow\n```\n\n### Настройка Accessibility Service\n\n```bash\nadb shell settings put secure enabled_accessibility_services ru.voboost.voiceassistant/ru.voboost.voiceassistant.VoiceActivationService\nadb shell settings put secure accessibility_enabled 1\n```\n\n---\n\n## ⚙️ Проверка установки\n\n### 1. Проверить процесс сервиса\n\n```bash\nadb shell ps | grep voboost\n```\n\n**Ожидаемый вывод:**\n```\nu0_a68    12345  1234 ru.voboost.voiceassistant\n```\n\n### 2. Проверить запущенный сервис\n\n```bash\nadb shell dumpsys activity services | grep -i voboost\n```\n\n**Ожидаемый вывод:**\n```\nACTIVITY ru.voboost.voiceassistant/.VoboostVoiceService\n```\n\n### 3. Проверить логи инициализации\n\n```bash\nadb logcat -d | grep -i \"voboost\\|vosk\\|sherpa\"\n```\n\n**Ожидаемые логи:**\n```\nI/VoboostVoiceService: onCreate\nI/SpeechRecognition: Vosk initialized successfully!\nI/SpeechSynthesis: Sherpa TTS initialized successfully!\n```\n\n---\n\n## 🐛 Решение проблем\n\n### Проблема 1: APK не найден\n\n**Ошибка:** `APK not found: app/build/outputs/apk/debug/app-debug.apk`\n\n**Решение:**\n```bash\ngradlew.bat assembleDebug\n```\n\n### Проблема 2: Сервис не запускается\n\n**Логи:** `Service started but crashed`\n\n**Решение:**\n```bash\n# Проверить разрешения\nadb shell dumpsys package ru.voboost.voiceassistant | grep -A 30 \"requested permissions\"\n\n# Дать все разрешения\nadb shell pm grant ru.voboost.voiceassistant android.permission.RECORD_AUDIO\nadb shell pm grant ru.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW\nadb shell pm grant ru.voboost.voiceassistant android.permission.FOREGROUND_SERVICE\n```\n\n### Проблема 3: Модели не найдены\n\n**Логи:** `Vosk model not found` или `Sherpa TTS model not found`\n\n**Решение:**\n```bash\n# Полная переустановка с моделями\nscripts/install/VoboostVoiceAssistant-install.bat\n```\n\n### Проблема 4: Конфликт со стандартными ассистентами\n\n**Логи:** `IVoka is also active` или `QGSpeechService is capturing audio`\n\n**Решение:**\n```bash\n# Отключить стандартные сервисы\nadb shell pm disable com.qinggan.ivoka\nadb shell pm disable com.qinggan.ivoka1\nadb shell pm disable com.qinggan.sttservice\n```\n\n---\n\n## 🎯 Установка для разработчиков\n\n### Сборка и установка через Android Studio:\n\n1. Открыть проект в Android Studio\n2. Выбрать устройство/эмулятор\n3. Нажать `Run` → `Run 'app'`\n4. Дождаться установки и запуска\n\n### Сборка через командную строку:\n\n```bash\n# Debug сборка\nggradlew.bat assembleDebug\n\n# Release сборка (без отладки)\nggradlew.bat assembleRelease\n```\n\n---\n\n## 📊 Сравнение методов установки\n\n| Метод | APK | Модели | Перезагрузка | Время |\n|-------|-----|--------|--------------|-------|\n| **VoboostVoiceAssistant-install.bat** | ✅ | ✅ | ✅ | ~3 мин |\n| **install-update.bat** | ✅ | ❌ | ❌ | ~15 сек |\n| **adb install** | ✅ | ❌ | ❌ | ~20 сек |\n| **Android Studio Run** | ✅ | ❌ | ❌ | ~30 сек |\n\n---\n\n## 📝 Чек-лист готовности\n\nПосле установки проверьте:\n\n- [ ] Сервис запущен (`adb shell ps | grep voboost`)\n- [ ] Разрешения выданы (`adb shell dumpsys package ru.voboost.voiceassistant`)\n- [ ] Модели установлены (файлы в `/data/user/0/ru.voboost.voiceassistant/files/models/`)\n- [ ] Конфигурация загружена (`config.json` создан)\n- [ ] Accessibility Service активен\n\n---\n\n## 🚀 Быстрый старт\n\nПосле установки:\n\n1. **Активация кнопкой:** Нажать кнопку на руле (KEYCODE=130)\n2. **Активация голосом:** Сказать \"Привет, Вобуст\"\n3. **Команда:** Сказать \"Открой лючок зарядки\"\n4. **Проверка логов:** `adb logcat | grep -i voboost`\n\n---\n\n## 📚 Дополнительная документация\n\n- [Настройка конфигурации](./CONFIGURATION.md)\n- [Решение проблем](./TROUBLESHOOTING.md)\n- [Интеграция с IVoka](../TECHNICAL_ANALYSIS/IVOKA_INTEGRATION.md)"