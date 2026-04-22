@echo off
chcp 65001 >nul
REM Скрипт для загрузки моделей с ModelScope (альтернатива HuggingFace)

echo ============================================
echo Sherpa-ONNX Model Downloader (ModelScope)
echo ============================================
echo.

set MODELS_DIR=sherpa-models
if not exist %MODELS_DIR% mkdir %MODELS_DIR%

echo Downloading Russian ASR model from ModelScope...
echo.

REM ModelScope mirror
curl -L -o %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz ^
  "https://www.modelscope.cn/models/k2-fsa/sherpa-onnx-zipformer-ru-2024-09-18/resolve/master/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ASR model downloaded successfully!
    echo Extracting...
    tar -xzf %MODELS_DIR%/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz -C %MODELS_DIR%/
) else (
    echo.
    echo Failed to download from ModelScope
    echo Please download manually from HuggingFace:
    echo https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18
)

echo.
echo ============================================
echo Downloading Russian TTS model from ModelScope...
echo.

curl -L -o %MODELS_DIR%/ru_RU-irina-low.onnx ^
  "https://www.modelscope.cn/models/k2-fsa/vits-piper-ru-ru-irina-low/resolve/master/ru_RU-irina-low.onnx"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo TTS model downloaded successfully!
) else (
    echo.
    echo Failed to download from ModelScope
    echo Please download manually from HuggingFace:
    echo https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low
)

echo.
echo ============================================
pause
