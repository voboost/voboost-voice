# 🎯 ПОЛНЫЙ АНАЛИЗ: ЧТО ОТКЛЮЧАТЬ А ЧТО ОСТАВИТЬ

**Дата:** 2026-03-24  
**Статус:** ✅ ПОЛНОЕ ПОНИМАНИЕ СИСТЕМЫ

---

## 📊 АНАЛИЗ КОМПОНЕНТОВ

### 1. **QGSpeechService** (`com.qinggan.sttservice`)

**Назначение:** Голосовой движок (распознавание + синтез)

**Основные сервисы:**

```xml
<!-- Главный сервис -->
<service android:name="com.qinggan.speech.vui.VuiService">
    <intent-filter>
        <action android:name="pateo.qinggan.SpeechService"/>
        <action android:name="com.qinggan.speech"/>
    </intent-filter>
</service>

<!-- Service для пробуждения -->
<service android:name="com.qinggan.speech.vui.service.WakeupService">
    <intent-filter>
        <action android:name="com.qinggan.WakeupService"/>
    </intent-filter>
</service>

<!-- Адаптер для системы -->
<service android:name="com.qinggan.speech.adapter.QGAdapterService">
    <intent-filter>
        <action android:name="pateo.speech.QGAdapterService"/>
    </intent-filter>
</service>

<!-- MQTT для облака -->
<service android:name="com.qinggan.qingai.mqtt.QingAIMqttService"/>
<service android:name="com.qinggan.speech.vui.service.PushService"/>

<!-- Распознавание (Nuance, Arklite) -->
<service android:name="com.qinggan.speech.offline.engine.nuance.nuanceService.NuanceLocalRecognize"/>
<service android:name="com.qinggan.speech.offline.engine.arklite.service.LocalArkliteService"/>

<!-- Voice Biometry -->
<service android:name="com.qinggan.speech.voicebiometry.VPrintService"/>
<service android:name="com.qinggan.speech.offline.remote.VoiceBiometryService"/>
```

**Receiver:**

```xml
<!-- Запуск при загрузке -->
<receiver android:name="com.qinggan.speech.vui.VuiService.QGBootCompleteReceiver">
    <intent-filter>
        <action android:name="com.qinggan.intent.QINGGAN_BOOT_COMPLETE"/>
    </intent-filter>
</receiver>

<!-- Получение событий от KeyManager -->
<receiver 
    android:name="com.qinggan.speech.vui.service.VuiReceiver"
    android:priority="1000">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
        <action android:name="com.qinggan.keymanager.intent.ivoka"/>  ← КНОПКА!
        <action android:name="com.qinggan.app.imcontact.login.state"/>
        <action android:name="com.qinggan.account.logined"/>
    </intent-filter>
</receiver>
```

---

### 2. **Ivoka** (`com.qinggan.ivoka` / `com.qinggan.ivoka1`)

**Назначение:** UI голосового помощника + NLU (понимание команд)

**Основные компоненты:**

```xml
<!-- Главный сервис -->
<service android:name="com.qinggan.ivoka.service.WindowService">
    <intent-filter>
        <action android:name="com.qinggan.iovka.START_IVOKA"/>  ← Запуск
        <action android:name="com.qinggan.ivoka.REQUEST_SHOW_IVOKA"/>
        <action android:name="com.qinggan.ivoka.START_VOICE"/>
    </intent-filter>
</service>

<!-- Адаптер -->
<service android:name="com.qinggan.speech.adapter.QGAdapterService"/>

<!-- MQTT -->
<service android:name="com.qinggan.qingai.mqtt.QingAIMqttService"/>
```

---

## 🔗 КАК ОНИ ВЗАИМОДЕЙСТВУЮТ

