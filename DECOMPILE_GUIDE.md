# 🔍 ЧТО ДЕКОМПИЛИРОВАТЬ ЧЕРЕЗ JADX

## 📁 **ГДЕ ИСКАТЬ APK ФАЙЛЫ**

### **Путь 1: System Extracted**
```
D:\Projects\Android\MM\6.11.1\system_extracted\system\priv-app\
```

**Искать APK:**
- `QGSpeechService.apk`
- `BluetoothPhone.apk`
- `CarService.apk`
- `QGTtsService.apk`

### **Путь 2: Export Projects**
```
D:\Projects\Android\MM\6.11.1\export\
```

**Искать в папках:**
- `QGSpeechService-release-signed/app/build/outputs/apk/`
- `BluetoothPhone-release-signed/app/build/outputs/apk/`

---

## 🎯 **КАКИЕ APK ДЕКОМПИЛИРОВАТЬ**

### **Приоритет 1: QGSpeechService.apk**

**Что ищем:**
```
IVuiService.aidl
IVuiService.java
VuiServiceMgr.java
CarControlHandler.java
CarOrderBean.java
```

**Команда jadx:**
```bash
jadx -d QGSpeechService-decompiled QGSpeechService.apk
```

**Искать в:**
```
QGSpeechService-decompiled/sources/
```

---

### **Приоритет 2: BluetoothPhone.apk**

**Что ищем:**
```
ControlManger.java
CarControlHandler.java
CarEventBus.java
ICarEventsObserver.java
CarOrderBean.java
```

**Команда jadx:**
```bash
jadx -d BluetoothPhone-decompiled BluetoothPhone.apk
```

---

### **Приоритет 3: CarService.apk**

**Что ищем:**
```
ICarService.aidl
CarPropertyManager.java
```

---

## 🔍 **ЧТО ИМЕННО ИСКАТЬ**

### **1. AIDL Файлы (самое важное!)**

**Искать файлы с расширением:**
```
*.aidl
```

**Или интерфейсы в Java:**
```java
public interface IVuiService extends IInterface {
    void sendCarOrderBean(CarOrderBean bean);
    boolean registerHandler(int appId, int[] actions, IVuiActionHandler handler);
}
```

**Где искать:**
```
decompiled/sources/com/qinggan/speech/
decompiled/sources/com/qinggan/car/
decompiled/sources/android/os/
```

---

### **2. Классы для отправки команд**

**Искать:**
```java
// ControlManger.java
public class ControlManger {
    private VuiServiceMgr mVuiMgr;
    private int[] MainRegisterAction = {120, 91, 122, 138};
    
    public void init(Context context) {
        mVuiMgr = VuiServiceMgr.getInstance(context, callback);
        mVuiMgr.registerHandler(
            AppServiceType.mVehicleControl.ordinal(),
            MainRegisterAction,
            CarControlHandler.getVoiceActionHandler()
        );
    }
}
```

**Где искать:**
```
decompiled/sources/com/qinggan/car/ControlManger.java
```

---

### **3. CarOrderBean**

**Искать:**
```java
// CarOrderBean.java
public class CarOrderBean implements Parcelable {
    private int classify;      // 35=ChargePort, 5=AirConditioner
    private int subClassify;   // позиция
    private int command;       // 0=OPEN, 1=CLOSE
    private int param;
    private double paramDouble;
    private String paramStr;
    
    // Parcelable methods
    public void writeToParcel(Parcel dest, int flags);
    public static final Creator<CarOrderBean> CREATOR;
}
```

**Где искать:**
```
decompiled/sources/com/qinggan/dcs/bean/car/CarOrderBean.java
```

---

### **4. VuiServiceMgr**

**Искать:**
```java
// VuiServiceMgr.java
public class VuiServiceMgr {
    public static VuiServiceMgr getInstance(Context context, VuiConnectionCallback callback);
    
    public boolean registerHandler(int appServiceType, int[] actions, VuiActionHandler handler);
    public boolean unregisterHandler(int appServiceType, int[] actions);
    
    public enum AppServiceType {
        mVehicleControl,  // ЭТО НАМ НУЖНО!
        mbTuner,
        mbBTPhone,
        ...
    }
}
```

**Где искать:**
```
decompiled/sources/com/qinggan/speech/VuiServiceMgr.java
```

---

### **5. Action ID (константы)**

**Искать:**
```java
// VoiceActionID.java
public class VoiceActionID {
    public static final int ACTION_DCS_CAR_VOICE_CONTROL = 120;
    public static final int ACTION_VEHICLE_DEVICE_CONTROL = 91;
    public static final int ACTION_VEHICLE_STATUS_QUERY = 92;
    public static final int ACTION_AIR_CONTROL_CMD = 122;
    public static final int ACTION_SMART_MODE_SET = 138;
}
```

