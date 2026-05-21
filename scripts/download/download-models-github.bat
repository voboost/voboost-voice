@echo off
chcp 65001 >nul
REM Скрипт для загрузки моделей Sherpa-ONNX с GitHub Releases
REM Ссылки: https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models

echo ============================================
echo Sherpa-ONNX Model Downloader (GitHub)
echo ============================================
echo.

set MODELS_DIR=sherpa-models
if not exist %MODELS_DIR% mkdir %MODELS_DIR%

echo Downloading Russian ASR model (Zipformer)...
echo Model: sherpa-onnx-zipformer-ru-2024-09-18
echo Size: ~300 MB
echo.

REM GitHub Releases - правильная ссылка
curl -L -o %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18.tar.bz2 ^
  "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-ru-2024-09-18.tar.bz2"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ASR model downloaded successfully!
    echo Extracting...
    
    REM Распаковка .tar.bz2
    tar -xjf %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18.tar.bz2 -C %MODELS_DIR%/
    
    echo ASR model extracted to %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18/
) else (
    echo.
    echo Failed to download ASR model
    echo Please download manually from:
    echo https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models
)

echo.
echo ============================================
echo Downloading Russian TTS model (VITS)...
echo Model: vits-piper-ru-ru-irina-low
echo Size: ~50 MB
echo.

REM TTS модель с GitHub
curl -L -o %MODELS_DIR%/ru_RU-irina-low.onnx ^
  "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/vits-piper-ru-ru-irina-low.onnx"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo TTS model downloaded successfully!
) else (
    echo.
    echo Failed to download TTS model
    echo Trying alternative source...
    echo.
    
    REM Альтернативная ссылка
    curl -L -o %MODELS_DIR%/ru_RU-irina-low.onnx ^
      "https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low/resolve/main/ru_RU-irina-low.onnx"
      
    if %ERRORLEVEL% EQU 0 (
        echo TTS model downloaded from alternative source!
    ) else (
        echo Please download manually from:
        echo https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models
    )
)

echo.
echo ============================================
echo Next steps:
echo 1. Copy ASR model to app/src/main/assets/sherpa/asr-ru-model/
echo 2. Copy TTS model to app/src/main/assets/sherpa/tts-ru-model/
echo.
echo Or run: copy-sherpa-models.bat
echo ============================================
pause

