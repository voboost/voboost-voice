# 🚀 ЗАПУСК FRIDA - ПРАВИЛЬНЫЙ СПОСОБ

**Дата:** 2026-03-24  
**Статус:** ✅ ПРОВЕРЕНО

---

## 🎯 ПРАВИЛЬНЫЙ ПРОЦЕСС ДЛЯ ПЕРЕХВАТА

**KeyManager** работает в отдельном процессе:
- **Процесс:** `com.qinggan.keymanager.service`
- **Сервис:** `KeyManagerService`
- **Метод:** `inputKeyEvent(int keyCode, int flags, String callingPackageName)`

---

## 📋 ШАГИ ЗАПУСКА

### Шаг 1: Проверить что KeyManagerService запущен

```bash
# Проверить процесс
adb shell ps | grep keymanager

# Должно быть:
# u0_a123  1234  567  ... com.qinggan.keymanager.service
```

Если не запущен - запустить:

```bash
# Запустить сервис
adb shell am startservice com.qinggan.keymanager.service/.KeyManagerService
```

### Шаг 2: Запустить Frida

**Вариант A: Перехват процесса (ТРЕБУЕТ ROOT!)**

```bash
# Запустить frida-server на устройстве
adb shell
su
/data/local/tmp/frida-server &
exit
exit

# На компьютере - перехват процесса KeyManagerService
frida -U -f com.qinggan.keymanager.service -l frida-voice-button.js --no-pause
```

**Вариант B: Подключиться к существующему процессу**

```bash
# Если процесс уже запущен
frida -U com.qinggan.keymanager.service -l frida-voice-button.js
```

**Вариант C: Без root (через Voboost)**

```bash
# Если нет root, перехватывать через Voboost процесс
frida -U ru.voboost.voiceassistant -l frida-voice-button.js
```

---

## 🔍 ОТЛАДКА

### Проверить что Frida подключился:

```
🚀 VoboostVoiceAssistant - KeyManagerService Intercept Loaded
   Target: com.qinggan.keymanager.service.KeyManagerService
   Method: inputKeyEvent(int, int, String)
   
✅ ✅ ✅ Frida hook loaded successfully ✅ ✅ ✅
```

### Проверить что кнопка перехватывается:

```
📢 KeyManagerService.inputKeyEvent():
   keyCode = 130 (0x82)
   flags = 0
   callingPackageName = com.qinggan.sttservice
   
🎯 IVA button pressed (KEYCODE_IVOKA=130)!
   isListening = false
🟢 ACTIVATE: Start Voboost recognition
📤 Sending broadcast: ru.voboost.voiceassistant.ACTIVATE
✅ Broadcast sent: ru.voboost.voiceassistant.ACTIVATE
```

---

## ⚠️ ВОЗМОЖНЫЕ ПРОБЛЕМЫ

### 1. Процесс не найден

**Ошибка:** `ProcessNotFoundError: unable to find process`

**Решение:**
```bash
# Запустить сервис вручную
adb shell am startservice com.qinggan.keymanager.service/.KeyManagerService

# Подождать 2-3 секунды
sleep 3

# Проверить что запущен
adb shell ps | grep keymanager

# Затем запустить Frida
frida -U -f com.qinggan.keymanager.service -l frida-voice-button.js --no-pause
```

### 2. Frida не может внедриться

**Ошибка:** `AccessDeniedException` или `Operation not permitted`

**Решение:**
```bash
# Проверить что frida-server запущен с root
adb shell
su
ps -A | grep frida

# Если нет - перезапустить
killall frida-server
/data/local/tmp/frida-server &

# Проверить версию Frida
frida --version
frida-server --version

# Должны совпадать!
```

### 3. Класс не найден

**Ошибка:** `ClassNotFoundException: com.qinggan.keymanager.service.KeyManagerService`

**Решение:**
```bash
# Проверить что сервис существует
adb shell pm list packages | grep keymanager

# Должно быть:
# package:com.qinggan.keymanager.service

# Если отключен - включить
adb shell pm enable com.qinggan.keymanager.service
```

### 4. Кнопка не перехватывается

**Проблема:** Логи показывают другие keyCode

**Решение:**
```bash
# Найти правильный keycode
# В логах Frida должно быть:
🔑 Key event: keyCode=XXX

# Изменить в скрипте:
if (keyCode === 130) {  // KEYCODE_IVOKA
→
if (keyCode === XXX) {  // Ваш keycode
```

---

## 📊 ПОЛНЫЙ ПУТЬ КНОПКИ

```
1. Кнопка на руле нажата
   ↓
2. CanBusService → CarSignalManager
   ↓
3. KeyManagerService (com.qinggan.keymanager.service)
   ↓
4. inputKeyEvent(130, 0, "com.qinggan.sttservice")
   ↓
5. Frida перехватывает
   ↓
6. sendBroadcast(ACTION_ACTIVATE)
   ↓
7. VoboostVoiceService.receiveBroadcast()
   ↓
8. activateVoiceAssistant()
   ↓
9. 🎵 Звук + Анимация
   ↓
10. Vosk распознавание
    ↓
11. Выполнение команды
```

---

## ✅ ЧЕК-ЛИСТ

- [ ] KeyManagerService запущен
- [ ] Frida-server запущен с root
- [ ] Скрипт frida-voice-button.js существует
- [ ] VoboostVoiceService установлен
- [ ] Разрешения выданы
- [ ] Ivoka и QGSpeechService отключены

---

## 🎯 КОМАНДЫ ДЛЯ БЫСТРОГО ЗАПУСКА

```bash
# 1. Запустить frida-server
adb shell su -c "/data/local/tmp/frida-server &"

# 2. Подождать
sleep 2

# 3. Запустить KeyManagerService
adb shell am startservice com.qinggan.keymanager.service/.KeyManagerService

# 4. Подождать
sleep 2

# 5. Запустить Frida
frida -U -f com.qinggan.keymanager.service -l frida-voice-button.js --no-pause
```

---

**Готово! 🚀**
