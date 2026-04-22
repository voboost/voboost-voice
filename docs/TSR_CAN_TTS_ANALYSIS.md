# 📁 Структурированный отчёт: Анализ системы предупреждений, TSR и TTS в автомобильной IVI-системе
> 📝 Данный документ является сводной технической базой, собранной на основе вашего реверс-инжиниринга кода системы `qgmedia`/`VoboostVoiceAssistant`. Все данные, константы, архитектурные паттерны и шаги интеграции структурированы для удобного использования в разработке.

---

## 📌 Введение
Система построена на Android Automotive/IVI-платформе с использованием:
- **CAN-bus** для получения данных с ЭБУ и ADAS-модулей
- **Событийной модели** (без polling) для обработки изменений состояний
- **Стратегии + Фабрики** в TTS-подсистеме для переключения движков синтеза речи
- **AIDL/Parcelable** для межпроцессного взаимодействия
- **Локальных и облачных источников** данных (OSM, навигация, камера TSR)

---

## 🔌 1. CAN-шина и состояния автомобиля (`VehicleState`)

### 📊 Ключевые параметры из логов
| Параметр | Расшифровка | Ед. изм. | Пример значения | Примечание |
|----------|-------------|----------|-----------------|------------|
| `AVG_FUEL` | Средний расход топлива | л/100км | `-9999` | Заглушка/нет данных (гибрид/ЭВ/ДВС выкл) |
| `AVG_POWER` | Средняя мощность | кВт / % | `23` | ~31 л.с. — спокойный режим |
| `AVG_SPEED` | Средняя скорость | км/ч | `23` | Синхронизировано с одометром |
| `ODO_THISTIME` | Пробег за поездку | км | `17` | С момента старта/сброса |
| `TIME_THISTIME` | Длительность поездки | мин | `43` | Время в пути |
| `DMS_DRIVE_MOOD` | Стиль вождения (DMS) | код | `2→0` | `0`=Eco, `1`=Normal, `2`=Sport |
| `BMS_SOC_DISPLAY` | Заряд ВВБ | % | `67` | Гибрид/ЭВ |
| `ASC_LF/RF/LR/RR_HEIGHT` | Высота подвески по углам | мм/ед. | `22/25/-6/-6` | Отклонение от калибровочного `0` |

### 🛡️ Статусы ISA/ISLC (`ISA_ISLC_STATUS`)
| State | Binary | Интерпретация |
|-------|--------|---------------|
| `0` | `000` | Выключено / Standby |
| `1` | `001` | Включено, ожидание условий |
| `2` | `010` | **Активно** (появляется ≥30 км/ч, система взводится) |
| `3` | `011` | Активно + включено, знак не распознан |
| `5` | `101` | Знак валиден, но скорость ниже лимита |
| `7` | `111` | **Превышение**: включено + активно + знак валиден → ограничение |
| `11`| `1011`| `7` + предупреждение (плохая видимость/ошибка) |
| `15`| `1111`| Полная активность + override водителя |

> ✅ `state: 2` → система мониторинга активна.  
> ✅ `state: 7` → система вмешивается при превышении лимита, считанного камерой.

---

## 🚦 2. Система распознавания дорожных знаков (TSR)

### 📡 Источники данных
| Источник | Тип | Надёжность |
|----------|-----|------------|
| `TSR_SPEED_LIMIT` | CAN-событие | ⭐⭐⭐⭐⭐ (реальное время) |
| `TSR_SIGN_TYPE` | CAN-событие | ⭐⭐⭐⭐ (тип знака) |
| `ISA_ISLC_STATUS` | CAN-битовая маска | ⭐⭐⭐ (статус ограничения) |
| GPS + OSM/HERE | Оффлайн-база | ⭐⭐⭐⭐⭐ (резерв/гибрид) |

### 🧩 Коды типов знаков (`TSR_SIGN_TYPE`)
| Код | Значение |
|-----|----------|
| `0` | Нет знака |
| `1` | Ограничение скорости |
| `2` | Конец зоны ограничения |
| `3` | Запрет обгона |
| `4` | Конец запрета обгона |
| `5` | STOP |
| `6` | Уступи дорогу |
| `7` | Другой/специфичный знак |

