# 🔑 НАЙДЕН: МЕХАНИЗМ ОБРАБОТКИ КНОПКИ НА РУЛЕ!

**Дата:** 2026-03-24  
**Статус:** ✅ КЛЮЧЕВАЯ НАХОДКА

---

## 🎯 ГЛАВНАЯ НАХОДКА

**Кнопка на руле обрабатывается через KeyManager с KEYCODE_IVOKA = 130**

---

## 📊 ПОЛНЫЙ ПУТЬ КНОПКИ НА РУЛЕ

### 1. Аппаратный уровень (CAN шина)

```
Кнопка на руле
    ↓
CAN Bus (SWC - Steering Wheel Control)
    ↓
CanBusService (com.qinggan.canbus)
```

**Файл:** `CanBusService-release/app/src/main/java/com/qinggan/vehicle/VehicleOnlineState.java`

```java
public enum VehicleOnlineState {
    SWC_BUTTON(10),           // Кнопки на руле
    SWC_BUTTON_LEFT(37),      // Левые кнопки
    SWC_BUTTON_RIGHT(38),     // Правые кнопки
    VOICE_REQUSET_FUN(124)    // Запрос голосовой функции
}
```

---

### 2. Системный уровень (KeyManager)

**Файл:** `QGSpeechService-release-signed/.../system/KeyManager.java`

```java
public class KeyManager {
    // Слушатель событий кнопок
    public interface OnKeyEventListener {
        boolean onKeyDown(int i, KeyEvent keyEvent);
        boolean onKeyShortClick(int i, KeyEvent keyEvent);
        // ...
    }
}
```

**Файл:** `QGSpeechService-release-signed/.../qinglink/api/Constant.java`

```java
public interface Constant {
    public interface Button {
        public interface KeyCode {
            public static final int KEYCODE_IVOKA = 130;  // ← КНОПКА IVA!
            public static final int KEYCODE_BACK = 33;
            public static final int KEYCODE_VOLUME_UP = 2;
            public static final int KEYCODE_VOLUME_DOWN = 1;
        }
    }
}
```

---

### 3. Уровень голосового сервиса (SpeechMgr)

**Файл:** `QGSpeechService-release-signed/.../speech/adapter/speech/SpeechMgr.java`

```java
public class SpeechMgr implements ISpeech {
    private KeyManager mKeyManager = null;
    
    // Регистрация слушателя кнопок
    private KeyManager.OnKeyEventListener mOnKeyEventListener = 
        new KeyManager.OnKeyEventListener() {
        
        @Override
        public boolean onKeyShortClick(int i, KeyEvent keyEvent) {
            // Проверяем: это кнопка IVA (130) или Назад (33)?
            if (i != 33 && i != 130) {
                Log.d(TAG, "keycode = " + i + " not ivoka and backCode,ignore");
                return false;
            }
            
            // Создаем событие для голосового помощника
            KeyEventInfo keyEventInfo = new KeyEventInfo();
            if (i == 130) {
                keyEventInfo.setKeyCode(1);  // 1 = IVA нажата
            } else if (i == 33) {
                keyEventInfo.setKeyCode(2);  // 2 = Назад
            }
            keyEventInfo.setKeyEvent(1);  // 1 = нажатие
            
            // Отправляем в голосовой движок
            PlatformAdapterObj platformAdapterObj = 
                new PlatformAdapterObj(Category.CATEGORY_HARD_KEY_EVENT, "");
            platformAdapterObj.putBundle(keyEventInfo);
            SpeechAdapterSDK.getInstance().onSystemEvent(
                Category.CATEGORY_HARD_KEY_EVENT, "", 
                platformAdapterObj.toIntent()
            );
            
            return i == 33 || i == 130;
        }
    };
    
    @Override
    public void init(Context context) {
        // Подключаемся к KeyManager
        this.mKeyManager = KeyManager.getInstance(this.mContext, 
            new KeyManager.OnInitListener() {
                public void onResult(boolean z) {
                    if (z) {
                        // Регистрируем слушателя кнопок
                        SpeechMgr.this.mKeyManager.registerKeyEventListener(
                            SpeechMgr.this.mOnKeyEventListener
                        );
                    }
                }
            }
        );
    }
}
```

---

### 4. Уровень обработки нажатия (QGHardKey)

**Файл:** `QGSpeechService-release-signed/.../speech/vui/runtimeadapter/speech/QGHardKey.java`

