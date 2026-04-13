# КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-12
**Статус:** ВСЕ РАБОТАЕТ -- звонки, кнопка, CAN-bus, распознавание, TTS, визуальный эффект, зона говорящего, команды "мне холодно/жарко"

---

## ОПИСАНИЕ ПРОЕКТА

**Оффлайн голосовой ассистент для автомобильных ГУ**
- **Платформа:** Android 11, API 30 (minSdk=26, targetSdk=33)
- **Package:** `ru.voboost.voiceassistant` (uid: u0_a68, НЕ system)
- **Build:** Gradle, Kotlin 2.1.0, AGP 8.13.2, compileSdk=36
- **Язык:** Kotlin 100%
- **Распознавание:** Vosk (offline, русский), TTS: Sherpa-ONNX (ru_RU-ruslan-medium) ИЛИ системный TTS (настраивается в config.json)
- **CAN-шина:** через AIDL `com.qinggan.canbus.ICanBusService`

---

## ПОСЛЕДНИЕ КОМИТЫ (хронологический порядок)

### bb927fa — fix: исправление конфига и зависимостей, удаление неиспользуемых phone handler
- Удалены `AbstractPhoneHandler.kt`, `PhoneCallContactHandler.kt`, `PhoneCallNumberHandler.kt` из `aidl/phone/`
- Остались только Intent-based handlers: `PhoneCallContactIntentHandler.kt`, `PhoneCallNumberIntentHandler.kt`

### 1f0f072 — feat: TTS engine выбирается из конфига (sherpa/system)
- Удалён жёсткий `TTS_ENGINE_TYPE`, теперь читается из `config.json` → `tts.offline.engine`
- Удалён мёртвый `TTSEngine.kt` (не использовался)
- rate/pitch применяются из конфига после инициализации TTS

### f970f66 — refactor: удаление неиспользуемых params из config.json
- Убраны `params.mode` из `smart_mode_*`, `params.location` из `charge_port_*`, `params.call_type` из `phone_call_*`
- Убраны пустые `params: {}` из команд

### 225f219 — refactor: полное удаление action из конфига и кода
- Удалён блок `action` из всех команд в config.json
- Удалён data class `ActionConfig` из `CommandConfig.kt`
- `ICommandHandler.execute(config, voiceParams)` → `execute(voiceParams)`
- `IVehicleCommandExecutor.executeByCommandId(id, config, params)` → `executeByCommandId(id, params)`
- Обновлены 34 handler файла

### cec0ae7 — refactor: смена package name с com.voboost.voiceassistant на ru.voboost.voiceassistant
- applicationId и namespace в build.gradle
- package в AndroidManifest.xml
- Все .kt файлы перемещены com/voboost/ → ru/voboost/
- AIDL com.qinggan.* остались без изменений

### b5a331c — feat: скрипт migrate-package.bat для переименования папок на устройстве
- Переименовывает `/data/user/0/com.voboost.voiceassistant` → `/data/user/0/ru.voboost.voiceassistant`
- НЕ очищает папки — библиотеки и модели сохраняются

### 8428707 — feat: замена визуального эффекта на оригинальный из Ivoka
- Frame-by-frame анимация из 41 PNG (voice_right000..040)
- AnimationDrawable, oneshot=false (зацикленная)
- Показывается при активации, скрывается при возврате в Idle

### d72bdce — feat: команды "мне холодно" (+2°C) и "мне жарко" (-2°C)
- `ac_temp_up` / `ac_temp_down` — корректируют температуру на ±2°C
- Учитывают зону говорящего

### 48d2e32 — cleanup: удалить черновик AirConditionerTempUpHandler.kt

### ca46011 — fix: AirConditionerSetTempHandler отправлял температуру на AC_POWER_SWITCH вместо AC_LEFT_TEMP
- Исправлен CanSignal для установки температуры

### a9743fa — feat: установка температуры климата по зоне говорящего (setTemperatureByZone)
- front_left → AC_LEFT_TEMP
- front_right → AC_RIGHT_TEMP
- center/all_location/second_* → обе стороны