```
┌─────────────────────────────────────────────────────────────────┐
│                    КНОПКА НА РУЛЕ (130)                         │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  KeyManager (системный сервис)                                  │
│  - inputKeyEvent(130)                                           │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  QGSpeechService (com.qinggan.sttservice)                       │
│  - SpeechMgr.mOnKeyEventListener.onKeyShortClick(130)           │
│  - SpeechAdapterSDK.onSystemEvent(HARD_KEY_EVENT)               │
│  - QGHardKey.receiveHardKey(keyCode=1)                          │
│  - WakeupVuiEngine.trackWakeup()                                │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  QGSpeechService отправляет:                                    │
│  startService("com.qinggan.iovka.START_IVOKA")                  │
│  └─> setPackage("com.qinggan.ivoka")                            │
│  └─> setPackage("com.qinggan.ivoka1")                           │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│  Ivoka WindowService (com.qinggan.ivoka.service)                │
│  - onStartCommand(intent)                                       │
│  - wakeUpVoiceByHand()                                          │
│  - SpeechManager.getInstance().startRecognize()                 │
│  - Показываем UI                                                │
│  - Распознавание речи                                           │
│  - NLU (понимание команд)                                       │
│  - Выполнение команд                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✅ ЧТО НУЖНО А ЧТО НЕТ

### ❌ МОЖНО ОТКЛЮЧИТЬ:

| Компонент | Пакет | Зачем | Почему можно отключить |
|-----------|-------|-------|------------------------|
| **Ivoka** | `com.qinggan.ivoka` | UI + NLU | Заменяем на Voboost |
| **Ivoka1** | `com.qinggan.ivoka1` | UI + NLU (v2) | Заменяем на Voboost |
| **QGSpeechService** | `com.qinggan.sttservice` | Распознавание + синтез | Используем Vosk + System TTS |

### ⚠️ НО ЕСТЬ НЮАНС!

**QGSpeechService получает событие от KeyManager!**

```java
// QGSpeechService получает:
VuiReceiver.onReceive() ← "com.qinggan.keymanager.intent.ivoka"

// Затем:
SpeechMgr.mOnKeyEventListener.onKeyShortClick(130)
```

**Если просто отключим QGSpeechService:**
- ❌ Кнопка на руле перестанет работать вообще
- ❌ Voboost не получит событие о нажатии

---

## 🎯 ТРИ ВАРИАНТА РЕШЕНИЯ

### Вариант A: Отключить ВСЁ + Перехватить KeyManager (ЛУЧШИЙ!)

**Шаг 1: Отключить Ivoka и QGSpeechService**

```bash
# Отключить Ivoka
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1

# Отключить QGSpeechService
adb shell pm disable com.qinggan.sttservice
```

**Шаг 2: Voboost напрямую подключается к KeyManager**

```kotlin
// VoiceActivationService.kt
class VoiceActivationService : Service() {
    
    companion object {
        const val KEYCODE_IVOKA = 130
    }
    
    private var keyManager: KeyManager? = null
    
