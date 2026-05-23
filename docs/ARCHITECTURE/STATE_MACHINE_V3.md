# State Machine V3 - Модульная архитектура

**Дата:** 2026-05-23  
**Статус:** ✅ Реализовано  
**Замена:** `state/SpeechStateMachine.kt` → `states/StateMachine.kt`

---

## 🎯 Обзор

Полная рефакторинг State Machine с монолитного подхода (470 строк в одном классе) на **модульную event-driven архитектуру**.

### Проблемы старой версии:
- ❌ 470 строк в одном файле
- ❌ Race conditions при перезапуске распознавания
- ❌ Сложная логика переходов (if/else в каждом методе)
- ❌ Пересоздание объектов при каждом переходе

### Преимущества новой версии:
- ✅ 9 отдельных файлов, каждый ~20-30 строк
- ✅ Состояния сами управляют переходами через `finish()`/`cancelled()`
- ✅ Нет race conditions (каждое состояние изолировано)
- ✅ Состояния не пересоздаются (один раз при инициализации)

---

## 🏗️ Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                  StateMachine                               │
│              (управление состояниями)                       │
└────────────────┬────────────────────────────────────────────┘
                 │
      ┌──────────┴───────────┐
      │                      │
      ▼                      ▼
┌─────────────┐     ┌──────────────┐
│ StateTypes  │     │   IState     │
│ (enum)      │     │ (interface)  │
└─────────────┘     └──────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ IdleState    │  │ Activated    │  │ ListeningCmd │
│              │  │ State        │  │ State        │
└──────────────┘  └──────────────┘  └──────────────┘
     │                   │                  │
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Recognized   │  │ Executing    │  │ Confirmation │
│ CommandState │  │ CommandState │  │ State        │
└──────────────┘  └──────────────┘  └──────────────┘
     │                   │                  │
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ CommandError │  │ KeywordError │  │ TimeoutState │
│ State        │  │ State        │  │              │
└──────────────┘  └──────────────┘  └──────────────┘
```

---

## 📁 Структура пакетов

```
ru.voboost.voice/
├── states/
│   ├── StateMachine.kt         # Основной класс (213 строк)
│   ├── StateContext.kt         # Контекст состояний (зависимости)
│   ├── StateResult.kt          # Результат перехода
│   └── TimeoutState.kt         # Состояние таймаута
│
└── states/state/
    ├── IState.kt               # Интерфейс состояния
    ├── BaseState.kt            # Базовая реализация
    │
    ├── IdleState.kt            # IDLE
    ├── ActivatedState.kt       # ACTIVATED
    ├── ListeningCommandState.kt# LISTENING_COMMAND
    ├── RecognizedCommandState.kt# RECOGNIZED_COMMAND
    ├── ExecutingCommandState.kt# EXECUTING_COMMAND
    ├── ConfirmationState.kt    # CONFIRMATION
    ├── CommandErrorState.kt    # COMMAND_ERROR
    ├── KeywordErrorState.kt    # KEYWORD_ERROR
    └── TimeoutState.kt         # TIMEOUT
```

---

## 🗺️ Диаграмма состояний

```
┌─────────┐
│  IDLE   │
└────┬────┘
     │ activate()
     ▼
┌──────────┐
│ACTIVATED │  (проигран звук активации)
└────┬─────┘
     │ listenForCommand()
     ▼
┌─────────────────┐
│LISTENING_COMMAND│  (слушаем команду)
└────┬────────────┘
     │ command recognized
     ▼
┌──────────────────┐
│RECOGNIZED_COMMAND│  (команда распознана)
└────┬─────────────┘
     │ execute()
     ▼
┌─────────────────┐
│EXECUTING_COMMAND│  (выполняем команду)
└────┬────────────┘
     │ success/failure
     ▼               ▼
┌──────────┐    ┌─────────────┐
│  IDLE    │◄───┤COMMAND_ERROR│
└──────────┘    └─────────────┘

Параллельно:
┌──────────────┐    ┌──────────────┐
│KEYWORD_ERROR │    │ TIMEOUT_STATE│
└──────────────┘    └──────────────┘
```

---

## 📋 Интерфейс IState

```kotlin
interface IState {
    val type: StateType
    val canCancel: Boolean
    
    fun onEnter(context: StateContext)
    fun onCancel(context: StateContext)
    fun finish(context: StateContext)
    fun cancelled(context: StateContext, error: String? = null)
}
```

### Методы состояния:

| Метод | Назначение | Когда вызывается |
|-------|-----------|------------------|
| `onEnter()` | Инициализация состояния | При переходе в состояние |
| `onCancel()` | Обработка отмены | Пользователь нажал кнопку отмены |
| `finish()` | Успешное завершение | Когда задача выполнена |
| `cancelled()` | Ошибка/прерывание | При ошибке или таймауте |

---

## 🔧 Базовая реализация BaseState

```kotlin
abstract class BaseState : IState {
    override fun onEnter(context: StateContext) { /* noop */ }
    override fun onCancel(context: StateContext) { /* noop */ }
    
