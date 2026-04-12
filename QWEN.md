# КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-12
**Статус:** ВСЕ РАБОТАЕТ -- звонки, кнопка, CAN-bus, распознавание, TTS engine из конфига

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

## ПОСЛЕДНИЕ ИЗМЕНЕНИЯ (коммиты)

### bb927fa -- fix: исправление конфига и зависимостей, удаление неиспользуемых phone handler
- Удалены `AbstractPhoneHandler.kt`, `PhoneCallContactHandler.kt`, `PhoneCallNumberHandler.kt` из `aidl/phone/`
- Остались только Intent-based handlers: `PhoneCallContactIntentHandler.kt`, `PhoneCallNumberIntentHandler.kt`

### 1f0f072 -- feat: TTS engine теперь выбирается из конфига (sherpa/system)
- Удалён жёсткий `TTS_ENGINE_TYPE`, теперь читается из `config.json` -> `tts.offline.engine`
- Удалён мёртвый `TTSEngine.kt` (не использовался, т.к. TTS_ENGINE_TYPE=SYSTEM)
- rate/pitch применяются из конфига после инициализации

### f970f66 -- refactor: удаление неиспользуемых params из config.json
- Убраны `params.mode` из `smart_mode_*` (modeId захардкожен в handlers)
- Убран `params.location` из `charge_port_*` (не читается)
- Убраны `params.call_type` из `phone_call_*` (handler определяется по commandId)
- Убраны пустые `params: {}` из `fuel_tank_open`, `ac_open`, `ac_close`, `window_*`

### 225f219 -- refactor: полное удаление action из конфига и кода
- Удалён блок "action" из всех 14 команд в config.json
- Удалён data class `ActionConfig` из `CommandConfig.kt`
- `ICommandHandler`: `execute(config, voiceParams)` -> `execute(voiceParams)`
- `IVehicleCommandExecutor`: `executeByCommandId(id, config, params)` -> `executeByCommandId(id, params)`
- `CommandExecutor`: убран `buildVehicleParams`
- Обновлены 34 handler файла (AIDL, Intent, Shell)

### cec0ae7 -- refactor: смена package name с com.voboost.voiceassistant на ru.voboost.voiceassistant
- applicationId и namespace в build.gradle
- package в AndroidManifest.xml
- Все .kt файлы перемещены com/voboost/ -> ru/voboost/
- Обновлены все .bat и .md файлы
- AIDL com.qinggan.* остались без изменений

### b5a331c -- feat: скрипт migrate-package.bat для переименования папок на устройстве
- Переименовывает `/data/user/0/com.voboost.voiceassistant` -> `/data/user/0/ru.voboost.voiceassistant`
- Обновляет APK и config.json
- НЕ очищает папки -- библиотеки и модели сохраняются

---

## АРХИТЕКТУРА

### Главный сервис: `VoboostVoiceService.kt`
- **Foreground service** с типом `microphone`
- Запускается через `BootActivity` (foreground context) для получения доступа к микрофону
- Управляет State Machine (9 состояний)
- Координирует все компоненты системы

### State Machine (9 состояний):
```
IdleState -> ActivatedState -> ListeningCommandState -> RecognizedCommandState -> ExecutingCommandState
                                       |                        |
                                 KeywordErrorState        ConfirmationState
                                        |                         |
                                   CommandErrorState          TimeoutState
```

---

## КЛЮЧЕВЫЕ КОМПОНЕНТЫ

| Компонент | Файл | Назначение |
|-----------|------|------------|
| **BootReceiver** | `BootReceiver.kt` | Автозапуск при BOOT_COMPLETED -> запускает BootActivity |
| **BootActivity** | `BootActivity.kt` | Невидимая Activity для foreground context при автозапуске |
| **State Machine** | `speech/state/*.kt` | Управление состояниями распознавания |
| **SpeechRecognizer** | `speech/SpeechRecognizer.kt` | Непрерывное распознавание речи |
| **NLUEngine** | `nlu/NLUEngine.kt` | Паттерн-матчинг команд из config.json |
| **CommandExecutor** | `executor/CommandExecutor.kt` | Выполнение команд через VehicleCommandExecutor |
| **TTS Engine** | `tts/*.kt` | Синтез речи (Sherpa-ONNX или системный TTS, выбирается из конфига) |
| **AudioSource** | `audio/*.kt` | Источники аудио (TransProxy, AndroidAudioSource) |
| **CanBusManager** | `canbus/CanBusServiceManager.kt` | AIDL обёртка к CAN-шине |
| **VoiceButtonHandler** | `canbus/VoiceButtonHandler.kt` | Кнопка на руле (keycode=16) |
| **TSRSpeedLimitHandler** | `canbus/TSRSpeedLimitHandler.kt` | Предупреждения о превышении скорости |
| **OverlayManager** | `ui/OverlayManager.kt` | UI overlay поверх всех окон |

---

## ТЕЛЕФОННЫЕ ЗВОНКИ

### Архитектура звонков:

