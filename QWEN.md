# КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-14
**Статус:** ВСЕ РАБОТАЕТ -- звонки, кнопка, CAN-bus, распознавание, TTS, визуальный эффект (центр), зона говорящего, температура (прямая + смещение), русские числа

---

## ОПИСАНИЕ ПРОЕКТА

**Оффлайн голосовой ассистент для автомобильных ГУ**
- **Платформа:** Android 11, API 30 (minSdk=26, targetSdk=33)
- **Package:** `ru.voboost.voiceassistant` (uid: u0_a68, НЕ system)
- **Build:** Gradle, Kotlin 2.1.0, AGP 8.13.2, compileSdk=36
- **Язык:** Kotlin 100%
- **Распознавание:** Vosk (offline, русский, small model 50MB)
- **TTS:** Sherpa-ONNX (ru_RU-ruslan-medium) — выбирается из config.json
- **CAN-шина:** через AIDL `com.qinggan.canbus.ICanBusService`
- **Установка:** 2 этапа — APK+libs → reboot → models+config+permissions

---

## ПОСЛЕДНИЕ КОМИТЫ

### fb8ba3c — fix: AirConditionerSetTempHandler uses 'temp' key matching config.json {temp}
- Исправлен ключ `voiceParams["temperature"]` → `voiceParams["temp"]`
- Теперь параметр из config.json `{temp}` правильно попадает в handler

### 8371386 — fix: installation scripts, overlay centering, phone MAC, temperature x10, russian numbers
- **VoboostVoiceAssistant-install.bat:** 2-этапная установка (APK+libs → reboot → models+config+perms)
- **install-update.bat:** упрощён для быстрых обновлений только APK
- **copy-vosk-to-internal.bat:** динамический UID через `pm list packages -U`
- **OverlayManager.kt:** центрирование анимации через post-layout measurement (реальный размер View)
- **PhoneCallContactIntentHandler.kt:** исправление получения Bluetooth MAC через SystemProperties(String, String), отправка пустого MAC
- **CanBusServiceManager.kt:** температура * 10 для CAN-bus (формат Ivoka: `(int)(10.0f * state)`)
- **AirConditionerSetTempHandler.kt:** парсинг русских числительных (двадцать четыре → 24)
- **ConfigManager.kt:** загрузка config.json ТОЛЬКО из `/data/user/0/ru.voboost.voiceassistant/files/` (убран assets fallback)

### bb927fa — fix: исправление конфига и зависимостей, удаление неиспользуемых phone handler

---

## СТРУКТУРА ДАННЫХ НА УСТРОЙСТВЕ

| Что | Путь |
|-----|------|
| **APK** | `/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk` |
| **Нативные библиотеки** | `/system/priv-app/VoboostVoiceAssistant/lib/arm64/` |
| **config.json** | `/data/user/0/ru.voboost.voiceassistant/files/config.json` |
| **Vosk модель** | `/data/user/0/ru.voboost.voiceassistant/files/models/vosk/vosk-model-small-ru-0.22/` |
| **Sherpa ASR** | `/data/user/0/ru.voboost.voiceassistant/files/models/sherpa/asr-ru-model/` |
| **Sherpa TTS** | `/data/user/0/ru.voboost.voiceassistant/files/models/sherpa/tts-ru-model/` |

---

## УСТАНОВКА (2 ЭТАПА)

### Этап 1: До перезагрузки
1. Отключение стандартных ассистентов (ivoka, sttservice)
2. Копирование APK в `/system/priv-app/`
3. Копирование нативных библиотек в `/system/priv-app/VoboostVoiceAssistant/lib/arm64/`
4. Перезагрузка устройства

### Этап 2: После перезагрузки (автоматически)
5. Копирование моделей (Vosk + Sherpa ASR/TTS)
6. Копирование config.json
7. Динамическое определение UID через `pm list packages -U`
8. Установка прав `chown UID:UID` на `/data/user/0/.../files`
9. Выдача runtime разрешений (RECORD_AUDIO, READ_CONTACTS, SYSTEM_ALERT_WINDOW)
10. Запуск VoboostVoiceService

