@echo off
chcp 65001 >nul
REM ============================================================================
REM  Копирование моделей Sherpa-ONNX во внешнее хранилище
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

REM Пакет приложения
set "PKG=ru.voboost.voice"

echo.
echo ============================================================================
echo  Copying Sherpa Models to External Storage
echo  /storage/emulated/0/Android/data/%PKG%/files/
echo ============================================================================
echo.

REM Проверка подключения
adb devices >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB не найден!
    pause
    exit /b 1
)

REM Создание директории
echo [1/3] Создание директорий для моделей...
adb shell "mkdir -p /storage/emulated/0/Android/data/%PKG%/files/models/sherpa/asr-ru-model"
adb shell "mkdir -p /storage/emulated/0/Android/data/%PKG%/files/models/sherpa/tts-ru-model"
echo [OK] Директории созданы
echo.

REM Копирование ASR модели
echo [2/3] Копирование ASR модели...
if exist "models\sherpa\asr-ru-model" (
    adb push "models\sherpa\asr-ru-model" "/storage/emulated/0/Android/data/%PKG%/files/models/sherpa/asr-ru-model"
    if errorlevel 1 (
        echo [ERROR] Ошибка копирования ASR модели!
        pause
        exit /b 1
    )
    echo [OK] ASR модель скопирована
) else (
    echo [ERROR] ASR модель не найдена: models\sherpa\asr-ru-model
    pause
    exit /b 1
)
echo.

REM Копирование TTS модели
echo [3/4] Копирование TTS модели...
if exist "models\sherpa\tts-ru-model" (
    adb push "models\sherpa\tts-ru-model" "/storage/emulated/0/Android/data/%PKG%/files/models/sherpa/tts-ru-model"
    if errorlevel 1 (
        echo [ERROR] Ошибка копирования TTS модели!
        pause
        exit /b 1
    )
    echo [OK] TTS модель скопирована
) else (
    echo [WARN] TTS модель не найдена: models\sherpa\tts-ru-model
    echo [INFO] Пропускаем TTS модель
)
echo.

REM Исправление разрешений для eSpeak-ng (критично для работы TTS!)
echo [4/4] Исправление разрешений для eSpeak-ng...
adb shell "chmod -R 755 /storage/emulated/0/Android/data/%PKG%/files/models/sherpa/tts-ru-model/espeak-ng-data"
echo [OK] Разрешения исправлены (755)
echo.

REM Проверка
echo Проверка установленных моделей:
adb shell "ls -la /storage/emulated/0/Android/data/%PKG%/files/models/sherpa/asr-ru-model/"
adb shell "ls -la /storage/emulated/0/Android/data/%PKG%/files/models/sherpa/tts-ru-model/"
echo.
echo Проверка разрешений espeak-ng-data:
adb shell "ls -la /storage/emulated/0/Android/data/%PKG%/files/models/sherpa/tts-ru-model/espeak-ng-data/" | findstr "espeak-ng-data"
echo.

echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Теперь нужно перезапустить приложение:
echo   adb shell am force-stop ru.voboost.voice
echo   adb shell am start-foreground-service -n ru.voboost.voice/.VoboostVoiceService
echo.
pause

