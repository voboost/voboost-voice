@echo off
chcp 65001 >nul
REM ============================================================================
REM  Копирование модели Vosk во внешнее хранилище
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

REM Пакет приложения
set "PKG=ru.voboost.voice"

echo.
echo ============================================================================
echo  Copying Vosk Model to External Storage
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
echo [1/2] Создание директории для модели...
adb shell "mkdir -p /storage/emulated/0/Android/data/%PKG%/files/models/vosk"
echo [OK] Директория создана
echo.

REM Копирование модели
echo [2/2] Копирование модели Vosk (это может занять несколько минут)...
if exist "models\vosk\vosk-model-small-ru-0.22" (
    adb push "models\vosk\vosk-model-small-ru-0.22" "/storage/emulated/0/Android/data/%PKG%/files/models/vosk/vosk-model-small-ru-0.22"
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

REM Проверка
echo Проверка установленной модели:
adb shell "ls -la /storage/emulated/0/Android/data/%PKG%/files/models/vosk/vosk-model-small-ru-0.22/"
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

