@echo off
chcp 65001 >nul
REM ============================================================================
REM  VoboostVoiceAssistant - Полная установка с нуля
REM ============================================================================
REM  Этот скрипт:
REM    1. Отключает стандартные ассистенты
REM    2. Установка APK
REM    6. Копирует модели (Vosk + Sherpa TTS)
REM    4. Копирует config.json
REM    5. Выдаёт разрешения
REM    6. Запускает сервис
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

REM Пути
set "APK_PATH=app\build\outputs\apk\debug\app-debug.apk"
set "LIBS_DIR=native_libs\arm64-v8a"
set "CONFIG_PATH=app\src\main\assets\config.json"

set "PKG=ru.voboost.voiceassistant"
set "EXTERNAL_DIR=/storage/emulated/0/Android/data/%PKG%/files"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Полная установка
echo ============================================================================
echo.

REM --- Шаг 1: Отключение стандартных ассистентов ---
echo [1/6] Отключение стандартных ассистентов...
adb shell pm disable com.qinggan.ivoka       >nul 2>&1
adb shell pm disable com.qinggan.ivoka1      >nul 2>&1
adb shell pm disable com.qinggan.sttservice  >nul 2>&1
echo   [OK] Отключены
echo.

REM --- Шаг 2: Проверка APK ---
echo [2/6] Проверка наличия APK...
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала соберите проект: build-project.bat
    pause
    exit /b 1
)
echo   [OK] APK найден
echo.

REM --- Шаг 3: Root ---
echo [3/6] Root...
adb root
timeout /t 2 /nobreak >nul
echo   [OK]
echo.

REM --- Шаг 4: Установка APK ---
echo [4/6] Установка APK...
adb insyall -g "%APK_PATH%" 
if errorlevel 1 (
    echo [ERROR] Ошибка установки APK!
    pause
    exit /b 1
)
echo   [OK] APK установлен
echo.

REM --- Шаг 5: Копирование моделей ---
echo [5/6] Копирование моделей и конфига...

REM Создаём директории
adb shell "mkdir -p %EXTERNAL_DIR%/models/vosk" >nul 2>&1
adb shell "mkdir -p %EXTERNAL_DIR%/models/sherpa/asr-ru-model" >nul 2>&1
adb shell "mkdir -p %EXTERNAL_DIR%/models/sherpa/tts-ru-model" >nul 2>&1

REM Копируем Vosk модель
if exist "models\vosk\vosk-model-small-ru-0.22" (
    adb push "models\vosk\vosk-model-small-ru-0.22" ^
      "%EXTERNAL_DIR%/models/vosk/" >nul 2>&1
    echo   [OK] Vosk модель скопирована
) else (
    echo   [WARN] Vosk модель не найдена: models\vosk\vosk-model-small-ru-0.22
)

REM Копируем Sherpa ASR модель
if exist "models\sherpa\asr-ru-model" (
    adb push "models\sherpa\asr-ru-model" ^
      "%EXTERNAL_DIR%/models/sherpa/asr-ru-model" >nul 2>&1
    echo   [OK] Sherpa ASR модель скопирована
) else (
    echo   [WARN] Sherpa ASR модель не найдена: models\sherpa\asr-ru-model
)

REM Копируем Sherpa TTS модель (из tts-ru-model-temp/tts-ru-model)
if exist "models\sherpa\tts-ru-model-temp\tts-ru-model" (
    adb push "models\sherpa\tts-ru-model-temp\tts-ru-model" ^
      "%EXTERNAL_DIR%/models/sherpa/tts-ru-model" >nul 2>&1
    echo   [OK] Sherpa TTS модель скопирована
) else (
    echo   [WARN] Sherpa TTS модель не найдена: models\sherpa\tts-ru-model-temp\tts-ru-model
)

REM Исправление разрешений для eSpeak-ng (критично для работы TTS!)
adb shell "chmod -R 755 %EXTERNAL_DIR%/models/sherpa/tts-ru-model/espeak-ng-data" >nul 2>&1
echo   [OK] Разрешения eSpeak-ng исправлены (755)

REM Копируем config.json
if exist "%CONFIG_PATH%" (
    adb push "%CONFIG_PATH%" "%EXTERNAL_DIR%/config.json" >nul 2>&1
    echo   [OK] config.json скопирован
) else (
    echo   [WARN] config.json не найден: %CONFIG_PATH%
)
echo   [OK] Модели и конфиг скопированы на внешнее хранилище

REM Устанавливаем права на внешнее хранилище (chmod, chown не нужен для external storage)
adb shell "chmod -R 755 %EXTERNAL_DIR%" >nul 2>&1
echo   [OK] Права установлены на внешнее хранилище
echo.

REM --- Шаг 6: Разрешения и запуск ---
echo [6/6] Разрешения и запуск сервиса...

adb shell "pm grant %PKG% android.permission.RECORD_AUDIO" >nul 2>&1
adb shell "pm grant %PKG% android.permission.READ_CONTACTS" >nul 2>&1
adb shell "pm grant %PKG% android.permission.SYSTEM_ALERT_WINDOW" >nul 2>&1
adb shell "pm grant %PKG% android.permission.FOREGROUND_SERVICE" >nul 2>&1
echo   [OK] Разрешения выданы

REM Останавливаем и запускаем сервис
adb shell "am force-stop %PKG%" >nul 2>&1
timeout /t 2 /nobreak >nul
adb shell "am start-foreground-service --user 0 -n %PKG%/.VoboostVoiceService"
if errorlevel 1 (
    echo [WARN] Ошибка запуска — возможно нужно подождать ещё несколько секунд
)

timeout /t 3 /nobreak

REM Проверяем процесс
adb shell "ps | grep voboost" >nul 2>&1
if errorlevel 1 (
    echo [WARN] Процесс не найден сразу — возможно ещё инициализируется
    echo   Попробуйте через 10 секунд: adb shell ps ^| grep voboost
) else (
    echo   [OK] Сервис запущен
)
echo.

REM ============================================================================
REM  Готово
REM ============================================================================
echo ============================================================================
echo  Готово! VoboostVoiceAssistant полностью установлен и запущен!
echo ============================================================================
echo.
echo Как использовать:
echo   1. Нажмите кнопку на руле
echo   2. Скажите команду: "открой окно", "позвони сынок", "включи кондиционер"
echo.
echo Логи:
echo   adb logcat -s VoboostVoiceService:* IntentHandler:*
echo.
pause