### 🔧 Статусы работы TSR (`TSR_OPERATING_STATUS`)
| Код | Значение |
|-----|----------|
| `0` | OFF |
| `1` | FUSION_MODE (камера + навигация) |
| `2` | VISION_ONLY |
| `3` | NAVIGATION_ONLY |
| `4` | FAILURE |
| `5` | CAMERA_BLOCKED (грязь/снег) |
| `6` | PERMANENT_FAILURE |
| `7` | NOT_CONFIG |

### 📦 Архитектура обработки TSR
```
Камера ADAS → CAN-шина → CanBusService → TSREventHandler → TSRCallback → VoboostVoiceService → TTS/UI
```
- **Модель событий**: данные приходят только при изменении (`onVehicleStateChanged`)
- **Задержка**: 100–500 мс между распознаванием и событием
- **Важно**: Всегда проверяйте `isTsrAvailable()` перед использованием данных

---

## 📢 3. Архитектура сообщений и идентификаторы

### 🎯 `RealityWarningInfo`
- Содержит поле `mICUVehicleDisplaySpeed` (скорость от приборной панели/ICU)
- Метод `getTTSSpeekContentByID(int id)` возвращает строку для озвучки
- Константы:
  - `ICU_VEHICLE_DISPLAY_SPEED_SPEK_CONTENT = "速度过快请降低车速"`
  - `ICU_VEHICLE_DISPLAY_SPEED_SPEK_CONTENT_ID = 4104`

### 🔢 Двойное использование ID `4104`
| Контекст | Значение |
|----------|----------|
| `ParkingStartMessage` | `z=true` (парковка началась) + `z2=true` (автоматическая) → `4104` |
| `RealityWarningInfo` | ID голосового оповещения о скорости/парковке |
| `BaseMessage` | `SUB_TYPE_PARKING_START_4 = 4104` |

> ⚠️ **Риск**: Один ID используется в двух доменах (событие парковки + TTS-контент). Требуется изоляция контекстов в коде.

### 🗃️ Модели данных скорости
| Класс | Поля | Назначение |
|-------|------|------------|
| `SpeedLimitModel` | `mSpeedLimit`, `mbIsRoadBorder` | Текущий лимит, граница зоны |
| `CameraInfo` | `speedLimit`, `remainDistance`, `isOverSpeed` | Данные камер фиксации |
| `SpeedLimitInfo` | `type`, `speedLimit`, `distance` | Тип дороги/участка |
| `ForwardSafetyInfo` | `speedLimit`, `averageSpeed`, `isOverSpeed` | Безопасность впереди |

---

## 🗣️ 4. Архитектура TTS-сервиса (`com/qinggan/ttsservice`)

### 🏗️ Компоненты
| Компонент | Роль |
|-----------|------|
| `TtsService` | Android Service, очередь запросов, управление воспроизведением |
| `ImplFactory` | Фабрика выбора движка по настройкам (SQLite) |
| `AbstractTtsImpl` | Базовый интерфейс всех движков |
| `CacheTtsImpl` | Кэширование результатов синтеза |
| `QGTtsPcmDump` | Отладочная запись PCM в `/sdcard/` |

### 🌐 Поддерживаемые движки
| Папка | Движок | Особенности |
|-------|--------|-------------|
| `impl/baidu/` | Baidu TTS | Облачный, `BaiduUtil` |
| `impl/iflytek/` | iFLYTEK | Native-биндинг, `ITTSPlayListener` |
| `impl/microsoft/` | MS Speech SDK | v1.x, обработка ошибок |
| `impl/aispeech/` | AiSpeech | Авторизация, оффлайн-фолбэк |
| `impl/arklite/` | ArkLite/Pateo | Собственный движок Geely/Pateo |
| `impl/xiaoice/` | XiaoIce | Microsoft-совместимый, кастомный |
| `impl/localaudio/` | Локальные файлы | `AudioTrack`/`MediaPlayer`, offline |
| `impl/combine/` | Гибриды | Nuance → XiaoIce и т.п. |

### 🔧 Точки модификации
- `ImplFactory.java` → смена движка по умолчанию
- `TtsService.java` → логирование, хуки, приоритеты очереди
- `CacheTtsImpl.java` → управление кэшем
- `util/Config.java` → флаги `LOGD`, `DEBUG_PCM`

