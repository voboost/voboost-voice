package ru.voboost.voice.speech.state

import ru.voboost.voice.SoundEffectManager
import ru.voboost.voice.config.ConfigManager
import ru.voboost.voice.core.ISpeechRecognizer
import ru.voboost.voice.core.QueueSpeechSynthesis
import ru.voboost.voice.executor.CommandExecutor
import ru.voboost.voice.nlu.INLUEngine
import ru.voboost.voice.nlu.RecognizedCommand
import ru.voboost.voice.ui.OverlayManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Контекст состояния голосового помощника
 *
 * Используется для передачи данных между стейтами:
 * - Распознанная команда
 * - Результат подтверждения
 * - Ошибки
 *
 * Преимущества:
 * - ? Явная передача данных между стейтами
 * - ? Легко тестировать (мокаем контекст)
 * - ? IState Machine не знает о бизнес-логике
 */
data class StateContext(var speechRecognizer: ISpeechRecognizer? = null,
                        var overlayManager: OverlayManager? = null,
                        var volumeManager: ru.voboost.voice.audio.VolumeManager? = null,
                        var queueSpeech: QueueSpeechSynthesis? = null,
                        var configManager: ConfigManager? = null,
                        var nluEngine: INLUEngine? = null,
                        var commandExecutor: CommandExecutor? = null,
                        var soundEffectManager: SoundEffectManager? = null,  // Для звуковых эффектов
    // Данные состояния
                        var recognizedCommand: RecognizedCommand? = null,
                        var commandText: String? = null,
                        var error: String? = null,
                        var zone: String = "front_left",  // Зона говорящего: front_left, front_right, second_left, second_right
                        val isCancelling: AtomicBoolean = AtomicBoolean(false))

