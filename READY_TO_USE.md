# ✅ ВСЕ НАЙДЕНО! ГОТОВЫЕ КЛАССЫ

## 🎯 **НАЙДЕННЫЕ ФАЙЛЫ**

### **1. ControlManger.java**
**Путь:**
```
D:\Projects\Android\MM\6.11.1\export\BluetoothPhone-release-signed\app\src\main\java\com\qinggan\car\ControlManger.java
```

**Что делает:**
```java
public class ControlManger {
    private VuiServiceMgr mVuiMgr;
    
    // Action ID для команд
    private int[] MainRegisterAction = {
        120,  // ACTION_DCS_CAR_VOICE_CONTROL
        91,   // ACTION_VEHICLE_DEVICE_CONTROL
        92,   // ACTION_VEHICLE_STATUS_QUERY
        25,   // ACTION_AIR_CONTROL_OPEN
        122,  // ACTION_AIR_CONTROL_CMD
        26,   // ACTION_AIR_CONTROL_ADJUST
        27,   // ACTION_AIR_CONTROL_CLOSE
        138,  // ACTION_SMART_MODE_SET
        281   // ACTION_CAR_DRIVER_RECORDER_CONTROL
    };
    
    // При подключении регистрируем handler
    mVuiMgr.registerHandler(
        AppServiceType.mVehicleControl.ordinal(),
        MainRegisterAction,
        CarControlHandler.getVoiceActionHandler()
    );
}
```

---

### **2. CarOrderBean.java**
**Путь:**
```
D:\Projects\Android\MM\6.11.1\export\BluetoothPhone-release-signed\app\src\main\java\com\qinggan\dcs\bean\car\CarOrderBean.java
```

**Структура:**
```java
public class CarOrderBean extends DcsBean implements Parcelable {
    private int classify;      // 35=ChargePort, 5=AirConditioner, 2=Window
    private int subClassify;   // позиция (0=all, 1=front_left, etc.)
    private int command;       // 0=OPEN, 1=CLOSE, 2=ADJUST, 3=SET
    private int param;         // числовой параметр
    private double paramDouble; // дробный параметр (температура)
    private String paramStr;   // строковый параметр (режим)
}
```

---

### **3. VuiServiceMgr.java**
**Путь:**
```
D:\Projects\Android\MM\6.11.1\export\QGSpeechService-release-signed\app\src\main\java\com\qinggan\speech\VuiServiceMgr.java
```

**Ключевые методы:**
```java
public class VuiServiceMgr {
    public static VuiServiceMgr getInstance(Context context, VuiConnectionCallback callback);
    
    public boolean registerHandler(int appServiceType, int[] actions, VuiActionHandler handler);
    public boolean unregisterHandler(int appServiceType, int[] actions);
    
    public enum AppServiceType {
        mVehicleControl,  // ЭТО НАМ НУЖНО! (ordinal = 106)
        mbTuner,
        mbBTPhone,
        ...
    }
}
```

---

## 📋 **ЧТО ДЕЛАТЬ ДАЛЬШЕ**

### **Вариант A: Использовать готовые классы (РЕКОМЕНДУЕТСЯ)**

**Шаг 1: Скопировать классы в наш проект**

```bash
# Скопировать из BluetoothPhone-release-signed:
D:\Projects\Android\MM\6.11.1\export\BluetoothPhone-release-signed\app\src\main\java\com\qinggan\car\
  ├── ControlManger.java       → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/car/
  ├── CarControlHandler.java   → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/car/
  ├── CarEventBus.java         → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/car/
  └── ICarEventsObserver.java  → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/car/

# Скопировать из QGSpeechService-release-signed:
D:\Projects\Android\MM\6.11.1\export\QGSpeechService-release-signed\app\src\main\java\com\qinggan\speech\
  ├── VuiServiceMgr.java       → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/speech/
  ├── VuiActionHandler.java    → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/speech/
  └── AppServiceType.java      → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/speech/

# Скопировать DcsBean:
D:\Projects\Android\MM\6.11.1\export\BluetoothPhone-release-signed\app\src\main\java\com\qinggan\dcs\bean\
  ├── DcsBean.java             → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/dcs/bean/
  └── car/CarOrderBean.java    → VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/dcs/bean/car/
```

---

**Шаг 2: Обновить CommandExecutor**