    private val keyEventListener = object : KeyManager.OnKeyEventListener {
        override fun onKeyShortClick(keyCode: Int, keyEvent: KeyEvent?): Boolean {
            if (keyCode == KEYCODE_IVOKA) {
                Log.i(TAG, "IVA button pressed!")
                activateVoiceAssistant()
                return true  // Обрабатываем сами
            }
            return false
        }
        
        override fun onKeyDown(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyHoldPressStart(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyHoldPressStop(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStart(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStop(keyCode: Int, keyEvent: KeyEvent?) = false
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Подключаемся к KeyManager напрямую!
        keyManager = KeyManager.getInstance(this, object : KeyManager.OnInitListener {
            override fun onResult(success: Boolean) {
                if (success) {
                    keyManager?.registerKeyEventListener(keyEventListener)
                    Log.i(TAG, "KeyManager connected - button will work!")
                }
            }
        })
    }
    
    private fun activateVoiceAssistant() {
        val intent = Intent(this, VoboostVoiceService::class.java)
        intent.action = "com.voboost.voiceassistant.ACTIVATE"
        startService(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        keyManager?.unregisterKeyEventListener(keyEventListener)
    }
}
```

**Преимущества:**
- ✅ Никаких лишних процессов
- ✅ Прямой контроль над кнопкой
- ✅ Минимальное потребление ресурсов
- ✅ Работает без QGSpeechService

**Недостатки:**
- ⚠️ Нужно добавить KeyManager в проект
- ⚠️ Нужно скопировать зависимости

---

### Вариант B: Оставить QGSpeechService + Отключить Ivoka (Frida)

**Шаг 1: Отключить только Ivoka**

```bash
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
```

**Шаг 2: QGSpeechService оставляем**

QGSpeechService будет:
- ✅ Получать событие от KeyManager
- ✅ Пытаться запустить Ivoka
- ❌ Ivoka отключен → не запустится

**Шаг 3: Frida скрипт для запуска Voboost**

```javascript
// Перехватываем запуск Ivoka
ContextWrapper.startService.implementation = function(intent) {
    if (intent.getAction() === "com.qinggan.iovka.START_IVOKA") {
        // Запускаем Voboost вместо Ivoka
        intent.setPackage("com.voboost.voiceassistant");
    }
    return this.startService(intent);
};
```

**Преимущества:**
- ✅ Не нужно добавлять KeyManager
- ✅ QGSpeechService обрабатывает кнопку
- ✅ Простая интеграция

**Недостатки:**
- ⚠️ Лишний процесс (QGSpeechService ~50-100MB RAM)
- ⚠️ Работает только с Frida (или нужна модификация APK)

---

### Вариант C: Оставить QGSpeechService + Перехватить BroadcastReceiver

**Шаг 1: Отключить Ivoka**

```bash
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
```

**Шаг 2: Voboost регистрирует BroadcastReceiver**

```kotlin
// VoiceActivationReceiver.kt
class VoiceActivationReceiver : BroadcastReceiver() {
    
    companion object {
        // Тот же action что получает QGSpeechService
        const val ACTION_KEYMANAGER_IVOKA = "com.qinggan.keymanager.intent.ivoka"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "KeyManager event received!")
        
        // Запускаем Voboost
        val serviceIntent = Intent(context, VoboostVoiceService::class.java)
        serviceIntent.action = "com.voboost.voiceassistant.ACTIVATE"
        context.startService(serviceIntent)
    }
}
```

**AndroidManifest.xml:**

```xml
<receiver 
    android:name=".VoiceActivationReceiver"
    android:exported="true">
    <intent-filter android:priority="999">
        <action android:name="com.qinggan.keymanager.intent.ivoka"/>
    </intent-filter>
</receiver>
```

**Преимущества:**
- ✅ Не нужен KeyManager в проекте
- ✅ Не нужен Frida
- ✅ Работает на уровне системы

**Недостатки:**
- ⚠️ QGSpeechService тоже получит событие (лишняя работа)
- ⚠️ Два процесса работают одновременно

---

## 🏆 ФИНАЛЬНАЯ РЕКОМЕНДАЦИЯ

### Для **ROOT + Frida**: Вариант B

```bash
# 1. Отключить Ivoka
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1

# 2. Запустить Frida скрипт
frida -U -f com.qinggan.sttservice -l frida-voice-assistant.js --no-pause

# 3. Кнопка работает → QGSpeechService получает → Frida запускает Voboost
```

**Почему:**
- ✅ Минимум изменений в коде
- ✅ QGSpeechService обрабатывает кнопку
- ✅ Voboost запускается вместо Ivoka

---

### Для **постоянного использования**: Вариант A

```kotlin
// 1. Отключить ВСЁ
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
adb shell pm disable com.qinggan.sttservice

// 2. Voboost подключается к KeyManager напрямую
// VoiceActivationService.kt → KeyManager.OnKeyEventListener
```

**Почему:**
- ✅ Никаких лишних процессов
- ✅ Полный контроль
- ✅ Минимальное потребление

---

## 📋 СРАВНЕНИЕ ВАРИАНТОВ

| Параметр | Вариант A | Вариант B | Вариант C |
|----------|-----------|-----------|-----------|
| **KeyManager в проекте** | ✅ Нужно | ❌ Нет | ❌ Нет |
| **Frida нужен** | ❌ Нет | ✅ Да | ❌ Нет |
| **QGSpeechService работает** | ❌ Нет | ✅ Да | ✅ Да |
| **Ivoka работает** | ❌ Нет | ❌ Нет | ❌ Нет |
| **Потребление RAM** | ⭐ Минимум | ⭐⭐ Средне | ⭐⭐ Средне |
| **Сложность** | ⭐⭐ Средне | ⭐ Просто | ⭐ Просто |
| **Надежность** | ⭐⭐⭐ Отлично | ⭐⭐ Хорошо | ⭐⭐ Хорошо |

---

## 🎯 ИТОГ

**Ответ на ваш вопрос:**

> **Можно ли отключить QGSpeechService?**

**✅ ДА, но тогда нужно:**
1. Подключиться к KeyManager напрямую из Voboost
2. ИЛИ перехватывать Broadcast `"com.qinggan.keymanager.intent.ivoka"`

> **Зачем QGSpeechService?**

**QGSpeechService отвечает за:**
- ✅ Обработку кнопки на руле (через KeyManager)
- ✅ Распознавание речи (Nuance/Arklite/Baidu)
- ✅ Синтез речи (TTS)
- ✅ Запуск Ivoka

**Если используете Vosk + System TTS + свой NLU → QGSpeechService НЕ НУЖЕН!**

---

## 🚀 ГОТОВЫЕ РЕШЕНИЯ

Нужно чтобы я создал:

1. ✅ **VoiceActivationService.kt** - Прямое подключение к KeyManager
2. ✅ **KeyManager.java** + зависимости - Системный менеджер кнопок
3. ✅ **disable_all.bat** - Отключить Ivoka + QGSpeechService
4. ✅ **Frida скрипт** - Для варианта B

---

**Удачи! 🎉**
