# 📦 BATCH ФАЙЛЫ - ПОЛНАЯ ИНСТРУКЦИЯ

## 📁 Доступные скрипты

### 1. **VoboostVoiceAssistant-install.bat** ⭐
**Назначение:** Установка приложения + все разрешения

**Использование:**
```bash
VoboostVoiceAssistant-install.bat
```

**Что делает:**
- ✅ Устанавливает APK
- ✅ Дает 6 разрешений (микрофон, overlay, accessibility, notifications, boot, foreground)
- ✅ Проверяет установку

**Когда использовать:** Первая установка приложения

---

### 2. **grant_permissions.bat**
**Назначение:** Повторная выдача разрешений

**Использование:**
```bash
grant_permissions.bat
```

**Что делает:**
- ✅ Выдает 6 разрешений
- ✅ Включает Accessibility Service

**Когда использовать:**
- После переустановки приложения
- Если сбросились разрешения

---

### 3. **test_auto_start.bat** 🆕
**Назначение:** Тестирование автозапуска

**Использование:**
```bash
test_auto_start.bat
```

**Что делает:**
- ✅ Эмулирует загрузку системы
- ✅ Проверяет запуск сервиса
- ✅ Показывает логи

**Когда использовать:**
- После установки
- Для проверки автозапуска

---

### 4. **check_status.bat** 🆕
**Назначение:** Полная проверка состояния

**Использование:**
```bash
check_status.bat
```

**Что делает:**
- ✅ Проверяет подключение устройства
- ✅ Проверяет установку приложения
- ✅ Проверяет разрешения
- ✅ Проверяет Accessibility Service
- ✅ Проверяет автозапуск

**Когда использовать:**
- Перед тестированием
- Для диагностики проблем

---

## 🚀 ПОЛНЫЙ ПРОЦЕСС УСТАНОВКИ

### **Шаг 1: Сборка**
```bash
gradlew.bat assembleDebug
```

### **Шаг 2: Установка**
```bash
VoboostVoiceAssistant-install.bat
```

### **Шаг 3: Проверка**
```bash
check_status.bat
```

### **Шаг 4: Тест автозапуска**
```bash
test_auto_start.bat
```

### **Шаг 5: Тестирование**
```
Сказать: "Привет, Вобуст"
Сказать: "Открой лючок зарядки"
```

---

## 📊 ТАБЛИЦА РАЗРЕШЕНИЙ

| Разрешение | Команда | Зачем |
|------------|---------|-------|
| **RECORD_AUDIO** | `appops set ... RECORD_AUDIO allow` | Распознавание речи |
| **SYSTEM_ALERT_WINDOW** | `appops set ... SYSTEM_ALERT_WINDOW allow` | Overlay (анимация, toast) |
| **ACCESSIBILITY** | `settings put secure enabled_accessibility_services` | Перехват кнопки |
| **POST_NOTIFICATION** | `appops set ... POST_NOTIFICATION allow` | Уведомления сервиса |
| **RECEIVE_BOOT_COMPLETED** | `appops set ... RECEIVE_BOOT_COMPLETED allow` | Автозапуск |
| **FOREGROUND_SERVICE** | `appops set ... FOREGROUND_SERVICE allow` | Фоновый сервис |

---

## 🔍 ДИАГНОСТИКА

### **Приложение не запускается:**
```bash
check_status.bat
```

### **Не работает автозапуск:**
```bash
test_auto_start.bat
```

### **Сбросились разрешения:**
```bash
grant_permissions.bat
```

### **Ничего не помогает:**
```bash
# Полная переустановка
VoboostVoiceAssistant-install.bat
```

---

## 📱 ПРОВЕРКА ЧЕРЕЗ ADB

### **Проверить разрешения:**
```bash
adb shell dumpsys package ru.voboost.voiceassistant
```

### **Проверить сервис:**
```bash
adb shell dumpsys activity services | findstr voboost
```

### **Проверить Accessibility:**
```bash
adb shell settings get secure enabled_accessibility_services
```

### **Проверить логи:**
```bash
adb logcat | grep -i "voboost"
```

---

## ✅ ЧЕК-ЛИСТ ГОТОВНОСТИ

- ✅ Приложение собрано (`app-debug.apk`)
- ✅ Устройство подключено (`adb devices`)
- ✅ Запущен `VoboostVoiceAssistant-install.bat`
- ✅ Все 6 разрешений выданы
- ✅ Accessibility Service включен
- ✅ Автозапуск проверен через `test_auto_start.bat`
- ✅ Логин показывает "Vosk initialized successfully"

---

## 🎯 БЫСТРЫЕ КОМАНДЫ

```bash
# Установить
VoboostVoiceAssistant-install.bat

# Проверить
check_status.bat

# Тест автозапуска
test_auto_start.bat

# Дать разрешения
grant_permissions.bat

# Собрать
gradlew.bat assembleDebug
```

---

## 🎉 ГОТОВО!

**Все скрипты готовы к использованию! 🚀**

**Следующий шаг:** Запустить `VoboostVoiceAssistant-install.bat` и протестировать!
