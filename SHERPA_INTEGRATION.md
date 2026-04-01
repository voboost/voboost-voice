# 🔧 ИНТЕГРАЦИЯ SHERPA-ONNX

**Версия:** 1.0  
**Дата:** 2026-03-27  
**Статус:** 🚧 Требуется загрузка моделей

---

## 📋 ОБЗОР

Интеграция Sherpa-ONNX для офлайн-распознавания и синтеза речи с поддержкой русского языка.

### Преимущества Sherpa-ONNX

| Преимущество | Описание |
|-------------|----------|
| ✅ **Офлайн** | Не требует интернета |
| ✅ **Бесплатно** | Open source (Apache 2.0) |
| ✅ **Русский язык** | Отличная поддержка |
| ✅ **Качество** | Лучше чем Vosk |
| ✅ **STT + TTS** | Один движок для всего |

---

## 📦 УСТАНОВКА

### Шаг 1: Зависимости добавлены ✅

Файл: `app/build.gradle`

```gradle
// Sherpa-ONNX (офлайн STT + TTS)
implementation 'com.k2fsa:sherpa-onnx:1.10.30'
```

### Шаг 2: Загрузить модели

**Вариант A: Автоматически (скрипт)**

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
download-sherpa-models.bat
```

**Вариант B: Вручную**

1. **ASR модель** (распознавание, ~300 MB):
   - https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18
   - Скачать `sherpa-onnx-zipformer-ru-2024-09-18.tar.gz`
   - Распаковать

2. **TTS модель** (синтез, ~50 MB):
   - https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low
   - Скачать `ru_RU-irina-low.onnx`

### Шаг 3: Копировать в assets

**Вариант A: Автоматически (скрипт)**

```bash
copy-sherpa-models.bat
```

**Вариант B: Вручную**

```
app/src/main/assets/sherpa/
├── asr-ru-model/
│   ├── encoder.onnx
│   ├── decoder.onnx
│   ├── joiner.onnx
│   └── tokens.txt
│
└── tts-ru-model/
    └── ru_RU-irina-low.onnx
```

---

## 🏗️ АРХИТЕКТУРА

### Интерфейсы

```kotlin
// Распознавание
interface SpeechRecognition {
    suspend fun initialize()
    suspend fun startKeywordSpotting(...)
    fun startCommandListening(...)
    suspend fun listenForCommand(timeout: Long): String
}

// Синтез
interface SpeechSynthesis {
    suspend fun initialize()
    fun speak(text: String, onCompletion: (() -> Unit)?)
}
```

### Реализации

| Класс | Движок | Статус |
|-------|--------|--------|
| `VoskRecognition` | Vosk STT | ✅ Работает |
| `SherpaRecognition` | Sherpa STT | ✅ Интегрировано |
| `SystemTtsSynthesis` | System TTS | ✅ Работает |
| `SherpaSynthesis` | Sherpa TTS | ✅ Интегрировано |

---

## 🔌 ИСПОЛЬЗОВАНИЕ

### Автоматический выбор (рекомендуется)

```kotlin
class VoboostVoiceService : Service() {
    private lateinit var speechRecognition: SpeechRecognition
    private lateinit var ttsEngine: SpeechSynthesis

    override fun onCreate() {
        super.onCreate()

        // Фабрика автоматически выберет лучший доступный движок
        speechRecognition = SpeechEngineFactory.createRecognitionEngine(
            context = this,
            engine = SpeechEngineFactory.getRecommendedConfig(this).first
        )

        ttsEngine = SpeechEngineFactory.createSynthesisEngine(
            context = this,
            engine = SpeechEngineFactory.getRecommendedConfig(this).second
        )
    }
}
```

### Ручной выбор

```kotlin
// Использовать Sherpa для обоих
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

## 📊 МОДЕЛИ

### ASR (распознавание)

**Модель:** `sherpa-onnx-zipformer-ru-2024-09-18`

