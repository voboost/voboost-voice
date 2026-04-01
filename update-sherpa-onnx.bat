@echo off
REM Скрипт для загрузки последней версии Sherpa-ONNX

echo ============================================
echo Sherpa-ONNX Latest Version Downloader
echo ============================================
echo.

set LIBS_DIR=app\libs
set VERSION=1.12.34

REM Создаем директорию libs если нет
if not exist %LIBS_DIR% mkdir %LIBS_DIR%

echo Downloading Sherpa-ONNX v%VERSION%...
echo.

REM Ссылка на SourceForge
set DOWNLOAD_URL=https://sourceforge.net/projects/sherpa-onnx.mirror/files/v%VERSION%/sherpa-onnx-v%VERSION%.jar/download

REM Используем curl для загрузки
curl -L -o %LIBS_DIR%/sherpa-onnx-v%VERSION%-java8.jar %DOWNLOAD_URL%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo Download successful!
    echo ============================================
    echo.
    echo File: %LIBS_DIR%\sherpa-onnx-v%VERSION%-java8.jar
    echo.
    echo Next steps:
    echo 1. Update build.gradle to use new version
    echo 2. Rebuild project
    echo.
) else (
    echo.
    echo ============================================
    echo Download failed!
    echo ============================================
    echo.
    echo Please download manually from:
    echo https://sourceforge.net/projects/sherpa-onnx.mirror/files/v%VERSION%/
    echo.
)

pause
