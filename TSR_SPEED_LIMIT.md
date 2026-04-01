# 🚸 TSR SPEED LIMIT — ПРЕДУПРЕЖДЕНИЯ О ПРЕВЫШЕНИИ СКОРОСТИ

**Добавлено:** 2026-04-01  
**Версия:** 13.2 (TSR Speed Limit Warning)

---

## 📋 ОБЗОР

Добавлена система голосовых предупреждений о превышении скорости на основе данных от камеры TSR (Traffic Sign Recognition).

**Компонент:** `TSRSpeedLimitHandler.kt`  
**Расположение:** `app/src/main/java/com/voboost/voiceassistant/canbus/TSRSpeedLimitHandler.kt`

---

## 🔧 КАК ЭТО РАБОТАЕТ

### 1. Источники данных о скорости

| Источник | VehicleState | Описание |
|----------|--------------|----------|
| **TSR Камера** | `TSR_SPEED_LIMIT` | Знаки ограничения с камеры |
| **Навигация** | `NAVI_SPEED_LIMIT` | Ограничение из карт |
| **Превышение (факт)** | `IPK_OVER_SPEED_WARNING` | Сигнал о превышении от авто |
| **Превышение (значение)** | `IPK_OVER_SPEED_VALUE` | На сколько км/ч превысили |

### 2. Логика работы

```
1. TSR камера считывает знак → 60 км/ч
   ↓
2. onVehicleStateChanged(TSR_SPEED_LIMIT, 60)
   ↓
3. TTS: "Ограничение скорости 60 километров в час"
   ↓
4. onVehicleSpeedChanged(75) ← Превышение на 15 км/ч
   ↓
5. TTS: "Превышение скорости на 15 километров в час"
```

---

## 📊 СОБЫТИЯ

### onVehicleStateChanged

| Событие | Параметр | Действие |
|---------|----------|----------|
| `TSR_SPEED_LIMIT` | limit (км/ч) | Голосовое: "Ограничение скорости {limit} км/ч" |
| `NAVI_SPEED_LIMIT` | limit (км/ч) | Сохранение лимита |
| `IPK_OVER_SPEED_WARNING` | 0/1 | Превышение: да/нет |
| `IPK_OVER_SPEED_VALUE` | value (км/ч) | Лог: "Превышение на X км/ч" |
| `TSR_SPEED_LIMIT_UNIT` | 0=км/ч, 1=mph | Единицы измерения |
| `ISA_ISLC_OVER_SPEED_WARNING_SWITCH` | 0/1 | Вкл/выкл ISA |

### onVehicleSpeedChanged

| Скорость | Лимит | Действие |
|----------|-------|----------|
| 75 км/ч | 60 км/ч | ⚠️ TTS: "Превышение на 15 км/ч" |
| 62 км/ч | 60 км/ч | ✅ Норма (порог 5 км/ч) |
| 58 км/ч | 60 км/ч | ✅ Норма |

---

## 🎯 ГОЛОСОВЫЕ ПРЕДУПРЕЖДЕНИЯ

| Событие | TTS Фраза |
|---------|-----------|
| Новый лимит TSR | "Ограничение скорости {N} километров в час" |
| Превышение > 5 км/ч | "Превышение скорости на {N} километров в час" |
| Превышение (общее) | "Превышение скорости" |

---

## ⚙️ НАСТРОЙКИ

### Порог срабатывания

```kotlin
// Превышение более чем на 5 км/ч
if (diff > 5 && !isWarningPlayed) {
    ttsCallback.playWarning(...)
}
```

### Гистерезис (защита от повторных срабатываний)

```kotlin
// Сброс если скорость снизилась до нормы + 3 км/ч
if (diff <= 3 && isWarningPlayed) {
    isWarningPlayed = false
}
```

### Отключение ISA (Intelligent Speed Assistance)

```kotlin
// Если ISA выключен — предупреждений не будет
if (!isaWarningEnabled) return
```

---

## 📝 ПРИМЕР ИСПОЛЬЗОВАНИЯ

### В VoboostVoiceService.kt