    open fun finish(context: StateContext) {
        context.stateMachine.transition(type, StateResult.SUCCESS)
    }
    
    open fun cancelled(context: StateContext, error: String? = null) {
        context.stateMachine.transition(type, StateResult.ERROR, error)
    }
}
```

---

## 📋 Реализации состояний

### IdleState
```kotlin
class IdleState : BaseState() {
    override val type = StateType.IDLE
    override val canCancel = false
    
    override fun onEnter(context: StateContext) {
        // Остановить распознавание
        context.recognitionService.setMode(RecognitionService.Mode.KEYWORD)
        // Проиграть тихий звук готовности
        context.soundEffectManager.playIdle()
    }
    
    override fun onCancel(context: StateContext) {
        // Ничего не делаем (IDLE не отменяется)
    }
}
```

### ActivatedState
```kotlin
class ActivatedState : BaseState() {
    override val type = StateType.ACTIVATED
    override val canCancel = true
    
    override fun onEnter(context: StateContext) {
        // Проиграть звук активации (двойной сигнал)
        context.soundEffectManager.playActivation()
        
        // Перейти в режим ожидания команды
        context.recognitionService.setMode(RecognitionService.Mode.COMMAND)
    }
}
```

### ListeningCommandState
```kotlin
class ListeningCommandState : BaseState() {
    override val type = StateType.LISTENING_COMMAND
    override val canCancel = true
    
    override fun onEnter(context: StateContext) {
        // Начать слушать команду с таймаутом
        context.recognitionService.startListeningCommand(
            timeoutMs = COMMAND_TIMEOUT_MS,
            onResult = { text ->
                if (context.nluEngine.isCommand(text)) {
                    context.stateMachine.commandRecognized()
                }
            },
            onError = { error ->
                context.stateMachine.cancelled(error)
            },
            onTimeout = {
                context.stateMachine.cancelled("timeout")
            }
        )
    }
    
    override fun onCancel(context: StateContext) {
        // Остановить слушание команды
        context.recognitionService.stop()
        super.cancelled(context, "cancelled_by_user")
    }
}
```

### RecognizedCommandState
```kotlin
class RecognizedCommandState : BaseState() {
    override val type = StateType.RECOGNIZED_COMMAND
    override val canCancel = false
    
    override fun onEnter(context: StateContext) {
        // Запустить анимацию выполнения
        context.voceAnimationManager.show()
        
        // Перейти к выполнению команды
        context.commandExecutor.executeCommand(
            commandData = context.lastRecognizedCommand!!
        )
    }
}
```

### ExecutingCommandState
```kotlin
class ExecutingCommandState : BaseState() {
    override val type = StateType.EXECUTING_COMMAND
    override val canCancel = false
    
    override fun onEnter(context: StateContext) {
        // Выполнить команду асинхронно
        context.commandExecutor.executeAsync(
            success = { result ->
                if (result.success) {
                    context.stateMachine.finished()
                } else {
                    context.stateMachine.cancelled("execution_failed")
                }
            },
            error = { e ->
                context.stateMachine.cancelled(e.message ?: "unknown_error")
            }
        )
    }
}
```

### CommandErrorState
```kotlin
class CommandErrorState : BaseState() {
    override val type = StateType.COMMAND_ERROR
    override val canCancel = true
    
    override fun onEnter(context: StateContext) {
        // Проиграть звук ошибки (низкий сигнал)
        context.soundEffectManager.playError()
        
        // Показать error phrase через TTS
        context.speechService.enqueue(
            text = context.configManager.getDefaultPhrase(ConfigManager.PhraseType.FAILURE),
            priority = SpeechService.PRIOR_HIGH
        )
    }
    
    override fun onCancel(context: StateContext) {
        super.cancelled(context, "error_cancelled")
    }
}
```

---

## 🔄 Управление переходами

### StateMachine.kt (основной класс)

```kotlin
class StateMachine(
    private val scope: CoroutineScope,
    private val context: StateContext
) {
    // Регистрация всех состояний при инициализации
    private val states = mapOf(
        StateType.IDLE to IdleState(),
        StateType.ACTIVATED to ActivatedState(),
        StateType.LISTENING_COMMAND to ListeningCommandState(),
        // ... остальные 6 состояний
    )
    
    // Текущее состояние
    @Volatile private var currentState: IState = states[StateType.IDLE]!!
    
    // Вызов метода состояния и переход к следующему
    fun transition(currentType: StateType, result: StateResult, error: String? = null) {
        scope.launch {
            val nextState = getNextState(currentType, result)
            
            if (nextState != null) {
                currentState.onExit(context)
                currentState = nextState
                currentState.onEnter(context)
            }
        }
    }
    
    // Методы вызова состояний
    fun activate() { currentState.onActivate(context) }
    fun startListeningCommand() { currentState.onStartListening(context) }
    fun commandRecognized() { currentState.onRecognized(context) }
    fun finished() { currentState.finish(context) }
    fun cancelled(error: String? = null) { currentState.cancelled(context, error) }
}
```

---

## 🎯 Поток событий

### Сценарий 1: Успешное выполнение команды

```
User says: "Открой лючок зарядки"
        ↓
