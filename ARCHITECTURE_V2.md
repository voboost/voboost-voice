# 🏗️ НОВАЯ АРХИТЕКТУРА VOBSKRECOGNITION

**Дата:** 2026-04-04
**Версия:** 16.0 (State Machine Refactoring)
**Статус:** ✅ ГОТОВО К ТЕСТИРОВАНИЮ

---

## 📋 ПРОБЛЕМЫ СТАРОЙ АРХИТЕКТУРЫ

### Было:

```kotlin
class VoskRecognition(...) {
    @Volatile private var isRunning = false
    @Volatile private var isListeningForKeyword = false
    @Volatile private var isProcessingCommand = false
    
    // 470 строк кода со всем подряд
    // - Загрузка модели
    // - Проверка разрешений
    // - Буферизация аудио
    // - Распознавание ключевой фразы
    // - Распознавание команд
    // - Парсинг JSON
    // - Управление корутинами
}
```

**Проблемы:**
- ❌ **Race conditions** с флагами состояния
- ❌ **Перезапуск** распознавания — можно пропустить ключевое слово
- ❌ **Нарушение SRP** — слишком много обязанностей
- ❌ **Сложно тестировать** — всё в одном классе

---

## ✅ НОВАЯ АРХИТЕКТУРА

### Компоненты:

```
VoskRecognition (координатор, ~100 строк)
├── VoskModelLoader (загрузка моделей)
├── VoskStream (распознавание PCM → Text)
├── KeywordChecker (проверка ключевых слов)
└── SpeechStateMachine (управление состоянием)
```

### Поток данных:

```
AudioSource (TransProxy)
    ↓ PCM данные
SpeechStateMachine (НЕПРЕРЫВНЫЙ ПОТОК)
    ↓
VoskStream.acceptWaveform(pcm)
    ↓ RecognitionResult
KeywordChecker.isActivationKeyword(text)
    ↓ true/false
Callbacks.onKeywordDetected() / onCommandReceived()
```

---

## 📁 НОВЫЕ ФАЙЛЫ

| Файл | Ответственность | Строк |
|------|-----------------|-------|
| `VoskModelLoader.kt` | Загрузка модели Vosk | ~40 |
| `VoskStream.kt` | Распознавание PCM → Text | ~90 |
| `KeywordChecker.kt` | Проверка ключевых слов | ~35 |
| `SpeechStateMachine.kt` | Управление состоянием | ~200 |
| `VoskRecognition.kt` | Координация компонентов | ~100 |

---

## 🔧 КАК ЭТО РАБОТАЕТ

### 1. VoskModelLoader

```kotlin
val modelLoader = VoskModelLoader()
val model = modelLoader.loadModel("/path/to/model")
val recognizer = modelLoader.createRecognizer(model)
```

**Ответственность:** ТОЛЬКО загрузка модели из файловой системы.

### 2. VoskStream

```kotlin
val voskStream = VoskStream(recognizer)

// Принять PCM данные
val result = voskStream.acceptWaveform(pcmBytes)

if (result?.isFinal == true) {
    println("Распознано: ${result.text}")
}
```

**Ответственность:** ТОЛЬКО преобразование PCM → Text.

### 3. KeywordChecker

```kotlin
val keywordChecker = KeywordChecker(configManager)

if (keywordChecker.isActivationKeyword("привет машина")) {
    // Ключевое слово распознано!
}
```

**Ответственность:** ТОЛЬКО проверка является ли текст ключевым словом.

### 4. SpeechStateMachine

```kotlin
val stateMachine = SpeechStateMachine(
    audioSource = audioSource,
    voskStream = voskStream,
    keywordChecker = keywordChecker
)

// Запустить распознавание
stateMachine.start(SpeechCallbacks(
    onKeywordDetected = { /* Ключевое слово */ },
    onCommandReceived = { text -> /* Команда */ },
    onError = { error -> /* Ошибка */ },
    onTimeout = { /* Таймаут */ }
))

// Активировать (после ключевого слова)
stateMachine.activate()
stateMachine.startListeningCommand()

// Вернуться к ожиданию ключевого слова
stateMachine.returnToKeywordListening()
```

**Ответственность:** ТОЛЬКО управление состоянием распознавания.

---

## 🎯 КЛЮЧЕВОЕ ОТЛИЧИЕ: НЕПРЕРЫВНЫЙ ПОТОК

### Было (ПЕРЕЗАПУСК):

