# 🎤 SPEAKER ZONE DETECTION — ОПРЕДЕЛЕНИЕ ЗОНЫ ГОВОРЯЩЕГО

**Дата:** 2026-04-01  
**Версия:** 13.4 (Speaker Zone Detection)

---

## 📋 ОБЗОР

Добавлена возможность определения зоны говорящего (водитель или пассажир) через системный сервис автомобиля.

**Компонент:** `SpeakerZoneHandler.kt`  
**Расположение:** `app/src/main/java/com/voboost/voiceassistant/canbus/SpeakerZoneHandler.kt`

---

## 🔧 КАК ЭТО РАБОТАЕТ

### 1. Использование системного сервиса

Оригинальный сервис Ivoka использует `WakeupService` для определения направления голоса через микрофонный массив:

```
Микрофонный массив (2/4 mic)
        ↓
DSP (Beamforming, AEC, NR)
        ↓
WakeupService (системный сервис)
        ↓
ISpeakerDirectionHandler (callback)
        ↓
onSpeakerDirection(direction: Int)
```

### 2. Получение доступа к сервису

Используется `ServiceManager.getService()` через reflection:

```kotlin
// ServiceManager — скрытый API, нужен reflection
val getServiceMethod = Class.forName("android.os.ServiceManager")
    .getMethod("getService", String::class.java)

// Получить сервис
val serviceBinder: IBinder? = getServiceMethod.invoke(null, "com.qinggan.WakeupService") as? IBinder
```

### 3. Регистрация callback'а

```kotlin
// Создать обработчик
val directionHandler = object : ISpeakerDirectionHandler.Stub() {
    override fun onSpeakerDirection(direction: Int) {
        when (direction) {
            ZONE_DRIVER -> // Водитель
            ZONE_PASSENGER -> // Пассажир
            else -> // Не определено
        }
    }
}

// Зарегистрировать
wakeupService.addSpeakerDirectionListener(directionHandler)
```

---

## 📊 ЗОНЫ

| Зона | Код | Описание |
|------|-----|----------|
| `ZONE_UNKNOWN` | 0 | Не определено |
| `ZONE_DRIVER` | 1 | Водитель |
| `ZONE_PASSENGER` | 2 | Пассажир |

---

## 🎯 ПРИМЕР ИСПОЛЬЗОВАНИЯ

### В VoboostVoiceService

```kotlin
// Инициализация
speakerZoneHandler = SpeakerZoneHandler(canBusManager, object : ZoneCallback {
    override fun onZoneDetected(zone: Int) {
        when (zone) {
            SpeakerZoneHandler.ZONE_DRIVER -> {
                Log.i(TAG, "👨‍💼 Команда от ВОДИТЕЛЯ - полный доступ")
                // Разрешить все команды
            }
            SpeakerZoneHandler.ZONE_PASSENGER -> {
                Log.i(TAG, "👤 Команда от ПАССАЖИРА - ограниченный доступ")
                // Ограничить некоторые команды
            }
            else -> {
                Log.w(TAG, "⚠️ Зона не определена")
            }
        }
    }
})

// Регистрация
if (speakerZoneHandler?.register() == true) {
    Log.i(TAG, "Speaker zone handler registered")
} else {
    Log.w(TAG, "Failed to register (система не поддерживает)")
}
```

---

## 🔍 ПРОВЕРКА ПОДДЕРЖКИ

```kotlin
// Проверить поддерживает ли система определение зоны
if (speakerZoneHandler.isZoneDetectionSupported()) {
    Log.i(TAG, "✅ Zone detection supported")
} else {
    Log.w(TAG, "❌ Zone detection not available")
}
```

---

## 🚗 ОГРАНИЧЕНИЯ ДЛЯ ПАССАЖИРА

Как в оригинальном Ivoka, можно ограничить команды пассажира:

```kotlin
override fun onZoneDetected(zone: Int) {
    when (zone) {
        ZONE_DRIVER -> {
            // ✅ Полный доступ
            commandExecutor.setRestrictions(emptyList())
        }
        ZONE_PASSENGER -> {
            // ⚠️ Ограниченный доступ
            commandExecutor.setRestrictions(listOf(
                "window_open",
                "window_close",
                "fuel_tank_open",
                "charge_port_open"
            ))
        }
    }
}
```

---

## ⚠️ ВАЖНЫЕ ЗАМЕЧАНИЯ

