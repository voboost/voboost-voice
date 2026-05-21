# VoboostVoiceAssistant

[![Documentation](https://img.shields.io/badge/Docs-ru-green.svg)](./docs/README.md)

Голосовой помощник для автомобилей с поддержкой русского языка.

> 📖 **Внимание:** Данный README содержит устаревшую информацию.  
> Для актуальной документации перейдите к [docs/README.md](./docs/README.md)

## Возможности

- ✅ Голосовое управление автомобилем (лючки, режимы, климат)
- ✅ Управление телефоном (звонки по контактам и номерам)
- ✅ Офлайн распознавание речи (Vosk + Sherpa-ONNX)
- ✅ Онлайн распознавание (Yandex SpeechKit - опционально)
- ✅ Активация по кнопке на руле или кодовой фразе
- ✅ Визуальные эффекты (анимация микрофона)
- ✅ TTS ответы (Sherpa-ONNX + системный)
- ✅ 26 команд из коробки
- ✅ Расширяемая система команд (JSON конфиг)

## 📚 Документация

| Документ | Описание |
|----------|----------|
| [docs/README.md](./docs/README.md) | **Основная документация** (установка, настройка, примеры) |
| [docs/ARCHITECTURE/OVERVIEW.md](./docs/ARCHITECTURE/OVERVIEW.md) | Архитектура системы |
| [docs/FEATURES/TSR.md](./docs/FEATURES/TSR.md) | Система предупреждения о скорости (TSR) |

---

## ⚠️ Устаревшая информация

Этот README содержит базовую информацию и ссылки на актуальную документацию.  
Для установки и настройки используйте [docs/README.md](./docs/README.md).

### Структура проекта (устаревшая)

```
app/src/main/java/ru/voboost/voice/
├── VoboostVoiceService.kt       # Главный сервис
├── VoiceActivationService.kt    # Accessibility для кнопки
├── VoiceCommandReceiver.kt      # Broadcast receiver
├── BootActivity.kt              # Для автозапуска
├── BootReceiver.kt              # Обработчик BOOT_COMPLETED
│
├── audio/                       # Источники аудио (8+ файлов)
├── canbus/                      # CAN Bus интеграция (5+ файлов)
├── config/                      # Конфигурация (6 файлов)
├── core/                        # Ядро системы (7+ файлов)
├── engine/                      # Движки STT/TTS (10+ файлов)
├── executor/                    # Обработчики команд (45+ файлов)
├── nlu/                         # Natural Language Understanding (6+ файлов)
├── speech/                      # Модули распознавания (10+ файлов)
└── ui/                          # UI компоненты
```

### Установка

```bash
# Собрать APK
cd VoboostVoiceAssistant
./gradlew assembleDebug

# Использовать скрипт установки (рекомендуется)
scripts/install/VoboostVoiceAssistant-install.bat
```

Для подробной инструкции см. [docs/SETUP/INSTALLATION.md](./docs/SETUP/INSTALLATION.md)

---

### Конфигурация команд

Команды настраиваются в `app/src/main/assets/config.json` или `/data/user/0/ru.voboost.voice/files/config.json`.

**26 команд из коробки:**
- Управление автомобилем (лючки, бензобак)
- Режимы умной системы (отдых, детский, романтика, мойка)
- Климат (вкл/выкл, температура)
- Телефон (звонки по имени и номеру)
- Окна (открыть/закрыть)
- Режимы вождения (эко, комфорт, спорт, бездорожье и др.)
- Режимы источника питания (электро, гибрид, сохранение)

Список всех команд см. в [docs/SETUP/CONFIGURATION.md](./docs/SETUP/CONFIGURATION.md)

---

### Активация

1. **Кодовая фраза:** "Привет, Вобуст" / "привет воях" (или "привет машина")
2. **Кнопка на руле:** KEYCODE=16 (настраивается в config.json)

---

## Добавление новых команд

См. инструкцию: [docs/SETUP/CONFIGURATION.md](./docs/SETUP/CONFIGURATION.md#%D0%B4%D0%BE%D0%B1%D0%B0%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-%D0%BD%D0%BE%D0%B2%D0%BE%D0%B9-%D0%BA%D0%BE%D0%BC%D0%B0%D0%BD%D0%B4%D1%8B)

---

## Архитектура

См. [docs/ARCHITECTURE/OVERVIEW.md](./docs/ARCHITECTURE/OVERVIEW.md) для подробного описания:
- State Machine (9 состояний)
- Speech Engine Factory (Vosk/Sherpa/System)
- Executor Pattern
- Audio Sources

---

## Статус

- ✅ Базовая функциональность
- ✅ Офлайн распознавание (Vosk + Sherpa-ONNX)
- ✅ 26 команд из коробки
- ✅ Кнопка на руле (KEYCODE=16 в config.json)
- ⚠️ Онлайн режим (требуются API ключи Yandex)

---

## 📞 Поддержка

**Author:** Voboost Team  
**License:** [License information to be added]
**Документация:** [docs/README.md](./docs/README.md)


