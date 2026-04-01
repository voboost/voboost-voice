# 🚀 FRIDA СКРИПТ ДЛЯ ПЕРЕХВАТА КНОПКИ НА РУЛЕ

**Дата:** 2026-03-24  
**Статус:** ✅ ГОТОВОЕ РЕШЕНИЕ С ROOT

---

## 🎯 АНАЛИЗ ПУТИ КНОПКИ

```
Кнопка на руле (KEYCODE_IVOKA = 130)
    ↓
KeyManager (системный сервис)
    ↓
KeyManager.inputKeyEvent(130)  ← Можно перехватить!
    ↓
SpeechMgr.mOnKeyEventListener.onKeyShortClick(130)  ← Можно перехватить!
    ↓
SpeechAdapterSDK.onSystemEvent(HARD_KEY_EVENT)  ← Можно перехватить!
    ↓
QGHardKey.receiveHardKey(keyCode=1)  ← Можно перехватить!
    ↓
WakeupVuiEngine.trackWakeup()  ← Можно перехватить!
    ↓
Intent: "com.qinggan.iovka.START_IVOKA"  ← Можно перехватить!
    ↓
Ivoka WindowService.startService()  ← Можно перехватить!
```

---

## 📊 ВАРИАНТЫ ПЕРЕХВАТА

### Вариант 1: Перехват `startService()` (ЛУЧШИЙ!)

**Перехватываем запуск Ivoka и подменяем на наш сервис**

```javascript
// frida-voice-assistant.js
// Перехват запуска Ivoka и подмена на VoboostVoiceAssistant

Java.perform(function() {
    var Context = Java.use("android.content.ContextWrapper");
    
    // Перехватываем startService()
    Context.startService.overload("android.content.Intent").implementation = function(intent) {
        var action = intent.getAction();
        var packageName = intent.getPackage();
        
        console.log("📢 startService called:");
        console.log("   Action: " + action);
        console.log("   Package: " + packageName);
        
        // Перехватываем запуск Ivoka
        if (action === "com.qinggan.iovka.START_IVOKA") {
            console.log("🎯 Intercepted Ivoka start!");
            console.log("   Original package: " + packageName);
            
            // Подменяем на наш пакет
            if (packageName === "com.qinggan.ivoka" || packageName === "com.qinggan.ivoka1") {
                console.log("🔄 Redirecting to VoboostVoiceAssistant!");
                
                // Создаем новый Intent для нашего сервиса
                var newIntent = Java.use("android.content.Intent").$new(intent);
                newIntent.setPackage("com.voboost.voiceassistant");
                
                // Запускаем наш сервис вместо Ivoka
                return this.startService(newIntent);
            }
        }
        
        // Все остальные запускаем как обычно
        return this.startService(intent);
    };
    
    console.log("✅ Frida hook installed: startService()");
});
```

**Преимущества:**
- ✅ Перехватываем в самом конце цепочки
- ✅ Не ломаем работу QGSpeechService
- ✅ Ivoka просто не запускается
- ✅ Наш сервис получает тот же Intent

---

### Вариант 2: Перехват `SpeechAdapterSDK.onSystemEvent()`

**Перехватываем событие о нажатии кнопки до запуска Ivoka**

