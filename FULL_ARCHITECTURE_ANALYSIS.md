# 🏗️ ПОЛНАЯ АРХИТЕКТУРА ГОЛОСОВОЙ СИСТЕМЫ

**Дата:** 2026-03-24  
**Статус:** ✅ ПОЛНОЕ ПОНИМАНИЕ СИСТЕМЫ

---

## 🎯 АРХИТЕКТУРА: ТРИ УРОВНЯ

```
┌─────────────────────────────────────────────────────────────────┐
│                    УРОВЕНЬ 1: СИСТЕМНЫЙ                         │
│  KeyManager (com.qinggan.system.KeyManager)                     │
│  - Обработка кнопок руля (KEYCODE_IVOKA = 130)                  │
│  - Системный сервис (доступен всем)                             │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    УРОВЕНЬ 2: ГОЛОСОВОЙ ДВИЖОК                  │
│  QGSpeechService (com.qinggan.sttservice)                       │
│  └─> SpeechMgr                                                  │
│      ├─> KeyManager.OnKeyEventListener ← Получает кнопку        │
│      ├─> SpeechAdapterSDK.onSystemEvent()                       │
│      ├─> QGHardKey.receiveHardKey()                             │
│      ├─> WakeupVuiEngine.trackWakeup()                          │
│      ├─> Распознавание речи (Nuance/Arklite/Baidu)              │
│      └─> startService("com.qinggan.iovka.START_IVOKA")          │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    УРОВЕНЬ 3: UI + NLU                          │
│  Ivoka (com.qinggan.ivoka / com.qinggan.ivoka1)                 │
│  └─> WindowService                                              │
│      ├─> Показ UI (волны, аватар)                               │
│      ├─> NLU (понимание команд)                                 │
│      ├─> Выполнение команд (Broadcast в машину)                 │
│      └─> TTS (синтез речи)                                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📊 ДЕТАЛЬНЫЙ АНАЛИЗ КОМПОНЕНТОВ

### 1️⃣ **KeyManager** (Системный уровень)

**Пакет:** `com.qinggan.system.KeyManager`  
**Расположение:** Системная библиотека (доступна всем APK)

**Назначение:**
- ✅ Обработка кнопок на руле через CAN шину
- ✅ Предоставляет API для регистрации слушателей

**API:**
```java
public class KeyManager {
    public interface OnKeyEventListener {
        boolean onKeyShortClick(int keyCode, KeyEvent keyEvent);
        boolean onKeyDown(int keyCode, KeyEvent keyEvent);
        // ...
    }
    
    public static KeyManager getInstance(Context ctx, OnInitListener listener);
    public void registerKeyEventListener(OnKeyEventListener listener);
    public void inputKeyEvent(int keyCode);
}
```

**Кто использует:**
- ✅ QGSpeechService
- ✅ Ivoka
- ✅ BluetoothPhone
- ✅ VehicleSetting
- ✅ Gallery (да, даже галерея!)

---

### 2️⃣ **QGSpeechService** (Голосовой движок)

**Пакет:** `com.qinggan.sttservice`  
**Расположение:** `/system/priv-app/QGSpeechService/`

**Назначение:**
- ✅ **Получает событие кнопки от KeyManager**
- ✅ Распознавание речи (ASR)
- ✅ Синтез речи (TTS)
- ✅ Запуск Ivoka

**Основные компоненты:**

```java
// SpeechMgr.java - Главный класс
public class SpeechMgr implements ISpeech {
    
    // 1. Слушатель кнопок от KeyManager
    private KeyManager.OnKeyEventListener mOnKeyEventListener = 
        new KeyManager.OnKeyEventListener() {
            public boolean onKeyShortClick(int i, KeyEvent keyEvent) {
                if (i == 130) {  // KEYCODE_IVOKA
                    // Создаем событие
                    KeyEventInfo keyEventInfo = new KeyEventInfo();
                    keyEventInfo.setKeyCode(1);  // 1 = IVA
                    keyEventInfo.setKeyEvent(1);
                    
                    // Отправляем в голосовой движок
                    SpeechAdapterSDK.getInstance().onSystemEvent(
                        Category.CATEGORY_HARD_KEY_EVENT, 
                        "", 
                        platformAdapterObj.toIntent()
                    );
                }
            }
        };
    
