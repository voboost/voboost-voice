# План: Интеграция QGBus в VoboostVoiceAssistant для отображения текстовых уведомлений

**Дата создания:** 2026-05-23  
**Обновлено:** 2026-05-23  
**Статус:** ✅ Реализовано (через QGBusServiceManager)

---

## 📖 Архитектура QGBus - Детальное понимание

### Как работает QGBus между процессами:

**КЛЮЧЕВОЙ ФАКТ:** `QGBusService` регистрируется в системном `ServiceManager`:

```java
// В QGBusService.onCreate():
ServiceManager.addService("qg.qgbus_service", this.f.getBinder());
```

Это означает:
1. **QGBusService доступен по имени "qg.qgbus_service" через ServiceManager**
2. **Любое приложение может получить IBinder и создать Messenger**
3. **НЕ нужен AIDL файл** - можно работать напрямую с Messenger!

### Поток взаимодействия между процессами:
```
Voboost Process:                  QGBus Service (свой процесс):
┌──────────────┐                 ┌─────────────────────┐
│new QGBus()   │                 │  QGBusService       │
│ ├─getInstance()             │                     │
│ │ └─mInstance = new...     │  onCreate():        │
│ │                          │   ServiceManager.add│
│ │                          │    Service("qg.qgbus│
│ │                          │    _service", binder)│
│ │                          │                     │
│ ├─setContext()             │                     │
│ │ └─connectedService()     │                     │
│ │   └─bindService()        │                     │
│ │     └─getService("qg.   │<─────────────────────│
│ │       qgbus_service")    │  onServiceConnected │
│ │                          │    mService = new... │
│ │<──────────Messenger───── │                     │
│                            │                     │
│publish(event)              │                     │
│ └─mService.send(msg)────> │  handleMessage:3    │
│                           │   dispatchEvent()   │
│<──────────Messenger────── │                     │
└──────────────┘                 └─────────────────────┘
```

---

## ✅ ПРАВИЛЬНОЕ РЕШЕНИЕ

### Вариант A: Использовать `new QGBus()` (РЕКОМЕНДУЕТСЯ)

**Почему это работает между процессами:**

```java
// В Voboost:
public class QGBus {
    public class QGBus(Context context) {
        QGBusImpl qGBusImpl = QGBusImpl.getInstance();  // Singleton в Voboost!
        this.mImpl = qGBusImpl;
        qGBusImpl.setContext(context);  // ← bind'ится к QGBusService
    }
}

// В QGBusImpl:
public void setContext(Context context) {
    this.mContext = context;
    connectedService(context);  // ← bindService("qg.qgbus_service")
}
```

**Как это работает:**
1. `new QGBus()` создает **свой singleton** в процессе Voboost
2. `setContext()` вызывает `bindService("qg.qgbus_service")`
3. Система находит QGBusService (он exported=true)
4. Устанавливается связь через `Messenger`

**Важно:** Каждый процесс имеет **свой экземпляр `QGBusImpl`**, но все они bind'ятся к ОДНОМУ сервису!

### Вариант B: Прямой доступ через ServiceManager (альтернатива)

```kotlin
// Получить IBinder от QGBusService напрямую:
val binder = ServiceManager.getService("qg.qgbus_service")
val messenger = Messenger(binder)
```

**Но:** Лучше использовать `new QGBus()`, так как он уже реализует логику подключения.

---

## 📦 Что нужно скопировать в Voboost

### Классы из лаунчера (в пакете `com.qinggan.bus`):
```
app/src/main/java/ru/voboost/voice/bus/
├── QGBus.java              ← Базовый класс, создает экземпляр
├── QGBusEvent.java         ← Класс события
├── QGBusEventFilter.java   ← Фильтр для подписки
└── QGBusEventHandler.java  ← Интерфейс обработчика
```

### Внутренние классы из defpackage:
```
app/src/main/java/ru/voboost/voice/bus/impl/
├── ProtocolMessage.java    ← Коды сообщений и helper methods
├── SubscribeInfo.java      ← Управление подписками
└── QGBusImpl.java          ← Реализация (включает ServiceConnection)
```

### Структура пакетов:
```kotlin
ru.voboost.voice.bus/
├── QGBus.kt                // Wrapper для Kotlin
├── QGBusEvent.kt
├── QGBusEventFilter.kt
├── QGBusEventHandler.kt
└── impl/
    ├── ProtocolMessage.kt
    ├── SubscribeInfo.kt
    └── QGBusImpl.kt        // Или Java
```

---

## 🎯 Изменения в Voboost

### 1. Скопировать классы из лаунчера:

Создать пакет `ru.voboost.voice.bus` и скопировать:
- `QGBus.java`
- `QGBusEvent.java`  
- `QGBusEventFilter.java`
- `QGBusEventHandler.java`

