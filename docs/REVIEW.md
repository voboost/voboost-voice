# Вобууст Голосовой Ассистент — Code Review

## Обзор архитектуры

Проект реализует голосового помощника для автомобилей на Android с офлайн-распознаванием речи (Vosk/Sherpa), NLU-движками (Parser/LLM/ONNX) и управлением автомобилем через CAN-шину.

### Сильные стороны
- ✅ **State Machine** — хорошее решение для голосового помощника (Idle → Listening → Recognized → Executing)
- ✅ **IAudioSource abstraction** — единый источник аудио, легко тестировать и менять реализацию
- ✅ **NLUEngineFactory** — поддержка нескольких движков NLU (Parser, LLM, ONNX)
- ✅ **CommandExecutor с ducking звука** — хорошая UX при голосовом ответе
- ✅ **Config-driven commands** — JSON конфиг, не нужно перекомпилировать для новых команд
- ✅ **Coroutines-based architecture** — современный Kotlin подход

---

## Найденные проблемы и рекомендации

### 1. ⚠️ Неполная реализация `activateVoiceAssistant()`

В `VoboostVoiceService.kt` метод пустой:
```kotlin
private fun activateVoiceAssistant() {}
```
Нужно реализовать логику активации (переход из KEYWORD в COMMAND mode).

**Рекомендация:** Реализовать переключение режима распознавания и запуск `stateMachine.activate()` при нажатии кнопки или обнаружении кодовой фразы.

---

### 2. ⚠️ Проблема с таймаутом при повторном запуске

В `startKeywordSpotting()` retry-логика имеет потенциальную утечку:
```kotlin
withContext(Dispatchers.Default) { delay(3000) }
```
При вызове `isDestroying = true` перед отменой scope, `delay()` может некорректно обработаться.

**Рекомендация:** Использовать `isActive` проверку внутри `delay()`, чтобы корректно обрабатывать cancellation при `onDestroy()`:
```kotlin
while (isActive) { delay(3000); if (!isDestroying && isActive) startKeywordSpotting() }
```

---

### 3. ⚠️ Дублирование буферов в SpeechRecognizer

В методе `audioListener`:
```kotlin
val newBuffer = ByteArray(currentBuffer.size + bytesRead)
System.arraycopy(...)
```
Это создаёт новый массив при каждом блоке аудио данных, что при 16kHz/16bit может быть ~320KB в секунду.

**Рекомендация:** Внедрить **Object Pool** или **Circular Buffer** для уменьшения GC-нагрузки:
- Использовать `ArrayDeque<ByteArray>` с пулом буферов
- Или реализовать собственный `RingBuffer` на основе одного большого массива

---

### 4. ⚠️ Опечатка в NLU Engine Factory логике

В `NLUEngineFactory.kt`:
```kotlin
else -> {
    Log.i(TAG, "TTS engine from config: LLM")  // ← Опечатка! Должно быть "NLU"
}
```

**Рекомендация:** Исправить на `"NLU engine from config: LLM"` для консистентности логов.

---

### 5. 📌 Отсутствие обработки ошибок инициализации движков

Если TTS или NLU не загрузились — сервис продолжает работу без них, но пользователь этого не замечает.

**Рекомендация:** Добавить логи и обработку fallback-режимов:
```kotlin
try { speechSynthesis = SpeechEngineFactory.create(...) }
catch (e: Exception) { 
    Log.e(TAG, "TTS failed, falling back to system TTS", e)
    speechSynthesis = SystemSpeechSynthesis() 
}
```

---

## Рекомендации по улучшениям

### 6. 🎯 Добавить **Zone-awareness** на уровне сервиса

В проекте есть `TdoaZoneDetector`, но он слабо интегрирован в общий поток. Нужно явно передавать зону от микрофона до TTS (чтобы говорить именно водителю).

---

### 7. 📦 Внедрить **Hilt/Dagger2** для dependency injection

Сейчас сервис создаёт компоненты вручную через `init {}`. Для масштабируемости лучше Hilt:
- Упростит тестирование
- Улучшит читаемость кода
- Поддержит lazy initialization тяжёлых компонентов (NLU, TTS)

---

### 8. 🔊 Добавить **Noise Suppression (NS)** и **AGC**

Для автомобильного окружения критично иметь шумоподавление в реальном времени (например, через `AudioEffect` или на уровне `IAudioSource`).

---

### 9. 📱 Реализовать **CarMode/DrivingMode detection**

Вместо статических timeout'ов — динамическое изменение поведения в зависимости от скорости автомобиля. При движении выше 60 км/ч:
- Уменьшить время ожидания команды (COMMAND_TIMEOUT_MS с 5000 до 3000)
- Использовать более короткие фразы
- Отключить confirmation для критичных команд

---

### 10. 🛡️ Добавить **Activity Recognition** (optional)

Для определения, кто говорит (водитель vs пассажир), чтобы правильно обрабатывать команды ("Открой окно слева" — водительское).

---

## Итоговая оценка

| Критерий | Оценка (из 5) | Комментарий |
|----------|---------------|-------------|
| Архитектура | ⭐⭐⭐⭐ | State Machine хорошо продумана |
| Код-качество | ⭐⭐⭐ | Есть места для рефакторинга |
| Документация | ⭐⭐⭐⭐ | Много документации в docs/ |
| Покрытие тестами | ⭐⭐ | Тесты нужны (особенно NLU + StateMachine) |
| Производительность | ⭐⭐⭐ | Буферизация требует оптимизации |
| UX-поток | ⭐⭐⭐⭐ | Ducking звука, overlay — хорошо |

**Общая оценка: 7.5/10** — хороший проект с потенциалом для production-ready приложения!

---

## Приоритеты исправлений (Priority Order)

1. **P0 (срочно)** — Реализовать `activateVoiceAssistant()`, опечатка в NLU Factory
2. **P1 (важно)** — Оптимизировать буферизацию аудио, обработка ошибок инициализации
3. **P2 (рекомендую)** — Hilt/Dagger2, Zone-awareness, Noise Suppression
4. **P3 (nice-to-have)** — CarMode detection, Activity Recognition

---

*Дата: 2025-01-27*  
*Автор: Code Review Assistant*