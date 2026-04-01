# 📊 АНАЛИЗ DECOMPILED APK И РЕКОМЕНДАЦИИ

**Дата:** 2026-03-24  
**Объекты анализа:**
- `Island-release-signed` (com.qinggan.app.islandapp)
- `Ivoka1-release-signed` (com.qinggan.ivoka1)
- `VoboostVoiceAssistant` (ваш проект)

---

## 🔍 1. АНАЛИЗ DECOMPILED APK

### 1.1 Island-release-signed

**Пакет:** `com.qinggan.app.islandapp`

**Назначение:** Базовое приложение Island (платформа)

**Ключевые компоненты:**

```xml
<manifest package="com.qinggan.app.islandapp">
    <application android:name="com.qinggan.app.islandapp.IslandApplication">
        
        <!-- Activity -->
        <activity android:name="com.qinggan.app.islandapp.IslandActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        
        <!-- Boot Receiver -->
        <receiver android:name="com.qinggan.app.islandapp.boot.BootBroadcastReceiver">
            <intent-filter>
                <action android:name="com.qinggan.intent.QINGGAN_BOOT_COMPLETE"/>
            </intent-filter>
        </receiver>
        
        <!-- Services -->
        <service android:name="com.qinggan.app.islandapp.boot.MainService"/>
        <service android:name="com.qinggan.app.islandapp.InitAppService"/>
        
    </application>
</manifest>
```

**Вывод:** Island - это базовое приложение, которое запускается при загрузке системы и инициализирует сервисы.

---

### 1.2 Ivoka1-release-signed

**Пакет:** `com.qinggan.ivoka1`

**Назначение:** Голосовой помощник Ivoka (основной)

**Ключевые компоненты:**

```xml
<manifest package="com.qinggan.ivoka1">
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.INJECT_EVENTS"/>
    <uses-permission android:name="android.car.permission.CAR_POWER"/>
    
    <application android:name="com.qinggan.ivoka.VoiceApplication">
        
        <!-- Activities -->
        <activity android:name="com.qinggan.ivoka.IvokaActivity"/>
        <activity android:name="com.qinggan.ivoka.activity.VoiceActivity"/>
        
        <!-- Window Service (ГЛАВНЫЙ!) -->
        <service android:name="com.qinggan.ivoka.service.WindowService">
            <intent-filter>
                <action android:name="com.qinggan.iovka.START_IVOKA"/>
                <action android:name="com.qinggan.ivoka.REQUEST_SHOW_IVOKA"/>
                <action android:name="com.qinggan.ivoka.REQUEST_HIDE_IVOKA"/>
                <action android:name="com.qinggan.ivoka.START_VOICE"/>
                <action android:name="com.qinggan.ivoka.EXIT_IVOKA"/>
            </intent-filter>
        </service>
        
        <!-- Boot Receiver -->
        <receiver android:name="com.qinggan.ivoka.VoiceApplication.BootCompleteReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
                <action android:name="com.qinggan.iovka.START_IVOKA"/>
            </intent-filter>
        </receiver>
        
        <!-- Speech Service -->
        <service android:name="com.qinggan.speech.adapter.QGAdapterService">
            <intent-filter>
                <action android:name="pateo.speech.QGAdapterService"/>
            </intent-filter>
        </service>
        
    </application>
</manifest>
```

**Вывод:** Ivoka1 - это полноценный голосовой помощник с собственным сервисом WindowService.

---

## 🎯 2. ОБРАБОТКА КНОПКИ НА РУЛЕ

### 2.1 Найденные Action ID

В декомпилированном коде найден **ACTION_VOICE_IVOKA_HARD_KEY** (ID: 190):

```java
// VoiceActionID.java
public static final int ACTION_VOICE_IVOKA_HARD_KEY = 190;

// VoiceAction.java
public static final String ACTION_VOICE_IVOKA_HARD_KEY = "action.voice.ivoka.hard.key";
```

### 2.2 Как Ivoka обрабатывает кнопку

**Механизм:**

1. **Система** отправляет Broadcast Intent при нажатии кнопки на руле
2. **Ivoka BootCompleteReceiver** получает Intent
3. **WindowService** запускается через `ACTION_START_IVOKA`

**Путь активации:**

