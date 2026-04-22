@echo off
chcp 65001 >nul
REM Скрипт для загрузки моделей Sherpa-ONNX с прямыми ссылками

echo ============================================
echo Sherpa-ONNX Model Downloader (Direct Links)
echo ============================================
echo.

set MODELS_DIR=sherpa-models
if not exist %MODELS_DIR% mkdir %MODELS_DIR%

echo Downloading Russian ASR model (Zipformer)...
echo Model: sherpa-onnx-zipformer-ru-2024-09-18
echo Size: ~300 MB
echo.

REM Прямая ссылка на HuggingFace
curl -L -o %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz ^
  "https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18/resolve/main/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ASR model downloaded successfully!
    echo Extracting...
    
    REM Распаковка
    tar -xzf %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz -C %MODELS_DIR%/
    
    echo ASR model extracted to %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18/
) else (
    echo.
    echo Failed to download ASR model
    echo Please download manually from:
    echo https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18
)

echo.
echo ============================================
echo Downloading Russian TTS model (VITS Piper)...
echo Model: vits-piper-ru-ru-irina-low
echo Size: ~50 MB
echo.

REM Прямая ссылка на HuggingFace
curl -L -o %MODELS_DIR%/ru_RU-irina-low.onnx ^
  "https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low/resolve/main/ru_RU-irina-low.onnx"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo TTS model downloaded successfully!
) else (
    echo.
    echo Failed to download TTS model
    echo Please download manually from:
    echo https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low
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
