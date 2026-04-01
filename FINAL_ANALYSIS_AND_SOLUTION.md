# 🎯 ФИНАЛЬНЫЙ АНАЛИЗ: ЗАПУСК IVOKA ЧЕРЕЗ STARTSERVICE

**Дата:** 2026-03-24  
**Статус:** ✅ ПОЛНОЕ ПОНИМАНИЕ МЕХАНИЗМА

---

## 📊 ОТВЕТЫ НА ВОПРОСЫ

### ❓ Вопрос 1: Ivoka подписан на обработку `com.qinggan.iovka.START_IVOKA`?

**✅ ДА!** Но есть важный нюанс...

**Ivoka НЕ использует BroadcastReceiver для этого!**

Вместо этого **WindowService** (Service, не Receiver) объявлен с intent-filter:

```xml
<!-- Ivoka1 AndroidManifest.xml -->
<service 
    android:name="com.qinggan.ivoka.service.WindowService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.qinggan.iovka.START_IVOKA"/>
        <action android:name="com.qinggan.ivoka.REQUEST_SHOW_IVOKA"/>
        <action android:name="com.qinggan.ivoka.START_VOICE"/>
        <!-- ... другие action -->
    </intent-filter>
</service>
```

---

### ❓ Вопрос 2: Как именно Ivoka получает START_IVOKA?

**Через `startService()`, а НЕ через Broadcast!**

**Файл:** `QGSpeechService-release-signed/.../speech/adapter/speech/SpeechMgr.java`

```java
// QGSpeechService отправляет это:
Intent intent = new Intent(VoiceBroadcastAction.ACTION_START_IVOKA);
intent.setPackage("com.qinggan.ivoka");  // ← ЯВНО указываем пакет!
SpeechMgr.this.mContext.startService(intent);  // ← startService, НЕ sendBroadcast!

// Затем для Ivoka1:
Intent intent2 = new Intent(VoiceBroadcastAction.ACTION_START_IVOKA);
intent2.setPackage("com.qinggan.ivoka1");
SpeechMgr.this.mContext.startService(intent2);
```

**Это НЕ Broadcast!** Это **прямой вызов Service** по имени пакета!

---

### ❓ Вопрос 3: Какой приоритет у Ivoka?

**ПРИОРИТЕТ НЕ ПРИМЕНИМ!**

Потому что:
- ❌ **НЕ BroadcastReceiver** → нет priority
- ✅ **Service** → запускается напрямую через `startService()`

Intent-filter в Service используется только для **разрешения** запуска из других приложений, а не для приоритета.

---

### ❓ Вопрос 4: Получит ли Ivoka сообщение если мы перехватим?

**НЕТ, потому что это НЕ Broadcast!**

`startService()` с явным указанием пакета (`intent.setPackage()`) означает:
- ✅ Запускается ТОЛЬКО указанный пакет
- ❌ Другие приложения НЕ получают этот Intent
- ❌ Priority НЕ работает
- ❌ Перехватить НЕВОЗМОЖНО через BroadcastReceiver

---

### ❓ Вопрос 5: Можно ли сделать disable Ivoka и оставить только наш?

**✅ ДА! Это ЛУЧШИЙ подход!**

---

## 🏆 РЕКОМЕНДУЕМЫЙ ПОДХОД

### Вариант A: Отключить Ivoka (РЕКОМЕНДУЕТСЯ!)

**Шаг 1: Отключить Ivoka через PackageManager**

```kotlin
// VoboostVoiceService.kt или Application
fun disableIvoka(context: Context) {
    val ivokaPackages = listOf(
        "com.qinggan.ivoka",
        "com.qinggan.ivoka1"
    )
    
    for (packageName in ivokaPackages) {
        try {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                ComponentName(packageName, "${packageName}.service.WindowService"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            Log.i(TAG, "Disabled: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable $packageName", e)
        }
    }
}
```

**Шаг 2: Или через ADB (для тестирования)**

```bash
# Отключить Ivoka
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1

# Проверить статус
adb shell pm list packages -d | grep ivoka

# Включить обратно (если нужно)
adb shell pm enable com.qinggan.ivoka
adb shell pm enable com.qinggan.ivoka1
```

**Шаг 3: Запускать VoboostVoiceService вместо Ivoka**

Поскольку QGSpeechService отправляет `startService()` с явным пакетом, нам нужно **перехватить на уровне KeyManager**:

