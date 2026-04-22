# 🚀 Быстрый старт: Многоканальная запись с libSpeechRecord4Mic.so

## ✅ Что готово

1. **NativeRecord4Mic.kt** — JNI wrapper для `libSpeechRecord4Mic.so`
2. **MultiChannelAudioSource.kt** — IAudioSource с TDOA определением зоны
3. **TdoaZoneDetector.kt** — Алгоритм определения зоны
4. **AudioSourceFactory.kt** — Автоматический выбор источника

---

## 🎯 Использование

### **Вариант 1: Автоматический выбор (рекомендуется)**

```kotlin
class VoboostVoiceService : Service() {
    private lateinit var audioSource: IAudioSource

    override fun onCreate() {
        super.onCreate()

        // Фабрика автоматически выберет лучший источник
        // Приоритет: MULTI_CHANNEL → RECORDER_MANAGER → ANDROID
        audioSource = AudioSourceFactory.create(this)

        audioSource.addListener { data, bytesRead, zone ->
            Log.d(TAG, "Audio: $bytesRead bytes, zone: $zone")
            // zone = "front_left", "front_right", "second_left", "second_right"
            
            // Передаём в распознаватель
            speechRecognizer.processAudio(data, zone)
        }

        audioSource.start()
    }
}
```

---

### **Вариант 2: Явное использование MultiChannelAudioSource**

```kotlin
// Проверка поддержки
if (AudioSourceFactory.isMultiChannelSupported(this)) {
    Log.i(TAG, "✅ Multi-channel supported (libSpeechRecord4Mic.so)")
    
    audioSource = MultiChannelAudioSource(
        context = this,
        sampleRate = 16000,
        micSpacing = 0.15f,  // 15 см между микрофонами
        channelCount = 6     // 4 mic + 2 reference для AEC
    )
} else {
    Log.w(TAG, "⚠️ Multi-channel not available, using fallback")
    audioSource = AudioSourceFactory.create(this, AudioSourceFactory.SourceType.ANDROID)
}
```

---

### **Вариант 3: Прямое использование NativeRecord4Mic**

```kotlin
// Для продвинутого использования
val nativeRecord = NativeRecord4Mic.getInstance()

if (nativeRecord.initialize(channels = 6)) {
    val listener = object : IRecordListenerWrapper {
        override fun onData(data: ByteArray, length: Int, channelCount: Int) {
            // data = interleaved PCM: [MIC1][MIC2][MIC3][MIC4][REF1][REF2]...
            Log.d(TAG, "Received $length bytes, $channelCount channels")
        }
        
        override fun onStart() { Log.i(TAG, "Recording started") }
        override fun onStop() { Log.i(TAG, "Recording stopped") }
    }
    
    nativeRecord.startRecord(listener)
}
```

---

## 📊 Зоны

| Зона | Описание |
|------|----------|
| `front_left` | Водитель (передний левый микрофон) |
| `front_right` | Передний пассажир |
| `second_left` | Задний левый пассажир |
| `second_right` | Задний правый пассажир |

---

## 🔧 Настройка

### **Расстояние между микрофонами**

```kotlin
val audioSource = MultiChannelAudioSource(
    context = this,
    micSpacing = when (deviceType) {
        "head_unit" -> 0.15f  // 15 см (стандарт для авто)
        "speaker_phone" -> 0.10f  // 10 см
        else -> 0.15f
    }
)
```

### **Количество каналов**

```kotlin
val channelCount = when {
    needAEC -> 6  // 4 mic + 2 reference для эхокомпенсации
    else -> 4     // Только 4 микрофона
}
```

---

## 🐛 Отладка

### **Включить логирование**

```bash
adb shell setprop log.tag.MultiChannelAudioSource DEBUG
adb shell setprop log.tag.NativeRecord4Mic DEBUG
adb shell setprop log.tag.TdoaZoneDetector DEBUG
```

### **Пример логов**

```
I/MultiChannelAudioSource: ✅ MultiChannelAudioSource initialized (channels=6)
I/MultiChannelAudioSource: ✅ Recording started via libSpeechRecord4Mic.so
D/TdoaZoneDetector: Energies: [15000, 8000, 12000, 6000]
D/TdoaZoneDetector: Delays: 1-2=3, 3-4=2, 1-3=-1, 2-4=0
D/TdoaZoneDetector: 🎯 Zone detected: front_left (maxEnergyIndex=0)
I/VoboostVoiceService: Audio data: 1280 bytes, zone: front_left
```

---

## ⚠️ Важные заметки

### **1. Библиотека системная**

`libSpeechRecord4Mic.so` находится в `/system/lib64/` — **копировать не нужно!**

```kotlin
// NativeRecord4Mic сам загрузит:
init {
    System.loadLibrary("SpeechRecord4Mic")  // Загрузит из /system/lib64/
}
```

### **2. Требуется RECORD_AUDIO permission**

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### **3. Конфликт с QGSpeechService**

Если QGSpeechService уже использует микрофон — будет конфликт!

**Решение:**
- Либо остановить QGSpeechService
- Либо использовать тот же RecorderManager (через `AudioSourceFactory.SourceType.RECORDER_MANAGER`)

### **4. Bluetooth телефон**

При звонке через Bluetooth:
- Микрофон переключается на телефон
- `libSpeechRecord4Mic.so` может вернуть ошибку

**Решение:**
```kotlin
if (bluetoothManager.isScoOn()) {
    // Использовать Bluetooth микрофон
    audioSource = AndroidAudioSource(this, audioSource = MediaRecorder.AudioSource.VOICE_CALL)
} else {
    // Использовать многоканальный
    audioSource = AudioSourceFactory.create(this, AudioSourceFactory.SourceType.MULTI_CHANNEL)
}
```

---

## ✅ Чеклист

- [x] `NativeRecord4Mic.kt` — JNI wrapper
- [x] `MultiChannelAudioSource.kt` — IAudioSource с TDOA
- [x] `TdoaZoneDetector.kt` — Алгоритм определения зоны
- [x] `AudioSourceFactory.kt` — Автоматический выбор
- [x] `IRecordListenerWrapper.kt` — JNI интерфейс
- [x] `VoboostVoiceService.kt` — Обновлён
- [x] `SpeechRecognizer.kt` — Обновлён
- [x] `SpeechEngineFactory.kt` — Обновлён
- [x] `MicphoneModeManager.kt` — Удалён
- [x] `VoiceZoneDetector.kt` — Удалён

---

## 🎯 Итог

**Готовая архитектура:**
1. ✅ `libSpeechRecord4Mic.so` — системная библиотека (4 микрофона)
2. ✅ `NativeRecord4Mic.kt` — JNI wrapper
3. ✅ `MultiChannelAudioSource.kt` — запись + TDOA
4. ✅ `TdoaZoneDetector.kt` — определение зоны
5. ✅ Автоматический fallback на 1 микрофон

**Просто используйте:**
```kotlin
val audioSource = AudioSourceFactory.create(context)
audioSource.addListener { data, size, zone ->
    // zone автоматически определяется!
}
```

**Готово к тестированию!** 🚀