### 9302162 — refactor: зона говорящего как отдельное поле в RecognizedCommand
- `RecognizedCommand.zone` — хранит зону говорящего

### 1b39bd1 — refactor: State Machine — единая инициализация, переходы по типу (StateType enum)
- Все 9 состояний создаются один раз в StateMachine
- Переходы по enum StateType вместо создания экземпляров
- `reset()` вызывается перед каждым переходом
- Нет циклических зависимостей между состояниями

---

## АРХИТЕКТУРА

### Главный сервис: `VoboostVoiceService.kt`
- **Foreground service** с типом `microphone`
- Запускается через `BootActivity` (foreground context) для получения доступа к микрофону
- Управляет State Machine (9 состояний)
- Координирует все компоненты системы

### State Machine (9 состояний, StateType enum):
```
IdleState -> ActivatedState -> ListeningCommandState -> RecognizedCommandState -> ExecutingCommandState
                                       |                        |
                                 KeywordErrorState        ConfirmationState
                                        |                         |
                                   CommandErrorState          TimeoutState
```

Все состояния создаются один раз при инициализации. Переходы выполняются по `StateType` enum. Перед каждым переходом вызывается `reset()`. Нет циклических зависимостей.

---

## СТРУКТУРА ПРОЕКТА

```
app/src/main/
├── java/ru/voboost/voiceassistant/
│   ├── audio/                  # AudioSource, VolumeManager, VoiceZoneDetector
│   ├── canbus/                 # CanBusServiceManager, VoiceButtonHandler, TSRHandler
│   ├── config/                 # ConfigManager, AppConfig, CommandConfig
│   ├── core/                   # SpeechEngineFactory, ISpeechRecognition, ISpeechSynthesis
│   ├── engine/
│   │   ├── sherpa/             # Sherpa STT + TTS
│   │   ├── system/             # System TTS (fallback)
│   │   └── vosk/               # Vosk STT
│   ├── executor/               # CommandExecutor, VehicleCommandExecutor
│   │   └── handlers/           # AIDL, Intent, Shell handlers
│   ├── nlu/                    # NLUEngine, Command
│   ├── speech/                 # SpeechRecognizer, CommandHandler
│   │   └── state/              # State Machine (9 состояний, StateType enum)
│   ├── ui/                     # OverlayManager, VoiceClickView
│   ├── BootActivity.kt
│   ├── BootReceiver.kt
│   ├── SoundEffectManager.kt
│   ├── VoboostVoiceService.kt  # ГЛАВНЫЙ СЕРВИС
│   └── VoiceCommandReceiver.kt
├── aidl/com/qinggan/           # AIDL файлы CAN-bus (VehicleState, AirConditionState и т.д.)
├── assets/
│   └── config.json             # Конфиг команд
└── res/
    ├── drawable/anim_voice_effect.xml  # Анимация из Ivoka
    └── drawable-land/voice_right000..040.png  # 41 кадр анимации
```

---

## КОНФИГ (config.json)

### TTS engine:
```json
"tts": {
    "offline": {
        "engine": "sherpa"  // "sherpa" или "system"
    }
}
```

### Параметры команд:
Остались только реально используемые params:
- `contact` — имя контакта для звонков
- `number` — номер телефона
- `temperature` — температура для кондиционера

Блок `action` полностью удалён. Handler определяется исключительно по `commandId`.

---

## CAN-BUS КОМАНДЫ (17)

