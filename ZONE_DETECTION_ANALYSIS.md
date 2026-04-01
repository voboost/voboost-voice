# 🎤 ZONE DETECTION — ОПРЕДЕЛЕНИЕ ЗОНЫ ГОВОРЯЩЕГО

**Дата:** 2026-04-01  
**Версия:** Анализ оригинального Ivoka

---

## 📋 ОБЗОР

Оригинальный сервис Ivoka использует **микрофонный массив** и **beamforming** для определения зоны, из которой говорит пассажир.

---

## 🔧 ТЕХНОЛОГИИ

### 1. Микрофонный массив

**Конфигурации:**
- **2 mic** — 2 микрофона (передняя зона)
- **4 mic** — 4 микрофона (передняя + задняя зоны)

**Файлы конфигурации:**
```java
// ConfigMgr.java
public static final String DMASP_4MIC_RES = "dmasp_cfg_bss_madoav4_mccnr.bin";
public static final String ECHO_4MIC_RES = "sspe_aec_ch6_mic4_ref2_taps4_emd0_bsstaps1_asr_v2.0.0.90_20210801.bin";
public static final String FESPCAR_BEAMFORMING_RES = "UDA_asr_ch2_2_ch2_50mm_VPrintSourceEnt_20200115_v1.1.0.8.test.v3_vp.bin";
public static final String WAKEUP_4MIC_RES = "wkp_aicar_botai_lantu_20210906_v2.bin";
```

### 2. Beamforming (формирование луча)

**Зоны beamforming:**
```java
// BeamformingZone.java
public static final int BF_ZONE_FRONT_LEFT = 1;   // Водитель
public static final int BF_ZONE_FRONT_RIGHT = 2;  // Пассажир передний
public static final int BF_ZONE_REAR_LEFT = 4;    // Пассажир задний левый
public static final int BF_ZONE_REAR_RIGHT = 8;   // Пассажир задний правый
public static final int BF_ZONE_NONE = 0;         // Не определено
```

### 3. Voice Print (голосовой отпечаток)

**Файлы:**
```java
public static final String VP_PATH = "vprint_aicar_nihaoxiaobei_20200403_sx3_ssp_outchannel2.bin";
public static final String WAKEUP_VP_PATH = "wkp_aicar_comm_2nd_20201204_v0.23.0_1.bin";
public static final String VP_FDM_PATH = "vprint_aicar_heylantu_20210416_1mic.bin";
```

---

## 📊 API ОПРЕДЕЛЕНИЯ ЗОНЫ

### WakeupManager

```java
// WakeupManager.java
public static final int SPEAKER_DIRECTION_DRIVER = 1;      // Водитель
public static final int SPEAKER_DIRECTION_PASSENGER = 2;   // Пассажир
public static final int SPEAKER_DIRECTION_UNKNOWN = 0;     // Не определено
```

### Регистрация слушателя

```java
// WakeupManager.java
public void addSpeakerDirectionListener(VuiSpeakerDirectionHandler handler) {
    WakeupServiceImpl wakeupServiceImpl = this.mImpl;
    if (wakeupServiceImpl != null) {
        wakeupServiceImpl.implAddSpeakerDirectionListener(handler);
    }
}

public void removeSpeakDirectionListener(VuiSpeakerDirectionHandler handler) {
    WakeupServiceImpl wakeupServiceImpl = this.mImpl;
    if (wakeupServiceImpl != null) {
        wakeupServiceImpl.implRemoveSpeakerDirectionListener(handler);
    }
}
```

### Интерфейс слушателя

```java
// VuiSpeakerDirectionHandler.java
public interface VuiSpeakerDirectionHandler {
    void onProcessSpeakerDirection(int direction);
}
```

**Параметры:**
- `direction = 1` — водитель
- `direction = 2` — пассажир
- `direction = 0` — не определено

---

## 🏗️ АРХИТЕКТУРА

```
┌─────────────────────────────────────────────────────────┐
│              Микрофонный массив (4 mic)                 │
│  ┌─────┐              ┌─────┐                          │
│  │ FL  │              │ FR  │  ← Передние микрофоны    │
│  └─────┘              └─────┘                          │
│  ┌─────┐              ┌─────┐                          │
│  │ RL  │              │ RR  │  ← Задние микрофоны      │
│  └─────┘              └─────┘                          │
└─────────────────────────────────────────────────────────┘
                            ↓
            ┌───────────────────────────────┐
            │   DSP (Digital Signal Proc)   │
            │  ┌─────────────────────────┐  │
            │  │  Beamforming (BSS)      │  │
            │  │  - определение зоны     │  │
            │  │  - подавление шума      │  │
            │  │  - эхокомпенсация (AEC) │  │
            │  └─────────────────────────┘  │
            └───────────────────────────────┘
                            ↓
            ┌───────────────────────────────┐
            │   Wakeup Engine (ASR)         │
            │  - распознавание ключевой     │
            │  - определение направления    │
            └───────────────────────────────┘
                            ↓
            ┌───────────────────────────────┐
            │   VuiSpeakerDirectionHandler  │
            │   onProcessSpeakerDirection() │
            └───────────────────────────────┘
```

