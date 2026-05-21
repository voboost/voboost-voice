# Speech Engine Factory

**Дата:** 2026-05-15  
**Статус:** ✅ Реализовано  
**Файл:** `core/SpeechEngineFactory.kt`  

---

## 🎯 Обзор

Фабрика для создания движков распознавания и синтеза речи. Позволяет легко переключаться между различными реализациями (Vosk, Sherpa-ONNX, System TTS) без изменения основной логики.

---

## 🏗️ Архитектура

```
┌──────────────────────────────────────────────────────────┐
│              SpeechEngineFactory                         │
│          (выбор и создание движков)                      │
└──────────────┬───────────────────────────────────────────┘
               │
      ┌────────┴────────┐
      │                 │
      ▼                 ▼
┌───────────┐    ┌────────────┐
│ STT Engines│    │ TTS Engines│
└────┬──────┘    └────┬───────┘
     │                │
  ┌──┴────┐       ┌───┴────┐
  │Vosk   │       │System  │
  │Sherpa │       │Sherpa  │
  └────────┘       └────────┘
```

---

## 📋 Интерфейсы

### ISpeechRecognizer (STT)

```kotlin
interface ISpeechRecognizer {
    suspend fun initialize(): Boolean
    fun isReady(): Boolean
    
    suspend fun startKeywordSpotting(
        onKeywordDetected: () -> Unit,
        onError: suspend (String) -> Unit
    )
    
    fun startCommandListening(
        onCommandReceived: (String) -> Unit,
        onError: (String) -> Unit,
        onTimeout: suspend () -> Unit
    )
    
    suspend fun listenForCommand(timeout: Long = 3000): String
    
    fun setActivationKeywords(keywords: List<String>)
    fun stop()
    fun shutdown()
}
```

### ISpeechSynthesis (TTS)

```kotlin
interface ISpeechSynthesis {
    suspend fun initialize(): Boolean
    fun isReady(): Boolean
    
    fun speak(text: String, onCompletion: (() -> Unit)? = null)
    fun stop()
    
    fun setRate(rate: Float)
    fun setPitch(pitch: Float)
    fun isAvailable(): Boolean
    fun clearQueue()
    fun shutdown()
}
```

---

## 🏭 Фабричные методы

### Создание STT движка

```kotlin
object SpeechEngineFactory {
    
    enum class RecognitionEngine {
        VOSK,       // Офлайн распознавание (Vosk)
        SHERPA,     // Офлайн распознавание (Sherpa-ONNX)
        SYSTEM      // Системный API (не поддерживает офлайн STT)
    }
    
    fun createRecognitionEngine(
        context: Context,
        engine: RecognitionEngine = getRecommendedEngine().first
    ): ISpeechRecognizer {
        return when (engine) {
            RecognitionEngine.VOSK -> VoskRecognition(context)
            RecognitionEngine.SHERPA -> SherpaRecognition(context, modelPath)
            RecognitionEngine.SYSTEM -> error("System doesn't support STT")
        }
    }
    
    private fun getRecommendedEngine(): Pair<RecognitionEngine, SynthesisEngine> {
        // Приоритет:Sherpa + Sherpa
        if (isSherpaAvailable()) return Pair(RecognitionEngine.SHERPA, SynthesisEngine.SHERPA)
        // Fallback: Vosk + System
        return Pair(RecognitionEngine.VOSK, SynthesisEngine.SYSTEM)
    }
}
```

### Создание TTS движка

```kotlin
object SpeechEngineFactory {
    
    enum class SynthesisEngine {
        SYSTEM,     // Системный TTS Android
        SHERPA      // Sherpa-ONNX TTS (лучшее качество)
    }
    
    fun createSynthesisEngine(
        context: Context,
        engine: SynthesisEngine = getRecommendedEngine().second
    ): ISpeechSynthesis {
        return when (engine) {
            SynthesisEngine.SYSTEM -> SystemSpeechSynthesis(context)
            SynthesisEngine.SHERPA -> SherpaSpeechSynthesis(context, modelPath)
        }
    }
}
```

---

## 🔄 Алгоритм выбора движка

```
1. Проверить доступность Sherpa-ONNX
   ├─ Доступна → Использовать Sherpa STT + Sherpa TTS
   └─ Не доступна → Перейти к шагу 2

2. Проверить доступность Vosk
   ├─ Доступен → Использовать Vosk STT + System TTS
   └─ Не доступен → Перейти к шагу 3

3. Fallback: Системный TTS (без распознавания)
```

---

## 📊 Движки

### Vosk (STT)

| Параметр | Значение |
|----------|----------|
| Тип | Офлайн |
| Язык | Русский |
| Размер модели | ~50 MB |
| Качество | Хорошее |
| Скорость | 200-500ms |

