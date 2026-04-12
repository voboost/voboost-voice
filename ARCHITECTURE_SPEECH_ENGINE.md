# 🏗️ АРХИТЕКТУРА SPEECH ENGINE

**Версия:** 1.0  
**Дата:** 2026-03-27  
**Статус:** ✅ Реализовано

---

## 📋 ОБЗОР

Новая модульная архитектура для распознавания и синтеза речи с поддержкой различных движков.

### Ключевые преимущества

| Преимущество | Описание |
|-------------|----------|
| 🔁 **Заменяемость** | Легко менять Vosk ↔ Sherpa без изменения основной логики |
| 🧪 **Тестируемость** | Каждый компонент тестируется отдельно |
| 📦 **Модульность** | Можно отключать ненужные движки |
| 🚀 **Расширяемость** | Легко добавить новый движок (Google Speech API, Yandex SpeechKit) |
| 🛠️ **Поддержка** | Код проще поддерживать и понимать |

---

## 📁 СТРУКТУРА ПАКЕТОВ

```
ru.voboost.voiceassistant/
│
├── core/                          # Ядро - интерфейсы и фабрики
│   ├── SpeechRecognition.kt       # Интерфейс для распознавания
│   ├── SpeechSynthesis.kt         # Интерфейс для синтеза
│   └── SpeechEngineFactory.kt     # Фабрика для создания движков
│
├── engine/                        # Реализации движков
│   ├── sherpa/
│   │   ├── SherpaRecognition.kt   # STT через Sherpa-ONNX
│   │   └── SherpaSynthesis.kt     # TTS через Sherpa-ONNX
│   │
│   ├── vosk/
│   │   └── VoskRecognition.kt     # STT через Vosk (обёртка)
│   │
│   └── system/
│       └── SystemTtsSynthesis.kt  # TTS через системный API
│
├── speech/                        # Старая реализация (для совместимости)
│   └── SpeechRecognitionModule.kt # Vosk (используется через VoskRecognition)
│
└── tts/                           # Старая реализация (для совместимости)
    └── TTSEngine.kt               # Системный TTS (используется через SystemTtsSynthesis)
```

---

## 🔌 ИНТЕРФЕЙСЫ

### SpeechRecognition (STT)

```kotlin
interface SpeechRecognition {
    suspend fun initialize()
    fun isReady(): Boolean
    suspend fun startKeywordSpotting(onKeywordDetected: () -> Unit, onError: suspend (String) -> Unit)
    fun startCommandListening(onCommandReceived: (String) -> Unit, onError: (String) -> Unit, onTimeout: suspend () -> Unit)
    suspend fun listenForCommand(timeout: Long = 3000): String
    fun setActivationKeywords(keywords: List<String>)
    fun stop()
    fun shutdown()
}
```

**Реализации:**
- `VoskRecognition` — текущая стабильная реализация
- `SherpaRecognition` — новая реализация (в разработке)

---

### SpeechSynthesis (TTS)

```kotlin
interface SpeechSynthesis {
    suspend fun initialize()
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

**Реализации:**
- `SystemTtsSynthesis` — системный TTS (стабильная)
- `SherpaSynthesis` — Sherpa-ONNX TTS (в разработке)

---

## 🏭 ФАБРИКА

### SpeechEngineFactory

Автоматически выбирает лучшую конфигурацию:

```kotlin
// Получить рекомендованную конфигурацию
val (asrEngine, ttsEngine) = SpeechEngineFactory.getRecommendedConfig(context)

// Создать движок распознавания
val recognition = SpeechEngineFactory.createRecognitionEngine(
    context = this,
    engine = SpeechEngineFactory.RecognitionEngine.VOSK  // или SHERPA
)

// Создать движок синтеза
val synthesis = SpeechEngineFactory.createSynthesisEngine(
    context = this,
    engine = SpeechEngineFactory.SynthesisEngine.SYSTEM  // или SHERPA
)
```

### Алгоритм выбора

```
1. Если Sherpa ASR + Sherpa TTS доступны → использовать оба
2. Если Sherpa ASR + System TTS доступны → гибрид
3. Иначе → Vosk ASR + System TTS (fallback)
```

---

## 🔄 МИГРАЦИЯ

### Текущее состояние (V3.0)

| Компонент | Реализация | Статус |
|-----------|------------|--------|
| **STT (распознавание)** | VoskRecognition | ✅ Работает |
| **TTS (синтез)** | SystemTtsSynthesis | ✅ Работает |
| **Sherpa STT** | SherpaRecognition | 🚧 В разработке |
| **Sherpa TTS** | SherpaSynthesis | 🚧 В разработке |

### План миграции

#### Этап 1: Интерфейсы ✅ (выполнено)
- [x] Создать интерфейсы `SpeechRecognition` и `SpeechSynthesis`
- [x] Создать обёртки `VoskRecognition` и `SystemTtsSynthesis`
- [x] Обновить `VoboostVoiceService` для работы с интерфейсами
- [x] Создать фабрику `SpeechEngineFactory`

#### Этап 2: Sherpa-ONNX (в процессе)
- [ ] Добавить зависимости Sherpa-ONNX в `build.gradle`
- [ ] Скачать модели для русского языка
- [ ] Реализовать `SherpaRecognition` (STT)
- [ ] Реализовать `SherpaSynthesis` (TTS)

#### Этап 3: Тестирование
- [ ] Протестировать VoskRecognition
- [ ] Протестировать SystemTtsSynthesis
- [ ] Протестировать SherpaRecognition (когда готов)
- [ ] Протестировать SherpaSynthesis (когда готов)

#### Этап 4: Переключение
- [ ] Обновить конфигурацию по умолчанию на Sherpa (если готов)
- [ ] Удалить старый код Vosk (опционально)

---

## 📝 ИСПОЛЬЗОВАНИЕ

### В VoboostVoiceService

```kotlin
class VoboostVoiceService : Service() {
    // Используем интерфейсы
    private lateinit var speechRecognition: SpeechRecognition
    private lateinit var ttsEngine: SpeechSynthesis

