# 📥 ЗАГРУЗКА МОДЕЛЕЙ SHERPA-ONNX

**Важно:** Модели нужно скачать вручную из-за ограничений HuggingFace.

---

## 🎯 Модели для русского языка

### 1. ASR Модель (распознавание речи)

**Модель:** `sherpa-onnx-zipformer-ru-2024-09-18`  
**Размер:** ~300 MB  
**Язык:** Русский

**Ссылка для скачивания:**
https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18/resolve/main/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz

**Куда сохранить:**
```
sherpa-models/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz
```

**Распаковка:**
```bash
cd sherpa-models
tar -xzf sherpa-onnx-zipformer-ru-2024-09-18.tar.gz
```

**Структура после распаковки:**
```
sherpa-models/
└── sherpa-onnx-zipformer-ru-2024-09-18/
    ├── encoder.onnx
    ├── decoder.onnx
    ├── joiner.onnx
    └── tokens.txt
```

---

### 2. TTS Модель (синтез речи)

**Модель:** `vits-piper-ru-ru-irina-low`  
**Размер:** ~50 MB  
**Язык:** Русский  
**Голос:** Ирина (женский)

**Ссылка для скачивания:**
https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low/resolve/main/ru_RU-irina-low.onnx

**Куда сохранить:**
```
sherpa-models/ru_RU-irina-low.onnx
```

---

## 📋 Копирование в assets

После загрузки и распаковки выполните:

```bash
copy-sherpa-models.bat
```

Или вручную:

1. **ASR модель:**
   ```
   Копировать: sherpa-models/sherpa-onnx-zipformer-ru-2024-09-18/
   Вставить: app/src/main/assets/sherpa/asr-ru-model/
   ```

2. **TTS модель:**
   ```
   Копировать: sherpa-models/ru_RU-irina-low.onnx
   Вставить: app/src/main/assets/sherpa/tts-ru-model/ru_RU-irina-low.onnx
   ```

---

## ✅ Проверка

После копирования проверьте:

```
app/src/main/assets/sherpa/
├── asr-ru-model/
│   ├── encoder.onnx      (должен быть ~100 MB)
│   ├── decoder.onnx      (должен быть ~50 MB)
│   ├── joiner.onnx       (должен быть ~50 MB)
│   └── tokens.txt        (несколько KB)
│
└── tts-ru-model/
    └── ru_RU-irina-low.onnx  (должен быть ~50 MB)
```

---

## 🚀 Альтернативные источники

### ASR Модель (Russian Zipformer)
- **GitHub:** https://github.com/k2-fsa/sherpa-onnx/releases
- **HuggingFace:** https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18

### TTS Модель (Russian VITS)
- **HuggingFace:** https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low
- **Coqui TTS:** https://github.com/coqui-ai/TTS

---

## ❓ Вопросы

### Почему нельзя скачать автоматически?
HuggingFace блокирует автоматические загрузки без аутентификации.

### Сколько места нужно?
~400 MB для обеих моделей.

### Можно ли использовать другие модели?
Да! Смотрите: https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models.html

---

**После загрузки моделей соберите проект:**
```bash
gradlew.bat assembleDebug
```
