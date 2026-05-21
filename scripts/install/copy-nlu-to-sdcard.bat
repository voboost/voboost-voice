@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================================
REM  VoboostVoiceAssistant - Копирование NLU моделей на устройство
REM ============================================================================

set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "OUT_PATH=/storage/emulated/0/Android/data/ru.voboost.voiceassistant/files"
set "PATH=%ADB_PATH%;%PATH%"
set "NLU_MODEL_PATH=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\models\nlu\model.onnx"
set "TOKENIZER_PATH=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\models\nlu\tokenizer.json"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Copy NLU Models
echo ============================================================================
echo.

REM 1. Проверка ADB
echo [1/4] Проверка подключения ADB...
adb shell "echo 1" >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Устройство не отвечает или ADB не настроен.
    pause & exit /b 1
)
echo [OK] Устройство найдено и доступно
echo.

REM 2. Создание директорий
echo [2/4] Создание директорий...
adb shell "mkdir -p %OUT_PATH%/models/nlu" >nul 2>&1
echo [OK] Директория готова
echo.

REM 3. NLU модель
echo [3/4] Копирование NLU модели...
if exist "%NLU_MODEL_PATH%" (
    echo       Копирование model.onnx...
    adb push "%NLU_MODEL_PATH%" "%OUT_PATH%/models/nlu/model.onnx"
    
    if !errorlevel! equ 0 (echo [OK] NLU модель скопирована) else (echo [ERROR] Сбой копирования! & pause & exit /b 1)
) else (
    echo [WARN] NLU модель не найдена! (ожидается models\nlu\model.onnx)
)
echo.

REM 4. Токенайзер
echo [4/4] Копирование токенайзера...
if exist "%TOKENIZER_PATH%" (
    echo       Копирование tokenizer.json...
    adb push "%TOKENIZER_PATH%" "%OUT_PATH%/models/nlu/tokenizer.json"
    
    if !errorlevel! equ 0 (echo [OK] Токенайзер скопирован) else (echo [ERROR] Сбой копирования! & pause & exit /b 1)
) else (
    echo [WARN] Токенайзер не найден! (ожидается models\nlu\tokenizer.json)
)
echo.

REM Финальная проверка ключевых файлов
echo ============================================================================
echo  Проверка целостности...
echo ============================================================================
adb shell "test -f '%OUT_PATH%/models/nlu/model.onnx' && echo [OK] NLU model || echo [FAIL] NLU model"
adb shell "test -f '%OUT_PATH%/models/nlu/tokenizer.json' && echo [OK] Tokenizer || echo [FAIL] Tokenizer"
echo.

echo ============================================================================
echo  Готово! Перезапустите приложение:
echo    adb shell am force-stop ru.voboost.voiceassistant
echo    adb shell am start-foreground-service ru.voboost.voiceassistant/.VoboostVoiceService
echo ============================================================================
pause