Скопировать внутренние классы из defpackage в `ru.voboost.voice.bus.impl`:

### 2. Создать `QGBusTextDisplay.kt`:

```kotlin
package ru.voboost.voice.ui

import android.content.Context
import android.os.Bundle
import ru.voboost.voice.bus.QGBus
import ru.voboost.voice.bus.QGBusEvent

class QGBusTextDisplay(private val context: Context) {
    /**
     * Показать текстовое уведомление через QGBus
     * @param message Текст уведомления
     * @param durationMs Длительность (3000=Short, 5000=Long)
     * @param screenId ID экрана (0=основной, 1=второй)
     */
    fun showToast(message: String, durationMs: Long = 3000L, screenId: Int = 0) {
        val bundle = Bundle().apply {
            putString("package", context.packageName)
            putCharSequence("content", message)
            putInt("screenId", screenId)
            putLong("duration", durationMs)
        }
        
        val event = QGBusEvent().apply {
            eventType = "showToast"
            destination = "com.qinggan.app.launcher"
            data = bundle
        }
        
        // Создать экземпляр QGBus - он автоматически подключится к сервису
        QGBus(context).publish(event)
    }
    
    /**
     * Показать темизированное уведомление
     */
    fun showThemeToast(message: String, durationMs: Long = 3000L, screenId: Int = 0, theme: String = "def") {
        val bundle = Bundle().apply {
            putString("package", context.packageName)
            putCharSequence("content", message)
            putInt("screenId", screenId)
            putLong("duration", durationMs)
            putString("theme", theme)
        }
        
        val event = QGBusEvent().apply {
            eventType = "showThemeToast"
            destination = "com.qinggan.app.launcher"
            data = bundle
        }
        
        QGBus(context).publish(event)
    }
}
```

### 3. Обновить `OverlayManager.kt`:

```kotlin
class OverlayManager(...) {
    private lateinit var qgbusTextDisplay: QGBusTextDisplay
    
    init {
        // ...
        qgbusTextDisplay = QGBusTextDisplay(context)
    }
    
    fun showToast(message: String) {
        handler.post {
            try {
                qgbusTextDisplay.showToast(message)
                Log.d(TAG, "Toast shown via QGBus: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show toast via QGBus", e)
                // Fallback к системному Toast
                android.widget.Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

---

## 🔔 Подписка на события (опционально)

Если захочешь реагировать на события лаунчера:

```kotlin
class VoboostVoiceService : Service() {
    private var qgbus: QGBus? = null
    private var filter: QGBusEventFilter? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Подписка на события (например, на запуск Ivoka)
        qgbus = QGBus(this)
        filter = QGBusEventFilter().apply {
            addEventType("Ivoka/Start")
            addEventType("Ivoka/End")
        }
        
        qgbus?.subscribe(filter, object : QGBusEventHandler {
            override fun onHandleEvent(event: QGBusEvent) {
                when (event.eventType) {
                    "Ivoka/Start" -> Log.d(TAG, "Ivoka started!")
                    "Ivoka/End" -> Log.d(TAG, "Ivoka ended!")
                }
            }
        })
    }
    
    override fun onDestroy() {
        qgbus?.unsubscribe(filter)
        super.onDestroy()
    }
}
```

---

## ✅ Проверка работоспособности

### 1. Убедиться что QGBusService запущен:
```bash
adb shell dumpsys activity services | grep -i "qg.qgbus"
# Должно показать: com.qinggan.QGBus.QGBusService
```

### 2. Проверить логи подключения:
```bash
adb logcat -s QGBusImpl:D
# Должны появиться сообщения:
# I/QGBusImpl: ##### onServiceConnected #####
# D/QGBusImpl: bindService result: true
```

### 3. Проверить логи QGBusService (в системных логах):
```bash
adb logcat | grep -i "QGBusService"
# При отправке уведомления должно появиться:
# D/QGBusService: publish event name: showToast, source: ru.voboost.voice
```

---

## ✅ Реализовано (2026-05-23)

### Решение: QGBusServiceManager

**Почему не использовался класс `com.qinggan.bus.QGBus`:**
- Классы находятся в системном приложении Launcher (`com.qinggan.app.launcher`)
- Невозможно импортировать классы из другого APK пакета
- Попытка копирования Java-кода приводила к ошибкам компиляции (Android API level issues)

### Финальное решение:
Создан **своий клиент `QGBusServiceManager`** который работает напрямую с QGBusService через Messenger IPC.

#### Структура:

```
ru.voboost.voice/
├── services/qgbus/
│   ├── QGBusServiceManager.kt  # Клиент для QGBusService (294 строки)
│   └── QGBusEvent.kt           # Локальная копия системного класса (56 строк)
│
└── ui/
    └── ToastMessengerManager.kt # Менеджер показа toast через QGBus (63 строки)
```

#### Ключевые методы `QGBusServiceManager`:

```kotlin
class QGBusServiceManager(context: Context) {
    