---

## 📝 ПРИМЕР ИСПОЛЬЗОВАНИЯ (оригинал)

```java
// Регистрация слушателя
WakeupManager.getInstance().addSpeakerDirectionListener(
    new VuiSpeakerDirectionHandler() {
        @Override
        public void onProcessSpeakerDirection(int direction) {
            switch (direction) {
                case WakeupManager.SPEAKER_DIRECTION_DRIVER:
                    Log.d("Zone", "🎤 Водитель говорит");
                    // Команды только для водителя
                    break;
                    
                case WakeupManager.SPEAKER_DIRECTION_PASSENGER:
                    Log.d("Zone", "🎤 Пассажир говорит");
                    // Ограниченные команды для пассажира
                    break;
                    
                default:
                    Log.d("Zone", "🎤 Зона не определена");
                    break;
            }
        }
    }
);

// Настройка микрофонного массива
// 2mic или 4mic в зависимости от конфигурации
QGSpeechSystemProperties.set("persist.sys.voice.mic_type", "4mic");

// Количество одновременных спикеров
QGSpeechSystemProperties.set("persist.sys.voice.speakers", "4");
```

---

## ⚙️ НАСТРОЙКИ (System Properties)

| Свойство | Значения | Описание |
|----------|----------|----------|
| `persist.sys.voice.mic_type` | "2mic", "4mic" | Тип микрофонного массива |
| `persist.sys.voice.cal_type` | "ivi" | Тип калибровки |
| `persist.sys.voice.speakers` | "1", "2", "4" | Макс. количество спикеров |
| `persist.sys.voice.hotword.dashboard` | "true", "false" | Hotword для dashboard |

---

## 🚗 ЗОНЫ АВТОМОБИЛЯ

### 4-зонная конфигурация (4mic)

```
        ┌─────────────────┐
        │    ПЕРЕДНИЕ     │
        │  ┌───┐   ┌───┐  │
        │  │ 1 │   │ 2 │  │ ← Водитель (1), Пассажир (2)
        │  └───┘   └───┘  │
        │                 │
        │  ┌───┐   ┌───┐  │
        │  │ 3 │   │ 4 │  │ ← Задние пассажиры
        │  └───┘   └───┘  │
        │    ЗАДНИЕ       │
        └─────────────────┘
```

**Зоны:**
1. `front_left` — водитель
2. `front_right` — передний пассажир
3. `second_left` — задний левый
4. `second_right` — задний правый

### 2-зонная конфигурация (2mic)

```
        ┌─────────────────┐
        │    ПЕРЕДНИЕ     │
        │  ┌───┐   ┌───┐  │
        │  │ 1 │   │ 2 │  │ ← Водитель (1), Пассажир (2)
        │  └───┘   └───┘  │
        └─────────────────┘
```

---

## 🔍 КАК ЭТО РАБОТАЕТ

### 1. Инициализация микрофонного массива

```java
// ConfigMgr.java
if ("4mic".equals(mic_type)) {
    this.voiceConfigInfo.setMicSize(4);
    this.voiceConfigInfo.setSpeakersTSTAbilityMax(4);
    
    ArrayList<String> speakerLocations = new ArrayList<>();
    speakerLocations.add("front_left");
    speakerLocations.add("front_right");
    speakerLocations.add("second_left");
    speakerLocations.add("second_right");
    this.voiceConfigInfo.setSpeakerLocations(speakerLocations);
}
```

### 2. Обработка сигнала

**DSP обработка:**
1. **BSS** (Blind Source Separation) — разделение источников
2. **AEC** (Acoustic Echo Cancellation) — подавление эха
3. **NR** (Noise Reduction) — шумоподавление
4. **Beamforming** — формирование луча

### 3. Определение направления

```java
// WakeupServiceImpl.java
public void implAddSpeakerDirectionListener(VuiSpeakerDirectionHandler handler) {
    if (this.mSpeakerDirectionHandler != null) {
        return; // Уже зарегистрирован
    }
    
    this.mSpeakerDirectionHandler = new SpeakerDirectionImplHandler(handler);
    this.mService.addSpeakerDirectionListener(this.mSpeakerDirectionHandler);
}
```