### 1. Зависимость от автомобиля

**Работает только если:**
- ✅ В автомобиле есть микрофонный массив (2/4 mic)
- ✅ Установлен системный сервис `com.qinggan.WakeupService`
- ✅ Поддержка beamforming на уровне DSP

**Не работает если:**
- ❌ Один микрофон
- ❌ Другой производитель системы (не Qinggan)
- ❌ Сервис не запущен

### 2. Reflection для ServiceManager

`ServiceManager` — скрытый API, используется reflection:

```kotlin
// Может быть заблокировано в будущих версиях Android
val serviceManagerClass = Class.forName("android.os.ServiceManager")
```

### 3. AIDL интерфейс

Интерфейсы `IWakeupService` и `ISpeakerDirectionHandler` созданы вручную на основе оригинального AIDL:

```kotlin
// Встроенные AIDL интерфейсы
interface IWakeupService : android.os.IInterface {
    fun addSpeakerDirectionListener(handler: ISpeakerDirectionHandler)
    fun removeSpeakerDirectionListener(handler: ISpeakerDirectionHandler)
}
```

---

## 📝 ЛОГИРОВАНИЕ

```
D/SpeakerZoneHandler: Trying to register speaker direction listener...
I/SpeakerZoneHandler: ✅ WakeupService found!
I/SpeakerZoneHandler: ✅ Speaker direction listener registered successfully
I/SpeakerZoneHandler: 👨‍💼 Команда от ВОДИТЕЛЯ
```

Или если не поддерживается:

```
W/SpeakerZoneHandler: ⚠️ WakeupService not found (com.qinggan.WakeupService)
W/SpeakerZoneHandler: Система не поддерживает определение зоны говорящего
```

---

## 🔄 АЛЬТЕРНАТИВЫ

Если системный сервис недоступен:

### 1. CarAudioManager (если есть)

```kotlin
val car = Car.createCar(context)
val carAudioManager = car.getCarManager(Car.CAR_AUDIO_SERVICE) as? CarAudioManager
val zoneId = carAudioManager?.getZoneForContext()
```

### 2. Выбор микрофона

```kotlin
val audioFormat = AudioFormat.Builder()
    .setChannelMask(AudioFormat.CHANNEL_IN_FRONT)  // Передний
    // или
    .setChannelMask(AudioFormat.CHANNEL_IN_BACK)   // Задний
    .build()
```

### 3. Внешние библиотеки

- Google Cloud Speech-to-Text
- ReSpeaker beamforming

---

## 📊 СРАВНЕНИЕ

| Функция | Ivoka (оригинал) | Voboost (новый) |
|---------|------------------|-----------------|
| **Микрофонный массив** | ✅ 2/4 mic | ❌ Зависит от авто |
| **Beamforming** | ✅ Есть | ❌ Зависит от авто |
| **Определение зоны** | ✅ Driver/Passenger | ✅ Через системный сервис |
| **AIDL интерфейсы** | ✅ Встроенные | ✅ Ручная реализация |
| **Reflection** | ❌ Нет | ✅ Для ServiceManager |

---

## 🛠️ ИНТЕГРАЦИЯ

### Файлы

| Файл | Описание |
|------|----------|
| `SpeakerZoneHandler.kt` | Обработчик зоны |
| `ZoneCallback.kt` | Callback интерфейс |
| `IWakeupService.aidl` | AIDL интерфейс (встроен) |
| `ISpeakerDirectionHandler.aidl` | AIDL интерфейс (встроен) |

### Зависимости

```kotlin
// В build.gradle не нужно ничего добавлять
// Все интерфейсы реализованы вручную
```

---

## ✅ ЧТО ДЕЛАЕТ КОД

1. **Проверка поддержки** — есть ли системный сервис
2. **Получение сервиса** — через `ServiceManager.getService()`
3. **Регистрация callback'а** — `addSpeakerDirectionListener()`
4. **Обработка событий** — `onSpeakerDirection(direction)`
5. **Уведомление** — `ZoneCallback.onZoneDetected(zone)`

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

### Опционально:

1. **Добавить ограничения для пассажира** в `CommandExecutor`
2. **Сохранять статистику** — кто чаще даёт команды
3. **Настроить TTS** — разные голоса для водителя/пассажира
4. **Добавить визуализацию** — показывать зону в UI

---

**Готово! Система определяет кто говорит — водитель или пассажир! 🎉**
