# Общая архитектура Voice Assistant

**Дата:** 2026-05-15  
**Статус:** ✅ Актуальная версия  

---

## 🏗️ Архитектурные компоненты

```
┌─────────────────────────────────────────────────────────────┐
│                  VoboostVoiceService                        │
│                    (Main Service)                           │
└──────────────────┬──────────────────────────────────────────┘
                   │
        ┌──────────┴──────────┬───────────────┬───────────────┐
        │                     │               │               │
   ┌────▼────┐          ┌────▼────┐      ┌────▼────┐     ┌────▼────┐
   │ Speech  │          │ Command │      │  Audio  │     │   TTS   │
   │Engine   │◄────────►│ Executor│◄─────►│ Manager │◄────┤ Engine  │
   │ Factory │          │ Pattern │      │Manager  │     │         │
   └────▲────┘          └─────────┘      └─────────┘     └─────────┘
        │                     │               │               │
        │        ┌────────────┴────────────┐  │               │
        │        │   Executor Implementations │               │
        │        ├─────────────────────────┤ │               │
        │        │ IntentVehicleCommand... │◄┘               │
        │        │ ShellVehicleCommand...  │                 │
        │        │ AutoVehicleCommand...   │                 │
        │        └─────────────────────────┘                 │
        │                                                     │
   ┌────▼────┐                                                │
   │ State   │◄───────────────────────────────────────────────┘
   │Machine  │
   └─────────┘
```

---

## 🔄 Поток обработки команды

```
Пользователь говорит: "Открой лючок зарядки"
        ↓
1. Audio Source (TransProxy/Multi-channel)
   ↓ PCM данные
2. Speech Engine Factory
   ├─ VoskRecognition (STT - офлайн)
   └─ SherpaSpeechSynthesis (TTS)
   ↓
3. State Machine (9 состояний)
   ├─ IDLE → ACTIVATED → LISTENING_COMMAND
   └─ RECOGNIZED_COMMAND → EXECUTING_COMMAND
   ↓
4. NLUEngine Парсинг
   └─ RecognizedCommand { id, patterns, action }
   ↓
5. CommandExecutor
   ├─ IntentVehicleCommandExecutor (Broadcast)
   ├─ ShellVehicleCommandExecutor (CAN)
   └─ AutoVehicleCommandExecutor (auto-select)
   ↓
6. Выполнение через AIDL/Intent
   └─ pateo.dls.ivoka.vehicle.CONTROL
```

---

## 🏛️ Паттерны проектирования

### 1. State Machine Pattern

**Файл:** `state/SpeechStateMachine.kt` (200 строк)

```kotlin
enum class SpeechState {
    IDLE, ACTIVATED, LISTENING_COMMAND,
    RECOGNIZED_COMMAND, EXECUTING_COMMAND,
    CONFIRMATION, COMMAND_ERROR, KEYWORD_ERROR, TIMEOUT
}
```

**Преимущества:**
- ✅ Четкое управление состояниями
- ✅ Предсказуемый поток выполнения
- ✅ Легко добавлять новые состояния

---

### 2. Factory Pattern (Speech Engine)

**Файл:** `core/SpeechEngineFactory.kt`

```kotlin
object SpeechEngineFactory {
    fun createSpeechRecognizer(context: Context): ISpeechRecognizer
    fun createSpeechSynthesis(config: TtsConfig): ISpeechSynthesis
}
```

**Преимущества:**
- ✅ Легко добавлять новые движки (Vosk, Sherpa, System)
- ✅ Поддержка fallback механизмов
- ✅ Отсутствие жесткой зависимости от конкретной реализации

---

### 3. Executor Pattern (Commands)

**Файл:** `executor/CommandExecutor.kt`

```kotlin
interface IVehicleCommandExecutor {
    suspend fun execute(context: Context, command: CommandConfig)
}
```

**Реализации:**
- `IntentVehicleCommandExecutor` - Broadcast Intent
- `ShellVehicleCommandExecutor` - Shell CAN-команды  
- `AutoVehicleCommandExecutor` - Автоматический выбор

---

### 4. Strategy Pattern (Audio Sources)

**Файл:** `audio/IAudioSource.kt`

```kotlin
interface IAudioSource {
    fun start()
    fun stop()
    fun addListener(listener: AudioListener)
}
```

**Реализации:**
- `AndroidAudioSource` - Микрофон Android API
- `MultiChannelAudioSource` - Мультиканальный аудио (зона говорящего)
- `RecorderManagerAudioSource` - RecorderManager API

---

## 📁 Структура пакетов

