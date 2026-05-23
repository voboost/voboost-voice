# QGBus Integration

**Дата:** 2026-05-23  
**Статус:** ✅ Реализовано  
**Цель:** Отображение toast-уведомлений через системную шину событий QGBus

---

## 🎯 Обзор

Система использует **QGBusService** для межпроцессного взаимодействия и распространения событий. Личный лаунчер (`com.qinggan.app.launcher`) имеет встроенный обработчик событий `showToast`, который отображает уведомления на экране.

### Проблема:
- Ранее использовался кастомный View overlay для Toast (свои анимации, стили)
- Не интегрирован с системной шиной событий

### Решение:
- Подключиться к `QGBusService` и отправлять события `showToast`
- Система сама отобразит уведомление через личный лаунчер
- Единый стиль уведомлений для всех приложений

---

## 🏗️ Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│              VoboostVoiceService                            │
└──────────────────┬──────────────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
   ┌────▼────┐          ┌────▼────┐
   │ QGBus   │          │ Toast   │
   │Service  │◄────────►│Messenger│
   │Manager  │          │Manager  │
   └─────────┘          └──────────┘
        │                     │
        ▼                     ▼
┌──────────────┐    ┌────────────────┐
│QGBusService  │    │ LauncherModel  │
│(system)      │◄──►│onHandleEvent() │
└──────────────┘    └────────────────┘
```

---

## 📁 Структура пакетов

```
ru.voboost.voice/
├── services/qgbus/
│   ├── QGBusServiceManager.kt  # Клиент для QGBusService
│   └── QGBusEvent.kt           # Локальная копия системного класса
│
└── ui/
    └── ToastMessengerManager.kt # Менеджер показа toast через QGBus
```

---

## 📋 Классы

### QGBusServiceManager

**Расположение:** `services/qgbus/QGBusServiceManager.kt` (294 строки)

**Назначение:** Управление подключением к QGBusService через Messenger IPC.

#### Методы:

| Метод | Описание |
|-------|----------|
| `connect()` | Подключение к сервису (bindService) |
| `disconnect()` | Отключение от сервиса |
| `isConnected()` | Проверка статуса подключения |
| `destroy()` | Полная очистка ресурсов |

#### Методы публикации:

| Метод | Описание |
|-------|----------|
| `showToast(message, durationMs, screenId)` | Отправить toast-событие |
| `publish(event: QGBusEvent)` | Опубликовать любое событие |
| `subscribe(eventTypes, handler)` | Подписаться на события |
| `unsubscribe(eventTypes)` | Отписаться от событий |

#### Пример использования:

```kotlin
// Инициализация
val qgbusServiceManager = QGBusServiceManager(context)
qgbusServiceManager.connect()

// Проверка подключения
if (qgbusServiceManager.isConnected()) {
    qgbusServiceManager.showToast("Привет, водитель!")
}

// Очистка при завершении
qgbusServiceManager.destroy()
```

#### События QGBus:

```kotlin
// Пример показа toast
val bundle = Bundle().apply {
    putString("package", context.packageName)
    putCharSequence("content", message)
    putInt("screenId", 0)        // 0=main screen, 1=second screen
    putLong("duration", 3000L)   // Short: 3000ms, Long: 5000ms
}

val event = QGBusEvent().apply {
    eventType = "showToast"
    source = context.packageName
    data = bundle
    destination = "com.qinggan.app.launcher"
    setSticky(false)
}
qgbusServiceManager.publish(event)
```

---

### ToastMessengerManager

**Расположение:** `ui/ToastMessengerManager.kt` (63 строки)

**Назначение:** Простой интерфейс для показа toast-уведомлений.

#### Методы:

| Метод | Описание |
|-------|----------|
| `show(message)` | Показать toast через QGBus (если подключен) |

#### Особенности:
- Автоматически проверяет подключение к QGBus
- Если не подключен — выводит warning в лог и пропускает
- Использует константы для Bundle ключей (KEY_PACKAGE, KEY_CONTENT, etc.)

#### Пример использования:

```kotlin
class CommandExecutor(...) {
    private val toastMessengerManager: ToastMessengerManager
    
    suspend fun executeCommand(commandData: CommandData) {
        try {
            // Выполнение команды
            val success = vehicleCommandExecutor.execute(...)
            
            if (success) {
                val phrase = getSuccessPhrase()
                
                // Голос + уведомление
                speechService.enqueueAsync(phrase)
                toastMessengerManager.show(phrase)  // ← через QGBus
            }
        } catch (e: Exception) {
            toastMessengerManager.show("Ошибка выполнения команды")
        }
    }
}
```

---

### QGBusEvent

**Расположение:** `services/qgbus/QGBusEvent.kt` (56 строк)

**Назначение:** Локальная копия системного `com.qinggan.bus.QGBusEvent`.

#### Поля:

| Поле | Тип | Описание |
|------|-----|----------|
| `eventType` | String? | Тип события ("showToast", "onICMReqCallModeChanged", etc.) |
| `needCache` | Int | Кэширование (0=нет, 1=да) |
| `source` | String? | Источник события (package name) |
| `destination` | String? | Назначение (пакет обработчика) |
| `priority` | Int | Приоритет (0=LOW, 1=NORMAL, 2=HIGH) |
| `data` | Bundle? | Данные события |

#### Примеры событий:

```kotlin
// Toast event
val toastEvent = QGBusEvent().apply {
    eventType = "showToast"
    source = context.packageName
    destination = "com.qinggan.app.launcher"
    data = bundleWithToastData
    setSticky(false)
}