---

## 🛠️ 5. Интеграция в `VoboostVoiceAssistant`

### 📁 Необходимые файлы
```
app/src/main/java/ru/voboost/voiceassistant/canbus/
├── TSRCallback.kt          # Интерфейс обратных вызовов
├── TSREventHandler.kt      # Обработчик событий CAN
└── VoboostVoiceService.kt  # Главный сервис (обновить)
```

### 🔌 Шаг 1: `TSRCallback.kt`
```kotlin
interface TSRCallback {
    fun onSpeedLimitChanged(limit: Int)
    fun onSignTypeChanged(signType: Int)
    fun onTsrWarning()
    fun onOperatingStatusChanged(status: Int, available: Boolean)
}
```

### 📡 Шаг 2: `TSREventHandler.kt` (ключевые блоки)
```kotlin
class TSREventHandler(
    private val canBusManager: CanBusServiceManager,
    private val callback: TSRCallback? = null
) {
    private val listener = object : CanBusListener() {
        override fun onVehicleStateChanged(vehicle: VehicleState, IState: Int) {
            when (vehicle) {
                VehicleState.TSR_SPEED_LIMIT -> {
                    callback?.onSpeedLimitChanged(IState)
                }
                VehicleState.TSR_SIGN_TYPE -> {
                    callback?.onSignTypeChanged(IState)
                }
                VehicleState.TSR_OPERATING_STATUS -> {
                    val available = IState !in listOf(4,5,6,7)
                    callback?.onOperatingStatusChanged(IState, available)
                }
                // ...
            }
        }
    }

    fun register() = canBusManager.registerCallback(listener)
    fun unregister() = canBusManager.unregisterCallback(listener)
    fun getCurrentSpeedLimit() = currentSpeedLimit
    fun isTsrAvailable() = isTsrAvailable
}
```

### ⚙️ Шаг 3: Интеграция в `VoboostVoiceService.kt`
```kotlin
// Поля
private var tsrEventHandler: TSREventHandler? = null

// В onCreate() / onServiceConnected()
tsrEventHandler = TSREventHandler(canBusManager, object : TSRCallback {
    override fun onSpeedLimitChanged(limit: Int) {
        Log.i(TAG, "🚸 TSR: Ограничение $limit км/ч")
        if (limit > 0) ttsEngine.speak("Ограничение скорости $limit километров в час")
    }
    override fun onSignTypeChanged(signType: Int) {
        when (signType) {
            5 -> ttsEngine.speak("Знак стоп")
            6 -> ttsEngine.speak("Уступи дорогу")
        }
    }
    override fun onTsrWarning() { ttsEngine.speak("Внимание! Дорожный знак") }
    override fun onOperatingStatusChanged(status: Int, available: Boolean) { /* ... */ }
})
tsrEventHandler?.register()

// В onDestroy()
tsrEventHandler?.unregister()
```

---

## 🔍 6. Отладка, логирование и тестирование

### 🖥️ Команды ADB
```bash
# Сборка и установка
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Логи TSR
adb logcat -s TSREventHandler
adb logcat -s VoboostVoiceService
adb logcat | grep -E "TSR|CanBus"

# Проверка разрешений
adb shell dumpsys package ru.voboost.voiceassistant | grep -A 30 "requested permissions"

# Принудительная выдача (если есть root/system)
adb shell pm grant ru.voboost.voiceassistant android.permission.CAR_POWER
adb shell appops set ru.voboost.voiceassistant MANAGE_EXTERNAL_STORAGE allow
```

### 📝 Пример вывода логов
```
I/TSREventHandler: ✅ TSREventHandler registered
I/TSREventHandler: 🚸 TSR_SPEED_LIMIT: 60 км/ч
I/TSREventHandler: 🚦 TSR_SIGN_TYPE: 1 (Ограничение скорости)
D/TSREventHandler: 🔧 TSR_OPERATING_STATUS: 1 (FUSION_MODE)
I/VoboostVoiceService: 🚸 TSR: Ограничение скорости 60 км/ч
```

---

## ⚠️ 7. Выявленные проблемы и рекомендации