```
ru.voboost.voiceassistant/
├── core/                    # Ядро системы
│   ├── ISpeechRecognizer.kt  # Интерфейс STT
│   ├── ISpeechSynthesis.kt   # Интерфейс TTS
│   └── SpeechEngineFactory.kt # Фабрика движков
│
├── engine/                  # Реализации движков
│   ├── sherpa/             # Sherpa-ONNX
│   │   ├── SherpaSpeechSynthesis.kt
│   │   ├── SherpaStream.kt
│   │   └── SherpaModelLoader.kt
│   ├── vosk/               # Vosk STT
│   │   ├── VoskRecognition.kt
│   │   ├── VoskStream.kt
│   │   └── VoskModelLoader.kt
│   └── system/             # Системные движки
│       └── SystemSpeechSynthesis.kt
│
├── state/                   # State Machine (9 состояний)
│   ├── SpeechStateMachine.kt  # Основной класс
│   ├── IdleState.kt
│   ├── ActivatedState.kt
│   ├── ListeningCommandState.kt
│   └── ...
│
├── nlu/                     # Natural Language Understanding
│   ├── NLUEngine.kt         # Парсер команд
│   └── Command.kt           # Data классы
│
├── executor/                # Executor Pattern
│   ├── CommandExecutor.kt               # Базовый класс
│   ├── IVehicleCommandExecutor.kt       # Интерфейс
│   ├── IntentVehicleCommandExecutor.kt
│   ├── ShellVehicleCommandExecutor.kt
│   └── AutoVehicleCommandExecutor.kt
│
├── audio/                   # Audio Management
│   ├── IAudioSource.kt              # Интерфейс
│   ├── AudioSourceFactory.kt        # Фабрика
│   ├── AndroidAudioSource.kt
│   ├── MultiChannelAudioSource.kt   # Мультиканальный
│   └── RecorderManagerAudioSource.kt
│
├── tts/                     # Text-to-Speech
│   ├── TTSEngine.kt         # Основной класс
│   └── SoundEffectManager.kt # Звуковые эффекты
│
├── canbus/                  # CAN-шина
│   ├── TSRCallback.kt           # Обработчик TSR
│   ├── TSREventHandler.kt       # CAN события
│   └── CanBusServiceManager.kt  # Управление сервисом
│
└── ui/                      # UI компоненты
    ├── OverlayManager.kt      # Оверлеи и анимация
    └── VoiceClickView.kt      # Анимация микрофона
```

---

## 🔄 Взаимодействие компонентов

### Инициализация сервиса:

```kotlin
// VoboostVoiceService onCreate()
1. Audio Source Factory (TransProxy/Multi-channel)
2. Speech Engine Factory (Vosk + Sherpa)
3. State Machine (9 состояний)
4. NLUEngine (Parser/LLM/ONNX)
5. Command Executor (Intent/Shell/Auto)
6. TTS Engine (Sherpa/System)
7. CAN Bus Handler (TSR, Speed, etc.)
```

### Обработка голосовой команды:

```kotlin
// Поток обработки
1. AudioSource → PCM данные
2. VoskRecognition → Text (распознавание)
3. NLUEngine → RecognizedCommand (парсинг)
4. StateMachine → State transition
5. CommandExecutor → Execute command
6. TTS Engine → Speak response
7. OverlayManager → Show animation
```

---

## 📊 Производительность

| Компонент | Время обработки | Мем. использование |
|-----------|-----------------|-------------------|
| Vosk STT (offline) | 200-500ms | ~100MB |
| Sherpa TTS | 300-800ms | ~50MB |
| NLUEngine Parser | <50ms | ~10MB |
| State Machine | <10ms | ~5MB |
| **Всего цикла** | **500-1400ms** | **~165MB** |

---

## 🔧 Расширение архитектуры

### Добавление нового STT движка:

```kotlin
// 1. Создать реализацию интерфейса
class MySTTDengine : ISpeechRecognizer {
    override suspend fun recognize(audio: ByteArray): String { ... }
}

// 2. Обновить SpeechEngineFactory
object SpeechEngineFactory {
    fun createSpeechRecognizer(context: Context): ISpeechRecognizer {
        return when (config.sttType) {
            SttType.VOSK -> VoskRecognition(context)
            SttType.MY_ENGINE -> MySTTDengine()
        }
    }
}
```

### Добавление нового типа команды:

```kotlin
// 1. Создать executor
class DatabaseVehicleCommandExecutor : IVehicleCommandExecutor {
    override suspend fun execute(context: Context, command: CommandConfig) { ... }
}

// 2. Обновить AutoVehicleCommandExecutor
object AutoVehicleCommandExecutor : IVehicleCommandExecutor {
    private val executors = listOf(
        IntentVehicleCommandExecutor,
        ShellVehicleCommandExecutor,
        DatabaseVehicleCommandExecutor, // ← Новый executor
    )
}
```

---

## 📚 См. также

- [State Machine](./STATE_MACHINE.md) - Подробное описание 9 состояний
- [Speech Engine Factory](./SPEECH_ENGINE.md) - Архитектура движков
- [Command Executor](./COMMAND_EXECUTOR.md) - Исполнение команд