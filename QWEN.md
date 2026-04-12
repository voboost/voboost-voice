# 📋 КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-12 13:53
**Статус:** ✅ ВСЕ РАБОТАЕТ — звонки, кнопка, CAN-bus, распознавание

---

## 🎯 ОПИСАНИЕ ПРОЕКТА

**Оффлайн голосовой ассистент для автомобильных ГУ**
- **Платформа:** Android 11, API 30 (minSdk=26, targetSdk=33)
- **Package:** `com.voboost.voiceassistant` (uid: u0_a68, НЕ system)
- **Build:** Gradle, Kotlin 2.1.0, AGP 8.13.2, compileSdk=36
- **Язык:** Kotlin 100%
- **Распознавание:** Vosk (offline, русский), TTS: Sherpa-ONNX (ru_RU-ruslan-medium)
- **CAN-шина:** через AIDL `com.qinggan.canbus.ICanBusService`

---

## 🏗 АРХИТЕКТУРА

### Главный сервис: `VoboostVoiceService.kt`
- **Foreground service** с типом `microphone`
- Запускается через `BootActivity` (foreground context) для получения доступа к микрофону
- Управляет State Machine (9 состояний)
- Координирует все компоненты системы

### State Machine (9 состояний):
```
IdleState → ActivatedState → ListeningCommandState → RecognizedCommandState → ExecutingCommandState
                                       ↓                        ↓
                                 KeywordErrorState        ConfirmationState
                                        ↓                         ↓
                                   CommandErrorState          TimeoutState
```

---

## 🔑 КЛЮЧЕВЫЕ КОМПОНЕНТЫ

| Компонент | Файл | Назначение |
|-----------|------|------------|
| **BootReceiver** | `BootReceiver.kt` | Автозапуск при BOOT_COMPLETED → запускает BootActivity |
| **BootActivity** | `BootActivity.kt` | Невидимая Activity для foreground context при автозапуске |
| **State Machine** | `speech/state/*.kt` | Управление состояниями распознавания |
| **SpeechRecognizer** | `speech/SpeechRecognizer.kt` | Непрерывное распознавание речи |
| **NLUEngine** | `nlu/NLUEngine.kt` | Паттерн-матчинг команд из config.json |
| **CommandExecutor** | `executor/CommandExecutor.kt` | Выполнение команд через VehicleCommandExecutor |
| **TTS Engine** | `tts/TTSEngine.kt` | Синтез речи (Sherpa-ONNX) |
| **AudioSource** | `audio/*.kt` | Источники аудио (TransProxy, AndroidAudioSource) |
| **CanBusManager** | `canbus/CanBusServiceManager.kt` | AIDL обёртка к CAN-шине |
| **VoiceButtonHandler** | `canbus/VoiceButtonHandler.kt` | Кнопка на руле (keycode=16) |
| **TSRSpeedLimitHandler** | `canbus/TSRSpeedLimitHandler.kt` | Предупреждения о превышении скорости |
| **OverlayManager** | `ui/OverlayManager.kt` | UI overlay поверх всех окон |

---

## 📞 ТЕЛЕФОННЫЕ ЗВОНКИ

### Архитектура звонков:

```
[Голос: "позвони Сынок"] → NLUEngine → phone_call_contact
                                        ↓
                    PhoneCallContactHandler (Intent-based)
                                        ↓
                    ContentResolver.query() → BluetoothPhone ContentProvider
                    URI: content://com.qinggan.bluetoothphone/contactsinfo/{MAC}
                    columns: name, number
                                        ↓
                    Найти номер: +375445413460
                                        ↓
                    Broadcast Intent:
                    action="com.qinggan.broadcast.action.ivokaphonecall"
                    extra "Ivoka_CallInfo"="+375445413460"
                    extra "screen_int"=0
                    extra "mac"=""
                                        ↓
                    BluetoothPhone → набирает номер через HFP
```

### Ключевые файлы:

| Файл | Роль |
|------|------|
| `PhoneCallContactHandler.kt` | Ищет номер по имени через ContentResolver, отправляет Intent |
| `PhoneCallNumberIntentHandler.kt` | Отправляет Intent с номером напрямую |
| `AbstractIntentHandler.kt` | Базовый класс с константами Intent |

### Константы Intent:
```kotlin
ACTION_IVOKA_PHONE_CALL = "com.qinggan.broadcast.action.ivokaphonecall"
EXTRA_IVOKA_CALL_INFO = "Ivoka_CallInfo"
EXTRA_SCREEN_INT = "screen_int"
EXTRA_MAC = "mac"
```

---

