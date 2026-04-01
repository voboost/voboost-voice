@echo off
REM Скрипт для копирования моделей Sherpa-ONNX в assets

echo ============================================
REM Копирование моделей Sherpa-ONNX в assets
echo ============================================
echo.

set SOURCE_DIR=sherpa-models
set ASSETS_DIR=app\src\main\assets\sherpa

REM Создаем директорию assets
if not exist %ASSETS_DIR% mkdir %ASSETS_DIR%

echo Copying ASR model...
if exist %SOURCE_DIR%\sherpa-onnx-zipformer-ru-2024-09-18 (
    rmdir /s /q %ASSETS_DIR%\asr-ru-model 2>nul
    mkdir %ASSETS_DIR%\asr-ru-model
    xcopy /E /I /Y %SOURCE_DIR%\sherpa-onnx-zipformer-ru-2024-09-18\* %ASSETS_DIR%\asr-ru-model\
    echo ASR model copied to %ASSETS_DIR%\asr-ru-model\
) else (
    echo ASR model not found. Run download-sherpa-models.bat first
)

echo.
echo Copying TTS model...
if exist %SOURCE_DIR%\ru_RU-irina-low.onnx (
    copy /Y %SOURCE_DIR%\ru_RU-irina-low.onnx %ASSETS_DIR%\tts-ru-model\
    echo TTS model copied to %ASSETS_DIR%\tts-ru-model\
) else (
    echo TTS model not found. Run download-sherpa-models.bat first
)

echo.
echo ============================================
echo Models copied successfully!
echo ============================================
pause
