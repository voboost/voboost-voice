---
name: ambiguous_state_implementation
description: Реализация состояния AMBIGUOUS для обработки неоднозначности команд в VoboostVoiceAssistant
type: project
---

Состояние AMBIGUOUS добавлено в State Machine для обработки случаев, когда NLU возвращает 2+ команды с похожим score. При неоднозначности:
- TTS говорит: "id1 или id2?" (из phrases.ambiguous или id команды как заглушка)
- Возвращается в LISTENING_COMMAND с контекстом (contextCmd)
- NLU использует контекст для повышения вероятности правильной команды

Изменённые файлы:
- app/src/main/java/ru/voboost/voice/states/state/AmbiguousState.kt (НОВЫЙ)
- StateResult.kt — добавлен AMBIGUOUS
- StateContext.kt — добавлено contextCmd: List<String>
- StateMachine.kt — инициализация AmbiguousState
- RecognizedCommandState.kt — проверка на неоднозначность
- ListeningCommandState.kt — передача контекста в NLU как второй параметр
- CommandConfig.kt — добавлено поле ambiguous в CommandPhrases
- config.json — добавлены фразы ambiguous для всех 27 команд
- IdleState, KeywordErrorState, CommandErrorState — очистка контекста

Сборка: BUILD SUCCESSFUL (app-debug.apk 106 MB)

**How to apply:** При работе с NLU или состояниями помнить о contextCmd и необходимости передачи контекста между стейтами.