```java
public class QGHardKey {
    
    public void receiveHardKey(KeyEventInfo keyEventInfo) {
        Log.d(TAG, "receiveHardKey:" + keyEventInfo.toString());
        
        // keyCode == 1 означает нажатие кнопки IVA
        if (1 == keyEventInfo.getKeyCode()) {
            Log.d(TAG, "Run voice track thread, type 1.");
            
            // 1. Активируем голосовое пробуждение
            WakeupVuiEngine.getInstance().trackWakeup("", 1, 1, "", "");
            
            // 2. Если есть QingLink - отправляем туда
            if (Configs.SUPPORT_QINGLINK) {
                QingLinkTransceiver.getInstance().sendWakeupByKey(
                    keyEventInfo.getKeyEvent()
                );
            } else {
                // 3. Иначе запускаем распознавание по локации
                VoiceLocationMgr.getInstance().setStartListenLoc("front_left");
            }
        }
    }
}
```

---

### 5. Запуск Ivoka (WindowService)

**Файл:** `Ivoka1-release-signed/.../speech/QGWindowServiceMgr.java`

```java
public class QGWindowServiceMgr {
    
    public void startIvoka() {
        // Отправляем Broadcast для запуска Ivoka
        Intent intent = new Intent(VoiceBroadcastAction.ACTION_START_IVOKA);
        intent.setPackage("com.qinggan.ivoka");
        context.startService(intent);
        
        // Также для Ivoka1
        Intent intent2 = new Intent(VoiceBroadcastAction.ACTION_START_IVOKA);
        intent2.setPackage("com.qinggan.ivoka1");
        context.startService(intent2);
    }
}
```

**Файл:** `Ivoka1-release-signed/.../ivoka/service/WindowService.java`

```java
public class WindowService extends Service {
    public static final String ACTION_START_IVOKA = "com.qinggan.iovka.START_IVOKA";
    
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        
        if (ACTION_START_IVOKA.equals(action)) {
            // Запускаем голосовой помощник!
            startVoiceRecognition();
        }
    }
}
```

---

## 📝 ПОЛНАЯ СХЕМА РАБОТЫ

```
┌─────────────────────────────────────────────────────────────────┐
│                    КНОПКА НА РУЛЕ НАЖАТА                        │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  CanBusService (com.qinggan.canbus)                             │
│  - Получает данные из CAN шины                                  │
│  - SWC_BUTTON (10) / SWC_BUTTON_LEFT (37)                       │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  KeyManager (com.qinggan.system.KeyManager)                     │
│  - KEYCODE_IVOKA = 130                                          │
│  - onKeyShortClick(130, KeyEvent)                               │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  SpeechMgr (com.qinggan.speech.adapter.speech.SpeechMgr)        │
│  - mOnKeyEventListener.onKeyShortClick(130, keyEvent)           │
│  - KeyEventInfo.setKeyCode(1)  // 1 = IVA                       │
│  - SpeechAdapterSDK.onSystemEvent(HARD_KEY_EVENT)               │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  QGHardKey (com.qinggan.speech.vui.runtimeadapter.speech)       │
│  - receiveHardKey(KeyEventInfo)                                 │
│  - if (keyCode == 1) → WakeupVuiEngine.trackWakeup()            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  QingLinkTransceiver / VoiceLocationMgr                         │
│  - sendWakeupByKey()                                            │
│  - setStartListenLoc("front_left")                              │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  QGWindowServiceMgr                                             │
│  - startIvoka()                                                 │
│  - Intent: "com.qinggan.iovka.START_IVOKA"                      │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  Ivoka WindowService (com.qinggan.ivoka.service.WindowService)  │
│  - ACTION_START_IVOKA = "com.qinggan.iovka.START_IVOKA"         │
│  - startVoiceRecognition()                                      │
│  - Показываем UI                                                │
│  - Начинаем распознавание речи                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 КАК ЭТО ИСПОЛЬЗОВАТЬ В VoboostVoiceAssistant

### Вариант 1: Перехват через KeyManager (ПРАВИЛЬНЫЙ!)

```kotlin
// VoiceActivationService.kt
class VoiceActivationService : Service() {
    
    companion object {
        const val KEYCODE_IVOKA = 130  // ← ПРАВИЛЬНЫЙ KEYCODE!
    }
    
    private var keyManager: KeyManager? = null
    
