package ru.voboost.voice.states

import ru.voboost.voice.executor.CommandData

/**
 * Контекст состояния голосового помощника
 *
 * Используется для передачи данных между стейтами:
 * - Распознанная команда
 * - Результат подтверждения
 * - Ошибки
 * - Контекст команд (список ID) для повышения вероятности правильной команды при неоднозначности
 *
 * Преимущества:
 * - ? Явная передача данных между стейтами
 * - ? Легко тестировать (мокаем контекст)
 * - ? IState Machine не знает о бизнес-логике
 */
data class StateContext(var commandData: CommandData? = null,
                        var attemptsCount: Int = 0)



