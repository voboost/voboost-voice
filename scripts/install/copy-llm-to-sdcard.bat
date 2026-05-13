@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================================
REM  VoboostVoiceAssistant - Копирование моделей и конфига на устройство
REM ============================================================================

set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "OUT_PATH=/storage/emulated/0/Android/data/ru.voboost.voiceassistant/files"
set "PATH=%ADB_PATH%;%PATH%"
set "MODEL_LLM_PATH=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\models\llm\Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task"

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
adb shell "mkdir -p %OUT_PATH%/models/llm" >nul 2>&1
echo [OK] Директоря готова
echo.


REM 3. LLM модель
echo [4/5] Копирование LLM модели...
if exist "%MODEL_LLM_PATH%" (
    echo       Копирование LLM...
    adb push "%MODEL_LLM_PATH%" "%OUT_PATH%/models/llm/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task"

    if !errorlevel! equ 0 (echo [OK] LLM скопирована) else (echo [ERROR] Сбой копирования! & pause & exit /b 1)
) else (
    echo [WARN] LLM модель не найдена! (ожидается models\llm\Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task)
)
echo.

REM Финальная проверка ключевых файлов
echo ============================================================================
echo  Проверка целостности...
echo ============================================================================
adb shell "test -f '%OUT_PATH%/models/llm/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task' && echo [OK] LLM model || echo [FAIL] LLM model"
echo.

echo ============================================================================
echo  Готово! Перезапустите приложение:
echo    adb shell am force-stop ru.voboost.voiceassistant
echo    adb shell am start-foreground-service ru.voboost.voiceassistant/.VoboostVoiceService
echo ============================================================================
pause