# 🔄 ЛОГИКА РАБОТЫ СЕРВИСА

**Версия:** 1.0  
**Дата:** 2026-03-28  
**Статус:** ✅ Реализована полная цикличность

---

## 📊 ДИАГРАММА СОСТОЯНИЙ

```
┌─────────────────────────────────────────────────────────────────┐
│                    VOICE ASSISTANT SERVICE                       │
└─────────────────────────────────────────────────────────────────┘

    ┌──────────────────┐
    │  SERVICE START   │
    └────────┬─────────┘
             │
             ▼
    ┌──────────────────────────────────────┐
    │  startKeywordSpotting()              │
    │  ─────────────────────               │
    │  🔍 Ожидание ключевой фразы          │
    │  "привет воях"                       │
    └──────────────┬───────────────────────┘
                   │
         ┌─────────┼─────────┬──────────────┬──────────────┐
         │         │         │              │              │
         ▼         ▼         ▼              ▼              ▼
    ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐ ┌──────────┐
    │ KEY    │ │ ERROR  │ │ TIMEOUT│ │  CANCEL  │ │ EXCEPTION│
    │ WORD   │ │        │ │        │ │          │ │          │
    └───┬────┘ └───┬────┘ └───┬────┘ └────┬─────┘ └────┬─────┘
        │          │          │           │            │
        │          │          │           │            │
        ▼          ▼          ▼           ▼            ▼
    ┌──────────────────────────────────────────────────────────┐
    │  activateVoiceAssistant()                                │
    │  ──────────────────────                                  │
    │  🎵 Звук начала                                          │
    │  🎬 Показать анимацию                                    │
    │  🔊 "Слушаю вас"                                         │
    └────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
    ┌──────────────────────────────────────┐
    │  startCommandListening()             │
    │  ─────────────────────               │
    │  🎤 Ожидание команды                 │
    │  (5 секунд таймаут)                  │
    └──────────────┬───────────────────────┘
                   │
         ┌─────────┼─────────┬──────────────┬──────────────┐
         │         │         │              │              │
         ▼         ▼         ▼              ▼              ▼
    ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐ ┌──────────┐
    │COMMAND │ │ ERROR  │ │ TIMEOUT│ │  CANCEL  │ │ EXCEPTION│
    │FOUND   │ │        │ │        │ │          │ │          │
    └───┬────┘ └───┬────┘ └───┬────┘ └────┬─────┘ └────┬─────┘
        │          │          │           │            │
        │          │          │           │            │
        ▼          ▼          ▼           ▼            ▼
    ┌──────────────────────────────────────────────────────────┐
    │  processVoiceCommand()                                   │
    │  ─────────────────────                                   │
    │  🧠 NLU парсинг                                          │
    │  ✅ Выполнение команды                                   │
    │  🔊 Ответ пользователю                                   │
    └────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
    ┌──────────────────────────────────────┐
    │  Return to Keyword Spotting          │
    │  ──────────────────────              │
    │  🎵 Звук окончания                   │
    │  🎬 Скрыть анимацию                  │
    │  ⏱️ Пауза 1 секунда                  │
    │  🔄 startKeywordSpotting()           │
    └──────────────────────────────────────┘
```

---

## 📋 СЦЕНАРИИ РАБОТЫ

### 1️⃣ Запуск сервиса

```kotlin
onCreate() → startKeywordSpotting()
```

**Лог:**
```
I/VoboostVoiceService: Starting keyword spotting (waiting for activation phrase)...
```

**Состояние:**
- `isListening = false`
- `isCommandMode = false`

---

### 2️⃣ Обнаружение ключевой фразы

```kotlin
onKeywordDetected → activateVoiceAssistant()
```

**Лог:**
```
I/VoboostVoiceService: 🎯 Keyword detected!
I/VoboostVoiceService: Starting command listening...
```

**Состояние:**
- `isListening = true`
- `isCommandMode = true`

---

### 3️⃣ Команда выполнена успешно

```kotlin
onCommandReceived → processVoiceCommand() → startKeywordSpotting()
```

**Лог:**
```
I/VoboostVoiceService: 📝 Command received: открой лючок зарядки
I/VoboostVoiceService: ✅ Command completed - returning to keyword spotting...
I/VoboostVoiceService: Starting keyword spotting (waiting for activation phrase)...
```

**Состояние:**
- `isListening = false`
- `isCommandMode = false`

