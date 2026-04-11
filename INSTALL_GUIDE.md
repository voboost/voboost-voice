# 📦 VoboostVoiceAssistant - Руководство по установке

## 🗂️ Доступные скрипты

### 🔨 Сборка проекта

| Скрипт | Описание | Когда использовать |
|--------|----------|-------------------|
| **`build-project.bat`** | Собирает Debug и/или Release APK | После изменения кода |

**Использование:**
```batch
build-project.bat
# Выберите: 1=Debug, 2=Release, 3=Оба
```

---

### 🚀 Установка на устройство

| Скрипт | Описание | Когда использовать |
|--------|----------|-------------------|
| **`install-update.bat`** | Быстрая установка APK (без моделей) | Обновление кода, модели уже есть |
| **`VoboostVoiceAssistant-install.bat`** | Полное развёртывание с моделями | Первая установка или после сброса |

**Использование:**
```batch
# Быстрое обновление (только APK)
install-update.bat

# Полная установка (APK + модели + перезагрузка)
VoboostVoiceAssistant-install.bat
```

---

## 📋 Полный рабочий процесс

### Первая установка:
```batch
1. build-project.bat                  # Собрать APK (выбрать 1=Debug)
2. VoboostVoiceAssistant-install.bat  # Полная установка с моделями
   └─ Устройство перезагрузится автоматически
```

### Обновление кода:
```batch
1. build-project.bat                  # Собрать новый APK
2. install-update.bat                 # Быстрая установка (без перезагрузки)
```

### Только модели (если их нет на устройстве):
```batch
copy-vosk-to-internal.bat             # Копировать Vosk модель
# Sherpa TTS копируется автоматически при полной установке
```

---

## 🔧 Что делает каждый скрипт

### `build-project.bat`
- ✅ Собирает Debug APK (`app-debug.apk`)
- ✅ Собирает Release APK (`app-release-unsigned.apk`)
- ✅ Выбор типа сборки (1/2/3)

### `install-update.bat` (5 шагов)
1. Отключает стандартные ассистенты (ivoka, ivoka1, sttservice)
2. Получает root и перемонтирует /system
3. Копирует APK в `/system/priv-app/`
4. Выдаёт разрешения и инициализирует данные
5. Перезапускает сервис

### `VoboostVoiceAssistant-install.bat` (7 шагов)
1. Отключает стандартные ассистенты (ivoka, ivoka1, sttservice)
2. Проверяет наличие APK
3. Устанавливает APK как системное приложение
4. Копирует модели (Vosk + Sherpa TTS)
5. Инициализирует данные и выдаёт разрешения
6. Перезагружает устройство (для регистрации системного приложения)
7. Запускает сервис и проверяет логи

---

## 🎯 Отключаемые стандартные сервисы

| Пакет | Описание | Почему отключаем |
|-------|----------|------------------|
| `com.qinggan.ivoka` | Штатный голосовой ассистент IVoka | Конфликтует с нашим |
| `com.qinggan.ivoka1` | Вторая версия IVoka | Конфликтует с нашим |
| `com.qinggan.sttservice` | Штатный STT сервис | Перехватывает микрофон |

---

## 📁 Расположение файлов на устройстве

### APK:
```
/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Модели:
```
/data/user/0/com.voboost.voiceassistant/files/models/
├── vosk/
│   └── vosk-model-small-ru-0.22/    # Распознавание речи (STT)
└── sherpa/
    └── tts-ru-model/                 # Синтез речи (TTS)
        ├── ru_RU-ruslan-medium.onnx
        ├── tokens.txt
        └── espeak-ng-data/
```

### Конфигурация:
```
/storage/emulated/0/Android/data/com.voboost.voiceassistant/files/config.json
```

---

## 🐛 Решение проблем

### APK не найден:
```
Ошибка: APK не найден: app\build\outputs\apk\debug\app-debug.apk
Решение: Запустите build-project.bat или соберите в Android Studio
```

### Сервис не запускается:
```batch
# Проверить процесс
adb shell ps | findstr voboost

# Посмотреть логи
adb logcat -d -t 100 | findstr /i "voboost fatal error"

# Перезапустить
adb shell am force-stop com.voboost.voiceassistant
adb shell am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService
```

### Модели не найдены:
```batch
# Полная переустановка с моделями
VoboostVoiceAssistant-install.bat
```

---

## 📊 Сравнение скриптов установки

| Действие | install-update | VoboostVoiceAssistant-install |
|----------|---------------|-------------------------------|
| Отключение штатных ассистентов | ✅ | ✅ |
| Установка APK | ✅ | ✅ |
| Копирование моделей | ❌ | ✅ |
| Инициализация данных | ✅ | ✅ |
| Перезагрузка устройства | ❌ | ✅ |
| Проверка запуска | ❌ | ✅ |
| Время выполнения | ~15 секунд | ~2-3 минуты (с перезагрузкой) |

---

**Последнее обновление:** 2026-04-07
**Версия:** 19.0
