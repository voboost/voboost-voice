# Решение проблем Voice Assistant

**Дата:** 2026-05-21  
**Статус:** ✅ Актуальная версия  
**Package:** ru.voboost.voice  

---

## ❓ Частые проблемы и их решения

### Проблема 1: Сервис не запускается

**Симптомы:**
- Приложение установлено, но не работает
- В логах ошибки инициализации
- Нет активности в UI

**Решение:**
```bash
adb shell pm grant ru.voboost.voice android.permission.RECORD_AUDIO
adb shell pm grant ru.voboost.voice android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant ru.voboost.voice android.permission.FOREGROUND_SERVICE
adb shell settings put secure enabled_accessibility_services ru.voboost.voice/ru.voboost.voice.VoiceActivationService
adb shell settings put secure accessibility_enabled 1
```

---

### Проблема 2: Модели не найдены

**Симптомы:** `Vosk model not found` или `Sherpa TTS model not found`

**Решение:**
```bash
scripts/install/VoboostVoiceAssistant-install.bat
```

---

### Проблема 3: Голос не слышен (TTS)

**Симптомы:** Распознавание работает, но ответов нет

**Решение:**
```bash
adb shell appops set ru.voboost.voice SYSTEM_ALERT_WINDOW allow
adb shell am force-stop ru.voboost.voice
adb shell am start-foreground-service -n ru.voboost.voice/.VoboostVoiceService
```

---

### Проблема 4: Кнопка на руле не работает

**Симптомы:** Нажатие кнопки не активирует помощник

**Решение:**
```bash
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
adb shell pm disable com.qinggan.sttservice
```

---

### Проблема 5: Конфликт со стандартными ассистентами

**Симптомы:** IVoka активен одновременно с Voboost

**Решение:**
```bash
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
adb shell pm disable com.qinggan.sttservice
```

---

### Проблема 6: Автозапуск не работает

**Симптомы:** Сервис не запускается после перезагрузки

**Решение:**
```bash
adb shell appops set ru.voboost.voice RECEIVE_BOOT_COMPLETED allow
```

---

### Проблема 7: Ошибки инициализации движков

**Симптомы:** Ошибки Vosk или Sherpa в логах

**Решение:**
```bash
adb shell pm clear ru.voboost.voice
scripts/install/VoboostVoiceAssistant-install.bat
```

---

### Проблема 8: AIDL не работает (CAN bus / AudioPolicy) ⚠️

**Симптомы:**
- Сервис запущен, но **не работает эхоподавление** при Bluetooth звонках
- Не приходят события от CAN bus
- В логах ошибки доступа к скрытым API

**Причина:**
Android блокирует доступ к скрытым API (`@hide`). AIDL интерфейсы (`IAudioPolicyService`, `ICanBusService`) используют скрытые методы.

**Решение:**
```bash
# Установить Hidden API Policy
adb shell "settings put global hidden_api_policy 1"
adb shell "settings put global hidden_api_policy_pre_p 1"
adb shell "settings put global hidden_api_policy_pre_q 1"
adb shell "settings put global hidden_api_policy_pre_r 1"

# Проверить
adb shell settings get global hidden_api_policy
# Должно вернуть: 1

# Перезапустить сервис
adb shell am force-stop ru.voboost.voice
adb shell am start-foreground-service -n ru.voboost.voice/.VoboostVoiceService
```

**Проверка:**
```bash
adb logcat | grep "AudioPolicyServiceManager\|PhoneCallPoller"
# Ожидаемый вывод при звонке:
# I/PhoneCallPoller: Call state: ACTIVE (muting recognizer)
# I/PhoneCallPoller: Call state: IDLE (restoring recognizer)
```

> **Примечание:** Скрипты установки автоматически устанавливают Hidden API Policy. При ручной установке через `adb install` нужно выполнить команды выше.

---

## 🔍 Диагностика

```bash
# Проверить процесс
adb shell ps | grep voboost

# Проверить сервис
adb shell dumpsys activity services | grep -i voboost

# Проверить Hidden API Policy
adb shell settings get global hidden_api_policy

# Логи
adb logcat | grep -i voboost
adb logcat | grep -i "PhoneCallPoller\|AudioPolicy\|AEC"
```

---

## 📊 Чек-лист

- [ ] Приложение установлено
- [ ] Разрешения выданы (RECORD_AUDIO, SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE)
- [ ] Accessibility Service активен
- [ ] Модели установлены
- [ ] Конфигурация загружена
- [ ] Стандартные ассистенты отключены
- [ ] Сервис запущен
- [ ] **Hidden API Policy установлен** (`adb shell settings get global hidden_api_policy = 1`)

---

## 📚 Документация

- [Установка](./INSTALLATION.md)
- [Настройка](./CONFIGURATION.md)