```kotlin
// VoiceActivationService.kt
class VoiceActivationService : Service() {
    
    companion object {
        const val KEYCODE_IVOKA = 130
    }
    
    private var keyManager: KeyManager? = null
    
    private val keyEventListener = object : KeyManager.OnKeyEventListener {
        override fun onKeyShortClick(keyCode: Int, keyEvent: KeyEvent?): Boolean {
            if (keyCode == KEYCODE_IVOKA) {
                Log.i(TAG, "IVA button pressed - launching Voboost!")
                activateVoiceAssistant()
                return true  // ← Обрабатываем сами, не передаем дальше
            }
            return false
        }
        
        override fun onKeyDown(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyHoldPressStart(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyHoldPressStop(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStart(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStop(keyCode: Int, keyEvent: KeyEvent?) = false
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Отключаем Ivoka
        disableIvoka(this)
        
        // Подключаемся к KeyManager
        keyManager = KeyManager.getInstance(this, object : KeyManager.OnInitListener {
            override fun onResult(success: Boolean) {
                if (success) {
                    keyManager?.registerKeyEventListener(keyEventListener)
                    Log.i(TAG, "KeyManager connected")
                }
            }
        })
    }
    
    private fun activateVoiceAssistant() {
        val intent = Intent(this, VoboostVoiceService::class.java)
        intent.action = "com.voboost.voiceassistant.ACTIVATE"
        startService(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        keyManager?.unregisterKeyEventListener(keyEventListener)
    }
}
```

---

### Вариант B: Модифицировать QGSpeechService (если есть доступ)

Если у вас есть доступ к исходному коду QGSpeechService, можно изменить:

```java
// Было:
Intent intent = new Intent(VoiceBroadcastAction.ACTION_START_IVOKA);
intent.setPackage("com.qinggan.ivoka");
context.startService(intent);

// Стало:
Intent intent = new Intent(VoiceBroadcastAction.ACTION_START_IVOKA);
intent.setPackage("com.voboost.voiceassistant");  // ← Ваш пакет
context.startService(intent);
```

---

### Вариант C: Перехват через System Server (ТРЕБУЕТ ROOT!)

Если есть root доступ, можно перехватить на уровне ActivityManager:

```bash
# Создать скрипт /system/etc/permissions/privapp-permissions-voboost.xml
<?xml version="1.0" encoding="utf-8"?>
<permissions>
    <privapp-permissions package="com.voboost.voiceassistant">
        <permission name="android.permission.INTERACT_ACROSS_USERS"/>
        <permission name="android.permission.PACKAGE_USAGE_STATS"/>
    </privapp-permissions>
</permissions>
```

Но это **излишне сложно** для вашей задачи.

---

## 📋 ИТОГОВАЯ ТАБЛИЦА

| Компонент | Тип | Как работает | Можно перехватить? |
|-----------|-----|--------------|-------------------|
| **KeyManager** | System Service | Перехват кнопок | ✅ ДА (лучший способ) |
| **QGHardKey** | Java Class | Обработка keyCode=1 | ✅ ДА (через KeyManager) |
| **SpeechMgr.startService()** | Java Code | `startService()` с пакетом | ❌ НЕТ (прямой вызов) |
| **WindowService** | Service | `onStartCommand()` | ❌ НЕТ (не Broadcast) |
| **BootCompleteReceiver** | BroadcastReceiver | `BOOT_COMPLETED` | ⚠️ Бесполезно (для загрузки) |

---

## ✅ ФИНАЛЬНАЯ РЕКОМЕНДАЦИЯ

### Используем **Вариант A** (Отключение Ivoka + KeyManager):

**Преимущества:**
1. ✅ **Не требует модификации системных APK**
2. ✅ **Работает на уровне системы** (KeyManager)
3. ✅ **Ivoka не будет мешать** (отключен)
4. ✅ **Чистое решение** (один сервис вместо двух)

**Недостатки:**
1. ⚠️ Нужно добавить KeyManager в проект
2. ⚠️ Требуется отключение Ivoka (один раз через ADB или в коде)

---

## 🎯 ПЛАН ДЕЙСТВИЙ

### Шаг 1: Добавить KeyManager в проект

Скопировать файлы из `BluetoothPhone-release-signed`:

```
com/qinggan/system/KeyManager.java
com/qinggan/system/IDirectionEventListener.java
com/qinggan/system/IKeyManagerCallback.java
com/qinggan/system/IKeyManagerService.java
```

### Шаг 2: Создать VoiceActivationService

```kotlin
// app/src/main/java/com/voboost/voiceassistant/VoiceActivationService.kt
```

### Шаг 3: Отключить Ivoka

**Через ADB (для тестирования):**
```bash
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
```

**Или в коде (при первом запуске):**
```kotlin
// В Application.onCreate()
disableIvoka(applicationContext)
```

### Шаг 4: Протестировать

```bash
# Собрать
gradlew.bat assembleDebug

# Установить
adb install app/build/outputs/apk/debug/app-debug.apk

# Дать разрешения
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
adb shell pm grant com.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW

# Запустить сервис
adb shell am startservice com.voboost.voiceassistant/.VoiceActivationService

# Нажать кнопку на руле
# Проверить логи
adb logcat | grep -i "voboost\|KeyManager"
```

---

## 🚀 ГОТОВЫЙ КОД

Нужно чтобы я создал готовые файлы для интеграции?

1. ✅ `VoiceActivationService.kt` - Сервис для перехвата кнопки
2. ✅ `KeyManager.java` + зависимости - Системный менеджер кнопок
3. ✅ `disable_ivoka.bat` - Batch файл для отключения Ivoka
4. ✅ Обновить `AndroidManifest.xml` - Добавить сервис

---

**Удачи! Теперь у нас есть ПОЛНОЕ понимание механизма! 🎉**
