# QGBus Messages Complete Guide - Voboost Voice Assistant

## Overview

В системе используется **QGBus** для межпроцессного взаимодействия между нативными приложениями. Все сообщения передаются через `QGBusEvent` с типом события и данными в Bundle.

---

## 📦 Базовая структура QGBusEvent

### Отправка сообщения:
```kotlin
val qGBus = QGBus(context)
val event = QGBusEvent().apply {
    eventType = "MyApp/MESSAGE_TYPE"
    destination = "com.qinggan.app.launcher"  // опционально
    sticky = true  // опционально, для сохранения последнего значения
    data = Bundle().apply {
        putString("key", "value")
        putInt("number", 123)
    }
}
qGBus.publish(event)
```

### Получение сообщения:
```kotlin
val handler = QGBusEventHandler { event ->
    when (event.eventType) {
        "MyApp/MESSAGE_TYPE" -> handleMyMessage(event.data)
    }
}

val filter = QGBusEventFilter().apply {
    addEventType("MyApp/MESSAGE_TYPE")
}
qGBus.subscribe(filter, handler)
```

---

## 📋 Полный список типов событий

### 1. 📱 PHONE / CALL EVENTS (Телефонные звонки)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `PHONE/ANSWER` | Ответ на вызов | - |
| `PHONE/DIAL` | Набор номера | `phoneNumber`, `contactName` |
| `PHONE/HANGUP` | Завершение вызова | - |
| `PHONE/CALL_CHANNEL` | Канал вызова | `channelId` |
| `PHONE/MUTE_MODE` | Режим глушения микрофона | `isMuted: boolean` |

**Использование в Voboost:**
- Можно **принимать/отклонять звонки** через голосовые команды
- Можно **набирать номера** по имени контакта (если известен)

---

### 2. 🔊 MEDIA EVENTS (Медиаплеер)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `Media/PLAY` | Управление воспроизведением | `action: String` (play/pause/next/prev) |
| `Media/STATE` | Состояние плеера | `type`, `package`, `state: int` |
| `Media/CONTROL` | Управление медиа | - |
| `Media/INFO` | Информация о треке | `title`, `artist`, `album` |
| `Media/PROGRESS` | Прогресс воспроизведения | `length`, `curTime` |
| `Media/ALBUMPIC` | Обложка альбома | `url: String` |

**Использование в Voboost:**
- ✅ **Включить/остановить музыку**: "Включи музыку", "Пауза"
- ✅ **Следующая/предыдущая песня**: "Следующий трек"
- ✅ **Громкость**: Уже работает через системные кнопки

---

### 3. 🌡️ AIR CONTROL (Климат)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `AirControl/ShowState` | Показать/скрыть панель климата | `viewState: int` (0=hide, 1=show) |

**Константы:**
```java
QGBusEventType.AIRCONTROL_INFO_HIDE = 0
QGBusEventType.AIRCONTROL_INFO_SHOW = 1
```

**Использование в Voboost:**
- ✅ **Уже интегрировано** через `voice.param.air.target/classify/command`
- Можно **добавить управление** через QGBus напрямую

---

### 4. 🌤️ WEATHER (Погода)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `Message/WeatherChange` | Изменение погоды | `weatherCode: String` |

**Пример отправки (из WeatherManager.java):**
```java
QGBusEvent event = new QGBusEvent();
event.setEventType("Message/WeatherChange");
Bundle bundle = new Bundle();
bundle.putString("weatherCode", "100");  // код погоды
event.setData(bundle);
qGBus.publish(event);
```

**Использование в Voboost:**
- Можно **запрашивать погоду**: "Какая сейчас погода?"
- Можно **синхронизировать погоду** между приложениями

---

### 5. 💬 WECHAT / IVOKA (WeChat сообщения)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `WeChat/NEWMSG` | Новое сообщение WeChat | `contact`, `message`, `fromCookies`, `myCookies` |
| `WeChat/REPLYMSG` | Отправка ответа | `contact`, `message` |
| `WeChat/IVOKAMSG` | Сообщение от Ivoka | - |

**Использование в Voboost:**
- ❌ **Сложно реализовать** (требует WeChat API)
- Можно **прочитать статус** WeChat (есть ли новые сообщения)

---

### 6. 📡 BT CALL (Bluetooth вызовы)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `BTCALL/State` | Состояние Bluetooth вызова | `state: int` |
| `BTCallPrivate` | Приватный вызов | - |
| `BTCallPrivate/State` | Состояние приватного вызова | `isInCall: boolean` |

**Состояния Bluetooth вызова (из BluetoothPhone):**
- IDLE = 0
- RINGING = 1  
- OFFHOOK = 2

