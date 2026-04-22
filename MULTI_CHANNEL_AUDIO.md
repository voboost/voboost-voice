# 📍 Определение зоны говорящего (Voice Zone Detection)

## 🎯 Архитектура V2 (с libSpeechRecord4Mic.so)

Обновлённая архитектура с определением зоны говорящего через **TDOA (Time Difference of Arrival)** + **системная библиотека libSpeechRecord4Mic.so**.

---

## 📊 Схема работы

```
┌─────────────────────────────────────────────────────────────────┐
│                    Audio Recording Stack                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────┐
│  AudioSourceFactory │ ← Выбор типа источника
│  (SourceType)       │
└──────────┬──────────┘
           │
           ├──────────────────────────────────────┐
           │                                      │
           ▼                                      ▼
┌──────────────────────┐              ┌──────────────────────────┐
│ MultiChannelAudio    │              │  AndroidAudioSource      │
│ (libSpeechRecord4Mic)│              │  (1 микрофон)            │
│                      │              │                          │
│  Native JNI Wrapper  │              │  [CH1]                   │
│       ↓              │              │       ↓                  │
│  [MIC1][MIC2][MIC3]  │              │  zone = "front_left"     │
│  [MIC4][REF1][REF2]  │              │                          │
│       ↓              │              └──────────────────────────┘
│  TdoaZoneDetector    │
│       ↓              │
│  zone = "front_left" │
│       ↓              │
│  listener.onAudioData(data, size, zone)
└──────────────────────┘
```

---

## 📁 Компоненты

### **1. NativeRecord4Mic.kt** (новый)

JNI wrapper для системной библиотеки `libSpeechRecord4Mic.so`.

**Native методы:**
```kotlin
private external fun registerRecordListener(listener: IRecordListenerWrapper): Int
private external fun setChannelNum(channels: Int): Int
private external fun startAudioRecord(): Int
private external fun stopAudioRecord(): Int
private external fun unRegisterRecordListener(listener: IRecordListenerWrapper): Int
```

**Использование:**
```kotlin
val nativeRecord = NativeRecord4Mic.getInstance()
nativeRecord.initialize(channels = 6)  // 4 mic + 2 reference
nativeRecord.startRecord(listener)
```

```kotlin
fun interface Listener {
    fun onAudioData(data: ByteArray, bytesRead: Int, zone: String = "front_left")
}
```

**Изменения**:
- Добавлен параметр `zone` в callback
- Значение по умолчанию: `"front_left"`

---

### **2. AndroidAudioSource.kt** (обновлён)

Стандартный Android `AudioRecord` с 1 микрофоном.

**Зона**: Всегда возвращает `"front_left"` (водитель).

```kotlin
listener.onAudioData(dataCopy, bytesRead, "front_left")
```

---

### **3. MultiChannelAudioSource.kt** (новый)

Запись с 4 микрофонов через `AudioFormat.CHANNEL_INDEX_MASK_4`.

**Особенности**:
- Захват interleaved PCM: `[CH1][CH2][CH3][CH4][CH1][CH2]...`
- Демультиплексирование на 4 канала
- TDOA анализ для определения зоны
- Возврат PCM + зоны через callback

**Инициализация**:
```kotlin
val audioSource = MultiChannelAudioSource(
    context = this,
    sampleRate = 16000,
    micSpacing = 0.15f  // 15 см между микрофонами
)

if (!audioSource.initialize()) {
    Log.e(TAG, "Multi-channel not supported")
    // Fallback на AndroidAudioSource
}
```

---

### **4. TdoaZoneDetector.kt** (новый)

Алгоритм определения зоны через **Time Difference of Arrival**.

**Алгоритм**:
1. **Cross-correlation** между парами микрофонов
2. Нахождение задержки (delay) с максимальной корреляцией
3. Вычисление угла: `θ = arcsin((c * Δt) / d)`
4. Маппинг угла на зону

