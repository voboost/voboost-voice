# State Machine - 9 состояний

**Дата:** 2026-05-15  
**Статус:** ✅ Реализовано  
**Файл:** `state/SpeechStateMachine.kt` (202 строки)  

---

## 🎯 Обзор

State Machine реализует **event-driven архитектуру** для голосового помощника. Все состояния создаются один раз при инициализации и сами вызывают `finish()` или `cancelled()` когда готовы к переходу.

---

## 🗺️ Диаграмма состояний

```
┌─────────┐
│  IDLE   │
└────┬────┘
     │ activate()
     ▼
┌──────────┐
│ ACTIVATED│  (проигран звук активации)
└────┬─────┘
     │ listenForCommand()
     ▼
┌─────────────────┐
│ LISTENING_COMMAND│  (слушаем команду)
└────┬────────────┘
     │ command recognized
     ▼
┌──────────────────┐
│ RECOGNIZED_COMMAND│  (команда распознана)
└────┬─────────────┘
     │ execute()
     ▼
┌─────────────────┐
│ EXECUTING_COMMAND│  (выполняем команду)
└────┬────────────┘
     │ success/failure
     ▼               ▼
┌──────────┐    ┌─────────────┐
│  IDLE    │◄───┤ COMMAND_ERROR│
└──────────┘    └─────────────┘
```

---

## 📋 Состояния

| Состояние | Описание | Переходы |
|-----------|----------|----------|
| **IDLE** | Ожидание активации (кнопка или ключевое слово) | `activate()` → ACTIVATED |
| **ACTIVATED** | Активирован, проигран звук | `listenForCommand()` → LISTENING_COMMAND |
| **LISTENING_COMMAND** | Слушаем команду пользователя | распознавание → RECOGNIZED_COMMAND или timeout → TIMEOUT |
| **RECOGNIZED_COMMAND** | Команда распознана, ожидает выполнения | `execute()` → EXECUTING_COMMAND |
| **EXECUTING_COMMAND** | Выполняем команду | success → IDLE или failure → COMMAND_ERROR |
| **CONFIRMATION** | Запрос подтверждения действия | confirmation → EXECUTING_COMMAND или cancel → IDLE |
| **COMMAND_ERROR** | Ошибка выполнения команды | `reset()` → IDLE |
| **KEYWORD_ERROR** | Ошибка распознавания ключевого слова | `reset()` → IDLE |
| **TIMEOUT** | Таймаут ожидания команды | `reset()` → IDLE |

---

## 🔧 Использование

### Базовое использование

```kotlin
class VoboostVoiceService : Service() {
    private lateinit var stateMachine: SpeechStateMachine
    
    override fun onCreate() {
        super.onCreate()
        
        stateMachine = SpeechStateMachine(
            audioSource = audioSource,
            voskStream = voskStream,
            keywordChecker = keywordChecker
        )
    }
    
    private fun activateVoiceAssistant() {
        // Активировать (переход из IDLE → ACTIVATED)
        stateMachine.activate()
    }
    
    private fun startListeningCommand() {
        // Начать слушать команду (ACTIVATED → LISTENING_COMMAND)
        stateMachine.startListeningCommand(
            onCommandReceived = { command ->
                Log.i("State", "Команда: $command")
                stateMachine.commandRecognized()
            },
            onError = { error ->
                Log.e("State", "Ошибка: $error")
                stateMachine.cancelled(SpeechState.KEYWORD_ERROR)
            },
            onTimeout = {
                Log.w("State", "Таймаут")
                stateMachine.cancelled(SpeechState.TIMEOUT)
            }
        )
    }
}
```

### Поток событий

```
1. User presses button → activateVoiceAssistant()
   ↓
2. SpeechStateMachine.activate() → ACTIVATED
   ↓
3. Play activation sound (double beep)
   ↓
4. startListeningCommand() → LISTENING_COMMAND
   ↓
5. User speaks: "открой лючок зарядки"
   ↓
6. KeywordChecker.isCommand(text) = true
   ↓
7. Callback.onCommandReceived("открой лючок зарядки")
   ↓
8. stateMachine.commandRecognized() → RECOGNIZED_COMMAND
   ↓
9. NLUEngine.parse("открой лючок зарядки")
   ↓
10. CommandExecutor.execute(command)
   ↓
11. Play completion sound (single beep)
   ↓
12. stateMachine.finished() → IDLE
```

---

## 🔄 Переходы состояний

### activate()
```kotlin
fun activate() {
    if (currentState != SpeechState.IDLE) return
    
    currentState = SpeechState.ACTIVATED
    playSound(SOUND_ACTIVATION)
}
```

### startListeningCommand()
```kotlin
fun startListeningCommand(
    onCommandReceived: (String) -> Unit,
    onError: suspend (String) -> Unit,
    onTimeout: suspend () -> Unit
) {
    if (currentState != SpeechState.ACTIVATED) return
    
    currentState = SpeechState.LISTENING_COMMAND
    voskStream.listenForCommand(
        timeout = COMMAND_TIMEOUT_MS,
        onResult = { text ->
            if (keywordChecker.isCommand(text)) {
                onCommandReceived(text)
            }
        },
        onError = onError,
        onTimeout = onTimeout
    )
}
```

### commandRecognized()
```kotlin
fun commandRecognized() {
    if (currentState != SpeechState.LISTENING_COMMAND) return
    
    currentState = SpeechState.RECOGNIZED_COMMAND
}
```

### finished()
```kotlin
fun finished() {
    // Сброс всех таймеров и потоков
    reset()
    currentState = SpeechState.IDLE
}
```

---

## 🧪 Тестирование

### Проверка состояний

```bash
adb logcat -s SpeechStateMachine:*
```

**Ожидаемые логи:**
```
SpeechStateMachine: State transition: IDLE → ACTIVATED
SpeechStateMachine: State transition: ACTIVATED → LISTENING_COMMAND
SpeechStateMachine: State transition: LISTENING_COMMAND → RECOGNIZED_COMMAND
SpeechStateMachine: State transition: RECOGNIZED_COMMAND → EXECUTING_COMMAND
SpeechStateMachine: State transition: EXECUTING_COMMAND → IDLE
```

---

## 📊 Метрики

| Показатель | Значение |
|-----------|----------|
| Строк кода | 202 |
| Количество состояний | 9 |
| Методов перехода | 8 |
| Таймеров | 3 (keyword, command, confirmation) |

---

## 🎯 Преимущества event-driven подхода

1. ✅ **Отсутствие race conditions** - состояния сами управляют переходами
2. ✅ **Предсказуемый поток** - легко отлаживать
3. ✅ **Легкое расширение** - добавление новых состояний
4. ✅ **Чистая архитектура** - разделение ответственности

---

## 📝 См. также

- [Общая архитектура](../ARCHITECTURE/OVERVIEW.md)
- [Speech Engine Factory](../ARCHITECTURE/SPEECH_ENGINE.md)
- [Command Executor](../ARCHITECTURE/COMMAND_EXECUTOR.md)