    // 2. Инициализация
    public void init(Context context) {
        // Подключаемся к KeyManager
        this.mKeyManager = KeyManager.getInstance(...);
        
        // Запускаем Ivoka
        Intent intent = new Intent("com.qinggan.iovka.START_IVOKA");
        intent.setPackage("com.qinggan.ivoka");
        context.startService(intent);
        
        // Запускаем Ivoka1
        Intent intent2 = new Intent("com.qinggan.iovka.START_IVOKA");
        intent2.setPackage("com.qinggan.ivoka1");
        context.startService(intent2);
        
        // Запускаем TTS
        Intent intent3 = new Intent("com.qinggan.iovka.START_TTS");
        intent3.setPackage("com.qinggan.ttsservice1");
        context.startService(intent3);
    }
}
```

**Сервисы внутри QGSpeechService:**

| Сервис | Назначение |
|--------|------------|
| `VuiService` | Главный сервис голосового UI |
| `WakeupService` | Словесное пробуждение ("Привет, Вобуст") |
| `QGAdapterService` | Адаптер для системы |
| `NuanceLocalRecognize` | Распознавание (Nuance engine) |
| `LocalArkliteService` | Распознавание (Arklite engine) |
| `VPrintService` | Voice Biometry (голосовой отпечаток) |
| `VoiceBiometryService` | Биометрия голоса |
| `QingAIMqttService` | MQTT для облака |

---

### 3️⃣ **Ivoka** (UI + NLU)

**Пакет:** `com.qinggan.ivoka` / `com.qinggan.ivoka1`  
**Расположение:** `/system/priv-app/Ivoka/`

**Назначение:**
- ✅ UI голосового помощника (волны, аватар)
- ✅ NLU (понимание команд)
- ✅ Выполнение команд (Broadcast в машину)
- ✅ TTS (синтез речи)

**Основные компоненты:**

```java
// WindowService.java - Главный сервис
public class WindowService extends Service {
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        
        if ("com.qinggan.iovka.START_IVOKA".equals(action)) {
            // Запускаем распознавание!
            wakeUpVoiceByHand();
            SpeechManager.getInstance().startRecognize();
        }
    }
    
    private void wakeUpVoiceByHand() {
        // Показываем UI
        mVoiceDialogManager.showVoiceView(EVENT_WAKEUP);
        
        // Начинаем слушать
        SpeechManager.getInstance().startRecognize();
    }
}
```

**Почему 2 версии Ivoka?**
- `com.qinggan.ivoka` - Оригинальная версия
- `com.qinggan.ivoka1` - Обновленная версия (v2)

QGSpeechService запускает **ОБЕ**, но работает только одна (первая).

---

## 🔗 ВЗАИМОСВЯЗИ

### Почему Ivoka зависит от QGSpeechService?

**Потому что QGSpeechService:**
1. ✅ **Получает кнопку от KeyManager** (Ivoka НЕ подключается напрямую!)
2. ✅ **Обрабатывает кнопку** (SpeechMgr.onKeyShortClick)
3. ✅ **Запускает Ivoka** (startService)
4. ✅ **Предоставляет распознавание** (через SpeechAdapterSDK)

**Ivoka НЕ может работать без QGSpeechService!**

```
❌ Если отключить QGSpeechService:
   - Кнопка на руле → KeyManager → НИКОМУ не отправляется
   - Ivoka НЕ запускается (некому отправить startService)
   - Ivoka НЕ получит распознавание речи

✅ Если отключить Ivoka:
   - QGSpeechService работает
   - Кнопка обрабатывается
   - Распознавание работает
   - Но UI не показывается (Ivoka отключен)
```

---

## 🎯 АРХИТЕКТУРА В ДЕЙСТВИИ

### Полный путь кнопки:

```
1. Кнопка на руле нажата
   ↓
2. CAN Bus → CanBusService
   ↓
3. KeyManager.inputKeyEvent(130)
   ↓
4. SpeechMgr.mOnKeyEventListener.onKeyShortClick(130)
   ↓
5. SpeechAdapterSDK.onSystemEvent(HARD_KEY_EVENT)
   ↓
6. QGHardKey.receiveHardKey(keyCode=1)
   ↓
7. WakeupVuiEngine.trackWakeup()
   ↓
8. startService("com.qinggan.iovka.START_IVOKA")
   ↓
9. Ivoka WindowService.onStartCommand()
   ↓
10. wakeUpVoiceByHand()
    ↓
11. SpeechManager.getInstance().startRecognize()
    ↓
12. Показываем UI (волны)
    ↓
13. Распознавание речи (Nuance/Arklite)
    ↓
14. NLU (понимание команд)
    ↓