### Скрипты:
- `VoboostVoiceAssistant-install.bat` — полная установка с нуля (2 этапа)
- `install-update.bat` — быстрое обновление только APK (без reboot)
- `copy-vosk-to-internal.bat` — копирование Vosk модели (динамический UID)

---

## ТЕМПЕРАТУРА КЛИМАТА

### Прямая установка:
- **Команда:** `"установи температуру {temp} градусов"`, `"поставь {temp} градусов"`
- **Парсер:** `AirConditionerSetTempHandler.parseTemperature()` поддерживает:
  - Цифры: `"22"` → 22
  - Русские числа: `"двадцать четыре"` → 24
  - Комбинации: `"двадцать" + " четыре"` → 24
- **CAN-bus:** `temperature * 10` (Ivoka формат: 24°C → 240)
- **Зона:** учитывается `_zone` из voiceParams

### Смещение:
- **"мне холодно"** → `ac_temp_up` → текущая +2°C
- **"мне жарко"** → `ac_temp_down` → текущая -2°C
- Текущая температура берётся из `airCondition.airLeftTemperature`

---

## ВИЗУАЛЬНЫЙ ЭФФЕКТ

- Frame-by-frame анимация из 41 PNG (voice_right000..040), взятых из Ivoka
- **Позиция:** верхний центр экрана (автоматическое центрирование post-layout)
- `params.x = (screenWidth - viewWidth) / 2`, `params.y = 0`

---

## ТЕЛЕФОННЫЕ ЗВОНКИ

### Архитектура звонков:
```
[Голос: "позвони Сынок"] → NLUEngine → phone_call_contact
                                        │
                    PhoneCallContactIntentHandler (Intent-based)
                                        │
                    getBluetoothMac() → SystemProperties.get("qinggan.bluetooth.mac", "")
                    Если MAC пустой → ContentProvider без MAC
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
                    extra "mac"=""  ← ПУСТАЯ СТРОКА (не "number"!)
                                        │
                    BluetoothPhone → HeadSetProfileManager.makeCall(number, "")
                    Если MAC пустой → mHfp.dial(number) без MAC
```

---

## КОНФИГ (config.json)

### Загрузка:
- **Путь:** `/data/user/0/ru.voboost.voiceassistant/files/config.json`
- **НЕ в assets** — APK облегчённый, конфиг только в data-папке
- Если не найден → дефолтная конфигурация

### Параметры команд:
- `temp` — температура для кондиционера (число или русское число)
- `contact` — имя контакта для звонков
- `number` — номер телефона

---

## РЕШЁННЫЕ ПРОБЛЕМЫ

### 9. Температура устанавливалась вместо 24°C → 18.5°C
- **Причина:** CAN-bus принимает `temperature * 10` (Ivoka формат)
- **Решение:** `CanBusServiceManager.setTemperatureByZone()` умножает на 10

### 10. Прямая установка температуры не работала
- **Причина 1:** `voiceParams["temperature"]` не совпадал с `{temp}` из конфига
- **Причина 2:** Vosk small модель путает числа ("двадцать" → "двадцать два")
- **Решение:** исправлен ключ на `voiceParams["temp"]`, добавлен `parseTemperature()` для русских числительных

### 11. Анимация была слева, не по центру
- **Причина:** `Gravity.TOP or Gravity.START` с фиксированным offset
- **Решение:** post-layout measurement → `params.x = (screenWidth - viewWidth) / 2`

### 12. UID приложения разный на разных устройствах
- **Причина:** хардкод `u0_a69` в скриптах
- **Решение:** динамическое определение через `pm list packages -U %PKG%`

### 13. Установка скриптов не работала после перезагрузки
- **Причина:** модели и конфиг копировались ДО перезагрузки, когда `/data/user/0/` ещё не создан
- **Решение:** 2-этапная установка (APK → reboot → данные)

---

## СОСТОЯНИЕ НА ДАННЫЙ МОМЕНТ

