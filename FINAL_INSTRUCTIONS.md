# 🚀 ИТОГОВАЯ ИНСТРУКЦИЯ: VOBOOST VOICE ASSISTANT

**Версия:** 2.0 (ПРЯМОЙ ПЕРЕХВАТ KEYMANAGER)  
**Дата:** 2026-03-24  
**Статус:** ✅ ГОТОВО

---

## 📋 ЧТО ИЗМЕНИЛОСЬ

### ✅ ПРЯМОЙ ПЕРЕХВАТ KEYMANAGER:

**Было:**
- Перехват через QGSpeechService
- QGSpeechService должен работать

**Стало:**
- **Прямой перехват KeyManager.inputKeyEvent()**
- **QGSpeechService ОТКЛЮЧЕН**
- **Voboost работает ВСЕГДА** (keyword spotting + кнопка)

---

## 🔧 УСТАНОВКА

### Шаг 1: Отключить стандартные сервисы

```bash
# Отключить Ivoka
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1

# Отключить QGSpeechService
adb shell pm disable com.qinggan.sttservice

# Проверить
adb shell pm list packages -d | grep -E "ivoka|sttservice"
```

### Шаг 2: Собрать и установить Voboost

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant

# Собрать
gradlew.bat assembleDebug

# Установить
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Дать разрешения
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
adb shell pm grant com.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant com.voboost.voiceassistant android.permission.FOREGROUND_SERVICE
```

### Шаг 3: Запустить Frida (ТРЕБУЕТ ROOT!)

**На устройстве:**
```bash
# Запустить frida-server (нужен root)
adb shell
su
/data/local/tmp/frida-server &
exit
exit
```

**На компьютере:**
```bash
# Перехват системного KeyManager
frida -U -f system_server -l frida-voice-button.js --no-pause
```

### Шаг 4: Запустить Voboost сервис

```bash
# Запустить сервис (будет работать ВСЕГДА)
adb shell am startservice com.voboost.voiceassistant/.VoboostVoiceService

# Проверить логи
adb logcat | grep -i "voboost\|SoundEffect"
```

---

## 🎯 КАК ЭТО РАБОТАЕТ

### Архитектура:

```
┌─────────────────────────────────────────────────────────┐
│  KeyManager (системный сервис)                          │
│  - inputKeyEvent(130) ← KEYCODE_IVOKA                   │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  Frida Script (frida-voice-button.js)                   │
│  - Перехватывает inputKeyEvent(130)                     │
│  - Отправляет broadcast в Voboost                       │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│  VoboostVoiceService (ВСЕГДА РАБОТАЕТ)                  │
│  - Keyword spotting (фоновый режим)                     │
│  - Получает broadcast: ACTION_ACTIVATE / ACTION_CANCEL  │
│  - Звуки + Анимация                                     │
│  - Vosk распознавание                                   │
│  - NLU понимание                                        │
│  - Выполнение команд                                    │
└─────────────────────────────────────────────────────────┘
```

### Путь кнопки:

```
Кнопка на руле
    ↓
KeyManager.inputKeyEvent(130)
    ↓
Frida: intercept!
    ↓
sendBroadcast(ACTION_ACTIVATE)
    ↓
VoboostVoiceService.receiveBroadcast()
    ↓
activateVoiceAssistant()
    ↓
🎵 Звук начала + Анимация
    ↓
Vosk: listenForCommand()
    ↓
NLU: parseCommand()
    ↓
CommandExecutor: execute()
    ↓
🎵 Звук окончания
```

### Отмена (повторное нажатие):

```
Кнопка на руле (во время распознавания)
    ↓
KeyManager.inputKeyEvent(130)
    ↓
Frida: isListening = true → sendBroadcast(ACTION_CANCEL)
    ↓
VoboostVoiceService.receiveBroadcast()
    ↓
cancelRecognition()
    ↓
🎵 Звук отмены
    ↓
Остановка распознавания
```

---

## 📊 ПОВЕДЕНИЕ

| Событие | Звук | Анимация | Действие |
|---------|------|----------|----------|
| **Нажатие кнопки** | ✅ Двойной сигнал | ✅ Показана | Запуск распознавания |
| **Распознавание** | - | ✅ Мигает | Слушаем команду |
| **Команда распознана** | ✅ Одиночный сигнал | ✅ Скрыта | Выполнение |
| **Повторное нажатие** | ✅ Низкий сигнал | ✅ Скрыта | Отмена |

---

## 🔍 ОТЛАДКА

### Логи Voboost:

```bash
adb logcat | grep -i "VoboostVoiceService\|SoundEffectManager"
```

**Ожидаемые логи:**

```
# Запуск сервиса
I/VoboostVoiceService: onCreate
I/VoboostVoiceService: CancelReceiver registered
I/VoboostVoiceService: Service started, keyword spotting active

# Кнопка нажата
I/VoboostVoiceService: Activate request received from button
I/SoundEffectManager: Playing start recognition sound
I/SoundEffectManager: Tone played: type=1

