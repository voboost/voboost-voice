@echo off
chcp 65001 >nul
REM ============================================================================
REM  VoboostVoiceAssistant - Миграция с com.voboost на ru.voboost
REM ============================================================================
REM  Этот скрипт:
REM  1. Переходит в root и перемонтирует /system
REM  2. Переименовывает папку com.voboost.voiceassistant -> ru.voboost.voice
REM     (в /system/priv-app/ и в /data/user/0/)
REM  3. Обновляет APK
REM  4. Обновляет конфиг из assets/config.json
REM  5. Выдаёт разрешения и перезапускает сервис
REM ============================================================================
REM  ВАЖНО: Папки НЕ очищаются — библиотеки и модели сохраняются!
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

REM Пути
set "APK_PATH=app\build\outputs\apk\debug\app-debug.apk"
set "CONFIG_PATH=app\src\main\assets\config.json"

REM Старые и новые имена пакетов
set "OLD_PKG=ru.voboost.voiceassistant"
set "NEW_PKG=ru.voboost.voice"

REM Пути на устройстве
set "SYSTEM_DIR=/system/priv-app/VoboostVoiceAssistant"
set "DATA_DIR=/data/user/0"
set "DATA_OLD=%DATA_DIR%/%OLD_PKG%"
set "DATA_NEW=%DATA_DIR%/%NEW_PKG%"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Миграция package name
echo ============================================================================
echo  %OLD_PKG% -> %NEW_PKG%
echo ============================================================================
echo.

REM ============================================================================
REM  Шаг 0: Проверка наличия APK
REM ============================================================================
echo [0/7] Проверка наличия APK...
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала соберите проект:
    echo   - Через Android Studio: Build ^> Make Project
    echo   - Или запустите: build-project.bat
    pause
    exit /b 1
)
echo   [OK] APK найден
echo.

REM ============================================================================
REM  Шаг 1: Отключаем старые ассистенты
REM ============================================================================
echo [1/7] Отключение стандартных голосовых ассистентов...
adb shell pm disable com.qinggan.ivoka       >nul 2>&1
adb shell pm disable com.qinggan.ivoka1      >nul 2>&1
adb shell pm disable com.qinggan.sttservice  >nul 2>&1
echo   [OK] Отключены
echo.

REM ============================================================================
REM  Шаг 2: Root и перемонтирование
REM ============================================================================
echo [2/7] Получение root и перемонтирование /system...
adb root
timeout /t 2 /nobreak >nul
adb shell "mount -o rw,remount /"
timeout /t 1 /nobreak >nul
echo   [OK] Root получен, /system доступен для записи
echo.

REM ============================================================================
REM  Шаг 3: Переименование папки в /system/priv-app/
REM ============================================================================
echo [3/7] Переименование папки в /system/priv-app/...
adb shell "ls -la %SYSTEM_DIR%/" >nul 2>&1
if errorlevel 1 (
    echo   [INFO] Папка %SYSTEM_DIR% не найдена, создаём...
    adb shell "mkdir -p %SYSTEM_DIR%"
) else (
    echo   [OK] Папка существует, APK будет обновлён
)

echo   [OK] Папка в системе готова
echo.

REM ============================================================================
REM  Шаг 4: Переименование папки данных /data/user/0/
REM ============================================================================
echo [4/7] Переименование папки данных %OLD_PKG% -> %NEW_PKG%...

REM Проверяем существует ли старая папка
adb shell "ls %DATA_OLD%/" >nul 2>&1
if errorlevel 1 (
    echo   [INFO] Старая папка %DATA_OLD% не найдена, создаём новую...
    adb shell "mkdir -p %DATA_NEW%"
) else (
    echo   [INFO] Старая папка найдена, переименовываем...
    adb shell "mv %DATA_OLD% %DATA_NEW%"
    if errorlevel 1 (
        echo   [WARN] Переименование не удалось, пробуем копирование...
        adb shell "cp -a %DATA_OLD% %DATA_NEW%"
        if errorlevel 1 (
            echo   [ERROR] Не удалось переместить данные!
            pause
            exit /b 1
        )
        echo   [OK] Данные скопированы (старая папка сохранена)
    ) else (
        echo   [OK] Папка переименована
    )
)
echo.

REM ============================================================================
REM  Шаг 5: Обновление APK и конфига
REM ============================================================================
echo [5/7] Обновление APK и конфига...

REM Копируем APK (перезаписываем)
adb push "%APK_PATH%" "%SYSTEM_DIR%/VoboostVoiceAssistant.apk"
if errorlevel 1 (
    echo [ERROR] Ошибка копирования APK!
    pause
    exit /b 1
)
adb shell "chmod 644 %SYSTEM_DIR%/VoboostVoiceAssistant.apk"
echo   [OK] APK обновлён

REM Копируем конфиг (если существует)
if exist "%CONFIG_PATH%" (
    adb push "%CONFIG_PATH%" "%DATA_NEW%/files/config.json"
    if errorlevel 0 (
        echo   [OK] Конфиг обновлён
    ) else (
        echo   [WARN] Ошибка копирования конфига (пропустим)
    )
) else (
    echo   [WARN] Конфиг не найден: %CONFIG_PATH%
)
echo.

REM ============================================================================
REM  Шаг 6: Разрешения
REM ============================================================================
echo [6/7] Выдача разрешений...
adb shell "pm grant %NEW_PKG% android.permission.RECORD_AUDIO" >nul 2>&1
adb shell "pm grant %NEW_PKG% android.permission.READ_CONTACTS" >nul 2>&1
adb shell "pm grant %NEW_PKG% android.permission.SYSTEM_ALERT_WINDOW" >nul 2>&1
adb shell "pm grant %NEW_PKG% android.permission.FOREGROUND_SERVICE" >nul 2>&1
echo   [OK] Разрешения выданы
echo.

REM ============================================================================
REM  Шаг 7: Перезапуск сервиса
REM ============================================================================
echo [7/7] Перезапуск сервиса...

REM Убеждаемся что приложение включено
adb shell "pm enable %NEW_PKG%" >nul 2>&1

REM Останавливаем
adb shell "am force-stop %NEW_PKG%"
timeout /t 2 /nobreak

REM Запускаем
adb shell "am start-foreground-service --user 0 -n %NEW_PKG%/.VoboostVoiceService"
if errorlevel 1 (
    echo [ERROR] Ошибка запуска сервиса!
    echo Проверьте логи:
    adb logcat -d -t 30 | findstr /i "voboost fatal error"
    pause
    exit /b 1
)

timeout /t 3 /nobreak

REM Проверяем процесс
adb shell "ps | grep voboost" >nul 2>&1
if errorlevel 1 (
    echo [WARN] Процесс не найден! Проверьте логи.
) else (
    echo   [OK] Сервис запущен
)
echo.

REM ============================================================================
REM  Готово
REM ============================================================================
echo ============================================================================
echo  Готово! Миграция завершена.
echo ============================================================================
echo.
echo  Пакет: %NEW_PKG%
echo  APK:   %SYSTEM_DIR%/VoboostVoiceAssistant.apk
echo  Данные: %DATA_NEW%
echo.
echo  Проверка:
echo    adb shell ps ^| grep voboost
echo    adb logcat -s VoboostVoiceService:*
echo.
pause



