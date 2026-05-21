@echo off
chcp 65001 >nul
REM ========================================
REM Загрузка нативных библиотек на устройство
REM ========================================
REM Скрипт копирует 4 нативные библиотеки в системную папку
REM для приложения VoboostVoiceAssistant
REM ========================================

setlocal enabledelayedexpansion

set LIBS_DIR=native_libs\arm64-v8a
set DEVICE_PATH=/system/priv-app/VoboostVoiceAssistant/lib/arm64

echo ========================================
echo  Загрузка нативных библиотек
echo  Путь: %DEVICE_PATH%
echo ========================================
echo.

REM Проверка наличия ADB
adb devices >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB не найден! Убедитесь что Android SDK Platform Tools установлен.
    pause
    exit /b 1
)

REM Проверка наличия библиотек
if not exist "%LIBS_DIR%\libjnidispatch.so" (
    echo [ERROR] %LIBS_DIR%\libjnidispatch.so не найден!
    exit /b 1
)
if not exist "%LIBS_DIR%\libonnxruntime.so" (
    echo [ERROR] %LIBS_DIR%\libonnxruntime.so не найден!
    exit /b 1
)
if not exist "%LIBS_DIR%\libsherpa-onnx-jni.so" (
    echo [ERROR] %LIBS_DIR%\libsherpa-onnx-jni.so не найден!
    exit /b 1
)
if not exist "%LIBS_DIR%\libvosk.so" (
    echo [ERROR] %LIBS_DIR%\libvosk.so не найден!
    exit /b 1
)

echo [1/6] Получение root-прав...
adb root
timeout /t 2 /nobreak >nul

echo [2/6] Перемонтирование /system в режим записи...
adb remount
timeout /t 2 /nobreak >nul

echo [3/6] Создание директории %DEVICE_PATH%...
adb shell mkdir -p %DEVICE_PATH%

echo [4/6] Копирование библиотек...
adb push %LIBS_DIR%\libjnidispatch.so %DEVICE_PATH%/
adb push %LIBS_DIR%\libonnxruntime.so %DEVICE_PATH%/
adb push %LIBS_DIR%\libsherpa-onnx-jni.so %DEVICE_PATH%/
adb push %LIBS_DIR%\libvosk.so %DEVICE_PATH%/

echo [5/6] Установка прав доступа (chmod 644)...
adb shell chmod 644 %DEVICE_PATH%/libjnidispatch.so
adb shell chmod 644 %DEVICE_PATH%/libonnxruntime.so
adb shell chmod 644 %DEVICE_PATH%/libsherpa-onnx-jni.so
adb shell chmod 644 %DEVICE_PATH%/libvosk.so

echo [6/6] Проверка загрузки...
adb shell ls -la %DEVICE_PATH%

echo.
echo ========================================
echo  Готово! Библиотеки загружены.
echo ========================================
echo.
echo Не забудьте перезапустить приложение:
echo   adb shell am force-stop ru.voboost.voice
echo.

pause

