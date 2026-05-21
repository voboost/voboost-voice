# Установка Voice Assistant

**Дата:** 2026-05-21  
**Статус:** ✅ Актуальная версия  
**Package:** ru.voboost.voice  

---

## 📦 Полная установка (первая)

### Шаг 1: Сборка проекта

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleDebug
```

### Шаг 2: Запуск скрипта установки

```bash
scripts/install/VoboostVoiceAssistant-install.bat
```

**Что делает скрипт:**
1. Отключает стандартные ассистенты (IVoka, QGSpeechService)
2. Устанавливает APK
3. Копирует модели (Vosk + Sherpa TTS + NLU + LLM)
4. Выдает разрешения
5. **Устанавливает Hidden API Policy для AIDL**
6. Запускает сервис

---

## 🔄 Быстрое обновление

```bash
gradlew.bat assembleDebug
scripts/install/install-update.bat
```

---

## 🔧 Настройка разрешений

### Основные разрешения:
```bash
adb shell pm grant ru.voboost.voice android.permission.RECORD_AUDIO
adb shell pm grant ru.voboost.voice android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant ru.voboost.voice android.permission.FOREGROUND_SERVICE
```

### Hidden API Policy (для работы с AIDL) ⚠️

**Критично важно!** Для доступа к скрытым AIDL интерфейсам (AudioPolicyService, CanBusService):

```bash
# Основной флаг (универсальный для всех Android)
adb shell "settings put global hidden_api_policy 1"

# Дополнительные флаги для совместимости (Android 9-11)
adb shell "settings put global hidden_api_policy_pre_p 1"
adb shell "settings put global hidden_api_policy_pre_q 1"
adb shell "settings put global hidden_api_policy_pre_r 1"

# Проверка
adb shell settings get global hidden_api_policy
# Должно вернуть: 1
```

**Почему это нужно:**
- Android блокирует доступ к скрытым API (`@hide`)
- AIDL интерфейсы (`IAudioPolicyService`, `ICanBusService`) используют скрытые методы
- Без этой настройки не работает эхоподавление и CAN bus события

> **Примечание:** Скрипты установки автоматически настраивают Hidden API Policy.

---

## ⚙️ Проверка установки

```bash
# Проверить процесс
adb shell ps | grep voboost

# Проверить сервис
adb shell dumpsys activity services | grep -i voboost

# Проверить Hidden API Policy
adb shell settings get global hidden_api_policy
```

---

## 🐛 Решение проблем

### Сервис не запускается
```bash
adb shell pm grant ru.voboost.voice android.permission.RECORD_AUDIO
adb shell pm grant ru.voboost.voice android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant ru.voboost.voice android.permission.FOREGROUND_SERVICE
```

### AIDL не работает (эхоподавление / CAN bus)
```bash
adb shell "settings put global hidden_api_policy 1"
adb shell "settings put global hidden_api_policy_pre_p 1"
adb shell "settings put global hidden_api_policy_pre_q 1"
adb shell "settings put global hidden_api_policy_pre_r 1"
adb shell am force-stop ru.voboost.voice
adb shell am start-foreground-service -n ru.voboost.voice/.VoboostVoiceService
```

---

## 📚 Документы

- [Настройка](./CONFIGURATION.md)
- [Решение проблем](./TROUBLESHOOTING.md)
