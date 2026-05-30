package ru.voboost.voice.states

import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.audio.MultiChannelAudioSource
import ru.voboost.voice.audio.VolumeManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.services.recognition.IRecognitionService
import ru.voboost.voice.executor.CommandExecutor
import ru.voboost.voice.nlu.INLUEngine
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.services.speech.ISpeechService
import ru.voboost.voice.ui.ToastMessengerManager
import ru.voboost.voice.ui.VoceAnimationManager
import java.util.concurrent.atomic.AtomicBoolean

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