## 🔑 PERMISSIONS И БЕЗОПАСНОСТЬ

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
- Если наше приложение объявит её как **normal** ДО того как система создаст signature — она станет normal и будет доступна нам
- После reboot система принимает наше объявление

### Выданные разрешения:
- `com.qinggan.bluetoothphone.PROVIDER: granted=true` ✅
- `com.qinggan.permission.WRITE_CANBUS: granted=true` ✅
- `android.permission.READ_CONTACTS: granted=true` ✅
- `android.permission.RECORD_AUDIO: granted=true` ✅

---

## 🔧 РЕШЁННЫЕ ПРОБЛЕМЫ

### 1. Микрофон в фоне (Android 10+)
- **Проблема:** ActivityManager блокирует FGS с микрофоном из background
- **Решение:** BootReceiver → BootActivity (foreground context) → startForegroundService → сервис получает микрофон

### 2. sharedUserId="android.uid.system" вызывает bootloop
- **Причина:** APK подписан debug key, а не platform key
- Без platform подписи система не может загрузить пакет как system UID

### 3. Значения CAN-шины инвертированы
- Бензобак (IVI_FUEL_PORT_CAP): toggle только с value=1 (не 0 или 2)
- Окна: ALL_WINDOW (3=OPEN, 1=CLOSE) правильно; DRIVER_WINDOW (97=CLOSE, 51=OPEN) — инвертировано
- Кондиционер (AC_POWER_SWITCH): toggle только с value=1 (не 0)
- Проверка статуса: `canBusManager.getAirCondition()?.airSWStatus` (1=ON, 0=OFF)

### 4. Кнопка на руле не регистрировалась
- **Проблема:** `VoiceButtonHandler.register()` вызывался до подключения CanBusService
- **Решение:** Асинхронная регистрация через `ConnectionCallback`:
  ```kotlin
  canBusManager.addConnectionCallback(object : ConnectionCallback {
      override fun onConnected() {
          registerCanBusHandlers()  // ← регистрируем когда сервис готов
      }
  })
  if (canBusManager.isConnected()) registerCanBusHandlers()  // ← если уже подключён
  ```

### 5. Privapp-permissions НЕ нужны
- `privapp-permissions-voboost.xml` удалён — работает без него
- RECORD_AUDIO выдаётся как runtime permission

### 6. Звонки через Intent — неправильный handler
- **Проблема:** `PhoneCallContactHandler` использовал AIDL CanBusService вместо Intent
- **Решение:** Переписан на отправку broadcast Intent с номером телефона

---

## 🚀 УСТАНОВКА

### Путь установки:
```
/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Конфиг:
- **Приоритет:** `/storage/emulated/0/Android/data/com.voboost.voiceassistant/files/config.json`
- **Fallback:** `assets/config.json` (в APK)

### Модели:
- **Vosk:** `/data/user/0/com.voboost.voiceassistant/files/models/vosk/`
- **Sherpa:** `/data/user/0/com.voboost.voiceassistant/files/models/sherpa/`

---

## 🔧 ADB КОМАНДЫ

### Установка APK:
```batch
adb root
adb remount
adb push <apk> /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
```

### Перезапуск:
```batch
adb shell "am force-stop com.voboost.voiceassistant && am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService"
```

### Мониторинг логов:
```batch
adb logcat -s VoboostVoiceService:I IntentHandler:D PhoneCommand:I CanBusServiceManager:I
```

### Тест звонка:
```batch
adb shell "am broadcast -a com.qinggan.broadcast.action.ivokaphonecall --es Ivoka_CallInfo '4008888488' --ei screen_int 0 --es mac ''"
```

---

## 📊 СОСТОЯНИЕ НА ДАННЫЙ МОМЕНТ

### ✅ РАБОТАЕТ:
- TTS (Sherpa) — "Слушаю вас", "Открываю окно"
- Vosk STT — распознаёт команды
- Кнопка на руле — keycode=16 через CAN-шину (асинхронная регистрация)
- State Machine — 9 состояний
- CAN-bus — AIDL команды (окна, кондиционер, бензобак и т.д.)
- AudioRecord — при автозапуске через BootActivity
- **Звонки по номеру** — через Intent broadcast
- **Звонки по имени контакта** — через ContentResolver + BluetoothPhone ContentProvider
- Permission `com.qinggan.bluetoothphone.PROVIDER` — получена
- Permission `com.qinggan.permission.WRITE_CANBUS` — получена

### ❌ НЕ РАБОТАЕТ:
- Всё работает! 🎉

---

**Последнее обновление:** 2026-04-12
**Build:** assembleDebug SUCCESS
**Git:** требуется коммит всех изменений