    override fun onCreate() {
        super.onCreate()

        // Фабрика автоматически выберет лучшую конфигурацию
        speechRecognition = SpeechEngineFactory.createRecognitionEngine(
            context = this,
            engine = SpeechEngineFactory.RecognitionEngine.VOSK
        )

        ttsEngine = SpeechEngineFactory.createSynthesisEngine(
            context = this,
            engine = SpeechEngineFactory.SynthesisEngine.SYSTEM
        )
    }
}
```

### Переключение на Sherpa

Когда Sherpa-ONNX будет готов, просто измените конфигурацию:

```kotlin
// Было (Vosk + System)
speechRecognition = SpeechEngineFactory.createRecognitionEngine(
    context = this,
    engine = SpeechEngineFactory.RecognitionEngine.VOSK
)

// Стало (Sherpa + Sherpa)
speechRecognition = SpeechEngineFactory.createRecognitionEngine(
    context = this,
    engine = SpeechEngineFactory.RecognitionEngine.SHERPA
)

ttsEngine = SpeechEngineFactory.createSynthesisEngine(
    context = this,
    engine = SpeechEngineFactory.SynthesisEngine.SHERPA
)
```

---

## 🔧 ДОБАВЛЕНИЕ НОВОГО ДВИЖКА

### Шаг 1: Создать реализацию

```kotlin
package ru.voboost.voiceassistant.engine.your_engine

import ru.voboost.voiceassistant.core.SpeechRecognition

class YourEngineRecognition(
    private val context: Context,
    private val modelPath: String
) : SpeechRecognition {

    override suspend fun initialize() {
        // Инициализация
    }

    override fun isReady(): Boolean = true

    // ... остальные методы
}
```

### Шаг 2: Добавить в фабрику

```kotlin
enum class RecognitionEngine {
    VOSK,
    SHERPA,
    YOUR_ENGINE  // ← Добавить новый
}

fun createRecognitionEngine(...): SpeechRecognition {
    return when (engine) {
        RecognitionEngine.VOSK -> VoskRecognition(context)
        RecognitionEngine.SHERPA -> SherpaRecognition(context, modelPath, keywords)
        RecognitionEngine.YOUR_ENGINE -> YourEngineRecognition(context, modelPath)  // ← Добавить
    }
}
```

### Шаг 3: Обновить getRecommendedConfig()

```kotlin
fun getRecommendedConfig(context: Context): Pair<RecognitionEngine, SynthesisEngine> {
    return when {
        // Проверить доступность нового движка
        isYourEngineAvailable(context) -> {
            Pair(RecognitionEngine.YOUR_ENGINE, SynthesisEngine.YOUR_ENGINE)
        }
        // ... остальные проверки
    }
}
```

---

## 📊 СРАВНЕНИЕ ДВИЖКОВ

| Движок | STT | TTS | Русский | Офлайн | Размер | Качество |
|--------|-----|-----|---------|--------|--------|----------|
| **Vosk** | ✅ | ❌ | ✅ | ✅ | ~50 MB | Хорошее |
| **Sherpa-ONNX** | ✅ | ✅ | ✅ | ✅ | ~100 MB | Отличное |
| **System TTS** | ❌ | ✅ | ⚠️ | ✅ | 0 MB | Зависит от устройства |
| **Yandex SpeechKit** | ✅ | ✅ | ✅ | ❌ | 0 MB | Отличное |

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

1. **Добавить Sherpa-ONNX зависимости**
   ```gradle
   implementation 'com.k2fsa.sherpa.onnx:sherpa-onnx:1.10.30'
   ```

2. **Скачать модели**
   - STT: https://github.com/k2-fsa/sherpa-onnx-asr-samples
   - TTS: https://github.com/k2-fsa/sherpa-onnx-tts-samples

3. **Реализовать интеграцию**
   - Заменить TODO в `SherpaRecognition.kt`
   - Заменить TODO в `SherpaSynthesis.kt`

4. **Протестировать**
   - Распознавание русских команд
   - Синтез русских фраз

---

## 📚 РЕСУРСЫ

- **Sherpa-ONNX документация:** https://k2-fsa.github.io/sherpa/onnx/
- **Vosk документация:** https://alphacephei.com/vosk/
- **Модели Sherpa:** https://github.com/k2-fsa/sherpa-onnx/releases

---

**Статус:** ✅ Архитектура реализована, готова к интеграции Sherpa-ONNX