```kotlin
// Инициализация
tsrSpeedLimitHandler = TSRSpeedLimitHandler(canBusManager, object : TTSCallback {
    override fun playWarning(text: String) {
        serviceScope.launch {
            Log.i(TAG, "🔊 TSR Warning: $text")
            ttsEngine.speak(text)
        }
    }
})

tsrSpeedLimitHandler?.register()
```

### Отписка (onDestroy)

```kotlin
tsrSpeedLimitHandler?.unregister()
tsrSpeedLimitHandler = null
```

---

## 🔍 ЛОГИРОВАНИЕ

```
🚸 TSR_SPEED_LIMIT: 60 км/ч
🔊 TSR Warning: Ограничение скорости 60 километров в час
🚗 Скорость: 75 км/ч
⚠️ ПРЕВЫШЕНИЕ на 15 км/ч! (75/60)
🔊 TSR Warning: Превышение скорости на 15 километров в час
```

---

## 🛠️ API

### Методы

| Метод | Возвращает | Описание |
|-------|------------|----------|
| `getCurrentSpeedLimit()` | Int | Текущий лимит скорости |
| `getCurrentSpeed()` | Int | Текущая скорость авто |
| `isISAWarningEnabled()` | Boolean | Активно ли ISA |

### Интерфейс TTSCallback

```kotlin
interface TTSCallback {
    fun playWarning(text: String)
}
```

---

## 📺 ДИАГРАММА

```
┌─────────────────────────────────────────────────────────┐
│           TSRSpeedLimitHandler                          │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │        CanBusListener (callback)                │   │
│  │                                                 │   │
│  │  onVehicleStateChanged()                        │   │
│  │    ├─ TSR_SPEED_LIMIT → TTS "Лимит: N"         │   │
│  │    ├─ NAVI_SPEED_LIMIT → save limit            │   │
│  │    ├─ IPK_OVER_SPEED_WARNING → flag            │   │
│  │    └─ ISA_ISLC_OVER_SPEED_WARNING_SWITCH       │   │
│  │                                                 │   │
│  │  onVehicleSpeedChanged()                        │   │
│  │    └─ checkSpeedLimit()                         │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │        checkSpeedLimit()                        │   │
│  │                                                 │   │
│  │  if (speed - limit > 5 && !played)             │   │
│  │    → TTS "Превышение на N км/ч"                │   │
│  │    → played = true                              │   │
│  │                                                 │   │
│  │  if (speed - limit <= 3 && played)             │   │
│  │    → played = false                             │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
                ┌───────────────────────┐
                │   TTS Engine (Sherpa) │
                │   "Ограничение..."    │
                └───────────────────────┘
```

---

## ⚠️ ВАЖНЫЕ ЗАМЕЧАНИЯ

1. **Порог 5 км/ч** — предупреждение срабатывает только при превышении более чем на 5 км/ч
2. **Гистерезис 3 км/ч** — для сброса предупреждения нужно снизить скорость на 3 км/ч ниже порога
3. **ISA переключатель** — если ISA выключен, предупреждения не воспроизводятся
4. **TTS приоритет** — предупреждения могут прерывать текущую речь

---

## 🔄 МОДИФИКАЦИЯ

### Изменить порог срабатывания

```kotlin
// Было: 5 км/ч
if (diff > 5 && !isWarningPlayed) {

// Стало: 10 км/ч
if (diff > 10 && !isWarningPlayed) {
```

### Изменить гистерезис

```kotlin
// Было: 3 км/ч
if (diff <= 3 && isWarningPlayed) {

// Стало: 5 км/ч
if (diff <= 5 && isWarningPlayed) {
```

### Отключить голосовые предупреждения

```kotlin
override fun playWarning(text: String) {
    // Просто логируем, не говорим
    Log.i(TAG, "TSR Warning: $text")
}
```

---

## 📊 СТАТИСТИКА

| Параметр | Значение |
|----------|----------|
| **Порог срабатывания** | 5 км/ч |
| **Гистерезис** | 3 км/ч |
| **Источники** | TSR, NAVI, IPK |
| **TTS движок** | Sherpa (русский) |
| **Классов** | 1 (TSRSpeedLimitHandler) |
| **Строк кода** | ~170 |

---

**Готово к использованию! 🚀**