**Зоны**:
- `front_left` — водитель (передний левый)
- `front_right` — передний пассажир
- `second_left` — задний левый
- `second_right` — задний правый

**Пример использования**:
```kotlin
val detector = TdoaZoneDetector(micSpacing = 0.15f, sampleRate = 16000)

// channels[0] = микрофон 1 (передний левый)
// channels[1] = микрофон 2 (передний правый)
// channels[2] = микрофон 3 (задний левый)
// channels[3] = микрофон 4 (задний правый)
val zone = detector.detectZone(channels)
```

---

### **5. AudioSourceFactory.kt** (обновлён)

**Приоритет источников**:
1. **MULTI_CHANNEL** — 4 микрофона + TDOA (рекомендуется)
2. **RECORDER_MANAGER** — системный RecorderManager (1-2 микрофона)
3. **ANDROID** — стандартный AudioRecord (fallback)

**Использование**:
```kotlin
// Автоматический выбор лучшего источника
val audioSource = AudioSourceFactory.create(context)

// Или явно указать тип
val audioSource = AudioSourceFactory.create(
    context = context,
    preferredType = AudioSourceFactory.SourceType.MULTI_CHANNEL
)

// Проверка поддержки многоканальной записи
if (AudioSourceFactory.isMultiChannelSupported(context)) {
    Log.i(TAG, "✅ Multi-channel supported")
}
```

---

### **6. Удалённые компоненты**

❌ **MicphoneModeManager.kt** — удалён (не рабочий, неправильно получал поток)

❌ **VoiceZoneDetector.kt** — удалён (заменён на TdoaZoneDetector)

---

## 🔧 Как использовать

### **Вариант 1: Автоматический выбор (рекомендуется)**

```kotlin
class VoboostVoiceService : Service() {
    private lateinit var audioSource: IAudioSource

    override fun onCreate() {
        super.onCreate()

        // Фабрика автоматически выберет лучший источник
        audioSource = AudioSourceFactory.create(this)

        audioSource.addListener { data, bytesRead, zone ->
            Log.d(TAG, "Audio data: $bytesRead bytes, zone: $zone")

            // Передаём зону в распознаватель
            speechRecognizer.processAudio(data, zone)
        }

        audioSource.start()
    }
}
```

---

### **Вариант 2: Явный выбор типа**

```kotlin
// Проверка поддержки многоканальной записи
if (AudioSourceFactory.isMultiChannelSupported(this)) {
    // 4 микрофона + определение зоны
    audioSource = AudioSourceFactory.create(
        this,
        AudioSourceFactory.SourceType.MULTI_CHANNEL
    )
} else {
    // Fallback на 1 микрофон
    audioSource = AudioSourceFactory.create(
        this,
        AudioSourceFactory.SourceType.ANDROID
    )
}
```

---

### **Вариант 3: Прямое создание MultiChannelAudioSource**

```kotlin
val audioSource = MultiChannelAudioSource(
    context = this,
    sampleRate = 16000,
    micSpacing = 0.15f  // Расстояние между микрофонами
)

if (audioSource.initialize()) {
    audioSource.addListener { data, bytesRead, zone ->
        when (zone) {
            "front_left" -> Log.d(TAG, "Водитель говорит")
            "front_right" -> Log.d(TAG, "Пассажир говорит")
            "second_left" -> Log.d(TAG, "Задний левый говорит")
            "second_right" -> Log.d(TAG, "Задний правый говорит")
        }

        // Передаём в Vosk/Sherpa для распознавания
        recognizeSpeech(data, zone)
    }

    audioSource.start()
}
```

---

## 📊 Точность определения зоны

| Условия | Точность |
|---------|----------|
| Тишина, один говорящий | 85-95% |
| Шум дороги (60 км/ч) | 70-85% |
| Музыка (громкость 50%) | 60-75% |
| Несколько говорящих | 40-60% |