**Использование:**
```kotlin
val vosk = VoskRecognition(context)
vosk.setActivationKeywords(listOf("привет машина", "привет вобуст"))
```

### Sherpa-ONNX (STT + TTS)

| Параметр | Значение |
|----------|----------|
| Тип | Офлайн |
| Язык | Русский |
| Размер модели | ~100 MB |
| Качество | Отличное |
| Скорость | STT: 150-400ms, TTS: 300-800ms |

**Использование:**
```kotlin
val sherpaSTT = SherpaRecognition(context, modelPath)
val sherpaTTS = SherpaSpeechSynthesis(context, modelPath)
```

### System TTS

| Параметр | Значение |
|----------|----------|
| Тип | Системный |
| Язык | Зависит от устройства |
| Размер модели | 0 MB (встроенный) |
| Качество | Переменное |
| Скорость | Быстрое |

**Использование:**
```kotlin
val systemTTS = SystemSpeechSynthesis(context)
systemTTS.speak("Привет, водитель!")
```

---

## 📁 Структура пакетов

```
ru.voboost.voice/
├── core/                          # Интерфейсы и фабрики
│   ├── ISpeechRecognizer.kt       # STT интерфейс
│   ├── ISpeechSynthesis.kt        # TTS интерфейс
│   └── SpeechEngineFactory.kt     # Фабрика выбора движков
│
├── engine/                        # Реализации движков
│   ├── sherpa/
│   │   ├── SherpaRecognition.kt   # STT через Sherpa-ONNX
│   │   └── SherpaSpeechSynthesis.kt # TTS через Sherpa-ONNX
│   ├── vosk/
│   │   └── VoskRecognition.kt     # STT через Vosk
│   └── system/
│       └── SystemSpeechSynthesis.kt # Системный TTS
```

---

## 🧪 Тестирование

### Проверка выбранного движка

```bash
adb logcat | grep -i "SpeechEngineFactory"
```

**Ожидаемые логи:**
```
I/SpeechEngineFactory: Creating VoskRecognition (offline STT)
I/SpeechEngineFactory: Creating SystemSpeechSynthesis (system TTS)
```

---

## 🎯 Расширение

### Добавление нового движка STT

```kotlin
// 1. Создать реализацию интерфейса
class MySTTDengine : ISpeechRecognizer {
    override suspend fun initialize(): Boolean { ... }
    override fun isReady(): Boolean { ... }
    // ... остальные методы
}

// 2. Обновить SpeechEngineFactory
enum class RecognitionEngine {
    VOSK, SHERPA, MY_ENGINE  // ← Новый
}

fun createRecognitionEngine(engine: RecognitionEngine): ISpeechRecognizer {
    return when (engine) {
        RecognitionEngine.MY_ENGINE -> MySTTDengine(context)
        // ... остальные
    }
}
```

---

## 📊 Сравнение движков

| Движок | STT | TTS | Русский | Офлайн | Размер | Качество |
|--------|-----|-----|---------|--------|--------|----------|
| **Vosk** | ✅ | ❌ | ✅ | ✅ | ~50 MB | Хорошее |
| **Sherpa-ONNX** | ✅ | ✅ | ✅ | ✅ | ~100 MB | Отличное |
| **System TTS** | ❌ | ✅ | ⚠️ | ✅ | 0 MB | Переменное |

---

## 📝 Использование в VoboostVoiceService

```kotlin
class VoboostVoiceService : Service() {
    private lateinit var speechRecognition: ISpeechRecognizer
    private lateinit var ttsEngine: ISpeechSynthesis
    
    override fun onCreate() {
        super.onCreate()
        
        // Автоматический выбор лучших движков
        val (sttEngine, ttsEngineType) = SpeechEngineFactory.getRecommendedEngine()
        
        speechRecognition = SpeechEngineFactory.createRecognitionEngine(
            context = this,
            engine = sttEngine
        )
        
        ttsEngine = SpeechEngineFactory.createSynthesisEngine(
            context = this,
            engine = ttsEngineType
        )
    }
}
```

---

## 📚 Ресурсы

- [Vosk Documentation](https://alphacephei.com/vosk/)
- [Sherpa-ONNX Documentation](https://k2-fsa.github.io/sherpa/onnx/)
- [Android TTS API](https://developer.android.com/guide/topics/text/text-to-speech)

---

## 📝 См. также

- [Общая архитектура](../ARCHITECTURE/OVERVIEW.md)
- [State Machine](../ARCHITECTURE/STATE_MACHINE.md)
- [Command Executor](../ARCHITECTURE/COMMAND_EXECUTOR.md)