### 4. Callback при активации

```java
// SpeakerDirectionImplHandler.java
@Override
public void onSpeakerDirection(int direction) {
    if (this.vuiSpeakerDirectionHandler != null) {
        this.vuiSpeakerDirectionHandler.onProcessSpeakerDirection(direction);
    }
}
```

---

## 🎯 ОГРАНИЧЕНИЯ ДЛЯ ПАССАЖИРА

Оригинальная система ограничивает команды пассажира:

```java
// ConfigMgr.java
ArrayList<String> passengerVoiceLimitList = new ArrayList<>();
passengerVoiceLimitList.add("navi");  // Только навигация
this.voiceConfigInfo.setPassengerVoiceLimitList(passengerVoiceLimitList);

// Passenger voice limit: 1 = enabled, 0 = disabled
this.voiceConfigInfo.setPassengerVoiceLimit(
    QGSettings.System.getInt(Utils.getContext().getContentResolver(), 
                              PASSENGER_VOICE_LIMIT, 1)
);
```

**Доступно пассажиру:**
- ✅ Навигация
- ❌ Управление автомобилем (окна, двери и т.д.)
- ❌ Настройки системы
- ❌ Звонки

---

## 🔧 ИНТЕГРАЦИЯ В Voboost

### Возможно ли реализовать?

**Проблема:** Vosk и Sherpa-ONNX **не поддерживают** микрофонные массивы и beamforming из коробки.

**Варианты решения:**

### 1. Использовать системный AudioRecord с правильным устройством

```kotlin
// Проверка доступных микрофонов
val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
val devices = audioManager.getDevices()

for (device in devices) {
    if (device.type == AudioDeviceInfo.TYPE_BUILTIN_MIC) {
        Log.d("Mic", "Найдено микрофонов: ${device.channelCounts.size}")
    }
}
```

### 2. Запросить конкретный канал (микрофон)

```kotlin
val audioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()

val audioFormat = AudioFormat.Builder()
    .setChannelMask(AudioFormat.CHANNEL_IN_FRONT)  // Передний микрофон
    // или
    .setChannelMask(AudioFormat.CHANNEL_IN_BACK)   // Задний микрофон
    .build()
```

### 3. Использовать CarAudioManager (если доступно)

```kotlin
val car = Car.createCar(context)
val carAudioManager = car.getCarManager(Car.CAR_AUDIO_SERVICE) as? CarAudioManager

// Запрос зоны
val zoneId = carAudioManager.getZoneForContext()
```

### 4. Внешняя библиотека для beamforming

**Варианты:**
- [Google Speech SDK](https://cloud.google.com/speech-to-text) — поддерживает массивы
- [Respeaker](https://wiki.seeedstudio.com/ReSpeaker/) — готовые решения
- Кастомная DSP обработка (сложно)

---

## 📊 СРАВНЕНИЕ

| Функция | Ivoka (оригинал) | Voboost (текущий) |
|---------|------------------|-------------------|
| **Микрофонный массив** | ✅ 2/4 mic | ❌ 1 mic |
| **Beamforming** | ✅ Есть | ❌ Нет |
| **Определение зоны** | ✅ Driver/Passenger | ❌ Нет |
| **Voice Print** | ✅ Есть | ❌ Нет |
| **Ограничения пассажира** | ✅ Есть | ❌ Нет |

---

## 💡 РЕКОМЕНДАЦИИ

### Минимальная реализация

1. **Использовать системный TTS** с правильным `AudioAttributes`:
   ```kotlin
   .setUsage(AudioAttributes.USAGE_ASSISTANT)
   ```

2. **Добавить определение зоны через CarAudioManager** (если доступно в автомобиле)

3. **Запрашивать разрешение на использование микрофонного массива**:
   ```xml
   <uses-permission android:name="android.permission.CAPTURE_AUDIO_OUTPUT" />
   ```

### Полная реализация

1. **Интегрировать внешнюю библиотеку** для beamforming
2. **Использовать Google Cloud Speech-to-Text** с поддержкой массивов
3. **Добавить Voice Print** для идентификации спикера

---

## 📚 ИСТОЧНИКИ

- `WakeupManager.java` — управление пробуждением
- `BeamformingZone.java` — зоны beamforming
- `ConfigMgr.java` — конфигурация микрофонов
- `VuiSpeakerDirectionHandler.java` — интерфейс слушателя
- `WakeupServiceImpl.java` — реализация сервиса

---

**Вывод:** Оригинальная система использует **аппаратный микрофонный массив + DSP + beamforming** для определения зоны. Для полной совместимости нужна поддержка на уровне системы или внешние библиотеки.
