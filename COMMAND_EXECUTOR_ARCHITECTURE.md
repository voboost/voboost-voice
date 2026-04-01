# 🏗️ Архитектура выполнения команд автомобилю

**Дата:** 2026-03-28  
**Версия:** 2.0  
**Статус:** ✅ Готово к использованию

---

## 📋 Обзор

Рефакторинг системы выполнения команд с использованием паттерна **Strategy**. Позволяет легко переключаться между различными способами отправки команд автомобилю без изменения основной логики.

---

## 🎯 Проблема

**До рефакторинга:**
- `CommandExecutor` напрямую отправлял Broadcast Intent
- Невозможно быстро переключиться на альтернативный метод (например, CAN-шины)
- Тестирование различных подходов требовало переписывания кода

**После рефакторинга:**
- Интерфейс `VehicleCommandExecutor` определяет контракт
- Реализации: `Intent`, `Shell`, `Auto`
- Легко добавить новый метод (например, прямой CAN через JNI)

---

## 🏛️ Архитектура

```
┌─────────────────────────────────────────────────────────┐
│              CommandExecutor                            │
│  (основная логика: подтверждения, фразы, overlay)       │
└────────────────────┬────────────────────────────────────┘
                     │ использует
                     ▼
┌─────────────────────────────────────────────────────────┐
│         VehicleCommandExecutor (интерфейс)              │
│  + execute(target, classify, command, params): Boolean  │
│  + executePhoneCommand(...): Boolean                    │
│  + executionMethod: String                              │
└───────────────┬─────────────────────────────────────────┘
                │ реализуют
    ┌───────────┼───────────┬─────────────────┐
    │           │           │                 │
    ▼           ▼           ▼                 ▼
┌────────┐  ┌────────┐  ┌────────┐    ┌──────────────┐
│ Intent │  │ Shell  │  │  Auto  │    │ Future: CAN  │
│ Executor│  │Executor│  │Executor│    │  over JNI    │
└────────┘  └────────┘  └────────┘    └──────────────┘
```

---

## 📁 Структура файлов

```
app/src/main/java/com/voboost/voiceassistant/executor/
│
├── VehicleCommandExecutor.kt           # ← Интерфейс
├── IntentVehicleCommandExecutor.kt     # ← Broadcast Intent
├── ShellVehicleCommandExecutor.kt      # ← Shell CAN-команды
├── AutoVehicleCommandExecutor.kt       # ← Auto (Intent → Shell)
├── VehicleCommandExecutorFactory.kt    # ← Фабрика
└── CommandExecutor.kt                  # ← Основная логика (обновлён)
```

---

## 🔧 Реализации

### 1. IntentVehicleCommandExecutor

**Метод:** Broadcast Intent  
**Получатель:** Системный сервис (BluetoothPhone/VuiServiceMgr)  
**Требования:** Стандартные разрешения Android

```kotlin
val executor = IntentVehicleCommandExecutor(context)

executor.execute(
    target = "Chargport",
    classify = 35,
    command = 1,  // 1 = открыть
    params = mapOf("location" to "all")
)
```

**Плюсы:**
- ✅ Работает без root-прав
- ✅ Стандартный Android API
- ✅ Интеграция с системным сервисом

**Минусы:**
- ❌ Требует получателя Intent
- ❌ Может не работать на некоторых прошивках

---

### 2. ShellVehicleCommandExecutor

**Метод:** Shell-команды (прямой CAN)  
**Получатель:** `qg.canbus` сервис  
**Требования:** Системные привилегии или root

```kotlin
val executor = ShellVehicleCommandExecutor()

executor.execute(
    target = "Chargport",
    classify = 35,
    command = 1,
    params = emptyMap()
)
```

**Команды:**
```bash
# Стандартная CAN команда
service call qg.canbus 58 i32 50 i32 <vehicle_state> i32 <value>

# Для режимов (через файл)
echo <value> > /sdcard/Download/myvoyah/files/drive_mode.txt
```

**Плюсы:**
- ✅ Прямой доступ к CAN
- ✅ Не требует системных сервисов
- ✅ Работает на уровне прошивки

**Минусы:**
- ❌ Требует root или системных привилегий
- ❌ Медленнее чем Intent
- ❌ Зависит от наличия `service` в PATH

---

### 3. AutoVehicleCommandExecutor

**Метод:** Автоматический выбор (Intent → Shell fallback)  
**Логика:**
1. Попытка выполнить через Intent
2. После 3 неудач — переключение на Shell
3. Телефонные команды только через Intent

```kotlin
val executor = AutoVehicleCommandExecutor(context)

executor.execute(...)  // Автоматически выберет метод
```

**Плюсы:**
- ✅ Лучшее из обоих миров
- ✅ Автоматический fallback
- ✅ Не требует вмешательства пользователя

**Минусы:**
- ⚠️ Первые команды могут не выполниться (до fallback)

---

## 🎮 Использование

### В VoboostVoiceService

```kotlin
// В onCreate():
val vehicleCommandExecutor = VehicleCommandExecutorFactory.createFromString(
    context = this,
    modeString = "auto"  // "intent", "shell", "auto"
)

commandExecutor = CommandExecutor(
    context = this,
    ttsEngine = ttsEngine,
    nluEngine = nluEngine,
    overlayManager = overlayManager,
    coroutineScope = serviceScope,
    vehicleCommandExecutor = vehicleCommandExecutor
)
```

