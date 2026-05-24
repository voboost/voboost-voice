package ru.voboost.voice.states

import kotlinx.coroutines.CoroutineScope
import ru.voboost.voice.SoundEffectManager
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
 *
 * Преимущества:
 * - ? Явная передача данных между стейтами
 * - ? Легко тестировать (мокаем контекст)
 * - ? IState Machine не знает о бизнес-логике
 */
data class StateContext(var recognitionService: IRecognitionService? = null,
                        var voceAnimationManager: VoceAnimationManager? = null,
                        var volumeManager: VolumeManager? = null,
                        var speechService: ISpeechService? = null,
                        var configManager: ConfigManager? = null,
                        var nluEngine: INLUEngine? = null,
                        var commandExecutor: CommandExecutor? = null,
                        var soundEffectManager: SoundEffectManager? = null,
                        // Данные состояния
                        var commandData: CommandData? = null,
                        var commandText: String? = null,
                        var error: String? = null,
                        var zone: String = "front_left",  // Зона говорящего: front_left, front_right, second_left, second_right
                        val isCancelling: AtomicBoolean = AtomicBoolean(false))