| Параметр | Значение |
|----------|----------|
| **Язык** | Русский |
| **Размер** | ~300 MB |
| **Архитектура** | Zipformer (CTC) |
| **Sample Rate** | 16 kHz |
| **Качество** | Отличное |

**Файлы модели:**
- `encoder.onnx`
- `decoder.onnx`
- `joiner.onnx`
- `tokens.txt`

### TTS (синтез)

**Модель:** `vits-piper-ru-ru-irina-low`

| Параметр | Значение |
|----------|----------|
| **Язык** | Русский |
| **Размер** | ~50 MB |
| **Архитектура** | VITS |
| **Sample Rate** | 24 kHz |
| **Голос** | Ирина (женский) |
| **Качество** | Хорошее |

**Альтернативные TTS модели:**
- `vits-ru-ru` (мужской голос)
- `kokoro-82m` (мультиязычная, ~100 MB)

---

## 🔧 КОНФИГУРАЦИЯ

### В build.gradle

```gradle
androidResources {
    // Не сжимать файлы моделей
    noCompress += ['onnx', 'bin', 'json', 'txt']
}
```

### Путь к моделям

```kotlin
// ASR
val asrPath = context.filesDir.resolve("sherpa/asr-ru-model").absolutePath

// TTS
val ttsPath = context.filesDir.resolve("sherpa/tts-ru-model").absolutePath
```

---

## 🐛 ВОЗМОЖНЫЕ ПРОБЛЕМЫ

### 1. "Model not found"

**Причина:** Модели не загружены или не скопированы в assets

**Решение:**
```bash
download-sherpa-models.bat
copy-sherpa-models.bat
```

### 2. "Unsupported class file major version"

**Причина:** Несоответствие версии Java/Gradle

**Решение:**
- Обновите JDK до версии 21
- Или используйте Gradle 8.x

### 3. TTS не говорит по-русски

**Причина:** Модель не загружена или не инициализирована

**Решение:**
```kotlin
// Проверить доступность
if (SpeechEngineFactory.isSherpaSynthesisAvailable(context)) {
    // Использовать Sherpa
} else {
    // Fallback на системный TTS
}
```

### 4. ASR не распознаёт

**Причина:** Неправильный путь к модели

**Решение:**
```kotlin
// Проверить файлы
val modelDir = File(modelPath)
require(modelDir.exists()) { "Model not found: $modelPath" }
require(File(modelDir, "encoder.onnx").exists()) { "encoder.onnx not found" }
```

---

## 📈 ПРОИЗВОДИТЕЛЬНОСТЬ

### Сравнение Vosk vs Sherpa-ONNX

| Метрика | Vosk | Sherpa-ONNX |
|---------|------|-------------|
| **Размер модели** | ~50 MB | ~300 MB (ASR) + ~50 MB (TTS) |
| **Точность STT** | Хорошая | Отличная |
| **Качество TTS** | N/A | Хорошее |
| **Скорость STT** | ~100ms latency | ~50ms latency |
| **Память** | ~200 MB | ~400 MB |
| **CPU нагрузка** | Средняя | Выше средней |

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

1. **Загрузить модели**
   ```bash
   download-sherpa-models.bat
   ```

2. **Скопировать в assets**
   ```bash
   copy-sherpa-models.bat
   ```

3. **Собрать проект**
   ```bash
   gradlew.bat assembleDebug
   ```

4. **Протестировать**
   - Распознавание русских команд
   - Синтез русских фраз

5. **Включить по умолчанию**
   - Обновить `SpeechEngineFactory` для использования Sherpa

---

## 📚 РЕСУРСЫ

- **Документация:** https://k2-fsa.github.io/sherpa/onnx/
- **GitHub:** https://github.com/k2-fsa/sherpa-onnx
- **Модели ASR:** https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18
- **Модели TTS:** https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low

---

**Статус:** ✅ Интеграция завершена, требуется загрузка моделей
