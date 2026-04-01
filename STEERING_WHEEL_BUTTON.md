# 🎛️ КНОПКА ГОЛОСОВОГО ПОМОЩНИКА НА РУЛЕ

**Дата исследования:** 2026-03-29
**Статус:** ✅ НАЙДЕНА ИНФОРМАЦИЯ В DECOMPILED CODE

---

## 📊 ЧТО НАЙДЕНО

### 1. VoiceActionID (из Ivoka/QGSpeechService):

```java
// Файл: com/qinggan/speech/VoiceActionID.java
public static final int ACTION_VOICE_IVOKA_HARD_KEY = 190;
```

### 2. AIDL Callback (из CanBusService):

```aidl
// Файл: com/qinggan/canbus/ICanBusServiceCallback.aidl
oneway interface ICanBusServiceCallback {
    // ... другие callbacks ...
    
    void onSWCAngleChanged(in SWCAngle swcAngle);  // ← КНОПКА НА РУЛЕ!
    
    // ... другие callbacks ...
}
```

### 3. SWC Angle Data:

```java
// Файл: com/qinggan/canbus/SWCAngle.java
public final class SWCAngle implements Parcelable {
    public static final int SWC_DIR_CENTER = 0;
    public static final int SWC_DIR_LEFT = 2;
    public static final int SWC_DIR_RIGHT = 1;
    
    public int mWheelDirection;  // Направление: 0=center, 1=right, 2=left
    public int mWheelAngle;      // Угол поворота руля
}
```

### 4. Регистрация Callback:

```aidl
// Файл: com/qinggan/canbus/ICanBusService.aidl
interface ICanBusService {
    // ...
    boolean addCallback(ICanBusServiceCallback cb);
    boolean removeCallback(ICanBusServiceCallback cb);
    // ...
}
```

---

## 🔄 ПОТОК ДАННЫХ

```
┌─────────────────┐
│ Кнопка на руле  │
│ (Voice Button)  │
└────────┬────────┘
         │
         ↓ (физическое нажатие)
┌─────────────────┐
│   SWC Module    │
│ (Steering Wheel │
│    Control)     │
└────────┬────────┘
         │
         ↓ (CAN шина)
┌─────────────────┐
│  CanBusService  │
│   (qg.canbus)   │
└────────┬────────┘
         │
         ↓ (AIDL callback)
┌─────────────────┐
│ onSWCAngleChan  │
│    ged()        │
└────────┬────────┘
         │
         ↓ (обработка)
┌─────────────────┐
│ ACTION_VOICE_   │
│   IVOKA_HARD_   │
│      KEY=190    │
└────────┬────────┘
         │
         ↓ (активация)
┌─────────────────┐
│ Голосовой       │
│ Помощник        │
└─────────────────┘
```

---

## 💻 ПРИМЕР РЕАЛИЗАЦИИ

### CanBusServiceCallback Listener:

```kotlin
class VoiceButtonHandler(
    private val context: Context,
    private val voiceAssistantService: VoboostVoiceService
) : ICanBusServiceCallback.Stub() {

    companion object {
        private const val TAG = "VoiceButtonHandler"
        
        // Порог для определения нажатия кнопки
        private const val SWC_VOICE_BUTTON_ANGLE_MIN = 100
        private const val SWC_VOICE_BUTTON_ANGLE_MAX = 200
    }

    /**
     * Вызывается при изменении угла поворота руля
     * Включает нажатия кнопок на руле
     */
    override fun onSWCAngleChanged(swcAngle: SWCAngle?) {
        swcAngle ?: return
        
        Log.d(TAG, "SWC Angle changed: direction=${swcAngle.mWheelDirection}, angle=${swcAngle.mWheelAngle}")
        
        // Проверяем нажатие кнопки голосового помощника
        if (isVoiceButtonPress(swcAngle)) {
            Log.i(TAG, "Voice button pressed on steering wheel!")
            
            // Активируем голосовой помощник
            voiceAssistantService.activateByHardwareKey()
        }
    }

    /**
     * Определить нажатие кнопки голосового помощника
     * 
     * В зависимости от автомобиля кнопка может быть:
     * - Отдельная кнопка "Voice" или микрофон
     * - Кнопка на подрулевом переключателе
     * - Сенсорная кнопка на руле
     */
    private fun isVoiceButtonPress(swcAngle: SWCAngle): Boolean {
        // Вариант 1: Проверка по углу (эмпирически подобрать значения)
        if (swcAngle.mWheelAngle in SWC_VOICE_BUTTON_ANGLE_MIN..SWC_VOICE_BUTTON_ANGLE_MAX) {
            return true
        }
        
        // Вариант 2: Проверка по направлению (если кнопка отдельно)
        // Обычно кнопка голоса - это отдельное направление
        if (swcAngle.mWheelDirection == SWC_DIR_CENTER) {
            // Центральная кнопка может быть голосовой
            return true
        }
        
        return false
    }

    // Остальные callback методы (можно оставить пустыми)
    override fun onDoorStatusChanged(p0: DoorStatus?) {}
    override fun onVehicleSpeedChanged(p0: Int) {}
    // ... и так далее все 40+ методов
}
```