```kotlin
// CommandExecutor.kt
import ru.voboost.voiceassistant.speech.VuiServiceMgr
import ru.voboost.voiceassistant.speech.AppServiceType
import ru.voboost.voiceassistant.dcs.bean.car.CarOrderBean
import ru.voboost.voiceassistant.car.CarEventBus
import ru.voboost.voiceassistant.car.ICarEventsObserver

class CommandExecutor(private val context: Context) {
    private var vuiMgr: VuiServiceMgr? = null
    
    init {
        // Инициализировать VuiServiceMgr
        vuiMgr = VuiServiceMgr.getInstance(context, object : VuiServiceMgr.VuiConnectionCallback {
            override fun onServiceConnected() {
                // Зарегистрировать handler
                vuiMgr?.registerHandler(
                    AppServiceType.mVehicleControl.ordinal(),
                    intArrayOf(120, 91, 122, 138),
                    voiceActionHandler
                )
            }
            
            override fun onServiceDisconnect() {}
        })
    }
    
    suspend fun executeCommand(recognizedCommand: RecognizedCommand) {
        val commandConfig = recognizedCommand.config
        val action = commandConfig.action
        
        // Создать CarOrderBean
        val carOrderBean = CarOrderBean().apply {
            classify = action.classify
            command = action.command
            subClassify = 0  // или из params
            param = action.params["param"] as? Int ?: 0
            paramDouble = action.params["temperature"] as? Double ?: 0.0
        }
        
        // Отправить через CarEventBus
        CarEventBus.getInstance().notifyObservers(carOrderBean)
    }
}
```

---

### **Вариант B: Продолжать использовать Broadcast (ТЕКУЩИЙ)**

**Оставляем текущий код:**
```kotlin
// CommandExecutor.kt
val intent = Intent(action.intentAction)
context.sendBroadcast(intent)
```

**Тестируем:**
```bash
gradlew.bat assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
"Привет, Вобуст"
"Открой лючок зарядки"
adb logcat | grep -i "CarControlHandler"
```

**Если видим в логах:**
```
I/CarControlHandler: onProcessResult: classify=35, command=1
I/CarEventBus: notifyObservers
```
→ **Broadcast работает! Оставляем!**

**Если тихо:**
→ **Используем Вариант A (VuiServiceMgr)**

---

## 🎯 **РЕКОМЕНДАЦИЯ**

### **Сначала тестировать Broadcast (Вариант B):**

**Почему:**
1. ✅ Уже реализовано
2. ✅ Не нужно копировать 10+ классов
3. ✅ Проще код
4. ✅ Работает без root

**Если сработает → отлично!**

---

### **Если не сработает → VuiServiceMgr (Вариант A):**

**Почему:**
1. ✅ Работает надежно (как в оригинале)
2. ✅ Все классы уже декомпилированы
3. ✅ Можно скопировать готовые

**Минусы:**
- ❌ Нужно скопировать 10+ файлов
- ❌ Больше кода
- ❌ Нужно разбираться с зависимостями

---

## 📊 **СРАВНЕНИЕ**

| Критерий | Broadcast | VuiServiceMgr |
|----------|-----------|---------------|
| **Сложность** | Низкая | Высокая |
| **Файлов** | 0 | 10+ |
| **Надежность** | Средняя | Высокая |
| **Код** | 5 строк | 100+ строк |

---

## ✅ **ПЛАН ДЕЙСТВИЙ**

### **Шаг 1: Тестировать текущий код**

```bash
# Собрать
gradlew.bat assembleDebug

# Установить
VoboostVoiceAssistant-install.bat

# Протестировать
"Привет, Вобуст"
"Открой лючок зарядки"

# Проверить логи
adb logcat | grep -i "CarControlHandler\|Intent\|CarEventBus"
```

---

### **Шаг 2: Если работает → ГОТОВО!**

**Оставляем Broadcast, проект готов!**

---

### **Шаг 3: Если не работает → Копируем классы**

**Скопировать файлы из:**
```
BluetoothPhone-release-signed/app/src/main/java/com/qinggan/
QGSpeechService-release-signed/app/src/main/java/com/qinggan/
```

**В:**
```
VoboostVoiceAssistant/app/src/main/java/com/voboost/voiceassistant/
```

**Обновить CommandExecutor.kt**

---

## 🎉 **ВЫВОД**

**Все классы уже декомпилированы и готовы к использованию!**

**Текущий подход (Broadcast) - правильный для начала!**

**Если не сработает → все классы уже есть в export папках!**

---

**Удачи! 🚀**