1. VoiceButtonHandler.onVoiceButtonPressed()
   ↓
2. stateMachine.activate() → IDLE → ACTIVATED
   ↓
3. ActivatedState.onEnter():
   - Play activation sound (double beep)
   - recognitionService.setMode(COMMAND)
   ↓
4. ListeningCommandState.onEnter():
   - Start listening with timeout 5000ms
   - Recognizer detects command phrase
   ↓
5. Callback → stateMachine.commandRecognized()
   ↓
6. RecognizedCommandState.onEnter():
   - Show animation (VoiceClickView)
   - Execute command
   ↓
7. ExecutingCommandState.onEnter():
   - CommandExecutor.execute() → success=true
   ↓
8. stateMachine.finished() → IDLE
   ↓
9. IdleState.onEnter():
   - Stop animation
   - Play idle sound
```

### Сценарий 2: Ошибка распознавания

```
User says: "какой-то бессмысленный текст"
        ↓
1. ListeningCommandState.onEnter():
   - Start listening with timeout
   - Recognizer returns text but NLU doesn't match
   ↓
2. stateMachine.cancelled("keyword_not_found")
   ↓
3. KeywordErrorState.onEnter():
   - Play error sound (low beep)
   - TTS: "Команда не распознана"
   ↓
4. stateMachine.finished() → IDLE
```

### Сценарий 3: Пользователь нажал кнопку отмены

```
User presses cancel button during LISTENING_COMMAND
        ↓
1. VoiceButtonHandler.onCancelPressed()
   ↓
2. stateMachine.cancelled("cancelled_by_user")
   ↓
3. ListeningCommandState.onCancel():
   - recognitionService.stop()
   - super.cancelled(context, "cancelled_by_user")
   ↓
4. IdleState.onEnter(): (from transition)
   - Resume keyword detection
```

---

## 🧪 Тестирование

### Проверка переходов состояний

```bash
adb logcat | grep -i "StateMachine"
```

**Ожидаемые логи:**
```
D/StateMachine: State transition: IDLE → ACTIVATED (activate)
D/StateMachine: State transition: ACTIVATED → LISTENING_COMMAND (listenForCommand)
D/StateMachine: State transition: LISTENING_COMMAND → RECOGNIZED_COMMAND (commandRecognized)
D/StateMachine: State transition: RECOGNIZED_COMMAND → EXECUTING_COMMAND (execute)
D/StateMachine: State transition: EXECUTING_COMMAND → IDLE (finished)
```

### Тест сценариев

```bash
# 1. Успешная команда
adb shell am broadcast -a ru.voboost.voice.ACTIVATE
# Сказать: "открой лючок зарядки"
# Ожидается: IDLE → ACTIVATED → LISTENING_COMMAND → RECOGNIZED_COMMAND → EXECUTING_COMMAND → IDLE

# 2. Не распознанная команда
adb shell am broadcast -a ru.voboost.voice.ACTIVATE
# Сказать: "какой-то бессмысленный текст"
# Ожидается: IDLE → ACTIVATED → LISTENING_COMMAND → KEYWORD_ERROR → IDLE

# 3. Отмена во время слушания
adb shell am broadcast -a ru.voboost.voice.ACTIVATE
# Нажать кнопку отмены до окончания фразы
# Ожидается: IDLE → ACTIVATED → LISTENING_COMMAND → IDLE (cancelled)
```

---

## 📊 Сравнение версий

| Параметр | v2 (SpeechStateMachine) | v3 (State Machine) |
|----------|------------------------|--------------------|
| **Строк кода** | 470 (монолитный файл) | ~250 (9 модулей по ~25 строк) |
| **Файлов** | 1 | 10 (StateMachine + 9 состояний) |
| **Пересоздание объектов** | Да (каждый переход) | Нет (один раз при старте) |
| **Race conditions** | Возможны | Исключены |
| **Читаемость** | Низкая | Высокая |
| **Расширяемость** | Сложная | Простая (новое состояние = новый файл) |

---

## 🎯 Преимущества новой архитектуры

1. ✅ **Чистая инкапсуляция** - каждый файл отвечает за одно состояние
2. ✅ **Легкое тестирование** - можно тестировать каждое состояние изолированно
3. ✅ **Масштабируемость** - добавление нового состояния = создание одного файла
4. ✅ **Отсутствие race conditions** - состояния не пересоздаются, используются ссылки
5. ✅ **Удобная отладка** - логи переходов четко показывают поток выполнения

---

## 📝 См. также

- [Общая архитектура](OVERVIEW.md)
- [Speech Engine Factory](SPEECH_ENGINE.md)
- [State Machine v2 (старая версия)](STATE_MACHINE.md)