**Использование в Voboost:**
- ✅ **Уже работает** через `AudioPolicyService.isInCall()`
- Можно **дополнительно проверять** Bluetooth вызовы

---

### 7. 📡 TUNER (Радио)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `Tuner/ScanPlay` | Автоподбор станций | - |
| `Tuner/ScanPlayStatus` | Статус автоподбора | `status: int` |

**Константы сканирования:**
```java
TYPE_SCAN_AUTO = 1          // Автоподбор включён
TYPE_SCAN_START = 2         // Начало сканирования
TYPE_SCAN_STOP = 3          // Стоп сканирования
TYPE_SCAN_AUTO_STOP = 4     // Автоподбор завершён
```

**Использование в Voboost:**
- Можно **управлять радио**: "Включи радио", "Следующая станция"
- Можно **изменять band**: AM/FM

---

### 8. 🧭 NAVI (Навигация)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `Navi/NaviInfo` | Информация о навигации | `destination`, `distance`, `eta` |
| `Navi/NavlCalcIfno` | Расчёт маршрута | - |
| `Navi/NavlLaneIfno` | Данные о полосах | - |

**Использование в Voboost:**
- Можно **запрашивать навигацию**: "Проложи маршрут до дома"
- Можно **читать расстояние до пункта назначения**

---

### 9. 🚗 VEHICLE CONTROL (Управление автомобилем)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `System/ReverseCamera` | Задний вид | `state: int` (0=exit, 1=enter) |

**Константы:**
```java
REVERSE_CAMERA_STATE_EXIT = 0
REVERSE_CAMERA_STATE_ENTER = 1
```

**Использование в Voboost:**
- Можно **активировать задний вид**: "Включи заднюю камеру"

---

### 10. 🎛️ SYSTEM EVENTS (Системные события)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `ActivityManager/ActivityChange` | Смена активности | `activity: String`, `state: String` |
| `App/Foreground` | Приложение на переднем плане | `package: String` |
| `App/Background` | Приложение в фоне | `package: String` |
| `App/Exit` | Выход из приложения | `package: String` |
| `bootanimOver` | Завершение загрузки | - |
| `System/Network` | Состояние сети | `type: String`, `isConnected: boolean` |

**Использование в Voboost:**
- Можно **следить за состоянием приложений**
- Можно **реагировать на статус сети**

---

### 11. 💡 UI EVENTS (UI уведомления)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `showToast` | Показать toast | `content: String`, `screenId: int`, `duration: long`, `package: String` |
| `showThemeToast` | Тематический toast | `content`, `screenId`, `duration`, `theme: String` |
| `anchor_visible` | Видимость якоря | `visible: boolean`, `type: int` |
| `navigation_bar_visibility` | Панель навигации | `visible: boolean`, `type: int` |

**Параметры showToast (из QGToast.java):**
```java
screenId: 1 = основной экран, 2 = второй экран
duration: 3000 = short, 5000 = long
```

**Использование в Voboost:**
- ✅ **Уже интегрировано** через `QGBusServiceManager`
- Можно **отправлять пользовательские сообщения**

---

### 12. 🔋 POWER EVENTS (Энергосбережение)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `PowerManager/AVNStateChange` | Смена состояния AVN | `kState: String` |
| `PowerManager/BTPhonePrivacyMode` | Приватный режим телефона | `isPrivate: boolean` |
| `ScreenAutoLift` | Автоподъём экрана | - |
| `ScreenAutoLift/State` | Состояние автоподъёма | `state: int` |

**Константы ScreenAutoLift:**
```java
STATE_SCREEN_AUTOLIFT_RESERVED = 0   // Зарезервировано
STATE_SCREEN_AUTOLIFT_ACTIVE = 1     // Активен
STATE_SCREEN_AUTOLIFT_INACTIVE = 2   // Неактивен
STATE_SCREEN_AUTOLIFT_MOVING = 3     // В движении
```

---

### 13. 🎵 MEDIA PLAYMODE (Режим воспроизведения медиа)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `Media/PLAYMODE` | Изменение режима проигрывания | `playMode: int` |

**Возможные режимы (из qgmedia):**
- 0 = Sequential (по порядку)
- 1 = Single Loop (повтор одного трека)
- 2 = List Loop (повтор списка)
- 3 = Random (случайный порядок)

---

### 14. 🧠 IVOKA EVENTS (Голосовой помощник Ivoka)

| Событие | Назначение | Параметры |
|---------|-----------|-----------|
| `Ivoka/Start` | Запуск Ivoka | - |
| `Ivoka/End` | Завершение Ivoka | - |
| `Ivoka/Status` | Статус Ivoka | `status: int`, `sessionId: String` |

