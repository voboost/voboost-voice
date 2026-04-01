# 📡 КАК РАБОТАЕТ ОТПРАВКА КОМАНД

## ❌ **БЫВШАЯ ПРОБЛЕМА**

```kotlin
// Было
val intent = Intent(action.intentAction)
intent.setPackage("com.qinggan.sttservice")  // ❌ ОГРАНИЧЕНИЕ!
context.sendBroadcast(intent)
```

**Проблема:**
- Intent отправляется ТОЛЬКО в пакет `com.qinggan.sttservice`
- QGSpeechService **НЕ СЛУШАЕТ** broadcast intents!
- Другие сервисы (BluetoothPhone, VehicleSetting) **НЕ ПОЛУЧАТ** команды

---

## ✅ **ИСПРАВЛЕНИЕ**

```kotlin
// Стало
val intent = Intent(action.intentAction)
// intent.setPackage("com.qinggan.sttservice")  // ❌ УБРАНО!
context.sendBroadcast(intent)
```

**Теперь:**
- ✅ Intent отправляется БЕЗ ограничений
- ✅ Система сама найдет получателя по `intentAction`
- ✅ Все сервисы, которые слушают этот action, получат команду

---

## 📋 **КАК ЭТО РАБОТАЕТ**

### **Оригинальная система:**

```
QGSpeechService (распознавание)
    ↓ (Binder call)
CarControlHandler (в том же процессе)
    ↓
CarEventBus
    ↓
Контроллеры (AirConditioner, Window, etc.)
```

**Все работает ВНУТРИ одного процесса через Binder!**

---

### **Наша реализация:**

```
VoboostVoiceService
    ↓ (Broadcast Intent)
System (находит получателя по action)
    ↓
QGSpeechService (если слушает intent)
    ↓ (Binder call)
CarControlHandler
    ↓
CarEventBus
    ↓
Контроллеры
```

**Мы отправляем broadcast, система находит QGSpeechService!**

---

## 📡 **INTENT ACTION'Ы**

### **Команды машины:**
```kotlin
"pateo.dls.ivoka.vehicle.CONTROL"      // Управление
"pateo.dls.ivoka.air_control.OPEN"     // Кондиционер
"pateo.dls.ivoka.air_control.CLOSE"    // Кондиционер
"pateo.dls.ivoka.air_control.ADJUST"   // Кондиционер
```

### **Телефон:**
```kotlin
"pateo.dls.ivoka.telephone.CALL"       // Звонок
"pateo.dls.ivoka.telephone.CMD"        // Команды
```

### **Режимы:**
```kotlin
"pateo.dls.ivoka.vehicle.CONTROL"      // SmartMode
```

---

## 🔍 **ПОЧЕМУ ЭТО РАБОТАЕТ**

### **QGSpeechService регистрирует receiver:**

```xml
<!-- В AndroidManifest QGSpeechService -->
<receiver android:name=".VoiceCommandReceiver">
    <intent-filter>
        <action android:name="pateo.dls.ivoka.vehicle.CONTROL" />
        <action android:name="pateo.dls.ivoka.telephone.CALL" />
        <!-- и т.д. -->
    </intent-filter>
</receiver>
```

**Когда мы отправляем broadcast:**
1. Android System ищет все receiver с таким action
2. Находит receiver в QGSpeechService
3. Передает Intent
4. QGSpeechService обрабатывает через CarControlHandler

---

## ⚠️ **ВАЖНО**

### **Если команды не работают:**

**1. Проверить, что QGSpeechService слушает intents:**
```bash
adb shell dumpsys package com.qinggan.sttservice | grep -i "intent.filter"
```

**2. Проверить логи:**
```bash
adb logcat | grep -i "intent\|receiver\|voice"
```

**3. Если QGSpeechService НЕ слушает broadcast:**

Нужно использовать **Binder** (сложнее):
```kotlin
// Найти сервис через ServiceManager
val binder = ServiceManager.getService("com.qinggan.speech.IVuiService")
val vuiService = IVuiService.Stub.asInterface(binder)

// Создать CarOrderBean
val carOrderBean = CarOrderBean()
carOrderBean.classify = 35
carOrderBean.command = 1

// Отправить напрямую
vuiService.sendCarOrderBean(carOrderBean)
```

---

## 📊 **СРАВНЕНИЕ ПОДХОДОВ**

| Подход | Плюсы | Минусы |
|--------|-------|--------|
| **Broadcast Intent** | ✅ Проще<br>✅ Не нужны AIDL<br>✅ Работает без root | ⚠️ Зависит от QGSpeechService<br>⚠️ Может не работать на некоторых устройствах |
| **Binder** | ✅ Надежнее<br>✅ Прямой вызов<br>✅ Точно работает | ❌ Сложнее<br>❌ Нужны AIDL файлы<br>❌ Требует root на некоторых устройствах |

---

## ✅ **ТЕКУЩАЯ РЕАЛИЗАЦИЯ**

**Используем Broadcast Intent (проще):**
```kotlin
val intent = Intent(action.intentAction)
// intent.setPackage("com.qinggan.sttservice")  // УБРАНО!
context.sendBroadcast(intent)
```

**Если не сработает → переключимся на Binder!**

---

## 🎯 **ТЕСТИРОВАНИЕ**

```bash
# Собрать
gradlew.bat assembleDebug

# Установить
VoboostVoiceAssistant-install.bat

# Протестировать
"Привет, Вобуст"
"Открой лючок зарядки"

# Проверить логи
adb logcat | grep -i "intent\|receiver"
```

**Ожидаемые логи:**
```
I/CommandExecutor: Sending intent: pateo.dls.ivoka.vehicle.CONTROL
I/CommandExecutor: Intent sent successfully
```

---

## 📚 **ЕСЛИ НЕ РАБОТАЕТ**

**Следующий шаг:** Использовать Binder

**Нужно:**
1. Найти AIDL файлы QGSpeechService
2. Создать IVuiService.aidl
3. Использовать прямой вызов через Binder

**Но начнем с Broadcast - это проще!**

---

**Удачи! 🚀**
