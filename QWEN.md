## Qwen Added Memories
- ## ADB на Windows - важные правила

**Путь к adb:** `d:\Projects\Android\MM\6.11.1\export\adb\adb.exe`

**ВАЖНО:** Все команды после `adb shell` нужно заключать в **двойные кавычки** на Windows:
```
adb shell "ps -A | grep dvr"
adb shell "kill -9 PID"
adb shell "am broadcast -a android.intent.action.BOOT_COMPLETED -n com.voboost.voiceassistant/.BootReceiver"
```
- ## VoboostVoiceAssistant (d:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant)

### Архитектура
- Оффлайн голосовой ассистент для автомобильных ГУ (Android 11, API 30)
- Package: com.voboost.voiceassistant, uid: u0_a68 (НЕ system)
- Build: Gradle, Kotlin 2.1.0, AGP 8.13.2, minSdk=26, targetSdk=33
- Распознавание: Vosk (offline, русский), TTS: Sherpa-ONNX (ru_RU-ruslan-medium)
- CAN-шина: через AIDL com.qinggan.canbus.ICanBusService

### Ключевые компоненты
- VoboostVoiceService.kt — главный foreground сервис (microphone type)
- BootActivity.kt — невидимая Activity для foreground context при автозапуске
- BootReceiver.kt — запускает BootActivity при BOOT_COMPLETED
- CanBusServiceManager.kt — обёртка AIDL к CAN-шине
- NLUEngine.kt — паттерн-матчинг команд из config.json
- State Machine: IdleState → ActivatedState → ListeningCommandState → RecognizedCommandState → ExecutingCommandState

### Установка
- APK: /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
- Конфиг: /sdcard/Android/data/com.voboost.voiceassistant/files/config.json (приоритет) + assets/config.json (fallback)
- Модели: /data/user/0/com.voboost.voiceassistant/files/models/vosk/ и sherpa/

### ADB команды
- adb root — root режим
- adb shell "mount -o remount,rw /" — перемонтировать /system в RW
- adb push <apk> /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk — залить APK
- adb shell "am force-stop com.voboost.voiceassistant && am broadcast -a android.intent.action.BOOT_COMPLETED -n com.voboost.voiceassistant/.BootReceiver" — перезапуск

### Решённые проблемы

#### 1. Микрофон в фоне (Android 10+)
- Проблема: ActivityManager блокирует FGS с микрофоном из background
- Решение: BootReceiver → BootActivity (foreground context) → startForegroundService → сервис получает микрофон

#### 2. sharedUserId="android.uid.system" вызывает bootloop
- Причина: APK подписан debug key, а не platform key
- Без platform подписи система не может загрузить пакет как system UID

#### 3. Значения CAN-шины инвертированы
- Бензобак (IVI_FUEL_PORT_CAP): toggle только с value=1 (не 0 или 2)
- Окна: ALL_WINDOW (3=OPEN, 1=CLOSE) правильно; DRIVER_WINDOW (97=CLOSE, 51=OPEN) — инвертировано
- Кондиционер (AC_POWER_SWITCH): toggle только с value=1 (не 0)
- Проверка статуса: canBusManager.getAirCondition()?.airSWStatus (1=ON, 0=OFF)

#### 4. Кондиционер — toggle с проверкой статуса
- AirConditionerOpenHandler: если airSWStatus=1 → пропускает, иначе value=1
- AirConditionerCloseHandler: если airSWStatus=0 → пропускает, иначе value=1
- Оба отправляют value=1, проверка статуса предотвращает двойное переключение

#### 5. Privapp-permissions НЕ нужны
- privapp-permissions-voboost.xml удалён — работает без него
- RECORD_AUDIO выдаётся как runtime permission

## Проблема с AudioRecord при автозапуске — РЕШЕНА ✅

**Причина:** Android 10+ блокирует доступ к микрофону при запуске foreground service из background контекста после BOOT_COMPLETED.

**Решение:** BootReceiver → **BootActivity** (невидимая Activity с foreground context) → `startForegroundService` → сервис получает полный доступ к микрофону.

```
BootReceiver.onReceive() 
  → BootActivity.launch() (Theme.Translucent.NoTitleBar, foreground context)
    → BootActivity.onStart() → startForegroundService(VoboostVoiceService)
      → AudioRecord создаётся успешно ✅
```

**Ключевые детали BootActivity:**
- Theme: `Theme.Translucent.NoTitleBar` (полностью невидимая)
- Flags: `excludeFromRecents=true`, `noHistory=true`
- Не появляется в UI, но даёт foreground context для запуска сервиса

**Рабочий тест:** AudioRecord создаётся с первого раза при автозапуске через BootActivity.