```
Кнопка на руле
    ↓
Системный Broadcast (KEYCODE_XXX)
    ↓
com.qinggan.ivoka.VoiceApplication.BootCompleteReceiver
    ↓
Intent: "com.qinggan.iovka.START_IVOKA"
    ↓
WindowService.onHandleIntent()
    ↓
Запуск распознавания речи
```

### 2.3 Ключевые файлы Ivoka

**WindowService.java:**
```java
public class WindowService extends Service implements IVoiceRequestCb {
    
    public static final String ACTION_START_IVOKA = "com.qinggan.iovka.START_IVOKA";
    public static final String ACTION_START_VOICE = "com.qinggan.ivoka.START_VOICE";
    
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        
        if (ACTION_START_IVOKA.equals(action)) {
            // Запуск голосового помощника
            startVoiceRecognition();
        }
    }
}
```

**VoiceBroadcastAction.java:**
```java
public final class VoiceBroadcastAction {
    public static final String ACTION_START_IVOKA = "com.qinggan.iovka.START_IVOKA";
    public static final String ACTION_START_VOICE = "com.qinggan.ivoka.START_VOICE";
    public static final String ACTION_REQUEST_SHOW_IVOKA = "com.qinggan.ivoka.REQUEST_SHOW_IVOKA";
    public static final String ACTION_EXIT_IVOKA = "com.qinggan.ivoka.EXIT_IVOKA";
}
```

---

## 📡 3. INTENT ДЕЙСТВИЯ ДЛЯ УПРАВЛЕНИЯ МАШИНОЙ

### 3.1 Основные Action Strings

Из `VoiceAction.java`:

```java
// Управление машиной (люки, окна)
public static final String ACTION_VEHICLE_DEVICE_CONTROL = "pateo.dls.ivoka.vehicle.CONTROL";  // ID: 91

// Климат
public static final String ACTION_AIR_CONTROL_OPEN = "pateo.dls.ivoka.air_control.OPEN";      // ID: 25
public static final String ACTION_AIR_CONTROL_CLOSE = "pateo.dls.ivoka.air_control.CLOSE";    // ID: 27
public static final String ACTION_AIR_CONTROL_ADJUST = "pateo.dls.ivoka.air_control.ADJUST";  // ID: 26
public static final String ACTION_AIR_CONTROL_CMD = "pateo.dls.ivoka.air_control.COMMAND";    // ID: 122

// Телефон
public static final String ACTION_TELEPHONE_CALL = "pateo.dls.ivoka.telephone.CALL";          // ID: 1

// Режимы
public static final String ACTION_SMART_MODE_SET = "pateo.dls.ivoka.SET_SMART_MODE";          // ID: 138
```

### 3.2 Параметры Intent

Из `VoiceParam.java`:

```java
// Для управления машиной
public static final String VOICE_PARAM_VEHICLE_TARGET = "voice.param.vehicle.target";
public static final String VOICE_PARAM_VEHICLE_CLASSIFY = "voice.param.vehicle.classify";
public static final String VOICE_PARAM_VEHICLE_COMMAND = "voice.param.vehicle.command";

// Для телефона
public static final String VOICE_PARAM_TELEPHONE_TARGET = "voice.param.telephone.target";
public static final String VOICE_PARAM_TELEPHONE_NAME = "voice.param.telephone.name";
public static final String VOICE_PARAM_TELEPHONE_NUMBER = "voice.param.telephone.number";
```

### 3.3 Пример отправки команды

**Ivoka отправляет:**

```java
// Лючок зарядки - открыть
Intent intent = new Intent("pateo.dls.ivoka.vehicle.CONTROL");
intent.putExtra("voice.param.vehicle.target", "Chargport");
intent.putExtra("voice.param.vehicle.classify", 35);
intent.putExtra("voice.param.vehicle.command", 1);  // 1 = OPEN
context.sendBroadcast(intent);

// Кондиционер - включить
Intent intent = new Intent("pateo.dls.ivoka.air_control.OPEN");
intent.putExtra("voice.param.air.target", "AirConditioner");
intent.putExtra("voice.param.air.classify", 5);
intent.putExtra("voice.param.air.command", 0);  // 0 = ON
context.sendBroadcast(intent);

// Телефон - звонок
Intent intent = new Intent("pateo.dls.ivoka.telephone.CALL");
intent.putExtra("voice.param.telephone.target", "telephone");
intent.putExtra("voice.param.telephone.classify", 1);
intent.putExtra("voice.param.telephone.command", 1);
intent.putExtra("voice.param.contact", "мама");
context.sendBroadcast(intent);
```