```javascript
// frida-voice-assistant-v2.js
// Перехват HARD_KEY_EVENT и запуск нашего сервиса

Java.perform(function() {
    // Находим класс SpeechAdapterSDK
    var SpeechAdapterSDK = Java.use("com.qinggan.speech.adapter.SpeechAdapterSDK");
    
    // Перехватываем onSystemEvent
    SpeechAdapterSDK.onSystemEvent.implementation = function(category, eventId, intent) {
        console.log("📢 onSystemEvent called:");
        console.log("   Category: " + category);
        console.log("   EventId: " + eventId);
        
        // Проверяем, что это нажатие кнопки
        if (category === "hard_key_event" || category.toString().includes("HARD_KEY")) {
            console.log("🎯 Hard key event detected!");
            
            // Получаем keyCode из Intent
            var keyCode = -1;
            if (intent.hasExtra("key_code")) {
                keyCode = intent.getIntExtra("key_code", -1);
            }
            
            console.log("   KeyCode: " + keyCode);
            
            if (keyCode === 1) {  // 1 = KEYCODE_IVOKA
                console.log("🔴 IVA button pressed!");
                
                // Запускаем наш сервис ПЕРЕД вызовом оригинала
                launchVoboostService();
                
                // МОЖНО заблокировать вызов Ivoka (если нужно)
                // return;  // Не вызываем оригинал → Ivoka не запустится
                
                // ИЛИ вызываем оригинал (Ivoka тоже запустится)
                // return this.onSystemEvent(category, eventId, intent);
            }
        }
        
        return this.onSystemEvent(category, eventId, intent);
    };
    
    // Функция запуска нашего сервиса
    function launchVoboostService() {
        try {
            var ContextWrapper = Java.use("android.content.ContextWrapper");
            var contexts = Java.choose("android.content.ContextWrapper", {
                onMatch: function(instance) {
                    if (instance.getPackageName() === "com.qinggan.speech") {
                        console.log("📍 Found QGSpeechService context");
                        
                        var Intent = Java.use("android.content.Intent");
                        var intent = Intent.$new();
                        intent.setClassName(
                            "com.voboost.voiceassistant",
                            "com.voboost.voiceassistant.VoboostVoiceService"
                        );
                        intent.setAction("com.voboost.voiceassistant.ACTIVATE");
                        
                        instance.startService(intent);
                        console.log("✅ VoboostVoiceService launched!");
                    }
                },
                onComplete: function() {}
            });
        } catch (e) {
            console.error("❌ Failed to launch Voboost: " + e);
        }
    }
    
    console.log("✅ Frida hook installed: onSystemEvent()");
});
```

---

### Вариант 3: Перехват `QGHardKey.receiveHardKey()`

**Перехватываем обработку кнопки в QGHardKey**

```javascript
// frida-voice-assistant-v3.js
// Перехват QGHardKey.receiveHardKey()

Java.perform(function() {
    var QGHardKey = Java.use("com.qinggan.speech.vui.runtimeadapter.speech.QGHardKey");
    
    QGHardKey.receiveHardKey.implementation = function(keyEventInfo) {
        var keyCode = keyEventInfo.getKeyCode();
        
        console.log("📢 QGHardKey.receiveHardKey():");
        console.log("   KeyCode: " + keyCode);
        
        if (keyCode === 1) {  // 1 = IVA button
            console.log("🎯 IVA button detected!");
            
            // Запускаем наш сервис
            launchVoboostService();
            
            // МОЖНО заблокировать Ivoka:
            // return;  // Не вызываем оригинал
            
            // ИЛИ разрешаем Ivoka тоже запуститься:
            // return this.receiveHardKey(keyEventInfo);
        }
        
        return this.receiveHardKey(keyEventInfo);
    };
    
    function launchVoboostService() {
        // ... та же функция что в варианте 2
    }
    
    console.log("✅ Frida hook installed: QGHardKey.receiveHardKey()");
});
```

---

### Вариант 4: Перехват `KeyManager.inputKeyEvent()` (САМЫЙ РАННИЙ!)

**Перехватываем на самом раннем этапе - в KeyManager**

