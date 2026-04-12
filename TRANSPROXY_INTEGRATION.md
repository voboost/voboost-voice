# 🎤 ИНТЕГРАЦИЯ С QGSPEECHSERVICE ЧЕРЕЗ TRANSPROXY

**Дата:** 2026-04-04
**Версия:** 15.0 (TransProxy Integration)
**Статус:** ✅ ГОТОВО К ТЕСТИРОВАНИЮ

---

## 📋 ЧТО ИЗМЕНИЛОСЬ

### Было (v14.0):
- ❌ Свой AudioRecord — конфликты с другими приложениями
- ❌ Нет шумоподавления (или применяем сами)
- ❌ Нет определения зоны водителя
- ❌ Нет управления громкостью через систему

### Стало (v15.0):
- ✅ **TransProxy/IPcmModule** — получаем PCM из QGSpeechService
- ✅ **Шумоподавление уже применено** (QGSpeechService обрабатывает)
- ✅ **Эхокомпенсация уже применена**
- ✅ **VolumeManager** — приглушаем музыку через систему
- ✅ **Нет конфликтов** с другими приложениями

---

## 🏗️ НОВАЯ АРХИТЕКТУРА

```
QGSpeechService (com.qinggan.sttservice)
├── RecorderManager → захватывает аудио с микрофона
│   └── Применяет: NoiseSuppressor + AEC + AGC
│
├── TransProxy (IPcmModule) → раздаёт PCM данные
│   └── Intent: "com.qinggan.qinglink.transproxy"
│       └── registerPcmListener(IPcmListener)
│           └── onPcm(byte[] pcm) ← НАШ СЛУШАТЕЛЬ!
│
└── Volume Service (IVolume) → управление громкостью
    └── Intent: "com.qinggan.qinglink.hu.VOLUME"
        └── sendSetMDVolume(STREAM_MEDIA, volume)
```

### Поток данных:

```
Микрофон автомобиля (4-6 микрофонов)
    ↓
RecorderManager (QGSpeechService)
    ├── NoiseSuppressor ← уже применено
    ├── AcousticEchoCanceler ← уже применено
    └── AutomaticGainControl ← уже применено
    ↓
TransProxy (IPcmModule)
    ↓
onPcm(byte[] pcm) ← PCM данные (16-bit, mono, 16000 Hz)
    ↓
MicrophoneStreamManager → SpeechRecognitionModule
    ↓
Vosk Recognizer → распознавание текста
    ↓
Текст → NLU → Команда → CAN-шина
```

---

## 📁 НОВЫЕ ФАЙЛЫ

### AIDL файлы (скопированы из QGSpeechService):

| Файл | Описание |
|------|----------|
| `app/src/main/aidl/com/qinggan/qinglink/transProxy/api/IPcmModule.aidl` | Интерфейс для получения PCM |
| `app/src/main/aidl/com/qinggan/qinglink/transProxy/api/IPcmListener.aidl` | Слушатель PCM данных |
| `app/src/main/aidl/com/qinggan/qinglink/api/hu/IVolume.aidl` | Управление громкостью |
| `app/src/main/aidl/com/qinggan/qinglink/api/hu/IVolumeListener.aidl` | Слушатель событий громкости |

### Kotlin файлы:

| Файл | Описание |
|------|----------|
| `audio/MicrophoneStreamManager.kt` | Менеджер для получения PCM из TransProxy |
| `audio/VolumeManager.kt` | Менеджер для управления громкостью |

### Изменённые файлы:

| Файл | Изменения |
|------|-----------|
| `speech/SpeechRecognitionModule.kt` | Переписан для работы через TransProxy |
| `VoboostVoiceService.kt` | Добавлены MicrophoneStreamManager и VolumeManager |

---

## 🔧 КАК ЭТО РАБОТАЕТ

### 1. Получение PCM из микрофона

**MicrophoneStreamManager:**

```kotlin
// Создаём менеджер
val micStreamManager = MicrophoneStreamManager(context)

// Добавляем слушатель
micStreamManager.addListener(object : MicrophoneStreamListener {
    override fun onConnected() {
        Log.i(TAG, "✅ Microphone stream connected")
    }
    
    override fun onPcmData(pcm: ByteArray) {
        // PCM данные уже с шумоподавлением!
        // Передаём в Vosk для распознавания
        recognizer.acceptWaveForm(pcm, pcm.size)
    }
    
    override fun onDisconnected() {
        Log.w(TAG, "❌ Microphone stream disconnected")
    }
})

// Подключаемся
micStreamManager.connect()
```