**Где искать:**
```
decompiled/sources/com/qinggan/speech/VoiceActionID.java
```

---

## 📋 **ПОШАГОВАЯ ИНСТРУКЦИЯ**

### **Шаг 1: Найти APK**

```bash
# Поиск в system_extracted
dir D:\Projects\Android\MM\6.11.1\system_extracted\system\priv-app\*.apk /s

# Поиск в export
dir D:\Projects\Android\MM\6.11.1\export\*.apk /s
```

---

### **Шаг 2: Декомпилировать через jadx**

```bash
# QGSpeechService
jadx -d D:\Projects\Android\MM\6.11.1\export\QGSpeechService-decompiled D:\path\to\QGSpeechService.apk

# BluetoothPhone
jadx -d D:\Projects\Android\MM\6.11.1\export\BluetoothPhone-decompiled D:\path\to\BluetoothPhone.apk
```

---

### **Шаг 3: Искать нужные файлы**

**В Windows Explorer:**
```
1. Открыть папку decompiled/sources
2. Поиск (Ctrl+F): "IVuiService"
3. Поиск: "CarOrderBean"
4. Поиск: "VuiServiceMgr"
5. Поиск: "ControlManger"
```

**Или через grep:**
```bash
grep -r "interface IVuiService" D:\Projects\Android\MM\6.11.1\export\QGSpeechService-decompiled\
grep -r "class CarOrderBean" D:\Projects\Android\MM\6.11.1\export\BluetoothPhone-decompiled\
grep -r "registerHandler" D:\Projects\Android\MM\6.11.1\export\QGSpeechService-decompiled\
```

---

### **Шаг 4: Скопировать найденные файлы**

**Что копировать в наш проект:**

```
VoboostVoiceAssistant/app/src/main/java/
  ├── com/qinggan/speech/
  │   ├── VuiServiceMgr.java          ← Скопировать
  │   ├── VuiActionHandler.java       ← Скопировать
  │   ├── AppServiceType.java         ← Скопировать
  │   └── VoiceActionID.java          ← Скопировать
  │
  └── com/qinggan/dcs/bean/car/
      └── CarOrderBean.java           ← Скопировать
```

---

### **Шаг 5: Использовать в коде**

```kotlin
// CommandExecutor.kt
import com.qinggan.speech.VuiServiceMgr
import com.qinggan.speech.AppServiceType
import com.qinggan.dcs.bean.car.CarOrderBean

// Создать CarOrderBean
val carOrderBean = CarOrderBean()
carOrderBean.classify = 35  // ChargePort
carOrderBean.command = 1    // OPEN

// Получить VuiServiceMgr
val vuiMgr = VuiServiceMgr.getInstance(context, callback)

// Зарегистрировать handler
vuiMgr.registerHandler(
    AppServiceType.mVehicleControl.ordinal(),
    intArrayOf(91, 120, 122),
    vuiActionHandler
)
```

---

## 🎯 **БЫСТРЫЙ ПОИСК**

### **Текст для поиска в jadx:**

```
1. "interface IVuiService"
2. "class CarOrderBean"
3. "class VuiServiceMgr"
4. "class ControlManger"
5. "ACTION_VEHICLE_DEVICE_CONTROL"
6. "registerHandler"
7. "sendCarOrderBean"
8. "AppServiceType"
9. "mVehicleControl"
10. "Parcelable"
```

---

## ✅ **ЧЕК-ЛИСТ**

- [ ] Найти QGSpeechService.apk
- [ ] Найти BluetoothPhone.apk
- [ ] Декомпилировать через jadx
- [ ] Найти IVuiService.aidl или IVuiService.java
- [ ] Найти CarOrderBean.java
- [ ] Найти VuiServiceMgr.java
- [ ] Найти AppServiceType.java
- [ ] Скопировать файлы в VoboostVoiceAssistant
- [ ] Обновить CommandExecutor.kt
- [ ] Протестировать

---

## 📝 **ЕСЛИ AIDL НЕ НАЙДЕН**

**Если не нашли .aidl файлы:**

1. **Искать в декомпилированном коде:**
   ```
   decompiled/sources/android/os/
   Искать: Binder, Stub, Proxy
   ```

2. **Создать AIDL вручную:**
   ```aidl
   // IVuiService.aidl
   package com.qinggan.speech;
   
   import com.qinggan.dcs.bean.car.CarOrderBean;
   
   interface IVuiService {
       boolean sendCarOrderBean(CarOrderBean bean);
       boolean registerHandler(int appId, int[] actions, IVuiActionHandler handler);
   }
   ```

3. **Использовать классы из декомпиляции:**
   ```java
   // Просто скопировать .java файлы
   // И использовать напрямую
   ```

---

**Удачи в поиске! 🎉**
