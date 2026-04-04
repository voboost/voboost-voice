# 🔄 КОНТЕКСТ ПРОЕКТА VOBOOST VOICE ASSISTANT

**Дата последнего обновления:** 2026-04-04 22:00
**Версия:** 18.0 (Voice Zone Detection)
**Статус:** ✅ ГОТОВО К ТЕСТИРОВАНИЮ

---

## 📋 КРАТКАЯ СУММАРИЗАЦИЯ

**Проект:** `VoboostVoiceAssistant`
**Папка:** `D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant`
**Задача:** Голосовой помощник для автомобиля на русском языке

---

## ✅ ЧТО РАБОТАЕТ

### 1. State Machine Pattern (НОВОЕ в v16.0)
- ✅ `StateMachine` — главный цикл состояний
- ✅ `StateContext` — передача данных между состояниями
- ✅ 9 состояний: Idle, Activated, ListeningCommand, RecognizedCommand, Confirmation, ExecutingCommand, Timeout, CommandError, KeywordError
- ✅ Автоматические переходы между состояниями
- ✅ `SpeechStateMachine` упрощён — только распознавание

### 2. AudioSource
- ✅ `AudioSource` интерфейс
- ✅ `MicrophoneStreamAudioSource` — через TransProxy (QGSpeechService)
- ✅ `AndroidAudioSource` — fallback с аудио-эффектами
- ✅ `AudioSourceFactory` — автоматический выбор

### 3. Распознавание речи
- ✅ `VoskStream` — распознавание через Vosk
- ✅ `SherpaStream` — распознавание через Sherpa (заглушка)
- ✅ `CommandHandler` — NLU → CommandExecutor

### 4. CAN-шина через Frida
- ✅ Frida hook обходит `checkPermission("com.qinggan.permission.WRITE_CANBUS")`
- ✅ Скрипт: `/data/local/tmp/voboost/canbus-permission-bypass.js`
- ✅ Все 13 команд работают (окна, кондиционер и т.д.)

### 5. TSR Speed Limit Warnings
- ✅ Предупреждения о превышении скорости
- ✅ Ограничения от камеры (TSR)
- ✅ Ограничения от навигации

### 6. Audio Channel Fix
- ✅ `USAGE_ASSISTANT` вместо `USAGE_MEDIA`
- ✅ Голос не приглушает музыку

---

## 🏗️ АРХИТЕКТУРА

```
VoboostVoiceService (координатор, ~485 строк)
├── StateMachine (с StateContext)
│   ├── IdleState → ActivatedState → ListeningCommandState
│   ├── RecognizedCommandState → ConfirmationState → ExecutingCommandState
│   ├── TimeoutState, CommandErrorState, KeywordErrorState
│
├── SpeechRecognizer (распознавание, ~270 строк)
│   ├── start() — один непрерывный поток
│   ├── setMode(KEYWORD/COMMAND) — переключение режима
│   ├── results: MutableSharedFlow<SpeechResult> — результаты с зоной
│   └── zoneDetector: VoiceZoneDetector? — опционально
│
├── VoiceZoneDetector (определение зоны говорящего)
│   ├── micphoneModeManager — подключение к IMicphoneMode AIDL
│   ├── detectZone() → front_left/front_right/second_left/second_right
│   └── fallback на front_left если сервис недоступен
│
├── CommandHandler (NLU → CommandExecutor)
├── VolumeManager (управление громкостью)
└── AudioSource (TransProxy/Android)
```

---

## 📁 СТРУКТУРА ПРОЕКТА

```
speech/
├── state/
│   ├── State.kt                    ← Интерфейс состояния
│   ├── StateMachine.kt             ← Главный цикл состояний (+stateJob для прерываний)
│   ├── StateContext.kt             ← Передача данных (zone поле добавлено)
│   ├── IdleState.kt                ← Ожидание ключевого слова (+zone)
│   ├── ActivatedState.kt           ← Активация
│   ├── ListeningCommandState.kt    ← Слушаем команду (+zone)
│   ├── RecognizedCommandState.kt   ← Распознавание команды
│   ├── ConfirmationState.kt        ← Запрос подтверждения
│   ├── ExecutingCommandState.kt    ← Выполнение команды
│   ├── TimeoutState.kt             ← Таймаут
│   ├── CommandErrorState.kt        ← Ошибка команды
│   └── KeywordErrorState.kt        ← Ошибка ключевого слова
│
├── SpeechRecognizer.kt             ← Распознавание (SharedFlow-based, +zone)
├── RecognitionEngine.kt            ← Интерфейс движка
├── RecognitionResult.kt            ← Результат распознавания
├── SpeechResult.kt                 ← sealed class (+zone поле)
├── KeywordChecker.kt               ← Проверка ключевых слов
└── CommandHandler.kt               ← NLU → CommandExecutor

audio/
├── AudioSource.kt                  ← Интерфейс аудио
├── AudioSourceFactory.kt           ← Фабрика аудио
├── MicrophoneStreamAudioSource.kt  ← TransProxy (+логирование PCM)
├── AndroidAudioSource.kt           ← Fallback
├── VolumeManager.kt                ← Громкость
├── MicphoneModeManager.kt          ← AIDL IMicphoneMode клиент (v18.0 NEW)
└── VoiceZoneDetector.kt            ← Определение зоны говорящего (v18.0 NEW)

engine/
├── vosk/
│   ├── VoskModelLoader.kt
│   ├── VoskStream.kt               ← реализует RecognitionEngine
│   └── VoskStreamFactory.kt
└── sherpa/
    ├── SherpaModelLoader.kt
    ├── SherpaStream.kt             ← реализует RecognitionEngine
    └── SherpaStreamFactory.kt

aidl/ (v18.0 NEW)
└── com/qinggan/qinglink/api/hu/
    ├── IMicphoneMode.aidl          ← AIDL интерфейс зоны
    └── IMicphoneModeListener.aidl  ← AIDL listener
```