| Проблема | Влияние | Решение |
|----------|---------|---------|
| `com.` vs `ru.` package mismatch | Privileged permissions не применяются | Унифицировать `applicationId` и `privapp-permissions.xml` |
| Дубликаты в `VehicleState` (напр. `923`, `924`) | `getVehicleState()` возвращает только первый сигнал | Переназначить ID, добавить валидацию |
| Опечатки в константах (`VEDIO`, `MANAUL`, `CONCERNTION`) | Снижает читаемость, риск ошибок при поиске | Стандартизировать именование, добавить Javadoc |
| Использование одного ID `4104` для парковки и TTS | Риск ложных срабатываний | Разделить домены или добавить контекстный префикс |
| Отсутствие runtime-запросов `RECORD_AUDIO`, `MANAGE_EXTERNAL_STORAGE` | TTS/логирование могут падать | Добавить `ActivityCompat.requestPermissions()` |
| `BOOT_COMPLETED` broadcast может не доходить | Автостарт сервиса не работает | Проверить `exported="true"`, корректный пакет, `quickboot` фильтр |

---

## ✅ 8. Чек-лист внедрения

- [ ] Создать `TSRCallback.kt` и `TSREventHandler.kt`
- [ ] Добавить импорты и поля в `VoboostVoiceService.kt`
- [ ] Инициализировать и зарегистрировать обработчик в `onCreate()`/`onServiceConnected()`
- [ ] Добавить отписку в `onDestroy()`
- [ ] Реализовать обработчики `handleSpeedLimitChange`, `handleSignTypeChange`, `playTsrWarning`
- [ ] Исправить mismatch пакетов (`com.` → `ru.`) в `AndroidManifest`, `build.gradle`, `privapp-permissions.xml`
- [ ] Добавить runtime-запросы разрешений
- [ ] Собрать проект: `./gradlew assembleDebug`
- [ ] Установить: `adb install -r ...`
- [ ] Протестировать на реальном авто с TSR-камерой
- [ ] Проверить логи: `adb logcat -s TSREventHandler`
- [ ] Добавить сохранение истории знаков (опционально)

---

## 📎 Приложения

### 🗃️ Справочник констант (`VehicleState`)
| Константа | Значение | Описание |
|-----------|----------|----------|
| `TSR_SPEED_LIMIT` | `280` | Текущий лимит скорости |
| `TSR_SIGN_TYPE` | `281` | Тип знака |
| `TSR_SPEED_LIMIT_UNIT` | `282` | 0=км/ч, 1=mph |
| `TSR_WARNING` | `283` | 1=предупреждение |
| `TSR_OPERATING_STATUS` | `284` | Статус системы |
| `ISA_ISLC_OVER_SPEED_WARNING_SWITCH` | `143` | Переключатель предупреждения |
| `HUM_NAVI_SPEEDLIMIT_STATUS` | `610` | Синхронизация навигации с CAN |

### 📐 Битовая маска `ISA_ISLC_STATUS`
```
Bit 0 (1): Система включена (Enable)
Bit 1 (2): Система активна (Active)
Bit 2 (4): Знак валиден (Sign Valid)
Bit 3 (8): Предупреждение/Override
```

### 🧩 Рекомендуемые улучшения архитектуры
1. Ввести единый `SpeedLimitData` (`value`, `type`, `distance`, `isValid`, `timestamp`)
2. Заменить ручные `setValue/postValue` на `Flow`/`LiveData` transform
3. Добавить модульные тесты для `TSREventHandler` и `CanBusManager`
4. Ввести логирование с контекстом: `source=can/gps, timestamp, confidence`

---
📄 **Сохраните этот документ как:** `TSR_CAN_TTS_ANALYSIS.md`  
🔗 **Источники:** Ваш реверс-инжиниринг `qgmedia`/`VoboostVoiceAssistant`, логи CAN-шины, анализ `com/qinggan/ttsservice`, декомпилированные классы `RealityWarningInfo`, `VehicleState`, `DFVehicleState`.

Если нужно экспортировать в PDF, CSV-таблицу констант или сгенерировать готовые `.kt`-файлы для копирования в проект — напишите, подготовлю сразу. 🚗💨