```
[Голос: "позвони Сынок"] -> NLUEngine -> phone_call_contact
                                        |
                    PhoneCallContactIntentHandler (Intent-based)
                                        |
                    ContentResolver.query() -> BluetoothPhone ContentProvider
                    URI: content://com.qinggan.bluetoothphone/contactsinfo/{MAC}
                    columns: name, number
                                        |
                    Найти номер: +375445413460
                                        |
                    Broadcast Intent:
                    action="com.qinggan.broadcast.action.ivokaphonecall"
                    extra "Ivoka_CallInfo"="+375445413460"
                    extra "screen_int"=0
                    extra "mac"=""
                                        |
                    BluetoothPhone -> набирает номер через HFP
```

### Ключевые файлы:

| Файл | Роль |
|------|------|
| `PhoneCallContactIntentHandler.kt` | Ищет номер по имени через ContentResolver, отправляет Intent |
| `PhoneCallNumberIntentHandler.kt` | Отправляет Intent с номером напрямую |
| `AbstractIntentHandler.kt` | Базовый класс с константами Intent |

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
<!-- Объявляем permission явно как normal, чтобы система приняла наше объявление -->
<permission
    android:name="com.qinggan.bluetoothphone.PROVIDER"
    android:protectionLevel="normal" />
<uses-permission android:name="com.qinggan.bluetoothphone.PROVIDER" />
```

### Почему это работает:
- BluetoothPhone использует `<uses-permission android:name="com.qinggan.bluetoothphone.PROVIDER"/>` но НЕ объявляет её через `<permission>`
- Система создаёт implicit permission как **signature** (по умолчанию)
- Если наше приложение объявит её как **normal** ДО того как система создаст signature -- она станет normal и будет доступна нам
- После reboot система принимает наше объявление

### Выданные разрешения:
- `com.qinggan.bluetoothphone.PROVIDER: granted=true`
- `com.qinggan.permission.WRITE_CANBUS: granted=true`
- `android.permission.READ_CONTACTS: granted=true`
- `android.permission.RECORD_AUDIO: granted=true`

---

## РЕШЁННЫЕ ПРОБЛЕМЫ

### 1. Микрофон в фоне (Android 10+)
- **Проблема:** ActivityManager блокирует FGS с микрофоном из background
- **Решение:** BootReceiver -> BootActivity (foreground context) -> startForegroundService -> сервис получает микрофон

### 2. sharedUserId="android.uid.system" вызывает bootloop
- **Причина:** APK подписан debug key, а не platform key
- Без platform подписи система не может загрузить пакет как system UID

### 3. Значения CAN-шины инвертированы
- Бензобак (IVI_FUEL_PORT_CAP): toggle только с value=1 (не 0 или 2)
- Окна: ALL_WINDOW (3=OPEN, 1=CLOSE) правильно; DRIVER_WINDOW (97=CLOSE, 51=OPEN) -- инвертировано
- Кондиционер (AC_POWER_SWITCH): toggle только с value=1 (не 0)
- Проверка статуса: `canBusManager.getAirCondition()?.airSWStatus` (1=ON, 0=OFF)

### 4. Кнопка на руле не регистрировалась
- **Проблема:** `VoiceButtonHandler.register()` вызывался до подключения CanBusService
- **Решение:** Асинхронная регистрация через `ConnectionCallback`:
  ```kotlin
  canBusManager.addConnectionCallback(object : ConnectionCallback {
      override fun onConnected() {
          registerCanBusHandlers()  // <-- регистрируем когда сервис готов
      }
  })
  if (canBusManager.isConnected()) registerCanBusHandlers()  // <-- если уже подключён
  ```

### 5. Privapp-permissions НЕ нужны
- `privapp-permissions-voboost.xml` удалён -- работает без него
- RECORD_AUDIO выдаётся как runtime permission

### 6. Звонки через Intent -- неправильный handler
- **Проблема:** `PhoneCallContactHandler` использовал AIDL CanBusService вместо Intent
- **Решение:** Переписан на отправку broadcast Intent с номером телефона (Intent-based handlers)

### 7. Package name изменён на ru.voboost.voiceassistant
- Все пути, intents, content provider URI обновлены
- Скрипт `migrate-package.bat` для миграции данных на устройстве

### 8. action блок удалён из конфига и кода
- Все команды теперь работают без action конфигурации
- Handler определяется исключительно по commandId

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

## СОСТОЯНИЕ НА ДАННЫЙ МОМЕНТ

### РАБОТАЕТ:
- TTS (Sherpa или системный) -- "Слушаю вас", "Открываю окна"
- TTS engine выбирается из config.json (tts.offline.engine: sherpa/system)
- Vosk STT -- распознаёт команды
- Кнопка на руле -- keycode=16 через CAN-шину (асинхронная регистрация)
- State Machine -- 9 состояний
- CAN-bus -- AIDL команды (окна, кондиционер, бензобак и т.д.)
- AudioRecord -- при автозапуске через BootActivity
- Звонки по номеру -- через Intent broadcast
- Звонки по имени контакта -- через ContentResolver + BluetoothPhone ContentProvider
- Permission `com.qinggan.bluetoothphone.PROVIDER` -- получена
- Permission `com.qinggan.permission.WRITE_CANBUS` -- получена
- Package name: ru.voboost.voiceassistant

### НЕ РАБОТАЕТ:
- Всё работает!

---

**Последнее обновление:** 2026-04-12
**Build:** assembleDebug SUCCESS
**Git коммиты:** bb927fa, 1f0f072, f970f66, 225f219, cec0ae7, b5a331c
