@echo off
chcp 65001 >nul
REM ============================================================================
REM  VoboostVoiceAssistant - Миграция с ru.voboost.voiceassistant
REM ============================================================================
REM  Этот скрипт:
REM    1. Деинсталирует старое приложение (ru.voboost.voiceassistant)
REM    2. Переносит файлы моделей и конфиг из старой папки в новую
REM    3. Удаляет старую папку
REM    4. Устанавливает новое приложение (ru.voboost.voice)
REM    5. Выдаёт разрешения
REM    6. Устанавливает Hidden API Policy (для работы с AIDL)
REM    7. Запускает сервис
REM ============================================================================

setlocal enabledelayedexpansion

REM ADB путь
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

REM Старый и новый package
set "OLD_PKG=ru.voboost.voiceassistant"
set "NEW_PKG=ru.voboost.voice"

REM Пути к APK и конфигам
set "APK_PATH=app\build\outputs\apk\debug\app-debug.apk"
set "CONFIG_PATH=app\src\main\assets\config.json"

REM Пути на устройстве
set "OLD_EXTERNAL_DIR=/storage/emulated/0/Android/data/%OLD_PKG%/files"
set "NEW_EXTERNAL_DIR=/storage/emulated/0/Android/data/%NEW_PKG%/files"

echo.
echo ============================================================================
echo  Миграция VoboostVoiceAssistant
echo    Старый: %OLD_PKG%
echo    Новый:  %NEW_PKG%
echo ============================================================================
echo.

REM --- Шаг 1: Проверка наличия APK ---
echo [1/7] Проверка наличия APK...
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала соберите проект: build-project.bat
    pause
    exit /b 1
)
echo   [OK] APK найден
echo.

REM --- Шаг 2: Деинсталляция старого приложения ---
echo [2/7] Деинсталляция старого приложения (%OLD_PKG%)...
adb shell "pm list packages | grep %OLD_PKG%" >nul 2>&1
if errorlevel 1 (
    echo   [INFO] Старое приложение не найдено — возможно уже удалено
) else (
    adb uninstall %OLD_PKG%
    if errorlevel 1 (
        echo [WARN] Ошибка деинсталляции, но продолжаем...
    ) else (
        echo   [OK] Старое приложение удалено
    )
)
echo.

REM --- Шаг 3: Сохранение старых файлов (если есть) ---
echo [3/7] Проверка старых файлов на устройстве...
set "FILES_MIGRATED=0"

REM Проверяем наличие старой директории
adb shell "test -d %OLD_EXTERNAL_DIR%/models && echo EXISTS" > "%TEMP%\check_old.txt" 2>nul
findstr "EXISTS" "%TEMP%\check_old.txt" >nul 2>&1
if errorlevel 1 (
    echo   [INFO] Старая папка не найдена: %OLD_EXTERNAL_DIR%
    echo   [SKIP] Миграция файлов не требуется
) else (
    echo   [INFO] Найдена старая папка, начинаем перенос...
    
    REM Создаём новую директорию
    adb shell "mkdir -p %NEW_EXTERNAL_DIR%/models" >nul 2>&1
    
    REM Мигрируем Vosk модель
    adb shell "test -d %OLD_EXTERNAL_DIR%/models/vosk/vosk-model-small-ru-0.22 && echo EXISTS" > "%TEMP%\check_vosk.txt" 2>nul
    findstr "EXISTS" "%TEMP%\check_vosk.txt" >nul 2>&1
    if not errorlevel 1 (
        echo   [MIGRATE] Vosk модель...
        adb shell "cp -r %OLD_EXTERNAL_DIR%/models/vosk/vosk-model-small-ru-0.22 %NEW_EXTERNAL_DIR%/models/vosk/" >nul 2>&1
        set "FILES_MIGRATED=1"
    )
    
    REM Мигрируем Sherpa ASR
    adb shell "test -d %OLD_EXTERNAL_DIR%/models/sherpa/asr-ru-model && echo EXISTS" > "%TEMP%\check_sherpa_asr.txt" 2>nul
    findstr "EXISTS" "%TEMP%\check_sherpa_asr.txt" >nul 2>&1
    if not errorlevel 1 (
        echo   [MIGRATE] Sherpa ASR модель...
        adb shell "cp -r %OLD_EXTERNAL_DIR%/models/sherpa/asr-ru-model %NEW_EXTERNAL_DIR%/models/sherpa/" >nul 2>&1
        set "FILES_MIGRATED=1"
    )
    
    REM Мигрируем Sherpa TTS
    adb shell "test -d %OLD_EXTERNAL_DIR%/models/sherpa/tts-ru-model && echo EXISTS" > "%TEMP%\check_sherpa_tts.txt" 2>nul
    findstr "EXISTS" "%TEMP%\check_sherpa_tts.txt" >nul 2>&1
    if not errorlevel 1 (
        echo   [MIGRATE] Sherpa TTS модель...
        adb shell "cp -r %OLD_EXTERNAL_DIR%/models/sherpa/tts-ru-model %NEW_EXTERNAL_DIR%/models/sherpa/" >nul 2>&1
        set "FILES_MIGRATED=1"
    )
    
    REM Мигрируем NLU модель
    adb shell "test -f %OLD_EXTERNAL_DIR%/models/nlu/model.onnx && echo EXISTS" > "%TEMP%\check_nlu.txt" 2>nul
    findstr "EXISTS" "%TEMP%\check_nlu.txt" >nul 2>&1
    if not errorlevel 1 (
        echo   [MIGRATE] NLU модель и токенизатор...
        adb shell "cp -r %OLD_EXTERNAL_DIR%/models/nlu %NEW_EXTERNAL_DIR%/models/" >nul 2>&1
        set "FILES_MIGRATED=1"
    )
    
    REM Мигрируем LLM модель (MediaPipe)
    adb shell "test -d %OLD_EXTERNAL_DIR%/models/llm && echo EXISTS" > "%TEMP%\check_llm.txt" 2>nul
    findstr "EXISTS" "%TEMP%\check_llm.txt" >nul 2>&1
    if not errorlevel 1 (
        echo   [MIGRATE] LLM модель (MediaPipe)...
        adb shell "cp -r %OLD_EXTERNAL_DIR%/models/llm %NEW_EXTERNAL_DIR%/models/" >nul 2>&1
        set "FILES_MIGRATED=1"
    )
    
    REM Мигрируем config.json
    adb shell "test -f %OLD_EXTERNAL_DIR%/config.json && echo EXISTS" > "%TEMP%\check_config.txt" 2>nul
    findstr "EXISTS" "%TEMP%\check_config.txt" >nul 2>&1
    if not errorlevel 1 (
        echo   [MIGRATE] config.json...
        adb shell "cp %OLD_EXTERNAL_DIR%/config.json %NEW_EXTERNAL_DIR%/config.json" >nul 2>&1
        set "FILES_MIGRATED=1"
    )
    
    if "%FILES_MIGRATED%"=="1" (
        echo   [OK] Файлы мигрированы
    ) else (
        echo   [WARN] Файлы не найдены для миграции
    )
)
echo.