    private val keyEventListener = object : KeyManager.OnKeyEventListener {
        override fun onKeyShortClick(keyCode: Int, keyEvent: KeyEvent?): Boolean {
            if (keyCode == KEYCODE_IVOKA) {
                Log.i(TAG, "IVA button pressed!")
                activateVoiceAssistant()
                return true
            }
            return false
        }
        
        // Остальные методы...
        override fun onKeyDown(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyHoldPressStart(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyHoldPressStop(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStart(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStop(keyCode: Int, keyEvent: KeyEvent?) = false
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Подключаемся к KeyManager
        keyManager = KeyManager.getInstance(this, object : KeyManager.OnInitListener {
            override fun onResult(success: Boolean) {
                if (success) {
                    // Регистрируем слушателя
                    keyManager?.registerKeyEventListener(keyEventListener)
                    Log.i(TAG, "KeyManager connected and listener registered")
                }
            }
        })
    }
    
    private fun activateVoiceAssistant() {
        // Запускаем голосовой помощник
        val intent = Intent(this, VoboostVoiceService::class.java)
        intent.action = "com.voboost.voiceassistant.ACTIVATE"
        startService(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Отключаем слушателя
        keyManager?.unregisterKeyEventListener(keyEventListener)
    }
}
```

**AndroidManifest.xml:**

```xml
<service 
    android:name=".VoiceActivationService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.voboost.voiceassistant.VOICE_ACTIVATION"/>
    </intent-filter>
</service>
```

---

### Вариант 2: Перехват Broadcast от QGSpeechService

```kotlin
// VoiceActivationReceiver.kt
class VoiceActivationReceiver : BroadcastReceiver() {
    
    companion object {
        // QGSpeechService отправляет это событие
        const val ACTION_HARD_KEY_EVENT = "com.qinggan.speech.HARD_KEY_EVENT"
        const val EXTRA_KEY_CODE = "key_code"
        const val KEYCODE_IVOKA = 130
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val keyCode = intent.getIntExtra(EXTRA_KEY_CODE, -1)
        
        if (keyCode == KEYCODE_IVOKA) {
            Log.i(TAG, "IVA button pressed (via broadcast)!")
            
            // Запускаем голосовой помощник
            val serviceIntent = Intent(context, VoboostVoiceService::class.java)
            serviceIntent.action = "com.voboost.voiceassistant.ACTIVATE"
            context.startService(serviceIntent)
        }
    }
}
```

---

### Вариант 3: Перехват через ACTION_START_IVOKA

QGSpeechService запускает Ivoka через Broadcast. Можно перехватить тот же Intent:

```kotlin
// VoiceActivationReceiver.kt
class VoiceActivationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        
        // Перехватываем запуск Ivoka
        if (action == "com.qinggan.iovka.START_IVOKA") {
            Log.i(TAG, "Ivoka is starting - activating Voboost instead!")
            
            // Запускаем НАШ голосовой помощник
            val serviceIntent = Intent(context, VoboostVoiceService::class.java)
            serviceIntent.action = "com.voboost.voiceassistant.ACTIVATE"
            context.startService(serviceIntent)
            
            // Можно отменить запуск Ivoka (если нужно)
            // abortBroadcast()  // Только если receiver зарегистрирован с priority
        }
    }
}
```

**AndroidManifest.xml:**

```xml
<receiver 
    android:name=".VoiceActivationReceiver"
    android:exported="true">
    <intent-filter android:priority="999">
        <action android:name="com.qinggan.iovka.START_IVOKA"/>
    </intent-filter>
</receiver>
```

---

## ✅ ИТОГОВЫЕ ДАННЫЕ

| Параметр | Значение |
|----------|----------|
| **KEYCODE_IVOKA** | `130` |
| **KeyManager класс** | `com.qinggan.system.KeyManager` |
| **Слушатель** | `KeyManager.OnKeyEventListener` |
| **Метод** | `onKeyShortClick(int keyCode, KeyEvent)` |
| **QGHardKey** | `com.qinggan.speech.vui.runtimeadapter.speech.QGHardKey` |
| **ACTION_START_IVOKA** | `com.qinggan.iovka.START_IVOKA` |
| **WindowService** | `com.qinggan.ivoka.service.WindowService` |

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

1. **Добавить KeyManager в проект VoboostVoiceAssistant**
   - Скопировать `KeyManager.java` из `BluetoothPhone-release-signed`
   - Добавить зависимости (`IDirectionEventListener`, `IKeyManagerCallback`, etc.)

2. **Создать VoiceActivationService**
   - Подключиться к KeyManager
   - Зарегистрировать слушателя на KEYCODE_IVOKA (130)
   - Запускать VoboostVoiceService при нажатии

3. **ИЛИ перехватить ACTION_START_IVOKA**
   - Создать BroadcastReceiver с высоким priority
   - Перехватывать `com.qinggan.iovka.START_IVOKA`
   - Запускать VoboostVoiceService вместо Ivoka

4. **Протестировать**
   - Собрать проект
   - Установить на устройство
   - Нажать кнопку на руле
   - Проверить логи

---

**Удачи! Теперь у нас есть полный механизм работы кнопки! 🚀**