15. Выполнение (Broadcast в машину)
```

---

## ✅ ВЫВОДЫ

### Почему такая архитектура?

**Разделение ответственности:**

| Компонент | Ответственность | Почему отдельно |
|-----------|-----------------|-----------------|
| **KeyManager** | Кнопки руля | Системный, общий для всех |
| **QGSpeechService** | Распознавание + TTS | Тяжелый движок (~100MB) |
| **Ivoka** | UI + NLU | Меняется чаще (дизайн, команды) |

**Преимущества:**
- ✅ Можно обновлять Ivoka без изменения распознавания
- ✅ Можно использовать разные движки распознавания
- ✅ Кнопка работает централизованно

**Недостатки:**
- ❌ Сложная цепочка вызовов
- ❌ Три процесса вместо одного
- ❌ Высокое потребление RAM

---

## 🏆 РЕКОМЕНДАЦИИ

### Вариант A: Полная замена (ОТКЛЮЧИТЬ ВСЁ)

```bash
# Отключить все 3 компонента
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
adb shell pm disable com.qinggan.sttservice

# Voboost подключается к KeyManager напрямую
```

**Voboost должен реализовать:**
1. ✅ Подключение к KeyManager
2. ✅ Распознавание речи (Vosk)
3. ✅ NLU (понимание команд)
4. ✅ TTS (синтез речи)
5. ✅ UI (волны/оверлей)

**Преимущества:**
- ✅ Полный контроль
- ✅ Минимальное потребление
- ✅ Нет лишних процессов

**Недостатки:**
- ⚠️ Нужно реализовать ВСЁ самостоятельно
- ⚠️ Нужно добавить KeyManager в проект

---

### Вариант B: Частичная замена (ОТКЛЮЧИТЬ Ivoka)

```bash
# Отключить только Ivoka
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1

# QGSpeechService оставляем
# Voboost использует его распознавание
```

**Voboost должен реализовать:**
1. ✅ Перехват запуска Ivoka (Frida)
2. ✅ NLU (понимание команд)
3. ✅ UI (волны/оверлей)

**Распознавание и TTS:**
- ✅ Используем QGSpeechService
- ✅ Перехватываем через SpeechAdapterSDK

**Преимущества:**
- ✅ Не нужно свое распознавание
- ✅ Не нужно свое TTS
- ✅ Меньше кода

**Недостатки:**
- ⚠️ QGSpeechService работает (~50-100MB RAM)
- ⚠️ Нужен Frida или модификация APK

---

### Вариант C: Минимальная замена (ПОДМЕНА Ivoka)

**Оставляем:**
- ✅ KeyManager (системный)
- ✅ QGSpeechService (распознавание + TTS)

**Заменяем:**
- ❌ Ivoka → VoboostVoiceService

**Как:**
```javascript
// Frida скрипт
ContextWrapper.startService.implementation = function(intent) {
    if (intent.getAction() === "com.qinggan.iovka.START_IVOKA") {
        intent.setPackage("ru.voboost.voiceassistant");
    }
    return this.startService(intent);
};
```

**Voboost получает:**
- ✅ Кнопку от QGSpeechService
- ✅ Распознавание от QGSpeechService
- ✅ TTS от QGSpeechService

**Voboost реализует:**
- ✅ NLU (понимание команд)
- ✅ UI (опционально)
- ✅ Выполнение команд

**Преимущества:**
- ✅ Минимум изменений
- ✅ Работает сразу
- ✅ Не нужно свое распознавание

**Недостатки:**
- ⚠️ QGSpeechService + Ivoka работают (~150MB RAM)
- ⚠️ Нужен Frida

---

## 📋 ИТОГОВАЯ ТАБЛИЦА

| Решение | KeyManager | QGSpeechService | Ivoka | Voboost | RAM | Сложность |
|---------|------------|-----------------|-------|---------|-----|-----------|
| **A: Всё отключить** | ✅ Используем | ❌ Отключен | ❌ Отключен | ✅ Всё сам | ~30MB | ⭐⭐⭐ Сложно |
| **B: Частичная замена** | ✅ QGSpeechService | ✅ Используем | ❌ Отключен | ✅ NLU + UI | ~80MB | ⭐⭐ Средне |
| **C: Подмена Ivoka** | ✅ QGSpeechService | ✅ Используем | ❌ Отключен | ✅ NLU | ~150MB | ⭐ Просто |

---

## 🎯 ФИНАЛЬНАЯ РЕКОМЕНДАЦИЯ

**Для начала:** Вариант C (Frida подмена)
- ✅ Быстро тестируется
- ✅ Минимум кода
- ✅ Работает сразу

**Для продакшена:** Вариант A (полная замена)
- ✅ Минимальное потребление
- ✅ Полный контроль
- ✅ Нет зависимостей

---

**Теперь понятно почему такая архитектура? 🎉**
