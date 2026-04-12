package ru.voboost.voiceassistant.speech.state

import ru.voboost.voiceassistant.SoundEffectManager
import ru.voboost.voiceassistant.config.ConfigManager
import ru.voboost.voiceassistant.core.ISpeechSynthesis
import ru.voboost.voiceassistant.executor.CommandExecutor
import ru.voboost.voiceassistant.nlu.NLUEngine
import ru.voboost.voiceassistant.nlu.RecognizedCommand
import ru.voboost.voiceassistant.speech.SpeechRecognizer
import ru.voboost.voiceassistant.ui.OverlayManager
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
 * - ✅ Явная передача данных между стейтами
 * - ✅ Легко тестировать (мокаем контекст)
 * - ✅ IState Machine не знает о бизнес-логике
 */
data class StateContext(
    // Зависимости (для создания новых состояний)
    var speechRecognizer: SpeechRecognizer? = null,
    var overlayManager: OverlayManager? = null,
    var volumeManager: ru.voboost.voiceassistant.audio.VolumeManager? = null,
    var ttsEngine: ISpeechSynthesis? = null,
    var configManager: ConfigManager? = null,
    var nluEngine: NLUEngine? = null,
    var commandExecutor: CommandExecutor? = null,
    var soundEffectManager: SoundEffectManager? = null,  // Для звуковых эффектов

    // Данные состояния
    var recognizedCommand: RecognizedCommand? = null,
    var commandText: String? = null,
    var error: String? = null,
    var zone: String = "front_left",  // Зона говорящего: front_left, front_right, second_left, second_right
    val isCancelling: AtomicBoolean = AtomicBoolean(false)  // Единая блокировка отмены
)