---

## 🔄 ЦЕПОЧКА ВЫПОЛНЕНИЯ

```
IdleState.execute()
    → waitForKeyword() ← Блокирует до ключевого слова
    → ActivatedState.execute()
        → ListeningCommandState.execute()
            → waitForCommand() ← Блокирует до команды
            → RecognizedCommandState.execute()
                → Если подтверждение → ConfirmationState
                    → ExecutingCommandState
                → Если нет → ExecutingCommandState
                    → IdleState (цикл повторяется)
```

---

## 📊 ИЗМЕНЕНИЯ ВЕРСИИ 16.0

**State Machine Pattern:**
- ✅ Создан `StateMachine` с `StateContext`
- ✅ Созданы 9 State классов
- ✅ Удалён `processVoiceCommand()` из VoboostVoiceService
- ✅ Удалены `cancelCurrentCommand()`, `updateNetworkState()`, `requestRecordPermission()`
- ✅ Упрощён `SpeechStateMachine` (~150 строк вместо ~300)
- ✅ VoboostVoiceService стал тонким координатором (~470 строк)

---

## 🚀 БЫСТРЫЙ СТАРТ

```batch
REM 1. Сборка
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleRelease

REM 2. Загрузка библиотек
copy-libs-to-device.bat

REM 3. Установка
adb root
adb remount
adb push app\build\outputs\apk\release\app-release-unsigned.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk

REM 4. Разрешения
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO

REM 5. Перезапуск
adb shell am force-stop com.voboost.voiceassistant
adb shell am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService

REM 6. Логи
adb logcat -s StateMachine:* SpeechRecognizer:* VoboostVoiceService:*
```

---

## 🎯 ТЕКУЩИЙ СТАТУС

| Компонент | Статус | Примечание |
|-----------|--------|------------|
| **Keyword Spotting** | ✅ Работает | "Привет машина" |
| **Command Recognition** | ✅ Работает | После ключевой фразы И кнопки |
| **Кнопка на руле** | ✅ Работает | Через State Machine |
| **TTS (Sherpa)** | ✅ Работает | Говорит "Слушаю вас" |
| **CAN-шина** | ✅ Работает | Через Frida hook |
| **TSR Speed Limit** | ✅ Работает | Предупреждения |
| **Audio Channel** | ✅ Работает | USAGE_ASSISTANT |
| **State Machine** | ✅ Работает | 9 состояний |
| **SpeechRecognizer** | ✅ Работает | SharedFlow-based |
| **VoiceZoneDetector** | ✅ Работает | IMicphoneMode AIDL |
| **ConfirmationState** | ⚠️ Заглушка | Не ждёт ответ пользователя |

---

## 📝 ФАЙЛЫ ДОКУМЕНТАЦИИ

| Файл | Описание |
|------|----------|
| `CONTEXT_RESTORE.md` | 📋 **ЭТОТ ФАЙЛ** — текущий контекст |
| `ARCHITECTURE_V17.md` | 🏗️ SpeechRecognizer рефакторинг |
| `AUDIO_SOURCE_REFACTORING.md` | 🎤 AudioSource рефакторинг |
| `TRANSPROXY_INTEGRATION.md` | 🔌 TransProxy интеграция |
| `AUDIO_SOURCE_FACTORY.md` | 🏭 Фабрика аудио-источников |
| `ARCHITECTURE_V2.md` | 🏗️ Архитектура v2 (устарела) |

---

## 📞 КОНТАКТЫ ДЛЯ ВОССТАНОВЛЕНИЯ

**Если нужно продолжить работу:**

1. **Открыть этот файл** (`CONTEXT_RESTORE.md`)
2. **Проверить логи:**
   ```bash
   adb logcat -s StateMachine:* SpeechStateMachine:* VoboostVoiceService:*
   ```
3. **Протестировать:**
   - Сказать "привет машина"
   - Проверить переходы состояний
   - Проверить выполнение команд

**Пример запроса:**
```
Продолжи работу над VoboostVoiceAssistant.
Нужно реализовать ConfirmationState для ожидания ответа пользователя.
```

---

## 📊 ВЕРСИИ

| Версия | Дата | Изменения |
|--------|------|-----------|
| 13.0 | 2026-03-31 | Frida CAN-bypass работает |
| 13.1 | 2026-04-01 | Библиотеки вынесены из APK |
| 13.2 | 2026-04-01 | TSR Speed Limit warnings |
| 13.3 | 2026-04-01 | Audio Channel Fix (USAGE_ASSISTANT) |
| 13.4 | 2026-04-01 | NaviInfo → Bundle |
| 13.5 | 2026-04-01 | Удалён Zone Detection |
| 13.6 | 2026-04-02 | Кнопка активации (keycode 16) |
| 13.7 | 2026-04-02 | Кнопка отмены (двойное нажатие) |
| 13.8 | 2026-04-02 | AudioRecord: MIC вместо VOICE_RECOGNITION |
| 14.0 | 2026-04-04 | AudioSource Refactoring |
| 15.0 | 2026-04-04 | State Machine Pattern |
| 16.0 | 2026-04-04 | Упрощение SpeechStateMachine |
| 17.0 | 2026-04-04 | SpeechRecognizer (Channel-based) |
| **18.0** | **2026-04-04** | **Voice Zone Detection (IMicphoneMode AIDL)** |

---

**Последнее обновление:** 2026-04-04 22:00
**Следующая задача:** Реализовать ConfirmationState для ожидания ответа пользователя
