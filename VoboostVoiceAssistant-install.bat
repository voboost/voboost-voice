@echo off
REM ============================================================================
REM  VoboostVoiceAssistant - Полная установка с нуля
REM ============================================================================
REM  Этот скрипт:
REM  ЭТАП 1 (ДО перезагрузки):
REM    1. Отключает стандартные ассистенты
REM    2. Копирует APK в /system/priv-app/
REM    3. Копирует нативные библиотеки
REM    4. Перезагружает устройство
REM
REM  ЭТАП 2 (ПОСЛЕ перезагрузки, автоматически):
REM    5. Копирует модели (Vosk + Sherpa TTS)
REM    6. Копирует config.json
REM    7. Выдаёт разрешения
REM    8. Запускает сервис
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
set "SYSTEM_DIR=/system/priv-app/VoboostVoiceAssistant"
set "DATA_DIR=/data/user/0/%PKG%"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Полная установка с нуля
echo ============================================================================
echo.

REM ============================================================================
REM  ЭТАП 1: ДО ПЕРЕЗАГРУЗКИ
REM ============================================================================
echo ============================================================================
echo  ЭТАП 1: Установка APK и библиотек (до перезагрузки)
echo ============================================================================
echo.

REM --- Шаг 1: Отключение стандартных ассистентов ---
echo [1/8] Отключение стандартных ассистентов...
adb shell pm disable com.qinggan.ivoka       >nul 2>&1
adb shell pm disable com.qinggan.ivoka1      >nul 2>&1
adb shell pm disable com.qinggan.sttservice  >nul 2>&1
echo   [OK] Отключены
echo.

REM --- Шаг 2: Проверка APK ---
echo [2/8] Проверка наличия APK...
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала соберите проект: build-project.bat
    pause
    exit /b 1
)
echo   [OK] APK найден
echo.

REM --- Шаг 3: Root + remount ---
echo [3/8] Root и перемонтирование /system...
adb root
timeout /t 2 /nobreak >nul
adb shell "mount -o rw,remount /"
timeout /t 1 /nobreak >nul
echo   [OK] /system доступен для записи
echo.

REM --- Шаг 4: Установка APK ---
echo [4/8] Установка APK...
adb shell "rm -rf %SYSTEM_DIR%" >nul 2>&1
adb shell "mkdir -p %SYSTEM_DIR%" >nul 2>&1
adb push "%APK_PATH%" "%SYSTEM_DIR%/VoboostVoiceAssistant.apk" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Ошибка копирования APK!
    pause
    exit /b 1
)
adb shell "chmod 644 %SYSTEM_DIR%/VoboostVoiceAssistant.apk" >nul 2>&1
echo   [OK] APK установлен
echo.

REM --- Шаг 5: Нативные библиотеки ---
echo [5/8] Копирование нативных библиотек...
adb shell "mkdir -p %SYSTEM_DIR%/lib/arm64" >nul 2>&1
if exist "%LIBS_DIR%\" (
    for %%f in ("%LIBS_DIR%\*.so") do (
        adb push "%%f" "%SYSTEM_DIR%/lib/arm64/" >nul 2>&1
        echo   [OK] %%~nxf
    )
    echo   [OK] Все библиотеки скопированы
) else (
    echo   [WARN] Папка библиотек не найдена: %LIBS_DIR%
    echo   Библиотеки будут извлечены из APK
)
echo.

REM --- Шаг 6: Перезагрузка ---
echo [6/8] Перезагрузка устройства...
adb shell reboot
echo   Ожидание перезагрузки...
echo.

REM ============================================================================
REM  ЭТАП 2: ПОСЛЕ ПЕРЕЗАГРУЗКИ
REM ============================================================================

REM Ждём пока устройство отключится
adb wait-for-device >nul 2>&1

echo   Ожидание загрузки системы (90 секунд)...
timeout /t 90 /nobreak

REM Ждём пока ADB снова увидит устройство
echo   Ожидание подключения ADB...
adb wait-for-device
timeout /t 10 /nobreak
echo   [OK] Устройство загружено
echo.

echo ============================================================================
echo  ЭТАП 2: Модели, конфиг, разрешения, запуск
echo ============================================================================
echo.

REM --- Шаг 7: Копирование моделей ---
echo [7/8] Копирование моделей и конфига...

REM Создаём директории
adb shell "mkdir -p %DATA_DIR%/files/models/vosk" >nul 2>&1
adb shell "mkdir -p %DATA_DIR%/files/models/sherpa" >nul 2>&1

REM Копируем Vosk модель
if exist "models\vosk\vosk-model-small-ru-0.22" (
    adb push "models\vosk\vosk-model-small-ru-0.22" ^
      "%DATA_DIR%/files/models/vosk/" >nul 2>&1
    echo   [OK] Vosk модель скопирована
) else (
    echo   [WARN] Vosk модель не найдена: models\vosk\vosk-model-small-ru-0.22
)

REM Копируем Sherpa ASR модель
if exist "models\sherpa\asr-ru-model" (
    adb push "models\sherpa\asr-ru-model" ^
      "%DATA_DIR%/files/models/sherpa/" >nul 2>&1
    echo   [OK] Sherpa ASR модель скопирована
) else (
    echo   [WARN] Sherpa ASR модель не найдена: models\sherpa\asr-ru-model
)

REM Копируем Sherpa TTS модель (из tts-ru-model-temp/tts-ru-model)
if exist "models\sherpa\tts-ru-model-temp\tts-ru-model" (
    adb push "models\sherpa\tts-ru-model-temp\tts-ru-model" ^
      "%DATA_DIR%/files/models/sherpa/tts-ru-model" >nul 2>&1
    echo   [OK] Sherpa TTS модель скопирована
) else (
    echo   [WARN] Sherpa TTS модель не найдена: models\sherpa\tts-ru-model-temp\tts-ru-model
)

REM Копируем config.json
if exist "%CONFIG_PATH%" (
    adb push "%CONFIG_PATH%" "%DATA_DIR%/files/config.json" >nul 2>&1
    echo   [OK] config.json скопирован
) else (
    echo   [WARN] config.json не найден: %CONFIG_PATH%
)

REM Устанавливаем права на файлы данных (динамический UID из pm list)
for /f "tokens=3" %%a in ('adb shell "pm list packages -U %PKG%"') do (
    for /f "tokens=2 delims=:" %%b in ("%%a") do set "APP_UID=%%~b"
)
if defined APP_UID (
    adb shell "chown -R %APP_UID%:%APP_UID% %DATA_DIR%/files" >nul 2>&1
    adb shell "chmod -R 755 %DATA_DIR%/files" >nul 2>&1
    adb shell "chown -R %APP_UID%:%APP_UID% %DATA_DIR%/cache" >nul 2>&1
    adb shell "chmod 775 %DATA_DIR%/cache" >nul 2>&1
    adb shell "chown -R %APP_UID%:%APP_UID% %DATA_DIR%/code_cache" >nul 2>&1
    adb shell "chmod 775 %DATA_DIR%/code_cache" >nul 2>&1
    echo   [OK] Права установлены (uid=%APP_UID%)
) else (
    echo   [WARN] Не удалось определить UID — пропускаем chown
)
echo.

REM --- Шаг 8: Разрешения и запуск ---
echo [8/8] Разрешения и запуск сервиса...

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
