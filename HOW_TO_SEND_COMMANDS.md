# 🎯 КАК ПРАВИЛЬНО ОТПРАВЛЯТЬ КОМАНДЫ

## 🔍 **АНАЛИЗ ОРИГИНАЛЬНОЙ СИСТЕМЫ**

### **Найдено в QGSpeechService:**

**Файл:** `com/qinggan/car/ControlManger.java`

```java
// Ключевой момент!
public class ControlManger {
    private VuiServiceMgr mVuiMgr;
    
    // Action ID для управления машиной
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
        AppServiceType.mVehicleControl.ordinal(),  // Тип сервиса
        MainRegisterAction,                         // Action ID
        CarControlHandler.getVoiceActionHandler()  // Обработчик
    );
}
```

---

## 📋 **КАК ЭТО РАБОТАЕТ**

### **Схема:**

```
VuiService (распознавание)
    ↓
VuiServiceMgr (менеджер)
    ↓ (Binder IPC)
registerHandler(AppServiceType.mVehicleControl, actions, handler)
    ↓
CarControlHandler (получает команды)
    ↓
CarEventBus
    ↓
Контроллеры (AirConditioner, Window, etc.)
```

---

## ✅ **ПРАВИЛЬНЫЙ СПОСОБ ОТПРАВКИ**

### **Вариант 1: Через VuiServiceMgr (как в оригинале)**

```kotlin
// 1. Получить VuiServiceMgr
val vuiMgr = VuiServiceMgr.getInstance(context, callback)

// 2. Зарегистрировать handler
val actions = intArrayOf(91, 120, 122)  // Action ID
vuiMgr.registerHandler(
    AppServiceType.mVehicleControl.ordinal(),
    actions,
    vuiActionHandler
)

// 3. Создать CarOrderBean
val carOrderBean = CarOrderBean().apply {
    classify = 35  // ChargePort
    command = 1    // OPEN
    subClassify = 0
}

// 4. Отправить через CarEventBus
CarEventBus.getInstance().notifyObservers(carOrderBean)
```

**Проблема:** Нужны AIDL файлы!

---

### **Вариант 2: Через Broadcast Intent (наш текущий)**

```kotlin
val intent = Intent("pateo.dls.ivoka.vehicle.CONTROL")
intent.putExtra("voice.param.vehicle.classify", 35)
intent.putExtra("voice.param.vehicle.command", 1)
intent.putExtra("voice.param.vehicle.subClassify", 0)
context.sendBroadcast(intent)
```

**Проблема:** QGSpeechService может не слушать broadcast!

---

### **Вариант 3: Прямой вызов через Binder (самый надежный)**

```kotlin
// 1. Найти сервис через ServiceManager
val binder = ServiceManager.getService("com.qinggan.speech.IVuiService")
val vuiService = IVuiService.Stub.asInterface(binder)

// 2. Создать CarOrderBean (нужен класс из QGSpeechService!)
val carOrderBean = CarOrderBean()
carOrderBean.classify = 35
carOrderBean.command = 1

// 3. Отправить напрямую
vuiService.sendCarOrderBean(carOrderBean)
```

**Проблема:** Нужен доступ к классам QGSpeechService!

---

## 🔧 **ЧТО ДЕЛАТЬ НАМ**

### **Текущая реализация (Broadcast):**

```kotlin
// CommandExecutor.kt
val intent = Intent(action.intentAction)
// intent.setPackage("com.qinggan.sttservice")  // УБРАНО!
context.sendBroadcast(intent)
```

**Плюсы:**
- ✅ Не нужны AIDL файлы
- ✅ Простая реализация

**Минусы:**
- ⚠️ QGSpeechService может не слушать broadcast
- ⚠️ Меньше контроля

---

### **Улучшенная реализация (если broadcast не работает):**

**Нужно найти AIDL файлы в QGSpeechService:**

```bash
# Поиск AIDL
find D:\Projects\Android\MM\6.11.1\export\QGSpeechService-release-signed -name "*.aidl"
```

**Создать аналогичные AIDL в нашем проекте:**

```aidl
// IVuiService.aidl
interface IVuiService {
    boolean sendCarOrderBean(CarOrderBean bean);
}
```

**Использовать Binder:**

```kotlin
val binder = ServiceManager.getService("com.qinggan.speech.IVuiService")
val vuiService = IVuiService.Stub.asInterface(binder)
vuiService.sendCarOrderBean(carOrderBean)
```

---

## 📊 **СРАВНЕНИЕ ПОДХОДОВ**

| Подход | Сложность | Надежность | Требуется |
|--------|-----------|------------|-----------|
| **Broadcast Intent** | Низкая | Средняя | - |
| **VuiServiceMgr** | Средняя | Высокая | AIDL файлы |
| **Binder (прямой)** | Высокая | Очень высокая | AIDL + классы |

---

## 🎯 **ТЕКУЩАЯ СТРАТЕГИЯ**

### **Шаг 1: Тестировать Broadcast**

```kotlin
val intent = Intent("pateo.dls.ivoka.vehicle.CONTROL")
intent.putExtra("voice.param.vehicle.classify", 35)
intent.putExtra("voice.param.vehicle.command", 1)
context.sendBroadcast(intent)
```

**Проверить логи:**
```bash
adb logcat | grep -i "CarControlHandler\|CarEventBus"
```

**Если видим:**
```
I/CarControlHandler: onProcessResult: classify=35, command=1
```

→ **Broadcast работает!** ✅

---

### **Шаг 2: Если не работает → использовать Binder**

**Найти AIDL файлы:**
```
QGSpeechService-release-signed/app/src/main/aidl/
```

**Скопировать:**
- IVuiService.aidl
- CarOrderBean.aidl (или Parcelable)

**Создать в нашем проекте:**
```
VoboostVoiceAssistant/app/src/main/aidl/
```

**Использовать Binder:**
```kotlin
val binder = ServiceManager.getService("com.qinggan.speech.IVuiService")
val vuiService = IVuiService.Stub.asInterface(binder)
// Отправить команду
```

---

## 📝 **ACTION ID ДЛЯ КОМАНД**

Из `ControlManger.java`:

| Action ID | Constant | Команда |
|-----------|----------|---------|
| **120** | ACTION_DCS_CAR_VOICE_CONTROL | Управление машиной (DCS) |
| **91** | ACTION_VEHICLE_DEVICE_CONTROL | Управление устройствами |
| **92** | ACTION_VEHICLE_STATUS_QUERY | Запрос статуса |
| **122** | ACTION_AIR_CONTROL_CMD | Команды климата |
| **25** | ACTION_AIR_CONTROL_OPEN | Включить климат |
| **27** | ACTION_AIR_CONTROL_CLOSE | Выключить климат |
| **138** | ACTION_SMART_MODE_SET | Умные режимы |

---

## ✅ **ВЫВОД**

**Текущий подход (Broadcast):**
```kotlin
val intent = Intent(action.intentAction)
// intent.setPackage("com.qinggan.sttservice")  // УБРАНО!
context.sendBroadcast(intent)
```

**Это ПРАВИЛЬНО для начала!**

**Если не сработает:**
1. Найти AIDL файлы в QGSpeechService
2. Создать аналогичные в нашем проекте
3. Использовать Binder для прямого вызова

---

**Удачи! 🚀**