| Command ID | Handler | Описание |
|------------|---------|----------|
| `window_open` | WindowControlHandler | Открыть окно |
| `window_close` | WindowControlHandler | Закрыть окно |
| `window_all_open` | WindowControlHandler | Открыть все окна |
| `window_all_close` | WindowControlHandler | Закрыть все окна |
| `charge_port_open` | ChargePortHandler | Открыть зарядный порт |
| `charge_port_close` | ChargePortHandler | Закрыть зарядный порт |
| `fuel_tank_open` | FuelTankHandler | Открыть бензобак |
| `smart_mode_leisure` | SmartModeHandler | Режим "Отдых" |
| `smart_mode_child` | SmartModeHandler | Режим "Ребёнок" |
| `smart_mode_romantic` | SmartModeHandler | Режим "Романтика" |
| `ac_open` | AirConditionerOpenHandler | Включить кондиционер |
| `ac_close` | AirConditionerCloseHandler | Выключить кондиционер |
| `ac_set_temp` | AirConditionerSetTempHandler | Установить температуру |
| `ac_temp_up` | AirConditionerTempUpHandler | +2°C (учитывает зону) |
| `ac_temp_down` | AirConditionerTempDownHandler | -2°C (учитывает зону) |
| `phone_call_contact` | PhoneCallContactIntentHandler | Звонок по имени контакта |
| `phone_call_number` | PhoneCallNumberIntentHandler | Звонок по номеру |

---

## ЗОНА ГОВОРЯЩЕГО ДЛЯ КЛИМАТА

Зона говорящего хранится в `RecognizedCommand.zone`. Метод `setTemperatureByZone(zone, temp)` в `CanBusServiceManager`:

| Зона | Сигнал |
|------|--------|
| `front_left` | AC_LEFT_TEMP |
| `front_right` | AC_RIGHT_TEMP |
| `center`, `all_location`, `second_*` | Обе стороны |

Команды `ac_temp_up` / `ac_temp_down` корректируют текущую температуру на ±2°C с учётом зоны говорящего.

---

## ВИЗУАЛЬНЫЙ ЭФФЕКТ

- Frame-by-frame анимация из 41 PNG (voice_right000..040), взятых из Ivoka
- `AnimationDrawable` через `anim_voice_effect.xml`, oneshot=false (зацикленная)
- Ресурсы: `res/drawable-land/voice_right000.png` .. `voice_right040.png`
- Показывается при активации (ActivatedState), скрывается при возврате в Idle

---

## ТЕЛЕФОННЫЕ ЗВОНКИ

### Архитектура звонков:
```
[Голос: "позвони Сынок"] → NLUEngine → phone_call_contact
                                        │
                    PhoneCallContactIntentHandler (Intent-based)
                                        │
                    ContentResolver.query() → BluetoothPhone ContentProvider
                    URI: content://com.qinggan.bluetoothphone/contactsinfo/{MAC}
                    columns: name, number
                                        │
                    Найти номер: +375445413460
                                        │
                    Broadcast Intent:
                    action="com.qinggan.broadcast.action.ivokaphonecall"
                    extra "Ivoka_CallInfo"="+375445413460"
                    extra "screen_int"=0
                    extra "mac"=""
                                        │
                    BluetoothPhone → набирает номер через HFP
```

### Константы Intent:
```kotlin
ACTION_IVOKA_PHONE_CALL = "com.qinggan.broadcast.action.ivokaphonecall"
EXTRA_IVOKA_CALL_INFO = "Ivoka_CallInfo"
EXTRA_SCREEN_INT = "screen_int"
EXTRA_MAC = "mac"
```

### Удалённые файлы (больше не используются):
- `aidl/phone/AbstractPhoneHandler.kt`
- `aidl/phone/PhoneCallContactHandler.kt`
- `aidl/phone/PhoneCallNumberHandler.kt`

---

## PERMISSIONS И БЕЗОПАСНОСТЬ

### Объявленные permissions:
```xml
<permission
    android:name="com.qinggan.bluetoothphone.PROVIDER"
    android:protectionLevel="normal" />
<uses-permission android:name="com.qinggan.bluetoothphone.PROVIDER" />
```

### Выданные разрешения:
- `com.qinggan.bluetoothphone.PROVIDER: granted=true`
- `com.qinggan.permission.WRITE_CANBUS: granted=true`
- `android.permission.READ_CONTACTS: granted=true`
- `android.permission.RECORD_AUDIO: granted=true`

---

## РЕШЁННЫЕ ПРОБЛЕМЫ