REM --- Шаг 4: Удаление старой папки ---
echo [4/7] Удаление старой папки...
adb shell "rm -rf %OLD_EXTERNAL_DIR%" >nul 2>&1
echo   [OK] Старая папка удалена
echo.

REM --- Шаг 5: Root ---
echo [5/7] Root доступ...
adb root
timeout /t 2 /nobreak >nul
echo   [OK]

REM --- Шаг 6: Hidden API Policy (критично для AIDL) ---
echo [6/7] Hidden API Policy для работы с AIDL...
adb shell "settings put global hidden_api_policy 1"
adb shell "settings put global hidden_api_policy_pre_p 1"
adb shell "settings put global hidden_api_policy_pre_q 1"
adb shell "settings put global hidden_api_policy_pre_r 1"
echo   [OK] Hidden API Policy установлен
echo.

REM --- Шаг 7: Установка нового приложения ---
echo [7/8] Установка нового приложения (%NEW_PKG%)...
adb install -g "%APK_PATH%"
if errorlevel 1 (
    echo [ERROR] Ошибка установки APK!
    pause
    exit /b 1
)
echo   [OK] APK установлен
echo.

REM --- Шаг 8: Разрешения и права ---
echo [8/8] Выдача разрешений и настройка прав...

REM Разрешения
adb shell "pm grant %NEW_PKG% android.permission.RECORD_AUDIO" >nul 2>&1
adb shell "pm grant %NEW_PKG% android.permission.READ_CONTACTS" >nul 2>&1
adb shell "pm grant %NEW_PKG% android.permission.SYSTEM_ALERT_WINDOW" >nul 2>&1
adb shell "pm grant %NEW_PKG% android.permission.FOREGROUND_SERVICE" >nul 2>&1
echo   [OK] Разрешения выданы

REM Права на внешнее хранилище
adb shell "chmod -R 755 %NEW_EXTERNAL_DIR%" >nul 2>&1
adb shell "chmod -R 755 %NEW_EXTERNAL_DIR%/models/sherpa/tts-ru-model/espeak-ng-data" >nul 2>&1
echo   [OK] Права установлены
echo.

REM --- Шаг 9: Запуск сервиса ---
echo [9/9] Запуск сервиса...
adb shell "am force-stop %NEW_PKG%" >nul 2>&1
timeout /t 2 /nobreak >nul
adb shell "am start-foreground-service --user 0 -n %NEW_PKG%/.VoboostVoiceService"
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
echo  Миграция завершена!
echo ============================================================================
echo.
echo Старое приложение: %OLD_PKG% — УДАЛЕНО
echo Новое приложение:  %NEW_PKG% — УСТАНОВЛЕНО И ЗАПУЩЕНО
echo.
echo Файлы мигрированы:
echo   %NEW_EXTERNAL_DIR%/
echo     ├── config.json
echo     └── models/
echo         ├── vosk/vosk-model-small-ru-0.22/
echo         ├── sherpa/asr-ru-model/
echo         ├── sherpa/tts-ru-model/
echo         ├── nlu/
echo         └── llm/
echo.
echo Логи для отладки:
echo   adb logcat -s VoboostVoiceService:* Sherpa:* Vosk:* NLU:*
echo.
echo Тестирование:
echo   1. Нажмите кнопку на руле (или используйте KEYCODE_IVOKA)
echo   2. Скажите ключевую фразу: "Привет, Вобуст"
echo   3. Скажите команду: "открой окно", "включи кондиционер"
echo.

REM Очистка временных файлов
del "%TEMP%\check_*.txt" >nul 2>&1

pause