# Распознавание
I/VoboostVoiceService: Recognized: открой лючок зарядки

# Выполнение
I/CommandExecutor: Sending intent: pateo.dls.ivoka.vehicle.CONTROL

# Окончание
I/SoundEffectManager: Playing end recognition sound
I/SoundEffectManager: Tone played: type=2

# Отмена (повторное нажатие)
I/VoboostVoiceService: Cancel request received from button
I/VoboostVoiceService: Cancelling recognition...
I/SoundEffectManager: Playing cancel sound
I/SoundEffectManager: Tone played: type=3
```

### Логи Frida:

```
🚀 VoboostVoiceAssistant - KeyManager Intercept Loaded
   Target: Intercept KEYCODE_IVOKA (130) directly from KeyManager
   QGSpeechService: DISABLED
   Ivoka: DISABLED

📌 Hooking KeyManager.inputKeyEvent(int)...
✅ Hook installed: KeyManager.inputKeyEvent(int)

# Кнопка нажата
📢 KeyManager.inputKeyEvent(): keyCode = 130
🎯 IVA button pressed (KEYCODE_IVOKA=130)!
   isListening = false
🟢 ACTIVATE: Start Voboost recognition
📤 Sending broadcast: com.voboost.voiceassistant.ACTIVATE
✅ Broadcast sent: com.voboost.voiceassistant.ACTIVATE
```

---

## ⚠️ ВОЗМОЖНЫЕ ПРОБЛЕМЫ

### 1. Frida не подключается к system_server

**Ошибка:** `Failed to spawn system_server: Operation not permitted`

**Решение:**
```bash
# Запустить Frida без -f (подключиться к существующему)
frida -U system_server -l frida-voice-button.js

# ИЛИ использовать Voboost процесс
frida -U com.voboost.voiceassistant -l frida-voice-button.js
```

### 2. Кнопка не работает

**Ошибка:** Кнопка на руле не активирует Voboost

**Решение:**
```bash
# Проверить что Frida перехватывает
# В логах Frida должно быть:
📢 KeyManager.inputKeyEvent(): keyCode = 130

# Если нет - проверить KEYCODE
# Возможно в вашей системе другой keycode
# Найти через:
adb logcat | grep -i "keycode\|KeyEvent"

# Найти правильный keycode и изменить в скрипте:
# if (keyCode === 130) → if (keyCode === XXX)
```

### 3. Voboost не получает broadcast

**Ошибка:** Кнопка работает, но Voboost не активируется

**Решение:**
```bash
# Проверить что сервис запущен
adb shell ps | grep voboost

# Проверить receiver в логах
adb logcat | grep "CancelReceiver"

# Должно быть:
I/VoboostVoiceService: CancelReceiver registered

# Перезапустить сервис
adb shell am stopservice com.voboost.voiceassistant
adb shell am startservice com.voboost.voiceassistant/.VoboostVoiceService
```

---

## 📝 МОДИФИКАЦИЯ

### Изменить keycode кнопки:

**Файл:** `frida-voice-button.js`

```javascript
// Было:
if (keyCode === 130) {  // KEYCODE_IVOKA

// Стало (другой keycode):
if (keyCode === XXX) {  // Ваш keycode
```

### Изменить звуки:

**Файл:** `SoundEffectManager.kt`

```kotlin
// Типы звуков
SOUND_START = 1      // Начало распознавания
SOUND_END = 2        // Окончание
SOUND_CANCEL = 3     // Отмена

// Изменить тон
ToneGenerator.TONE_PROP_BEEP      // Стандартный
ToneGenerator.TONE_PROP_ACK       // Подтверждение
ToneGenerator.TONE_PROP_NACK      // Отмена
```

---

## ✅ ЧЕК-ЛИСТ ГОТОВНОСТИ

- [x] SoundEffectManager.kt
- [x] VoboostVoiceService.kt (обновлен)
- [x] frida-voice-button.js (ПРЯМОЙ ПЕРЕХВАТ)
- [x] Документация
- [ ] Собрать проект
- [ ] Установить на устройство
- [ ] Отключить Ivoka + QGSpeechService
- [ ] Запустить Frida (root required)
- [ ] Запустить Voboost сервис
- [ ] Протестировать кнопку
- [ ] Протестировать отмену

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

```bash
# 1. Собрать
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleDebug

# 2. Установить
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. Разрешения
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
adb shell pm grant com.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW

# 4. Отключить штатные
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
adb shell pm disable com.qinggan.sttservice

# 5. Запустить Frida (требует root!)
adb shell su -c "/data/local/tmp/frida-server &"
frida -U -f system_server -l frida-voice-button.js --no-pause

# 6. Запустить Voboost
adb shell am startservice com.voboost.voiceassistant/.VoboostVoiceService

# 7. Тестировать
# Нажать кнопку на руле
```

---

**Готово! 🚀**