### Настройка режима

**Вариант 1: Хардкод в сервисе**
```kotlin
modeString = "intent"  // или "shell", "auto"
```

**Вариант 2: Из config.json**
```json
{
  "vehicle": {
    "command_executor": "auto"
  }
}
```

**Вариант 3: Через BuildConfig**
```kotlin
modeString = BuildConfig.VEHICLE_COMMAND_MODE
```

---

## 📊 Сравнение методов

| Метод | Требования | Скорость | Надёжность | Рекомендация |
|-------|------------|----------|------------|--------------|
| **Intent** | Стандартные | ⚡⚡⚡ | ⭐⭐⭐ | По умолчанию |
| **Shell** | Root/System | ⚡⚡ | ⭐⭐⭐⭐ | Если Intent не работает |
| **Auto** | Зависит | ⚡⚡ | ⭐⭐⭐⭐⭐ | **Рекомендуется** |

---

## 🧪 Тестирование

### 1. Проверка текущего режима

```bash
adb shell am startservice com.voboost.voiceassistant/.VoboostVoiceService
adb logcat | grep "VehicleCommandFactory"
```

**Ожидаемый лог:**
```
I/VehicleCommandFactory: Creating AutoVehicleCommandExecutor (Intent with Shell fallback)
I/CommandExecutor: Initialized with execution method: Intent (auto)
```

### 2. Тест команд

```bash
# Лючок зарядки
adb shell am broadcast -a "pateo.dls.ivoka.vehicle.CONTROL" \
  --es "voice.param.vehicle.target" "Chargport" \
  --ei "voice.param.vehicle.classify" 35 \
  --ei "voice.param.vehicle.command" 1

# Проверка логов
adb logcat | grep -E "IntentVehicleCommand|ShellVehicleCommand|AutoVehicleCommand"
```

### 3. Переключение режима

Измените в `VoboostVoiceService.kt`:
```kotlin
modeString = "shell"  // Принудительно Shell
```

Пересоберите и проверьте логи.

---

## 🔍 Отладка

### Логи IntentExecutor

```
D/IntentVehicleCommand: Sending intent: target=Chargport, classify=35, command=1
I/IntentVehicleCommand: Intent sent successfully
```

### Логи ShellExecutor

```
D/ShellVehicleCommand: Executing shell command: target=Chargport, classify=35, command=1
D/ShellVehicleCommand: Executing: service call qg.canbus 58 i32 50 i32 IVI_CHRG_PORT_CAP i32 2
I/ShellVehicleCommand: Shell command executed successfully
```

### Логи AutoExecutor

```
D/AutoVehicleCommand: Attempting Intent execution
D/IntentVehicleCommand: Sending intent...
W/AutoVehicleCommand: Intent failed (count: 1/3)
D/AutoVehicleCommand: Falling back to Shell execution
```

---

## 🚀 Расширение

### Добавление новой реализации

1. Создайте класс:
```kotlin
class CanOverJniVehicleCommandExecutor : VehicleCommandExecutor {
    override val executionMethod = "CAN over JNI"
    
    override fun execute(...) = true
    override fun executePhoneCommand(...) = false
}
```

2. Добавьте в фабрику:
```kotlin
enum class ExecutionMode {
    INTENT, SHELL, AUTO, CAN_JNI  // ← Новый
}
```

3. Обновите `create()`:
```kotlin
ExecutionMode.CAN_JNI -> CanOverJniVehicleCommandExecutor()
```

---

## ⚠️ Известные ограничения

### ShellExecutor

1. **Root-права:** Требуется доступ к `service` команде
2. **Формат:** VehicleState должен быть строкой (не кодом)
3. **Режимы:** Переключаются через запись в файл

### IntentExecutor

1. **Получатель:** Требуется системный сервис
2. **Формат:** Extra должны быть с префиксом `voice.param.vehicle.*`

---

## 📝 Чек-лист готовности

- [x] Интерфейс `VehicleCommandExecutor` создан
- [x] Реализация `IntentVehicleCommandExecutor` готова
- [x] Реализация `ShellVehicleCommandExecutor` готова
- [x] Реализация `AutoVehicleCommandExecutor` готова
- [x] Фабрика `VehicleCommandExecutorFactory` готова
- [x] `CommandExecutor` обновлён
- [x] `VoboostVoiceService` обновлён
- [ ] Тесты на устройстве
- [ ] Документация обновлена

---

## 🎯 Рекомендации

### Для тестирования

1. **Начните с `Auto` режима** — автоматически подберёт рабочий метод
2. **Проверьте логи** — определите, какой метод работает
3. **Зафиксируйте режим** — используйте рабочий метод напрямую

### Для продакшена

1. **Используйте `Auto`** — максимальная надёжность
2. **Добавьте логирование** — для отладки на устройстве
3. **Настройте fallback** — под конкретную прошивку

---

## 📞 Контакты

**Вопросы:** См. документацию в папке `docs/`  
**Проблемы:** Проверяйте логи `adb logcat | grep -i "vehiclecommand"`

---

**Удачи с интеграцией! 🚀**