// CAN bus event (для тестов)
val canEvent = QGBusEvent().apply {
    eventType = "onICMReqCallModeChanged"
    source = context.packageName
    destination = "com.qinggan.canbus.service"
    data = bundleWithCanData
    priority = QGBusEvent.EVENT_NORMAL_PRIORITY
}
```

---

## 🔧 Интеграция в VoboostVoiceService

```kotlin
class VoboostVoiceService : Service() {
    
    // 1. Объявление полей
    private lateinit var qgbusServiceManager: QGBusServiceManager
    private lateinit var toastMessengerManager: ToastMessengerManager
    
    override fun onCreate() {
        super.onCreate()
        
        // 2. Создание экземпляров
        qgbusServiceManager = QGBusServiceManager(this)
        toastMessengerManager = ToastMessengerManager(this, qgbusServiceManager)
        
        // 3. Подключение к QGBusService (автоматически при создании)
        qgbusServiceManager.connect()
        Log.i(TAG, "QGBusServiceManager connected")
        
        // 4. Передача менеджера в StateMachine
        val context = StateContext(
            soundEffectManager = soundEffectManager,
            recognitionService = recognitionService,
            voceAnimationManager = voceAnimationManager,
            speechService = speechService,
            configManager = configManager,
            nluEngine = nluEngine,
            commandExecutor = CommandExecutor(..., toastMessengerManager, ...)
        )
        
        stateMachine = StateMachine(serviceScope, context)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 5. Отключение при завершении
        qgbusServiceManager.destroy()
        Log.i(TAG, "QGBusServiceManager destroyed")
    }
}
```

---

## 🔄 Поток отправки уведомления

```
CommandExecutor.executeCommand()
        ↓
1. ToastMessengerManager.show(message)
   ↓
2. if (!qgbusServiceManager.isConnected()) → return (log warning)
   ↓
3. Bundle().apply {
     putString("package", componentName)
     putCharSequence("content", message)
     putInt("screenId", 0)
     putLong("duration", 3000L)
   }
   ↓
4. QGBusEvent().apply {
     eventType = "showToast"
     source = componentName
     data = bundle
     destination = "com.qinggan.app.launcher"
     setSticky(false)
   }
   ↓
5. qgbusServiceManager.publish(event)
   ↓
6. Message.obtain().apply {
     what = PUBLISH_EVENT
     obj = bundleWithEvent
   }.sendTo(qgbusMessenger)
   ↓
7. QGBusService.onTransact() receives event
   ↓
8. LauncherModel.onHandleEvent("showToast")
   ↓
9. Toast shown on screen!
```

---

## 📊 Логи

### Подключение к QGBus:

```
D/QGBusServiceManager: bindService result: true
I/QGBusServiceManager: onServiceConnected: ComponentInfo{com.qinggan.QGBus/com.qinggan.QGBus.QGBusService}
I/VoboostVoiceService: QGBusServiceManager connected
```

### Отправка toast:

```
D/QGBusServiceManager: Publishing showToast event
I/QGBusImpl: ##### onServiceConnected #####
D/QGBusService: publish event name: showToast, source: ru.voboost.voice
```

---

## 🧪 Тестирование

### 1. Проверка подключения:

```bash
adb shell am startservice ru.voboost.voice/.VoboostVoiceService

# Ожидаем логи:
I/QGBusServiceManager: onServiceConnected: ComponentInfo{com.qinggan.QGBus/...}
I/VoboostVoiceService: QGBusServiceManager connected
```

### 2. Тест уведомления:

Сказать команду с `show_notification: true` (например, "открой лючок зарядки")

```bash
adb logcat | grep -i "ToastMessenger"
```

**Ожидаемые логи:**
```
D/ToastMessengerManager: Toast shown via QGBus: Лючок зарядки открыт
D/QGBusServiceManager: Publishing showToast event
```

### 3. Проверка экрана:

На экране должен появиться Toast уведомление от системного лаунчера (то же что делает `QGToast.showToast()`)

---

## 📋 Сравнение с View overlay

| Параметр | View Overlay (старый) | QGBus (новый) |
|----------|----------------------|---------------|
| **Реализация** | Кастомный View в window manager | Системное событие |
| **Стиль** | Кастомный (toast_voice.xml) | Системный (launcher style) |
| **Производительность** | Низкая (создание View каждый раз) | Высокая (IPC) |
| **Интеграция** | Изолировано от системы | Полная интеграция |
| **Логика** | В приложении | В системе (launcher) |

---

## 🎯 Преимущества QGBus

1. ✅ **Единый стиль** — все уведомления выглядят одинаково
2. ✅ **Меньше ресурсов** — нет создания кастомных View
3. ✅ **Системная интеграция** — работает с системными настройками
4. ✅ **Надежность** — launcher обрабатывает события стабильно
5. ✅ **Простота** — 1 строка кода вместо 20+ для View overlay

---

## 📝 См. также

- [Общая архитектура](OVERVIEW.md)
- [State Machine V3](STATE_MACHINE_V3.md)