```kotlin
// Отписаться
audioSource.removeListener(listener)
// ... пользователь говорит "привет машина" ← ПРОПУЩЕНО!
// Подписаться
audioSource.addListener(listener)
```

### Стало (НЕПРЕРЫВНЫЙ ПОТОК):

```kotlin
// Один поток работает ВСЕГДА
audioSource.addListener { pcm ->
    when (state) {
        LISTENING_KEYWORD -> checkKeyword(pcm)
        LISTENING_COMMAND -> recognizeCommand(pcm)
    }
}
// Никаких перезапусков!
```

---

## 📊 СОСТОЯНИЯ STATE MACHINE

```
STOPPED → LISTENING_KEYWORD (start())
LISTENING_KEYWORD → ACTIVATED (ключевое слово)
ACTIVATED → LISTENING_COMMAND (activate() + startListeningCommand())
LISTENING_COMMAND → LISTENING_KEYWORD (команда выполнена)
LISTENING_KEYWORD → LISTENING_KEYWORD (таймаут, сброс recognizer)
```

---

## 🚀 КАК ИСПОЛЬЗОВАТЬ

### Базовое использование:

```kotlin
// Создать AudioSource
val audioSource = AudioSourceFactory.create(context, AudioSourceFactory.SourceType.TRANSPROXY)

// Создать VoskRecognition
val voskRecognition = VoskRecognition(context, audioSource)

// Инициализировать
voskRecognition.initialize()

// Запустить распознавание ключевого слова
voskRecognition.startKeywordSpotting(
    onKeywordDetected = {
        Log.i("Vosk", "🎯 Ключевое слово распознано!")
        
        // Активировать и слушать команду
        voskRecognition.startCommandListening(
            onCommandReceived = { command ->
                Log.i("Vosk", "📝 Команда: $command")
                // Обработать команду...
                
                // Вернуться к ожиданию ключевого слова
                voskRecognition.startKeywordSpotting(...)
            },
            onError = { error -> Log.e("Vosk", "Ошибка: $error") },
            onTimeout = {
                Log.w("Vosk", "Таймаут команды")
                voskRecognition.startKeywordSpotting(...)
            }
        )
    },
    onError = { error -> Log.e("Vosk", "Ошибка: $error") }
)
```

---

## 🐛 ОТЛАДКА

### Логи состояний:

```bash
adb logcat -s SpeechStateMachine:* VoskRecognition:*
```

**Ожидаемые логи:**
```
SpeechStateMachine: State transition: STOPPED → LISTENING_KEYWORD
SpeechStateMachine: Starting recognition via MicrophoneStreamAudioSource
VoskRecognition: 🎯 KEYWORD DETECTED: привет машина
SpeechStateMachine: State transition: LISTENING_KEYWORD → ACTIVATED
SpeechStateMachine: State transition: ACTIVATED → LISTENING_COMMAND
VoskRecognition: 📝 COMMAND RECEIVED: открой окно
SpeechStateMachine: State transition: LISTENING_COMMAND → LISTENING_KEYWORD
```

---

## 📝 СРАВНЕНИЕ

| Характеристика | Было | Стало |
|----------------|------|-------|
| **Строк кода** | 470 | ~465 (разделено на 5 файлов) |
| **Флаги состояния** | 3 @Volatile | 1 enum SpeechState |
| **Перезапуск** | ❌ Да | ✅ Нет |
| **Race conditions** | ❌ Возможны | ✅ Нет |
| **Тестируемость** | ❌ Сложно | ✅ Легко |
| **SRP** | ❌ Нарушен | ✅ Соблюден |

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

### 1. Протестировать на устройстве

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleRelease

adb root && adb remount
adb push app\build\outputs\apk\release\app-release-unsigned.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk

adb shell am force-stop ru.voboost.voiceassistant
adb shell am start-foreground-service -n ru.voboost.voiceassistant/.VoboostVoiceService

adb logcat -s SpeechStateMachine:* VoskRecognition:*
```

### 2. Проверить что ключевое слово не пропускается

- Сказать "привет машина" сразу после активации
- Убедиться что распознаётся

### 3. Проверить переходы состояний

- Ключевое слово → ACTIVATED → LISTENING_COMMAND
- Команда → LISTENING_KEYWORD
- Таймаут → LISTENING_KEYWORD

---

**Последнее обновление:** 2026-04-04
**Следующая задача:** Протестировать State Machine на устройстве