### Регистрация в VoboostVoiceService:

```kotlin
class VoboostVoiceService : LifecycleService() {
    
    private var canBusService: ICanBusService? = null
    private var voiceButtonHandler: VoiceButtonHandler? = null
    private var isCallbackRegistered = false

    override fun onCreate() {
        super.onCreate()
        
        // Инициализация обработчика кнопки
        voiceButtonHandler = VoiceButtonHandler(this, this)
        
        // Подключение к CanBusService
        bindToCanBusService()
    }

    private fun bindToCanBusService() {
        val intent = Intent("com.qinggan.canbus.CanBusService")
        intent.setPackage("com.qinggan.canbus.service")
        
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                canBusService = ICanBusService.Stub.asInterface(service)
                Log.i(TAG, "Connected to CanBusService")
                
                // Регистрируем callback для кнопки на руле
                registerVoiceButtonCallback()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                canBusService = null
                isCallbackRegistered = false
                Log.w(TAG, "Disconnected from CanBusService")
            }
        }, BIND_AUTO_CREATE)
    }

    private fun registerVoiceButtonCallback() {
        if (canBusService != null && voiceButtonHandler != null && !isCallbackRegistered) {
            try {
                canBusService?.addCallback(voiceButtonHandler)
                isCallbackRegistered = true
                Log.i(TAG, "Voice button callback registered")
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to register callback", e)
            }
        }
    }

    /**
     * Активация по кнопке на руле
     * Вызывается из VoiceButtonHandler
     */
    fun activateByHardwareKey() {
        Log.i(TAG, "Activated by steering wheel voice button")
        
        // Запуск распознавания
        startListening()
        
        // Визуальная обратная связь
        overlayManager.showListeningOverlay()
        
        // Звуковая обратная связь (опционально)
        playActivationSound()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Отключаем callback
        if (canBusService != null && voiceButtonHandler != null && isCallbackRegistered) {
            try {
                canBusService?.removeCallback(voiceButtonHandler)
                Log.i(TAG, "Voice button callback removed")
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to remove callback", e)
            }
        }
        
        isCallbackRegistered = false
    }
}
```

---

## 🔧 ЧТО НУЖНО СДЕЛАТЬ

### 1. Добавить AIDL файлы в проект:

Убедитесь что есть:
- `app/src/main/aidl/com/qinggan/canbus/ICanBusServiceCallback.aidl`
- `app/src/main/aidl/com/qinggan/canbus/SWCAngle.aidl`

### 2. Создать VoiceButtonHandler:

Скопируйте класс `VoiceButtonHandler` из примера выше.

### 3. Обновить VoboostVoiceService:

Добавьте регистрацию callback при подключении к CanBusService.

### 4. Подобрать пороги срабатывания:

Эмпирически определить значения `SWC_VOICE_BUTTON_ANGLE_MIN` и `SWC_VOICE_BUTTON_ANGLE_MAX`:

```kotlin
// В VoiceButtonHandler.kt
override fun onSWCAngleChanged(swcAngle: SWCAngle) {
    Log.d(TAG, "SWC: dir=${swcAngle.mWheelDirection}, angle=${swcAngle.mWheelAngle}")
    // Смотрим логи и подбираем значения для вашей кнопки
}
```

---

## 📝 ЗАМЕТКИ

### Важные моменты:

1. **SWC (Steering Wheel Control)** - это общий интерфейс для всех кнопок на руле
2. **Разные автомобили** могут использовать разные углы/направления для кнопки голоса
3. **Требуется тестирование** на реальном автомобиле для подбора порогов
4. **CanBusService должен быть запущен** в системе (системный сервис)

### Альтернативные варианты:

Если кнопка не через SWC, возможно:
- Отдельный **Broadcast Intent** при нажатии
- **KeyEvent** с определенным keycode
- **InputEvent** от сенсорной кнопки

---

## 🎯 ССЫЛКИ НА ИСХОДНЫЙ КОД

**Ivoka (декомпилированный):**
- `VoiceActionID.java:388` - `ACTION_VOICE_IVOKA_HARD_KEY = 190`
- `ICanBusServiceCallback.aidl:65` - `onSWCAngleChanged()`
- `SWCAngle.java` - данные о кнопке
- `VehicleState.java:126` - `WARM_SWITCH(ACTION_VOICE_IVOKA_HARD_KEY)`

**QGSpeechService (декомпилированный):**
- `VehicleOnlineState.java:150` - `LEFRRIGHT(ACTION_VOICE_IVOKA_HARD_KEY)`

---

**Удачи с реализацией! 🚀**