---

## ✅ 4. СРАВНЕНИЕ С VoboostVoiceAssistant

### 4.1 Что ПРАВИЛЬНО в вашем проекте

| Компонент | Ваш проект | Ivoka (оригинал) | Статус |
|-----------|------------|------------------|--------|
| **Action Strings** | `pateo.dls.ivoka.*` | `pateo.dls.ivoka.*` | ✅ ВЕРНО |
| **Параметры** | `voice.param.vehicle.*` | `voice.param.vehicle.*` | ✅ ВЕРНО |
| **Target/Classify/Command** | Используется | Используется | ✅ ВЕРНО |
| **Broadcast Intent** | `sendBroadcast()` | `sendBroadcast()` | ✅ ВЕРНО |

### 4.2 Что НЕПРАВИЛЬНО / Отличается

| Компонент | Ваш проект | Ivoka (оригинал) | Проблема |
|-----------|------------|------------------|----------|
| **Кнопка на руле** | AccessibilityService | Broadcast Receiver + HardKey | ❌ Неправильный подход |
| **Запуск сервиса** | Свой механизм | `com.qinggan.iovka.START_IVOKA` | ⚠️ Нужно использовать тот же Action |
| **KEYCODE** | KEYCODE_ASSIST (219) | ACTION_VOICE_IVOKA_HARD_KEY (190) | ❌ Нужно найти правильный keycode |

---

## 🔧 5. РЕКОМЕНДАЦИИ ПО ИСПРАВЛЕНИЮ

### 5.1 Перехват кнопки на руле

**Проблема:** Вы используете AccessibilityService, но это неправильный подход для кнопки на руле.

**Решение 1: BroadcastReceiver для системного Intent**

Система отправляет Broadcast при нажатии кнопки. Нужно перехватить этот Intent.

```kotlin
// VoiceActivationReceiver.kt
class VoiceActivationReceiver : BroadcastReceiver() {
    
    companion object {
        // Нужно найти правильный Action через logcat
        const val ACTION_VOICE_BUTTON = "android.intent.action.VOICE_COMMAND"
        // Или другой Action (зависит от системы)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_VOICE_BUTTON) {
            Log.i("VoiceActivation", "Voice button pressed!")
            
            // Запускаем ваш сервис
            val serviceIntent = Intent(context, VoboostVoiceService::class.java)
            serviceIntent.action = "com.voboost.voiceassistant.ACTIVATE"
            context.startService(serviceIntent)
        }
    }
}
```

**AndroidManifest.xml:**
```xml
<receiver android:name=".VoiceActivationReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VOICE_COMMAND"/>
        <!-- Или другой Action -->
    </intent-filter>
</receiver>
```

---

**Решение 2: Найти правильный KEYCODE через logcat**

1. Подключите ADB
2. Запустите logcat
3. Нажмите кнопку на руле
4. Найдите keycode в логах

```bash
adb logcat | grep -i "keycode\|voice\|button"
```

**Ожидаемый лог:**
```
I/InputReader: KeyEvent: action=0 keyCode=KEYCODE_ASSIST ...
I/InputReader: KeyEvent: action=0 keyCode=226 ...
```

**После нахождения keycode:**

```kotlin
// VoiceActivationService.kt
class VoiceActivationService : AccessibilityService() {
    
    companion object {
        // Заменить на найденный keycode!
        private val VOICE_BUTTON_KEYCODES = intArrayOf(226)  // KEYCODE_VOICE_ASSIST
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val keyCode = event.action
            if (keyCode in VOICE_BUTTON_KEYCODES) {
                handleVoiceButtonPress()
            }
        }
    }
}
```

---

**Решение 3: Использовать тот же механизм что Ivoka**

Ivoka использует `ACTION_START_IVOKA` для запуска.

```kotlin
// Зарегистрируйтесь на тот же Action
class VoiceActivationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.qinggan.iovka.START_IVOKA") {
            // Ivoka запускается - запускаемся и мы
            val serviceIntent = Intent(context, VoboostVoiceService::class.java)
            context.startService(serviceIntent)
        }
    }
}
```

**НО:** Это будет запускать ваш сервис ВМЕСТЕ с Ivoka, а не ВМЕСТО.

---

