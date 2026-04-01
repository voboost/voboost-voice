# ✅ ЗАГРУЖЕННЫЕ МОДЕЛИ

## ✅ ASR Модель (распознавание) - ЗАГРУЖЕНА!

**Модель:** `sherpa-onnx-zipformer-ru-2024-09-18`  
**Размер:** 332 MB ✓  
**Статус:** ✅ **ЗАГРУЖЕНА И РАСПАКОВАНА**

**Путь:**
```
sherpa-models/sherpa-onnx-zipformer-ru-2024-09-18/
├── encoder.onnx      (259 MB)
├── decoder.onnx      (2 MB)
├── joiner.onnx       (1 MB)
├── decoder.int8.onnx (1 MB)
├── encoder.int8.onnx (68 MB)
└── joiner.int8.onnx  (259 KB)
```

---

## ⏳ TTS Модель (синтез) - ТРЕБУЕТСЯ ЗАГРУЗИТЬ

**Модель:** `ru_RU-irina-medium.onnx` (Piper TTS)  
**Размер:** ~60 MB  
**Статус:** ⏳ **ТРЕБУЕТСЯ ЗАГРУЗИТЬ ВРУЧНУЮ**

### Способ 1: GitHub Piper (рекомендуется)

**Ссылка:**
```
https://huggingface.co/rhasspy/piper-voices/resolve/main/ru/ru_RU/irina/medium/ru_RU-irina-medium.onnx
```

**Куда сохранить:**
```
sherpa-models/ru_RU-irina-medium.onnx
```

### Способ 2: HuggingFace Piper

**Страница модели:**
```
https://huggingface.co/rhasspy/piper-voices/tree/main/ru/ru_RU/irina/medium
```

1. Откройте ссылку в браузере
2. Нажмите на файл `ru_RU-irina-medium.onnx`
3. Нажмите кнопку "Download"
4. Сохраните в `sherpa-models/`

---

## 📋 КОПИРОВАНИЕ В ASSETS

После загрузки TTS модели выполните:

```bash
copy-sherpa-models.bat
```

Или вручную:

### 1. ASR модель (уже готова!)

```
Копировать: sherpa-models/sherpa-onnx-zipformer-ru-2024-09-18/
Вставить: app/src/main/assets/sherpa/asr-ru-model/
```

Файлы должны быть:
```
app/src/main/assets/sherpa/asr-ru-model/
├── encoder.onnx      ✓
├── decoder.onnx      ✓
├── joiner.onnx       ✓
└── tokens.txt        ✓
```

### 2. TTS модель (после загрузки)

```
Копировать: sherpa-models/ru_RU-irina-medium.onnx
Вставить: app/src/main/assets/sherpa/tts-ru-model/ru_RU-irina-medium.onnx
```

---

## 🚀 СБОРКА ПРОЕКТА

После копирования моделей:

```bash
gradlew.bat clean assembleDebug
```

---

## 🔧 ПЕРЕКЛЮЧЕНИЕ НА SHERPA

В файле `VoboostVoiceService.kt` (строка ~140):

```kotlin
// TTS Engine - переключить на Sherpa
ttsEngine = SpeechEngineFactory.createSynthesisEngine(
    context = this,
    engine = SpeechEngineFactory.SynthesisEngine.SHERPA  // ← Измените!
)

// Speech Recognition - переключить на Sherpa
speechRecognition = SpeechEngineFactory.createRecognitionEngine(
    context = this,
    engine = SpeechEngineFactory.RecognitionEngine.SHERPA  // ← Измените!
)
```

---

## ✅ ПРОВЕРКА

После сборки проверьте логи:

```bash
adb logcat | grep -i "sherpa"
```

Должно быть:
```
SherpaRecognition: Sherpa-ONNX recognition initialized successfully!
SherpaSynthesis: Sherpa-ONNX TTS initialized successfully!
```

---

**Обновлено:** 2026-03-28  
**Статус:** ASR ✓ | TTS ⏳