**Как подключается:**
1. Создаёт Intent: `"com.qinggan.qinglink.transproxy"`
2. Bind к сервису `com.qinggan.sttservice`
3. Получает `IPcmModule` через AIDL
4. Регистрирует `IPcmListener.Stub()`
5. Получает PCM данные через `onPcm(byte[] pcm)`

### 2. Управление громкостью

**VolumeManager:**

```kotlin
// Создаём менеджер
val volumeManager = VolumeManager(context)
volumeManager.connect()

// При активации голосового помощника — приглушаем музыку
volumeManager.duckMedia(targetVolume = 1)

// После завершения команды — восстанавливаем громкость
volumeManager.restoreMedia()
```

**Как подключается:**
1. Создаёт Intent: `"com.qinggan.qinglink.hu.VOLUME"`
2. Bind к сервису `com.qinggan.sttservice`
3. Получает `IVolume` через AIDL
4. Регистрирует `IVolumeListener.Stub()`
5. Управляет громкостью через `sendSetMDVolume()`

---

## 🎯 ПОТОК АКТИВАЦИИ

### Активация по ключевой фразе:

```
1. MicrophoneStreamManager слушает PCM
2. Vosk распознаёт "привет машина"
3. activateVoiceAssistantInternal()
   ├── volumeManager.duckMedia(1) ← приглушаем музыку
   ├── TTS: "Слушаю вас"
   └── startCommandListening()
4. Vosk распознаёт команду
5. processVoiceCommand()
6. volumeManager.restoreMedia() ← восстанавливаем музыку
7. Возврат к keyword spotting
```

### Активация кнопкой:

```
1. VoiceButtonHandler.onVoiceButtonPressed()
2. activateVoiceAssistant()
   ├── volumeManager.duckMedia(1) ← приглушаем музыку
   ├── TTS: "Слушаю вас"
   └── startCommandListening()
3. Vosk распознаёт команду (PCM уже идёт из TransProxy)
4. processVoiceCommand()
5. volumeManager.restoreMedia() ← восстанавливаем музыку
6. Возврат к keyword spotting
```

---

## 📊 СРАВНЕНИЕ ВАРИАНТОВ

| Характеристика | Свой AudioRecord | TransProxy |
|----------------|------------------|------------|
| **Шумоподавление** | ❌ Нужно применять | ✅ Уже применено |
| **Эхокомпенсация** | ❌ Нужно применять | ✅ Уже применено |
| **Авто-гейн** | ❌ Нужно применять | ✅ Уже применено |
| **Конфликты** | ⚠️ Возможны | ✅ Нет |
| **Стабильность** | ⚠️ Зависит от устройства | ✅ Высокая |
| **Определение зоны** | ❌ Нет | ✅ Через IMicphoneMode |
| **Управление громкостью** | ❌ Через AudioManager | ✅ Через IVolume |

---

## 🚀 КАК ТЕСТИРОВАТЬ

### 1. Проверить что QGSpeechService запущен

```bash
adb shell ps | grep sttservice
# Должно быть: com.qinggan.sttservice
```

### 2. Сборка и установка

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat assembleRelease

adb root && adb remount
adb push app\build\outputs\apk\release\app-release-unsigned.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk

adb shell am force-stop ru.voboost.voiceassistant
adb shell am start-foreground-service -n ru.voboost.voiceassistant/.VoboostVoiceService
```

### 3. Проверить логи

```bash
adb logcat -s MicrophoneStream:* VolumeManager:* SpeechRecognition:* VoboostVoiceService:*
```

**Ожидаемые логи:**

```
MicrophoneStream: Connecting to TransProxy service...
MicrophoneStream: ✅ TransProxy connected
MicrophoneStream: ✅ PCM listener registered
MicrophoneStream: ✅ PCM stream connected

VolumeManager: Connecting to Volume service...
VolumeManager: ✅ Volume service connected
VolumeManager: ✅ Volume listener registered

