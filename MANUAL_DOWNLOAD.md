# 📥 РУЧНАЯ ЗАГРУЗКА МОДЕЛЕЙ SHERPA-ONNX

**Причина:** HuggingFace требует аутентификацию для скачивания.

---

## 🎯 Инструкция по загрузке

### Шаг 1: Откройте браузер

Используйте Chrome, Firefox, Edge или любой другой браузер.

---

### Шаг 2: Скачайте ASR модель (распознавание)

**1. Перейдите по ссылке:**
```
https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18/resolve/main/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz
```

**2. Сохраните файл:**
```
sherpa-models/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz
```

**3. Распакуйте архив:**
```bash
cd sherpa-models
tar -xzf sherpa-onnx-zipformer-ru-2024-09-18.tar.gz
```

**Или используйте Windows:**
- Кликните правой кнопкой на файле
- Выберите "Извлечь всё..."
- Извлеките в папку `sherpa-models/`

---

### Шаг 3: Скачайте TTS модель (синтез)

**1. Перейдите по ссылке:**
```
https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low/resolve/main/ru_RU-irina-low.onnx
```

**2. Сохраните файл:**
```
sherpa-models/ru_RU-irina-low.onnx
```

---

### Шаг 4: Скопируйте в assets проекта

**Вариант A: Автоматически**
```bash
copy-sherpa-models.bat
```

**Вариант Б: Вручную**

1. **ASR модель:**
   - Откройте: `sherpa-models/sherpa-onnx-zipformer-ru-2024-09-18/`
   - Скопируйте всё содержимое в:
     ```
     app/src/main/assets/sherpa/asr-ru-model/
     ```

2. **TTS модель:**
   - Скопируйте файл: `sherpa-models/ru_RU-irina-low.onnx`
   - Вставьте в:
     ```
     app/src/main/assets/sherpa/tts-ru-model/ru_RU-irina-low.onnx
     ```

---

## ✅ Проверка

После копирования проверьте структуру:

```
app/src/main/assets/sherpa/
├── asr-ru-model/
│   ├── encoder.onnx      (~100 MB)
│   ├── decoder.onnx      (~50 MB)
│   ├── joiner.onnx       (~50 MB)
│   └── tokens.txt        (несколько KB)
│
└── tts-ru-model/
    └── ru_RU-irina-low.onnx  (~50 MB)
```

**Проверьте размеры файлов:**
- `encoder.onnx` > 50 MB ✓
- `decoder.onnx` > 20 MB ✓
- `joiner.onnx` > 20 MB ✓
- `ru_RU-irina-low.onnx` > 20 MB ✓

Если файлы маленькие (< 1 MB) - загрузка не удалась!

---

## 🔧 Альтернативные источники

### Если HuggingFace не работает:

**1. ModelScope (китайское зеркало):**
- ASR: https://www.modelscope.cn/models/k2-fsa/sherpa-onnx-zipformer-ru-2024-09-18
- TTS: https://www.modelscope.cn/models/k2-fsa/vits-piper-ru-ru-irina-low

**2. GitHub Releases:**
- https://github.com/k2-fsa/sherpa-onnx/releases

**3. Официальная документация:**
- https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models.html

---

## 🚀 После загрузки

**1. Соберите проект:**
```bash
gradlew.bat assembleDebug
```

**2. Установите на устройство:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**3. Переключите на Sherpa в коде:**

В `VoboostVoiceService.kt` (строка ~140):
```kotlin
// TTS Engine
ttsEngine = SpeechEngineFactory.createSynthesisEngine(
    context = this,
    engine = SpeechEngineFactory.SynthesisEngine.SHERPA  // ← Измените с SYSTEM на SHERPA
)

// Speech Recognition
speechRecognition = SpeechEngineFactory.createRecognitionEngine(
    context = this,
    engine = SpeechEngineFactory.RecognitionEngine.SHERPA  // ← Измените с VOSK на SHERPA
)
```

**4. Протестируйте:**
- Запустите приложение
- Скажите: "привет воях"
- Скажите команду: "открой лючок зарядки"

---

## ❓ Вопросы

### Сколько места нужно?
~400 MB для обеих моделей.

### Можно ли использовать другие модели?
Да! Смотрите: https://k2-fsa.github.io/sherpa/onnx/

### Что если модель не работает?
Проверьте:
1. Размер файлов (> 20 MB)
2. Правильность пути в assets
3. Логи: `adb logcat | grep -i "sherpa"`

---

**Удачи! 🚀**
