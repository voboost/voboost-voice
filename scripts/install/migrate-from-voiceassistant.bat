@echo off
chcp 65001 >nul
setlocal

REM ============================================================================
REM  VoboostVoiceAssistant - Миграция с ru.voboost.voiceassistant
REM ============================================================================

set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

set "OLD_PKG=ru.voboost.voiceassistant"
set "NEW_PKG=ru.voboost.voice"

set "APK_PATH=d:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\app\build\outputs\apk\debug\app-debug.apk"
set "OLD_EXTERNAL_DIR=/storage/emulated/0/Android/data/%OLD_PKG%/files"
set "NEW_EXTERNAL_DIR=/storage/emulated/0/Android/data/%NEW_PKG%/files"

echo.
echo ============================================================================
echo  Миграция VoboostVoiceAssistant
echo    Старый: %OLD_PKG%
echo    Новый:  %NEW_PKG%
echo ============================================================================
echo.

REM --- 1. Проверка APK ---
echo [1/8] Проверка наличия APK...
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала соберите проект: build-project.bat
    pause & exit /b 1
)
echo   [OK] APK найден
echo.

REM --- 2. Деинсталляция старого ---
echo [2/8] Деинсталляция старого приложения...
adb shell pm list packages | findstr /c:"%OLD_PKG%" >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    adb uninstall %OLD_PKG%
    if %ERRORLEVEL% EQU 0 (echo   [OK] Удалено) else (echo   [WARN] Ошибка удаления, продолжаем)
) else (
    echo   [INFO] Старое приложение не найдено
)
echo.

REM --- 3. Миграция файлов ---
echo [3/8] Проверка и миграция файлов...
set "FILES_MIGRATED=0"

adb shell "test -d '%OLD_EXTERNAL_DIR%/models'"
if %ERRORLEVEL% NEQ 0 (
    echo   [INFO] Старая папка не найдена. Миграция пропущена.
    goto :step4
)

echo   [INFO] Найдена старая папка. Начинаем перенос...
adb shell "mkdir -p '%NEW_EXTERNAL_DIR%/models'"

REM Vosk
adb shell "test -d '%OLD_EXTERNAL_DIR%/models/vosk/vosk-model-small-ru-0.22'"
if %ERRORLEVEL% EQU 0 (
    echo   [MIGRATE] Vosk модель...
    adb shell "cp -r '%OLD_EXTERNAL_DIR%/models/vosk/vosk-model-small-ru-0.22' '%NEW_EXTERNAL_DIR%/models/vosk/'"
    set "FILES_MIGRATED=1"
)

REM Sherpa ASR
adb shell "test -d '%OLD_EXTERNAL_DIR%/models/sherpa/asr-ru-model'"
if %ERRORLEVEL% EQU 0 (
    echo   [MIGRATE] Sherpa ASR модель...
    adb shell "cp -r '%OLD_EXTERNAL_DIR%/models/sherpa/asr-ru-model' '%NEW_EXTERNAL_DIR%/models/sherpa/'"
    set "FILES_MIGRATED=1"
)

REM Sherpa TTS
adb shell "test -d '%OLD_EXTERNAL_DIR%/models/sherpa/tts-ru-model'"
if %ERRORLEVEL% EQU 0 (
    echo   [MIGRATE] Sherpa TTS модель...
    adb shell "cp -r '%OLD_EXTERNAL_DIR%/models/sherpa/tts-ru-model' '%NEW_EXTERNAL_DIR%/models/sherpa/'"
    set "FILES_MIGRATED=1"
)

REM NLU
adb shell "test -f '%OLD_EXTERNAL_DIR%/models/nlu/model.onnx'"
if %ERRORLEVEL% EQU 0 (
    echo   [MIGRATE] NLU модель...
    adb shell "cp -r '%OLD_EXTERNAL_DIR%/models/nlu' '%NEW_EXTERNAL_DIR%/models/'"
    set "FILES_MIGRATED=1"
)

REM LLM
adb shell "test -d '%OLD_EXTERNAL_DIR%/models/llm'"
if %ERRORLEVEL% EQU 0 (
    echo   [MIGRATE] LLM модель...
    adb shell "cp -r '%OLD_EXTERNAL_DIR%/models/llm' '%NEW_EXTERNAL_DIR%/models/'"
    set "FILES_MIGRATED=1"
)

REM config.json
adb shell "test -f '%OLD_EXTERNAL_DIR%/config.json'"
if %ERRORLEVEL% EQU 0 (
    echo   [MIGRATE] config.json...
    adb shell "cp '%OLD_EXTERNAL_DIR%/config.json' '%NEW_EXTERNAL_DIR%/config.json'"
    set "FILES_MIGRATED=1"
)

if "%FILES_MIGRATED%"=="1" (echo   [OK] Файлы успешно мигрированы) else (echo   [WARN] Файлы для миграции не найдены)

:step4
echo.

REM --- 4. Удаление старой папки ---
echo [4/8] Удаление старой папки...
adb shell "rm -rf '%OLD_EXTERNAL_DIR%'"
echo   [OK] Старая папка удалена
echo.

REM --- 5. Root ---
echo [5/8] Получение Root доступа...
adb root
timeout /t 2 /nobreak >nul
echo   [OK]
echo.

REM --- 6. Hidden API Policy ---
echo [6/8] Настройка Hidden API Policy...
adb shell settings put global hidden_api_policy 1
adb shell settings put global hidden_api_policy_pre_p 1
adb shell settings put global hidden_api_policy_pre_q 1
adb shell settings put global hidden_api_policy_pre_r 1
echo   [OK]
echo.

REM --- 7. Установка нового APK ---
echo [7/8] Установка нового приложения...
adb install -g "%APK_PATH%"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Ошибка установки!
    pause & exit /b 1
)
echo   [OK] APK установлен
echo.

REM --- 8. Разрешения и права ---
echo [8/8] Выдача разрешений...
adb shell pm grant %NEW_PKG% android.permission.RECORD_AUDIO >nul 2>nul
adb shell pm grant %NEW_PKG% android.permission.READ_CONTACTS >nul 2>nul
adb shell pm grant %NEW_PKG% android.permission.SYSTEM_ALERT_WINDOW >nul 2>nul
adb shell pm grant %NEW_PKG% android.permission.FOREGROUND_SERVICE >nul 2>nul
echo   [OK] Разрешения выданы

adb shell chmod -R 755 '%NEW_EXTERNAL_DIR%/models/' >nul 2>nul
echo   [OK] Rights installed for all models
echo.

REM --- 9. Запуск сервиса ---
echo [9/9] Запуск сервиса...
adb shell am force-stop %NEW_PKG% >nul 2>nul
timeout /t 2 /nobreak >nul
adb shell am start-foreground-service --user 0 -n %NEW_PKG%/.VoboostVoiceService
if %ERRORLEVEL% NEQ 0 (echo   [WARN] Ошибка запуска, возможно требуется задержка)

timeout /t 3 /nobreak >nul
adb shell ps -A | findstr /i "voboost" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo   [WARN] Процесс не найден сразу. Подождите 10 сек и проверьте: adb shell ps -A ^| findstr voboost
) else (
    echo   [OK] Сервис запущен
)
echo.

echo ============================================================================
echo  Миграция завершена!
echo ============================================================================
echo.
echo Логи: adb logcat -s VoboostVoiceService:* Sherpa:* Vosk:* NLU:*
echo.
pause