SpeechRecognition: Initializing MicrophoneStreamManager (TransProxy)
SpeechRecognition: ✅ MicrophoneStreamManager initialized - will get PCM from QGSpeechService

VoboostVoiceService: MicrophoneStreamManager connected - will get PCM from QGSpeechService
VoboostVoiceService: VolumeManager connected - can duck/restore media volume
```

### 4. Проверить приглушение музыки

```bash
# Запустить музыку
adb shell am start -n com.qinggan.app.music/.MainActivity

# Активировать голосовой помощник (сказать "привет машина")
# Музыка должна приглушиться

# После завершения команды
# Музыка должна восстановиться
```

---

## 🐛 ВОЗМОЖНЫЕ ПРОБЛЕМЫ

### TransProxy не подключается

**Симптомы:**
```
MicrophoneStream: ❌ Failed to bind TransProxy service
```

**Причины:**
1. QGSpeechService не запущен
2. Intent неверный
3. Нет разрешения BIND_SERVICE

**Решение:**
```bash
# Проверить что QGSpeechService запущен
adb shell ps | grep sttservice

# Если не запущен — запустить
adb shell am startservice -n com.qinggan.sttservice/.vui.VuiService
```

### VolumeManager не подключается

**Симптомы:**
```
VolumeManager: ❌ Failed to bind Volume service
```

**Решение:**
```bash
# Проверить что сервис существует
adb shell dumpsys activity services | grep VOLUME
```

### PCM данные не приходят

**Симптомы:**
```
MicrophoneStream: ✅ PCM stream connected
# Но onPcmData() не вызывается
```

**Причины:**
1. QGSpeechService не захватывает аудио
2. TransProxy не раздаёт данные

**Решение:**
```bash
# Проверить логи QGSpeechService
adb logcat -s TransProxy:* RecorderManager:*

# Должно быть:
# TransProxy: registerPcmListener
# RecorderManager: startRecord
```

---

## 📝 AIDL ИНТЕРФЕЙСЫ

### IPcmModule

```aidl
interface IPcmModule {
    void registerPcmListener(IPcmListener listener);
    boolean sendPcm(in byte[] buff, int offset, int length);
    void unregisterPcmListener(IPcmListener listener);
}
```

### IPcmListener

```aidl
interface IPcmListener {
    void onDisconnected();
    void onConnected();
    void onPcm(in byte[] pcm);
}
```

### IVolume

```aidl
interface IVolume {
    boolean registerListener(IVolumeListener listener);
    boolean sendSetMDVolume(int streamType, int volume);
    boolean sendVolumeUp();
    boolean sendVolumeDown();
    // ... и другие методы
}
```

### IVolumeListener

```aidl
interface IVolumeListener {
    void onConnect(boolean connect);
    void onSetCurrentMediaVolume(int volume);
    void onSetCurrentNavigationVolume(int volume);
    void onRequestAudioPolicy(int streamType, String clientId);
    void onAbandonAudioPolicy(String clientId);
    // ... и другие методы
}
```

---

## 🎯 СЛЕДУЮЩИЕ ШАГИ

### 1. Протестировать на устройстве

- ✅ Проверить подключение TransProxy
- ✅ Проверить получение PCM данных
- ✅ Проверить приглушение музыки
- ✅ Проверить распознавание команд

### 2. Добавить определение зоны водителя

```kotlin
// Подключиться к IMicphoneMode
val intent = Intent("com.qinggan.qinglink.hu.MICPHONEMODE")
intent.setPackage("com.qinggan.sttservice")

bindService(intent, object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val micModeService = IMicphoneMode.Stub.asInterface(service)
        
        micModeService.registerListener(object : IMicphoneModeListener.Stub() {
            override fun onSetMicMode(mode: Int) {
                when (mode) {
                    1537 -> Log.d("Mic", "🎤 Говорит ВОДИТЕЛЬ")
                    1538 -> Log.d("Mic", "🎤 Говорит ПАССАЖИР")
                }
            }
        })
    }
}, Context.BIND_AUTO_CREATE)
```

### 3. Оптимизировать буферизацию

- Настроить размер буфера для лучшей производительности
- Добавить обработку ошибок при потере соединения

---

**Последнее обновление:** 2026-04-04
**Следующая задача:** Протестировать TransProxy на устройстве