```javascript
// frida-voice-assistant-v4.js
// Перехват KeyManager.inputKeyEvent()

Java.perform(function() {
    var KeyManager = Java.use("com.qinggan.system.KeyManager");
    
    KeyManager.inputKeyEvent.overload("int").implementation = function(keyCode) {
        console.log("📢 KeyManager.inputKeyEvent():");
        console.log("   KeyCode: " + keyCode);
        
        if (keyCode === 130) {  // KEYCODE_IVOKA
            console.log("🎯 IVA button pressed (KEYCODE_IVOKA=130)!");
            
            // Запускаем наш сервис
            launchVoboostService();
            
            // МОЖНО заблокировать:
            // return true;  // Блокируем дальнейшую обработку
            
            // ИЛИ разрешаем:
            // return this.inputKeyEvent(keyCode);
        }
        
        return this.inputKeyEvent(keyCode);
    };
    
    function launchVoboostService() {
        // ... та же функция
    }
    
    console.log("✅ Frida hook installed: KeyManager.inputKeyEvent()");
});
```

---

## 🏆 РЕКОМЕНДУЕМЫЙ ВАРИАНТ

**Вариант 1 (перехват `startService()`) - ЛУЧШИЙ!**

**Причины:**
1. ✅ **Минимальное вмешательство** - не ломаем внутреннюю логику
2. ✅ **Работает надежно** - перехватываем стандартный Android API
3. ✅ **Ivoka не запускается** - просто подменяем пакет
4. ✅ **Наш сервис получает Intent** - тот же самый action
5. ✅ **Просто тестировать** - включил/выключил скрипт

---

## 📝 ГОТОВЫЙ СКРИПТ (ВАРИАНТ 1)

```javascript
// frida-voice-assistant.js
// Запуск: frida -U -f com.qinggan.speech -l frida-voice-assistant.js

Java.perform(function() {
    console.log("🚀 VoboostVoiceAssistant - Frida Hook Loaded");
    console.log("   Target: Intercept Ivoka start and redirect to Voboost");
    
    var ContextWrapper = Java.use("android.content.ContextWrapper");
    var Intent = Java.use("android.content.Intent");
    
    // Перехватываем startService()
    ContextWrapper.startService.overload("android.content.Intent").implementation = function(intent) {
        try {
            var action = intent.getAction();
            var packageName = intent.getPackage();
            
            // Логируем только важные события
            if (action && action.includes("ivoka") || action && action.includes("voboost")) {
                console.log("📢 startService: " + action + " → " + packageName);
            }
            
            // Перехватываем запуск Ivoka
            if (action === "com.qinggan.iovka.START_IVOKA") {
                console.log("🎯 Intercepted: com.qinggan.iovka.START_IVOKA");
                console.log("   Original package: " + packageName);
                
                if (packageName === "com.qinggan.ivoka" || packageName === "com.qinggan.ivoka1") {
                    console.log("🔄 Redirecting to com.voboost.voiceassistant");
                    
                    // Создаем новый Intent для нашего сервиса
                    var newIntent = Intent.$new(intent);
                    newIntent.setPackage("com.voboost.voiceassistant");
                    
                    // Запускаем наш сервис
                    var result = this.startService(newIntent);
                    console.log("✅ VoboostVoiceService started: " + result);
                    return result;
                }
            }
            
            // Все остальные запускаем как обычно
            return this.startService(intent);
            
        } catch (e) {
            console.error("❌ Error in startService hook: " + e);
            return this.startService(intent);
        }
    };
    
    console.log("✅ Hook installed successfully");
    console.log("   Waiting for button press...");
});
```

---

## 🔧 КАК ЗАПУСТИТЬ

### Шаг 1: Установить Frida

```bash
# На компьютер
pip install frida-tools

# На Android device (через Magisk)
# Или скачать с https://github.com/frida/frida/releases
frida-server-16.x.x-android-arm64.xz
```

### Шаг 2: Запустить frida-server на устройстве

```bash
# Подключиться по ADB
adb shell

# Запустить frida-server (нужен root)
su
/data/local/tmp/frida-server &

# Проверить
ps -A | grep frida
```

### Шаг 3: Запустить скрипт