### РАБОТАЕТ:
- ✅ TTS Sherpa — русская речь (ru_RU-ruslan-medium)
- ✅ Vosk STT — распознавание команд
- ✅ Кнопка на руле — keycode=16 через CAN-шину
- ✅ State Machine — 9 состояний
- ✅ CAN-bus — 17 AIDL команд
- ✅ Звонки по имени контакта (через BluetoothPhone ContentProvider)
- ✅ Звонки по номеру
- ✅ Визуальный эффект — по центру вверху экрана
- ✅ Температура: прямая установка ("поставь двадцать четыре градусов")
- ✅ Температура: смещение ("мне холодно" / "мне жарко")
- ✅ Зона говорящего для климата
- ✅ 2-этапная установка (APK → reboot → данные)
- ✅ Динамический UID в скриптах

### НЕ РАБОТАЕТ:
- Vosk small модель иногда путает числа (20 ↔ 22) — ограничение модели

---

**Последнее обновление:** 2026-04-14 23:30
**Build:** assembleDebug SUCCESS
**Git коммиты:** 9623e65, fb8ba3c, 8371386, bb927fa, 1f0f072, f970f66, 225f219, cec0ae7, b5a331c, 8428707, d72bdce, 48d2e32, ca46011, a9743fa, 9302162, 1b39bd1

---

## ПОСЛЕДНИЙ КОМИТ

### 9623e65 — fix: Sherpa TTS eSpeak-ng permissions + ExternalStoragePaths
- **Исправление разрешений espeak-ng-data** (chmod 755) для работы TTS
- **Добавлен ExternalStoragePaths** для путей к внешнему хранилищу
- **Обновлены скрипты:** `copy-sherpa-models.bat`, `VoboostVoiceAssistant-install.bat`
- **SherpaSynthesis:** восстановлен `setDataDir()` для eSpeak-ng
- **Конфигурация TTS** загружается из `/data/user/0/.../files/config.json`

**Проблема:** После переезда на внешнее хранилище TTS не работал с ошибкой 
`Failed to set eSpeak-ng voice`. 

**Причина:** Неправильные права на папку `espeak-ng-data` 
(`drwxr-s--x` вместо `drwxr-xr-x`). Нативная библиотека eSpeak-ng не могла 
прочитать файлы словарей.

**Решение:** `chmod -R 755` для `espeak-ng-data` после копирования модели.

---

## СТРУКТУРА ДАННЫХ НА УСТРОЙСТВЕ (ОБНОВЛЕНО)

| Что | Путь | Примечание |
|-----|------|------------|
| **APK** | `/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk` | |
| **Нативные библиотеки** | `/system/priv-app/VoboostVoiceAssistant/lib/arm64/` | |
| **config.json** | `/data/user/0/ru.voboost.voiceassistant/files/config.json` | |
| **Vosk модель** | `/storage/emulated/0/Android/data/.../files/models/vosk/` | Внешнее хранилище |
| **Sherpa ASR** | `/storage/emulated/0/Android/data/.../files/models/sherpa/asr-ru-model/` | Внешнее хранилище |
| **Sherpa TTS** | `/storage/emulated/0/Android/data/.../files/models/sherpa/tts-ru-model/` | Внешнее хранилище |
| **TTS eSpeak-ng-data** | `/storage/emulated/0/Android/data/.../files/models/sherpa/tts-ru-model/espeak-ng-data/` | **Разрешения: 755** |

---

## СКРИПТЫ (ОБНОВЛЕНО)

### copy-sherpa-models.bat
```batch
[4/4] Исправление разрешений для eSpeak-ng...
adb shell "chmod -R 755 /storage/emulated/0/Android/data/%PKG%/files/models/sherpa/tts-ru-model/espeak-ng-data"
```

### VoboostVoiceAssistant-install.bat
```batch
REM Исправление разрешений для eSpeak-ng (критично для работы TTS!)
adb shell "chmod -R 755 %EXTERNAL_DIR%/models/sherpa/tts-ru-model/espeak-ng-data"
```