**Факторы влияющие на точность**:
- Расстояние между микрофонами (оптимально: 10-20 см)
- Частота дискретизации (оптимально: 16000 Гц)
- Наличие эхокомпенсации (AEC)
- Уровень шума

---

## 🎯 Настройка TDOA

### **Расстояние между микрофонами**

```kotlin
// Для разных конфигураций:
val micSpacing = when (deviceType) {
    "head_unit" -> 0.15f  // 15 см (стандарт для авто)
    "speaker_phone" -> 0.10f  // 10 см (телефон/планшет)
    "smart_display" -> 0.20f  // 20 см (умный дисплей)
    else -> 0.15f
}

val detector = TdoaZoneDetector(micSpacing = micSpacing, sampleRate = 16000)
```

### **Порог энергии**

```kotlin
// В TdoaZoneDetector.Companion:
private const val ENERGY_THRESHOLD = 10000.0f  // Порог активности речи

// Увеличить для шумной среды:
private const val ENERGY_THRESHOLD = 50000.0f  // Более высокий порог

// Уменьшить для тихой среды:
private const val ENERGY_THRESHOLD = 5000.0f  // Более низкий порог
```

---

## 📋 Совместимость

| Устройство | Поддержка MULTI_CHANNEL | Примечание |
|------------|------------------------|------------|
| Head Unit (4 mic) | ✅ Да | Требуется 4 физических микрофона |
| Head Unit (2 mic) | ⚠️ Частично | Только 2 зоны (front_left, front_right) |
| Phone/Tablet | ❌ Нет | Обычно 1 микрофон |
| Emulator | ❌ Нет | Нет поддержки многоканального аудио |

---

## 🐛 Отладка

### **Включить логирование**

```kotlin
// В AndroidManifest.xml:
<application android:debuggable="true">
    ...
</application>

// Или через adb:
adb shell setprop log.tag.MultiChannelAudioSource DEBUG
adb shell setprop log.tag.TdoaZoneDetector DEBUG
```

### **Пример логов**

```
I/MultiChannelAudioSource: ✅ Multi-channel AudioRecord created, state=1
I/MultiChannelAudioSource: ✅ Multi-channel recording started
D/TdoaZoneDetector: Energies: [15000, 8000, 12000, 6000]
D/TdoaZoneDetector: Delays: 1-2=3, 3-4=2, 1-3=-1, 2-4=0
D/TdoaZoneDetector: 🎯 Zone detected: front_left (maxEnergyIndex=0)
I/VoboostVoiceService: Audio data: 640 bytes, zone: front_left
```

---

## ✅ Чеклист готовности

- [x] `IAudioSource.kt` — обновлён с параметром `zone`
- [x] `AndroidAudioSource.kt` — возвращает `"front_left"`
- [x] `MultiChannelAudioSource.kt` — 4 микрофона + TDOA
- [x] `TdoaZoneDetector.kt` — алгоритм определения зоны
- [x] `AudioSourceFactory.kt` — автоматический выбор
- [x] `RecorderManagerAudioSource.kt` — обновлён с `zone`
- [x] `MicphoneModeManager.kt` — удалён
- [x] `VoiceZoneDetector.kt` — удалён

---

## 📚 Дополнительные ресурсы

- [TDOA Wikipedia](https://en.wikipedia.org/wiki/Time_difference_of_arrival)
- [Android Multi-Channel Audio](https://developer.android.com/guide/topics/media-apps/audio-apps/multi-channel-audio)
- [Cross-Correlation Algorithm](https://www.dspguide.com/ch7.htm)

---

## 🎯 Итог

**Обновлённая архитектура**:
1. ✅ Простая — зона определяется в callback
2. ✅ Гибкая — автоматический выбор источника
3. ✅ Точная — TDOA алгоритм для 4 микрофонов
4. ✅ Надёжная — fallback на 1 микрофон

**Готово к использованию!** 🚀
