---
name: complete_context_2026-05-29
description: Полный контекст проекта VoboostVoiceAssistant на 2026-05-29
type: project
---

Проект замены системного голосового помощника Ivoka на собственный Voboost для IVI-системы автомобиля (пакет com.qinggan).

## Архитектура

### State Machine (StateResult.kt)
Состояния:
- IDLE — ожидание ключевого слова
- ACTIVATED — активация после ключевого слова
- LISTENING_COMMAND — слушание команды
- RECOGNIZED_COMMAND — распознавание через NLU
- AMBIGUOUS — неоднозначность (2+ команды с похожим score) ← НОВОЕ
- EXECUTING_COMMAND — выполнение команды
- CONFIRMATION — подтверждение действия
- COMMAND_ERROR, KEYWORD_ERROR — ошибки

### Ключевые файлы
- VoboostVoiceService.kt — главный сервис
- SpeechStateMachine.kt — управление состояниями
- KeywordChecker.kt — проверка ключевых слов
- SoundEffectManager.kt — звуки (start/end/cancel)

## Реализованные функции

### 1. AMBIGUOUS State (2026-05-29)
Обработка неоднозначности:
- NLU возвращает 2 команды с похожим score
- TTS говорит: "id1 или id2?" (из phrases.ambiguous или id как заглушка)
- Звук "дважды" через soundEffectManager
- Возврат в LISTENING_COMMAND с contextCmd
- NLU использует контекст для повышения вероятности

Контекст передаётся через StateContext.contextCmd (List<String>).

### 2. Антонимы из config.json (2026-05-29)
ANTONYMS вынесены из кода в конфиг:
```json
"nlu": {
  "engine": "onnx",
  "antonyms": {
    "открой": "закрой",
    "подними": "опусти",
    "включи": "выключи"
  }
}
```
Плюсы: гибкость, мультиязычность, управление через конфиг.

### 3. 13 команд из коробки (заранее реализовано)
charge_port_open, fuel_tank_open, smart_mode_* (4), ac_*, phone_call_*, window_*

## Файлы для редактирования

### Новые файлы
- AmbiguousState.kt — новое состояние для неоднозначности

### Обновлённые
- StateResult.kt — добавлен AMBIGUOUS
- StateContext.kt — добавлен contextCmd
- StateMachine.kt — инициализация AmbiguousState
- RecognizedCommandState.kt — проверка на неоднозначность
- ListeningCommandState.kt — передача контекста в NLU
- CommandConfig.kt — добавлено поле ambiguous
- AppConfig.kt — добавлено поле antonyms в NLUConfig
- config.json — добавлены phrases.ambiguous и nlu.antonyms

## Сборка
```
BUILD SUCCESSFUL in 18s
37 actionable tasks: 4 executed, 33 up-to-date
APK: app/build/outputs/apk/debug/app-debug.apk (106 MB)
```

## Использование контекста

При неоднозначности:
1. User says "окно"
2. NLU возвращает contextCmd=["window_open", "ac_set_temp"]
3. AmbiguousState говорит: "Открыть окно или Включить климат?"
4. Переход в LISTENING_COMMAND с contextCmd
5. User says "окно" → NLU использует контекст → window_open

**How to apply:** При работе с NLU помнить о contextCmd и необходимости передачи между стейтами.