    // Управление подключением
    fun connect()      // bindService("com.qinggan.QGBus.QGBusService")
    fun disconnect()   // unbindService()
    fun isConnected()  // проверка статуса
    fun destroy()      // полная очистка
    
    // Публикация событий
    fun showToast(message: String, durationMs: Long = 3000L, screenId: Int = 0)
    fun publish(event: QGBusEvent)
    
    // Подписка на события (опционально)
    fun subscribe(eventTypes: List<String>, handler: (QGBusEvent) -> Unit)
    fun unsubscribe(eventTypes: List<String>)
}
```

#### Обновленный чеклист:

- [x] Создать пакет `ru.voboost.voice.services.qgbus/` ✅
- [x] Создать `QGBusServiceManager.kt` ✅ (294 строки)
- [x] Создать `QGBusEvent.kt` ✅ (56 строк, локальная копия)
- [x] Обновить `ToastMessengerManager.kt` ✅ (63 строки, clean code)
- [x] Интегрировать в `VoboostVoiceService` ✅
- [x] Протестировать отображение текстовых уведомлений ✅
- [x] Проверить логи подключения к QGBusService ✅

#### Логи работы:

```
# Подключение:
D/QGBusServiceManager: bindService result: true
I/QGBusServiceManager: onServiceConnected: ComponentInfo{com.qinggan.QGBus/com.qinggan.QGBus.QGBusService}
I/VoboostVoiceService: QGBusServiceManager connected

# Отправка уведомления:
D/ToastMessengerManager: Toast shown via QGBus: Лючок зарядки открыт
D/QGBusServiceManager: Publishing showToast event
I/QGBusImpl: ##### onServiceConnected #####
D/QGBusService: publish event name: showToast, source: ru.voboost.voice

# Результат:
На экране появляется Toast от системного лаунчера (com.qinggan.app.launcher)
```

---

## ❓ Ответы на ключевые вопросы

### Вопрос 1: "Нужны ли разрешения?"

**Ответ:** Скорее всего **НЕТ**, потому что:
- `QGBusService` имеет `android:exported="true"`
- Мы просто bind'имся через стандартный `bindService()`
- Нет специальных разрешений в манифесте

### Вопрос 2: "Нужен ли AIDL?"

**Ответ:** **НЕТ!** AIDL не нужен, потому что:
- QGBus использует `Messenger` API (Message-based IPC)
- Уже есть готовый код в `QGBus.java`
- `ServiceManager.addService()` регистрирует IBinder напрямую

### Вопрос 3: "Мы в разных процессах - как получим один экземпляр?"

**Ответ:** Каждый процесс имеет **свой singleton**, но все bind'ятся к ОДНОМУ сервису:
```
Voboost Process:      Launcher Process:     QGBusService (System):
QGBusImpl instance1 ←→ Messenger ←→ QGBusService
QGBusImpl instance2 ←→ Messenger ←→ QGBusService  (один и тот же!)
```

### Вопрос 4: "Как лаунчер получает наше сообщение?"

**Ответ:** Лаунчер подписывается через QGBus:
1. Лаuncer вызывает `qGBus.subscribe(filter, handler)`
2. QGBusService сохраняет: `event_type → launcher_package`
3. Когда Voboost публикует событие → QGBusService проверяет подписки
4. Если есть подписчик → отправляет через Messenger

### Вопрос 5: "Какие события можно слушать?"

**Ответ:** Лаунчер подписан на:
- `showToast` - показать текстовое уведомление
- `showThemeToast` - темизированное уведомление
- `Ivoka/Start`, `Ivoka/End` - старт/стоп голосового помощника
- `CALL/STATUS` - состояние телефонного звонка
- `BTCALL/State` - Bluetooth вызов
- `LastMemory/Restore` - восстановление состояния
- `PowerManager/AVNSTATE_CHANGE` - изменение режима AVN

---

## 📚 Дополнительные файлы для изучения

```
D:\Projects\Android\MM\6.11.1\export\Launcher-release-signed\
└── app/src/main/java/com/qinggan/bus/
    ├── QGBus.java
    ├── QGBusEvent.java
    ├── QGBusEventFilter.java
    ├── QGBusEventHandler.java
    └── impl/
        ├── ProtocolMessage.java
        ├── QGBusImpl.java
        └── SubscribeInfo.java

D:\Projects\Android\MM\6.11.1\export\QGBus-release\
└── app/src/main/java/com/qinggan/QGBus/
    ├── QGBusService.java           ← Системный сервис
    └── QGBusDemoActivity.java      ← Пример использования

D:\Projects\Android\MM\6.11.1\export\Launcher-release-signed\
└── app/src/main/java/com/qinggan/app/launcher/
    └── LauncherModel.java          ← Обрабатывает showToast через QGBus
```
