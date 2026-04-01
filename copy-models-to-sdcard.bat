@echo off
REM ============================================================================
REM  VoboostVoiceAssistant - Копирование моделей и конфига на SD-карту
REM ============================================================================
REM  Этот скрипт копирует config.json и модели на SD-карту устройства
REM  Используйте после установки APK в /system/priv-app/
REM ============================================================================

setlocal enabledelayedexpansion

REM Добавляем путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Copy Config and Models to SD Card
echo ============================================================================
echo.

REM Проверка ADB
echo [1/6] Проверка подключения ADB...
adb devices >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB не найден! Убедитесь что устройство подключено.
    pause
    exit /b 1
)
echo [OK] ADB подключен
echo.

REM Создаём директорию на SD-карте
echo [2/6] Создание директорий на SD-карте...
adb shell "mkdir -p /sdcard/Android/data/com.voboost.voiceassistant/files/models/vosk"
adb shell "mkdir -p /sdcard/Android/data/com.voboost.voiceassistant/files/models/sherpa"
echo [OK] Директории созданы
echo.

REM Копирование config.json
echo [3/6] Копирование config.json...
if exist "app\src\main\assets\config.json" (
    adb push "app\src\main\assets\config.json" "/sdcard/Android/data/com.voboost.voiceassistant/files/config.json"
    if errorlevel 1 (
        echo [WARNING] Ошибка копирования config.json
    ) else (
        echo [OK] config.json скопирован
    )
) else (
    echo [WARNING] config.json не найден в assets
)
echo.

REM Копирование Vosk модели
echo [4/6] Копирование Vosk модели...
if exist "models\vosk\vosk-model-small-ru-0.22" (
    echo       Копирование файлов модели (это может занять несколько минут)...
    adb push "models\vosk\vosk-model-small-ru-0.22" "/sdcard/Android/data/com.voboost.voiceassistant/files/models/vosk/vosk-model-small-ru-0.22"
    if errorlevel 1 (
        echo [ERROR] Ошибка копирования Vosk модели!
        pause
        exit /b 1
    )
    echo [OK] Vosk модель скопирована
) else if exist "vosk-model-small-ru-0.22.tar.gz" (
    echo       Копирование архива Vosk...
    adb push "vosk-model-small-ru-0.22.tar.gz" "/sdcard/Android/data/com.voboost.voiceassistant/files/models/vosk/"
    echo       Распаковка...
    adb shell "cd /sdcard/Android/data/com.voboost.voiceassistant/files/models/vosk && tar -xzf vosk-model-small-ru-0.22.tar.gz && rm vosk-model-small-ru-0.22.tar.gz"
    echo [OK] Vosk модель распакована
) else (
    echo [WARNING] Vosk модель не найдена!
    echo          Проверьте путь: models\vosk\vosk-model-small-ru-0.22
)
echo.

REM Копирование Sherpa TTS модели
echo [5/6] Копирование Sherpa TTS модели...
if exist "models\sherpa\tts-ru-model" (
    echo       Копирование файлов модели (это может занять несколько минут)...
    adb push "models\sherpa\tts-ru-model" "/sdcard/Android/data/com.voboost.voiceassistant/files/models/sherpa/tts-ru-model"
    if errorlevel 1 (
        echo [ERROR] Ошибка копирования Sherpa модели!
        pause
        exit /b 1
    )
    echo [OK] Sherpa TTS модель скопирована
) else if exist "models\sherpa\tts-ru-model.tar.gz.bin" (
    echo       Копирование архива Sherpa...
    adb push "models\sherpa\tts-ru-model.tar.gz.bin" "/sdcard/Android/data/com.voboost.voiceassistant/files/models/sherpa/"
    echo       Переименование и распаковка...
    adb shell "cd /sdcard/Android/data/com.voboost.voiceassistant/files/models/sherpa && mv tts-ru-model.tar.gz.bin tts-ru-model.tar.gz && tar -xzf tts-ru-model.tar.gz && rm tts-ru-model.tar.gz"
    echo [OK] Sherpa TTS модель распакована
) else (
    echo [WARNING] Sherpa TTS модель не найдена!
    echo          Проверьте путь: models\sherpa\tts-ru-model
)
echo.

REM Проверка
echo [6/6] Проверка установленных файлов...
echo.
echo Config:
adb shell "ls -la /sdcard/Android/data/com.voboost.voiceassistant/files/config.json" 2>nul || echo "  config.json не найден"
echo.
echo Vosk модель:
adb shell "ls -la /sdcard/Android/data/com.voboost.voiceassistant/files/models/vosk/" 2>nul || echo "  Vosk модель не найдена"
echo.
echo Sherpa TTS модель:
adb shell "ls -la /sdcard/Android/data/com.voboost.voiceassistant/files/models/sherpa/" 2>nul || echo "  Sherpa модель не найдена"
echo.

echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Файлы скопированы на SD-карту в директорию:
echo   /sdcard/Android/data/com.voboost.voiceassistant/files/
echo.
echo Для применения изменений перезапустите приложение или устройство:
echo   adb reboot
echo.
echo Чтобы обновить конфиг в будущем:
echo   1. Отредактируйте app/src/main/assets/config.json
echo   2. Запустите этот скрипт снова
echo   ИЛИ скопируйте конфиг вручную:
echo   adb push config.json /sdcard/Android/data/com.voboost.voiceassistant/files/
echo.
pause
