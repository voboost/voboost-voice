@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================================
REM  VoboostVoiceAssistant - Копирование моделей и конфига на устройство
REM ============================================================================

set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "OUT_PATH=/storage/emulated/0/Android/data/ru.voboost.voiceassistant/files"
set "PATH=%ADB_PATH%;%PATH%"
set "MODEL_SHERPA_PATH=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\models\sherpa"
set "MODEL_VOSK_PATH=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\models\vosk"
set "MODEL_CONFIG_PATH=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\app\src\main\assets\config.json"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Copy Config & Models
echo ============================================================================
echo.

REM 1. Проверка ADB
echo [1/5] Проверка подключения ADB...
adb shell "echo 1" >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Устройство не отвечает или ADB не настроен.
    pause & exit /b 1
)
echo [OK] Устройство найдено и доступно
echo.

REM 2. Создание директорий
echo [2/5] Создание директорий...
adb shell "mkdir -p %OUT_PATH%/models/vosk" >nul 2>&1
adb shell "mkdir -p %OUT_PATH%/models/sherpa" >nul 2>&1
echo [OK] Директории готовы
echo.

REM 3. Config
echo [3/5] Копирование config.json...
if exist "%MODEL_CONFIG_PATH%" (
    adb push "%MODEL_CONFIG_PATH%" "%OUT_PATH%/config.json" >nul 2>&1
    if !errorlevel! equ 0 (echo [OK] config.json скопирован) else (echo [ERROR] Ошибка копирования config.json & pause & exit /b 1)
) else (
    echo [WARN] config.json не найден в assets
)
echo.

REM 4. Vosk модель
echo [4/5] Копирование Vosk модели...
if exist "%MODEL_VOSK_PATH%\vosk-model-small-ru-0.22" (
    echo       Прямое копирование папки...
    adb push "%MODEL_VOSK_PATH%\vosk-model-small-ru-0.22" "%OUT_PATH%/models/vosk/" >nul 2>&1
    if !errorlevel! equ 0 (echo [OK] Папка скопирована) else (echo [ERROR] Сбой копирования! & pause & exit /b 1)
) else if exist "%MODEL_VOSK_PATH%\vosk-model-small-ru-0.22.tar.gz" (
    echo       Копирование архива + распаковка...
    adb push "%MODEL_VOSK_PATH%\vosk-model-small-ru-0.22.tar.gz" "%OUT_PATH%/models/vosk/" >nul 2>&1
    if !errorlevel! equ 0 (
        echo       Распаковка на устройстве...
        adb shell "cd %OUT_PATH%/models/vosk && tar -xzf vosk-model-small-ru-0.22.tar.gz"
        if !errorlevel! equ 0 (
            adb shell "rm -f %OUT_PATH%/models/vosk/vosk-model-small-ru-0.22.tar.gz"
            echo [OK] Модель распакована
        ) else (
            echo [ERROR] Распаковка tar.gz не удалась! & pause & exit /b 1
        )
    ) else (
        echo [ERROR] Ошибка загрузки архива! & pause & exit /b 1
    )
) else (
    echo [WARN] Vosk модель не найдена! (ожидается models\vosk\vosk-model-small-ru-0.22 или архив .tar.gz)
)
echo.

REM 5. Sherpa TTS модель
echo [5/5] Копирование Sherpa TTS модели...
if exist "%MODEL_SHERPA_PATH%\tts-ru-model" (
    echo       Прямое копирование папки...
    adb push "%MODEL_SHERPA_PATH%\tts-ru-model" "%OUT_PATH%/models/sherpa/" >nul 2>&1
    if !errorlevel! equ 0 (echo [OK] Папка скопирована) else (echo [ERROR] Сбой копирования! & pause & exit /b 1)
) else if exist "%MODEL_SHERPA_PATH%\tts-ru-model.tar.gz" (
    echo       Копирование архива + распаковка...
    adb push "%MODEL_SHERPA_PATH%\tts-ru-model.tar.gz" "%OUT_PATH%/models/sherpa/" >nul 2>&1
    if !errorlevel! equ 0 (
        echo       Распаковка на устройстве...
        adb shell "cd %OUT_PATH%/models/sherpa && tar -xzf tts-ru-model.tar.gz"
        if !errorlevel! equ 0 (
            adb shell "rm -f %OUT_PATH%/models/sherpa/tts-ru-model.tar.gz"
            echo [OK] Модель распакована
        ) else (
            echo [ERROR] Распаковка tar.gz не удалась! & pause & exit /b 1
        )
    ) else (
        echo [ERROR] Ошибка загрузки архива! & pause & exit /b 1
    )
) else (
    echo [WARN] Sherpa модель не найдена!
)
echo.

REM Финальная проверка ключевых файлов
echo ============================================================================
echo  Проверка целостности...
echo ============================================================================
adb shell "test -f '%OUT_PATH%/config.json' && echo [OK] config.json || echo [FAIL] config.json"
adb shell "test -f '%OUT_PATH%/models/vosk/vosk-model-small-ru-0.22/am/final.mdl' && echo [OK] Vosk model || echo [FAIL] Vosk model (проверьте распаковку)"
adb shell "test -f '%OUT_PATH%/models/sherpa/tts-ru-model/ru_RU-ruslan-medium.onnx' && echo [OK] Sherpa TTS || echo [FAIL] Sherpa TTS"
echo.

echo ============================================================================
echo  Готово! Перезапустите приложение:
echo    adb shell am force-stop ru.voboost.voiceassistant
echo    adb shell am start-foreground-service ru.voboost.voiceassistant/.VoboostVoiceService
echo ============================================================================
pause