**Использование в Voboost:**
- Можно **синхронизировать состояние** с Ivoka
- Можно **реагировать на активацию Ivoka**

---

## 🔌 ПРИМЕРЫ ИНТЕГРАЦИИ

### Пример 1: Управление радио через голос

```kotlin
// Команда: "Включи радио"
fun enableRadio() {
    val qGBus = QGBus(context)
    val event = QGBusEvent().apply {
        eventType = "Tuner/ScanPlay"
        data = Bundle().apply {
            putInt("band", 1)  // FM = 1, AM = 0
            putInt("frequency", 985)  // 98.5 MHz
        }
    }
    qGBus.publish(event)
}

// Команда: "Следующая станция"
fun nextStation() {
    val qGBus = QGBus(context)
    val event = QGBusEvent().apply {
        eventType = "Tuner/ScanPlayStatus"
        data = Bundle().apply {
            putInt("action", 1)  // Next station
        }
    }
    qGBus.publish(event)
}
```

### Пример 2: Управление климатом

```kotlin
// Команда: "Включи кондиционер"
fun acOn() {
    val qGBus = QGBus(context)
    val event = QGBusEvent().apply {
        eventType = "AirControl/ShowState"
        sticky = true
        data = Bundle().apply {
            putInt("viewState", 1)  // SHOW
        }
    }
    qGBus.publish(event)
}

// Команда: "Установи температуру 22 градуса"
fun setTemperature(temp: Int) {
    // Для точного управления температурой нужен специфичный event
    // Текущая реализация через AirControl/ShowState
}
```

### Пример 3: Показать пользовательское сообщение

```kotlin
// Отправить toast на главный экран
fun showToast(message: String) {
    val qGBus = QGBus(context)
    val event = QGBusEvent().apply {
        eventType = "showToast"
        destination = "com.qinggan.app.launcher"
        data = Bundle().apply {
            putString("package", context.packageName)
            putCharSequence("content", message)
            putInt(QGBusConst.BundleExtra.EXTRA_SCREEN_ID, 1)  // Main screen
            putLong("duration", 3000L)  // Short duration
        }
    }
    qGBus.publish(event)
}
```

---

## 📌 КЛЮЧЕВЫЕ НАБЛЮДЕНИЯ ДЛЯ VOOBOOST

### ✅ Что уже работает:
1. **Toast notifications** — через `QGBusServiceManager` (интегрировано)
2. **Echo cancellation** — через `AudioPolicyService.isInCall()`
3. **Команды Ivoka** — через Intent с теми же action'ами

### 🔍 Что можно добавить:

| Функция | Событие | Пример |
|---------|---------|--------|
| Управление радио | `Tuner/ScanPlay` | "Включи 98 FM" |
| Управление плеером | `Media/PLAY` | "Включи музыку", "Пауза" |
| Смена режима проигрывания | `Media/PLAYMODE` | "Перемешать треки" |
| Управление климатом (расширенное) | `AirControl/*` | "Установи 22 градуса" |
| Погода | `Message/WeatherChange` | "Какая погода?" |
| Навигация | `Navi/NaviInfo` | "Проложи маршрут" |

---

## ⚠️ ВАЖНЫЕ ЗАМЕЧАНИЯ

1. **Структура Bundle** должна точно соответствовать ожидаемой (проверяй в декомпилированном коде)
2. **destination** — если не указан, событие рассылается всем подписчикам
3. **sticky = true** — сохраняет последнее значение для новых подписчиков
4. **Пакеты должны быть синхронизированы** — некоторые события требуют определённого порядка параметров

---

## 📚 ДОПОЛНИТЕЛЬНЫЕ ФАЙЛЫ ДЛЯ ИССЛЕДОВАНИЯ

```
export/BluetoothPhone-release-signed/app/src/main/java/com/qinggan/bus/QGBusEventType.java
export/Launcher-release-signed/app/src/main/java/com/qinggan/bus/QGBusEventType.java
export/qgmedia-release-signed/app/src/main/java/com/qinggan/bus/QGBusEventType.java
export/VehicleAir-release-signed/app/src/main/java/com/qinggan/bus/QGBusEventType.java

export/BluetoothPhone-release-signed/app/src/main/java/com/qinggan/wechat/
export/Launcher-release-signed/app/src/main/java/com/qinggan/wechat/
export/qgmedia-release-signed/app/src/main/java/com/qinggan/media/
export/VehicleAir-release-signed/app/src/main/java/com/qinggan/app/vehiclebase/
```

---

**Последнее обновление:** 24 мая 2026  
**Статус:** ✅ Полный анализ завершён
