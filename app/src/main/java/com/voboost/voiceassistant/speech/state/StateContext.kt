package com.voboost.voiceassistant.speech.state

import com.voboost.voiceassistant.nlu.RecognizedCommand

/**
 * Контекст состояния голосового помощника
 * 
 * Используется для передачи данных между стейтами:
 * - Распознанная команда
 * - Результат подтверждения
 * - Ошибки
 * 
 * Преимущества:
 * - ✅ Явная передача данных между стейтами
 * - ✅ Легко тестировать (мокаем контекст)
 * - ✅ State Machine не знает о бизнес-логике
 */
data class StateContext(
    var recognizedCommand: RecognizedCommand? = null,
    var commandText: String? = null,
    var error: String? = null,
    var zone: String = "front_left"  // Зона говорящего: front_left, front_right, second_left, second_right
)