### 1. Микрофон в фоне (Android 10+)
- **Проблема:** ActivityManager блокирует FGS с микрофоном из background
- **Решение:** BootReceiver → BootActivity (foreground context) → startForegroundService

### 2. sharedUserId="android.uid.system" вызывает bootloop
- **Причина:** APK подписан debug key, а не platform key

### 3. Значения CAN-шины инвертированы
- Бензобак (IVI_FUEL_PORT_CAP): toggle только с value=1
- Окна: ALL_WINDOW (3=OPEN, 1=CLOSE); DRIVER_WINDOW (97=CLOSE, 51=OPEN) — инвертировано
- Кондиционер (AC_POWER_SWITCH): toggle только с value=1

### 4. Кнопка на руле не регистрировалась
- **Решение:** Асинхронная регистрация через `ConnectionCallback`

### 5. action блок удалён из конфига и кода
- Все команды работают без action конфигурации
- Handler определяется исключительно по commandId

### 6. TTS engine теперь из конфига
- Жёсткий `TTS_ENGINE_TYPE` заменён на чтение из config.json
- Удалён неиспользуемый `TTSEngine.kt`

### 7. Package name изменён на ru.voboost.voiceassistant
- Скрипт `migrate-package.bat` для миграции данных на устройстве

### 8. State Machine рефакторинг
- Все состояния создаются один раз, переходы по StateType enum
- `reset()` перед каждым переходом, нет циклических зависимостей

---

## УСТАНОВКА

### Путь установки:
```
/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Конфиг:
- **Приоритет:** `/storage/emulated/0/Android/data/ru.voboost.voiceassistant/files/config.json`
- **Fallback:** `assets/config.json` (в APK)

### Модели:
- **Vosk:** `/data/user/0/ru.voboost.voiceassistant/files/models/vosk/`
- **Sherpa:** `/data/user/0/ru.voboost.voiceassistant/files/models/sherpa/`

---

## ADB КОМАНДЫ

### Установка APK:
```batch
adb root
adb remount
adb push <apk> /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Перезапуск:
```batch
adb shell "am force-stop ru.voboost.voiceassistant && am start-foreground-service -n ru.voboost.voiceassistant/.VoboostVoiceService"
```

### Мониторинг логов:
```batch
adb logcat -s VoboostVoiceService:I IntentHandler:D PhoneCommand:I CanBusServiceManager:I
```

### Тест звонка:
```batch
adb shell "am broadcast -a com.qinggan.broadcast.action.ivokaphonecall --es Ivoka_CallInfo '4008888488' --ei screen_int 0 --es mac ''"
```

### Миграция package name:
```batch
adb shell migrate-package.bat
```

---

## СОСТОЯНИЕ НА ДАННЫЙ МОМЕНENT

### РАБОТАЕТ:
- TTS (Sherpa или системный) — выбирается из config.json
- Vosk STT — распознаёт команды
- Кнопка на руле — keycode=16 через CAN-шину (асинхронная регистрация)
- State Machine — 9 состояний, переходы по StateType enum
- CAN-bus — 17 AIDL команд (окна, кондиционер, бензобак, smart mode, звонки)
- Звонки по номеру и имени контакта — через Intent broadcast
- Permission `com.qinggan.bluetoothphone.PROVIDER` — получена
- Permission `com.qinggan.permission.WRITE_CANBUS` — получена
- Визуальный эффект — оригинальная анимация из Ivoka (41 PNG)
- Температура климата по зоне говорящего
- Команды "мне холодно" / "мне жарко" (+2/-2°C)
- Package name: ru.voboost.voiceassistant

### НЕ РАБОТАЕТ:
- Всё работает!

---

**Последнее обновление:** 2026-04-12
**Build:** assembleDebug SUCCESS
**Git коммиты:** bb927fa, 1f0f072, f970f66, 225f219, cec0ae7, b5a331c, 8428707, d72bdce, 48d2e32, ca46011, a9743fa, 9302162, 1b39bd1
