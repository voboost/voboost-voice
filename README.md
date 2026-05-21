# 🚧 Voboost Voice Assistant (Development)

[![Documentation](https://img.shields.io/badge/Docs-ru-green.svg)](./docs/README.md)
[![License](https://img.shields.io/github/license/voboost/voboost-voice)](https://github.com/voboost/voboost-voice/blob/main/LICENSE)

> 📌 **Это экспериментальный репозиторий.** Используйте ветку `feature/development` для разработки, `main` — для стабильных релизов.

## 📋 Содержание
- [Структура веток](#-структура-веток)
- [Workflow](#-workflow)
- [Установка зависимостей](#-установка-зависимостей)
- [Сборка проекта](#-сборка-проекта)

---

## 🌿 Структура веток

| Ветка | Описание | Использование |
|-------|----------|---------------|
| `main` (protected) | Стабильные релизы | Только для production, PR только с review |
| `feature/development` (default) | Рабочая разработка | Все активные изменения и эксперименты |

### Почему так?

- **`main`** — защищена от случайных изменений. Только после code review и тестирования.
- **`feature/development`** — ваша основная ветка для работы. Здесь все эксперименты, новые функции, багфиксы.

---

## 🔀 Workflow

### Для разработки (все новые изменения):

```bash
# 1. Убедитесь что на feature/development
git checkout feature/development

# 2. Создайте ветку для новой функции/бага
git checkout -b feat/my-new-feature
# или: git checkout -b fix/bug-description

# 3. Работайте в своей ветке, коммитьте изменения
git add .
git commit -m "feat: describe your changes"

# 4. Отправьте ветку на GitHub
git push -u origin feat/my-new-feature

# 5. Создайте Pull Request в feature/development
```

### Для релизов (создание нового release):

```bash
# 1. Убедитесь что feature/development стабилен
git checkout feature/development
git pull origin feature/development

# 2. Слейте изменения в main
git checkout main
git merge feature/development --no-ff -m "Release: v1.x.x"

# 3. Создайте тег и отправьте на GitHub
git tag v1.0.0
git push origin main --tags

# 4. На GitHub создайте Release из тега
```

---

## ⚙️ Установка зависимостей

### Модели (не хранятся в git)

Модели STT/TTS загружаются автоматически при первом запуске:

```bash
# Скачать моделиSherpa TTS/ASR
scripts/download/download-sherpa-models.bat

# Скачать модель Vosk ASR
scripts/download/download-vosk-models.bat

# Скачать модели LLM/NLU (опционально)
scripts/download/download-llm-models.bat
```

---

## 🛠️ Сборка проекта

### Автоматическая сборка:

```bash
# Из папки export
cd D:\Projects\Android\MM\6.11.1\export

# Только Debug
VoboostVoiceAssistant\scripts\build-project.bat 1

# Только Release
VoboostVoiceAssistant\scripts\build-project.bat 2

# Оба режима
VoboostVoiceAssistant\scripts\build-project.bat 3
```

### Ручная сборка:

```bash
cd VoboostVoiceAssistant

# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

---

## 📚 Документация

| Документ | Описание |
|----------|----------|
| [docs/README.md](./docs/README.md) | **Основная документация** (установка, настройка, примеры) |
| [docs/ARCHITECTURE/OVERVIEW.md](./docs/ARCHITECTURE/OVERVIEW.md) | Архитектура системы |
| [CONTEXT.md](./CONTEXT.md) | История изменений и текущее состояние проекта |

---

## ⚠️ Важно

1. **Модели не хранятся в репозитории** — используются скрипты загрузки
2. **`main` защищена** — только через Pull Request с review
3. **Все изменения в `feature/development`** — даже небольшие фиксы

---

## 📞 Поддержка

- **Author:** Voboost Team  
- **License:** [License information to be added]  
- **Documentation:** [docs/README.md](./docs/README.md)

---

*Последнее обновление: 2026-05-21*