---

### 4️⃣ Ошибка распознавания команды

```kotlin
onError → speak(failure) → startKeywordSpotting()
```

**Лог:**
```
E/VoboostVoiceService: ❌ Command listening error: recognition failed
I/VoboostVoiceService: 🔄 Returning to keyword spotting after error...
```

**Состояние:**
- `isListening = false`
- `isCommandMode = false`

---

### 5️⃣ Таймаут (команда не сказана)

```kotlin
onTimeout → startKeywordSpotting()
```

**Лог:**
```
W/VoboostVoiceService: ⏱️ Command timeout - returning to keyword spotting
I/VoboostVoiceService: 🔄 Returning to keyword spotting after timeout...
```

**Состояние:**
- `isListening = false`
- `isCommandMode = false`

---

### 6️⃣ Отмена пользователем

```kotlin
cancelRecognition() → startKeywordSpotting()
```

**Лог:**
```
I/VoboostVoiceService: ❌ Cancelling recognition...
I/VoboostVoiceService: ✅ Recognition cancelled
I/VoboostVoiceService: 🔄 Returning to keyword spotting after cancel...
```

**Состояние:**
- `isListening = false`
- `isCommandMode = false`

---

### 7️⃣ Ошибка keyword spotting

```kotlin
onError → speak(failure) → delay(2s) → startKeywordSpotting()
```

**Лог:**
```
E/VoboostVoiceService: Keyword spotting error: audio failed
I/VoboostVoiceService: 🔄 Returning to keyword spotting after error...
```

**Состояние:**
- Сервис автоматически перезапускается

---

### 8️⃣ Исключение в keyword spotting

```kotlin
catch (e) → delay(3s) → startKeywordSpotting()
```

**Лог:**
```
E/VoboostVoiceService: Failed to start keyword spotting
I/VoboostVoiceService: Retrying keyword spotting after exception...
```

**Состояние:**
- Сервис пытается перезапуститься через 3 секунды

---

## ✅ ТАБЛИЦА ПЕРЕХОДОВ

| Событие | Действие | Возврат в keyword spotting |
|---------|----------|---------------------------|
| **Ключевая фраза** | activateVoiceAssistant() | ❌ Нет (переход в command mode) |
| **Команда найдена** | processVoiceCommand() | ✅ Да (через 1 сек) |
| **Ошибка команды** | speak(failure) | ✅ Да (через 0.5 сек) |
| **Таймаут команды** | hideAnimation() | ✅ Да (через 0.5 сек) |
| **Отмена** | playCancelSound() | ✅ Да (через 1 сек) |
| **Ошибка keyword** | speak(failure) | ✅ Да (через 2 сек) |
| **Исключение keyword** | log error | ✅ Да (через 3 сек) |

---

## 🔍 ЛОГИРОВАНИЕ

### Основные теги логов:

| Тег | Описание |
|-----|----------|
| `🔍` | Ожидание ключевой фразы |
| `🎯` | Ключевая фраза обнаружена |
| `🎤` | Прослушивание команды |
| `📝` | Команда получена |
| `✅` | Команда выполнена |
| `❌` | Ошибка / Отмена |
| `⏱️` | Таймаут |
| `🔄` | Возврат в keyword spotting |
| `🎵` | Звуковой эффект |
| `🎬` | Анимация |

---

## 🛡️ ЗАЩИТА ОТ СБОЕВ

### 1. Автоматический перезапуск

```kotlin
// При ошибке keyword spotting
delay(2000)
startKeywordSpotting()

// При исключении
delay(3000)
startKeywordSpotting()
```

### 2. Проверка состояния

```kotlin
if (serviceScope.isActive) {
    startKeywordSpotting()
}
```

### 3. Защита от повторного запуска

```kotlin
if (isListening) {
    Log.w(TAG, "Already running, skipping")
    return
}
```

---

## 📊 СТАТИСТИКА

**Время перехода в keyword spotting:**

| Сценарий | Задержка |
|----------|----------|
| Команда выполнена | 1000 ms |
| Ошибка команды | 500 ms |
| Таймаут команды | 500 ms |
| Отмена | 1000 ms |
| Ошибка keyword | 2000 ms |
| Исключение keyword | 3000 ms |

---

**Сервис ВСЕГДА возвращается в режим ожидания ключевой фразы!** ✅
