@echo off
chcp 65001 >nul
REM Скрипт для загрузки TTS модели с GitHub Piper

echo ============================================
echo Russian TTS Model Downloader (Piper)
echo ============================================
echo.

set MODELS_DIR=sherpa-models
if not exist %MODELS_DIR% mkdir %MODELS_DIR%

echo Downloading Russian TTS model from Piper...
echo Model: ru_RU-irina-medium (Piper)
echo Size: ~60 MB
echo.

REM Piper TTS модель - рабочая ссылка
curl -L -o %MODELS_DIR%/ru_RU-irina-medium.onnx ^
  "https://github.com/rhasspy/piper/releases/download/v1.2.0/ru_RU-irina-medium.onnx"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo TTS model downloaded successfully!
    echo.
    REM Проверка размера
    for %%A in (%MODELS_DIR%\ru_RU-irina-medium.onnx) do set SIZE=%%~zA
    if %SIZE% GTR 1000000 (
        echo File size: %SIZE% bytes - OK!
    ) else (
        echo File size: %SIZE% bytes - TOO SMALL!
    )
) else (
    echo.
    echo Failed to download TTS model
    echo Please download manually from:
    echo https://github.com/rhasspy/piper/releases
)

echo.
echo ============================================
pause