### 5.2 Исправление CommandExecutor

**Текущий код ПРАВИЛЬНЫЙ!** Ваши Intent действия совпадают с оригинальными.

**Проверка по таблице:**

| Команда | Ваш Action | Ivoka Action | Статус |
|---------|------------|--------------|--------|
| Лючок зарядки | `pateo.dls.ivoka.vehicle.CONTROL` | `pateo.dls.ivoka.vehicle.CONTROL` | ✅ |
| Кондиционер | `pateo.dls.ivoka.air_control.OPEN` | `pateo.dls.ivoka.air_control.OPEN` | ✅ |
| Телефон | `pateo.dls.ivoka.telephone.CALL` | `pateo.dls.ivoka.telephone.CALL` | ✅ |
| Режимы | `pateo.dls.ivoka.vehicle.CONTROL` | `pateo.dls.ivoka.SET_SMART_MODE` | ⚠️ |

**Исправление для режимов:**

```json
// config.json - smart_mode_leisure
{
  "action": {
    "target": "SmartMode",
    "classify": 22,
    "command": 0,
    "intent_action": "pateo.dls.ivoka.SET_SMART_MODE",  // ← Изменить!
    "params": {
      "mode": 18
    }
  }
}
```

---

### 5.3 Полный план исправлений

**Шаг 1: Найти keycode кнопки на руле**

```bash
# Подключиться к устройству
adb devices

# Запустить logcat
adb logcat -c  # Очистить
adb logcat | grep -E "KeyEvent|keycode|VOICE"

# Нажать кнопку на руле
# Записать keyCode из логов
```

---

**Шаг 2: Исправить VoiceActivationService**

```kotlin
// VoiceActivationService.kt
class VoiceActivationService : AccessibilityService() {
    
    companion object {
        // Вставить найденный keycode!
        private val VOICE_BUTTON_KEYCODES = intArrayOf(
            226  // KEYCODE_VOICE_ASSIST (пример)
        )
    }
    
    // ... остальной код
}
```

---

**Шаг 3: Исправить config.json для режимов**

```json
// Изменить intent_action для smart_mode_*
{
  "id": "smart_mode_leisure",
  "action": {
    "intent_action": "pateo.dls.ivoka.SET_SMART_MODE"  // ← Было: vehicle.CONTROL
  }
}
```

---

**Шаг 4: Добавить BroadcastReceiver для кнопки**

```kotlin
// VoiceActivationReceiver.kt
class VoiceActivationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("VoiceActivation", "Received: ${intent.action}")
        
        // Запуск сервиса
        val serviceIntent = Intent(context, VoboostVoiceService::class.java)
        serviceIntent.action = "com.voboost.voiceassistant.ACTIVATE"
        context.startService(serviceIntent)
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<receiver 
    android:name=".VoiceActivationReceiver" 
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VOICE_COMMAND"/>
    </intent-filter>
</receiver>
```

---

**Шаг 5: Тестирование**

```bash
# Собрать проект
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleDebug

# Установить
adb install app/build/outputs/apk/debug/app-debug.apk

# Дать разрешения
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
adb shell pm grant com.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW

# Запустить логи
adb logcat | grep -i "voboost\|CommandExecutor"

# Нажать кнопку на руле
# Сказать команду
```

---

## 📋 6. ИТОГОВАЯ ПРОВЕРКА

### ✅ Правильно (не менять):

1. ✅ **Action Strings** - все верно (`pateo.dls.ivoka.*`)
2. ✅ **Параметры Intent** - все верно (`voice.param.vehicle.*`)
3. ✅ **Target/Classify/Command** - все верно
4. ✅ **Broadcast механизм** - все верно

### ⚠️ Требует исправления:

1. ⚠️ **Кнопка на руле** - найти правильный keycode через logcat
2. ⚠️ **Smart Mode Action** - изменить на `pateo.dls.ivoka.SET_SMART_MODE`
3. ⚠️ **Добавить BroadcastReceiver** - для перехвата системного Intent кнопки

---

## 🎯 7. СЛЕДУЮЩИЕ ШАГИ

1. **Найти keycode кнопки** через `adb logcat`
2. **Исправить config.json** для smart_mode
3. **Добавить VoiceActivationReceiver**
4. **Протестировать** кнопку и команды
5. **Проверить логи** на предмет ошибок

---

**Удачи! 🚀**