```bash
# Вариант A: Перехватить процесс QGSpeechService
frida -U -f com.qinggan.speech -l frida-voice-assistant.js --no-pause

# Вариант B: Подключиться к уже запущенному
frida -U com.qinggan.speech -l frida-voice-assistant.js

# Вариант C: Перехватить системный процесс (требует root)
frida -U -f system_server -l frida-voice-assistant.js --no-pause
```

### Шаг 4: Нажать кнопку на руле

Смотрим логи:
```
🚀 VoboostVoiceAssistant - Frida Hook Loaded
📢 startService: com.qinggan.iovka.START_IVOKA → com.qinggan.ivoka
🎯 Intercepted: com.qinggan.iovka.START_IVOKA
🔄 Redirecting to com.voboost.voiceassistant
✅ VoboostVoiceService started
```

---

## 🎯 МОДИФИКАЦИЯ QGSpeechService (ОПЦИОНАЛЬНО)

Если хотите **навсегда** изменить поведение без Frida:

### Шаг 1: Декомпилировать QGSpeechService

```bash
apktool d QGSpeechService-release-signed.apk
```

### Шаг 2: Изменить SpeechMgr.smali

**Файл:** `QGSpeechService-release-signed/smali/com/qinggan/speech/adapter/speech/SpeechMgr.smali`

Найти метод `onKeyShortClick` и изменить:

```smali
# Было:
const-string v0, "com.qinggan.ivoka"
invoke-virtual {v1, v0}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

# Стало:
const-string v0, "com.voboost.voiceassistant"
invoke-virtual {v1, v0}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;
```

### Шаг 3: Скомпилировать обратно

```bash
apktool b QGSpeechService-release-signed -o QGSpeechService-modified.apk
```

### Шаг 4: Подписать и установить

```bash
# Подписать
apksigner sign --key testkey.pk8 --cert testkey.x509.pem QGSpeechService-modified.apk

# Установить (нужен system partition rw)
adb remount
adb push QGSpeechService-modified.apk /system/priv-app/QGSpeechService/
adb reboot
```

---

## ✅ СРАВНЕНИЕ ПОДХОДОВ

| Подход | Сложность | Надежность | Обратимость | Рекомендация |
|--------|-----------|------------|-------------|--------------|
| **Frida (startService)** | ⭐ Просто | ⭐⭐⭐ Отлично | ✅ Полная | ✅ **РЕКОМЕНДУЕТСЯ** |
| **Frida (onSystemEvent)** | ⭐⭐ Средне | ⭐⭐ Хорошо | ✅ Полная | ✅ Хорошо |
| **Frida (QGHardKey)** | ⭐⭐ Средне | ⭐⭐ Хорошо | ✅ Полная | ✅ Хорошо |
| **Frida (KeyManager)** | ⭐⭐⭐ Сложно | ⭐⭐ Хорошо | ✅ Полная | ⚠️ Сложно |
| **Модификация APK** | ⭐⭐⭐ Сложно | ⭐⭐⭐ Отлично | ❌ Навсегда | ⚠️ Только если точно уверены |
| **KeyManager в приложении** | ⭐⭐ Средне | ⭐⭐ Хорошо | ✅ Полная | ⚠️ Нужно добавлять зависимости |

---

## 🎯 ФИНАЛЬНАЯ РЕКОМЕНДАЦИЯ

**Используйте Frida скрипт (Вариант 1)** - перехват `startService()`:

```bash
# 1. Запустить frida-server на устройстве
adb shell
su
/data/local/tmp/frida-server &

# 2. Запустить скрипт
frida -U -f com.qinggan.speech -l frida-voice-assistant.js --no-pause

# 3. Нажать кнопку на руле
# 4. Ваш сервис запустится вместо Ivoka!
```

**Преимущества:**
- ✅ Не нужно модифицировать системные APK
- ✅ Работает сразу
- ✅ Можно отключить в любой момент
- ✅ Минимальное вмешательство в систему

---

**Удачи! 🚀**
