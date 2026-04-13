@echo off
REM ============================================================================
REM  Копирование модели Vosk во внутреннюю память приложения
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

echo.
echo ============================================================================
echo  Copying Vosk Model to Internal Storage
echo ============================================================================
echo.

REM Проверка подключения
adb devices >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB не найден!
    pause
    exit /b 1
)

REM Получение root
echo [1/4] Получение root прав...
adb root
timeout /t 2 /nobreak >nul
adb remount
echo [OK] Root получен
echo.

REM Создание директории
echo [2/4] Создание директории для модели...
adb shell "mkdir -p /data/user/0/ru.voboost.voiceassistant/files/models/vosk"
echo [OK] Директория создана
echo.

REM Копирование модели
echo [3/4] Копирование модели Vosk (это может занять несколько минут)...
if exist "models\vosk\vosk-model-small-ru-0.22" (
    adb push "models\vosk\vosk-model-small-ru-0.22" "/data/user/0/ru.voboost.voiceassistant/files/models/vosk/vosk-model-small-ru-0.22"
    if errorlevel 1 (
        echo [ERROR] Ошибка копирования!
        pause
        exit /b 1
    )
    echo [OK] Модель скопирована
) else (
    echo [ERROR] Модель не найдена: models\vosk\vosk-model-small-ru-0.22
    pause
    exit /b 1
)
echo.

REM Установка прав (динамический UID из pm list)
echo [4/4] Установка прав доступа...
for /f "tokens=3" %%a in ('adb shell "pm list packages -U ru.voboost.voiceassistant"') do (
    for /f "tokens=2 delims=:" %%b in ("%%a") do set "APP_UID=%%~b"
)
if defined APP_UID (
    adb shell "chown -R %APP_UID%:%APP_UID% /data/user/0/ru.voboost.voiceassistant/files/models/vosk"
    echo [OK] Права установлены (uid=%APP_UID%)
) else (
    echo [WARN] Не удалось определить UID
)
echo.

REM Проверка
echo Проверка установленной модели:
adb shell "ls -la /data/user/0/ru.voboost.voiceassistant/files/vosk/vosk-model-small-ru-0.22/"
echo.

echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Теперь нужно перезапустить приложение:
echo   adb shell am force-stop ru.voboost.voiceassistant
echo   adb shell am start-foreground-service -n ru.voboost.voiceassistant/.VoboostVoiceService
echo.
pause
