@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================================
REM  VoboostVoiceAssistant - Обновление config.json + перезапуск сервиса
REM ============================================================================

set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PKG=ru.voboost.voiceassistant"
set "OUT_DIR=/storage/emulated/0/Android/data/%PKG%/files"
set "CONFIG_SRC=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\app\src\main\assets\config.json"
set "PATH=%ADB_PATH%;%PATH%"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Обновление config.json + перезапуск
echo ============================================================================
echo.

REM 1. Проверка подключения
echo [1/3] Проверка подключения...
adb shell "echo 1" >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Устройство не отвечает. Проверьте кабель/отладку.
    pause & exit /b 1
)
echo [OK] Устройство подключено
echo.

REM 2. Root (обязательно для Android 11+ / Android/data)
echo [2/3] Получение root-прав...
adb root >nul 2>&1
timeout /t 2 /nobreak >nul
echo [OK]
echo.

REM 3. Копирование и перезапуск
echo [3/3] Копирование config.json...
if not exist "%CONFIG_SRC%" (
    echo [ERROR] Файл не найден: "%CONFIG_SRC%"
    pause & exit /b 1
)

adb push "%CONFIG_SRC%" "%OUT_DIR%/config.json" >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Ошибка копирования! На Android 11+ требуется рабочий adb root.
    pause & exit /b 1
)
echo [OK] config.json обновлён

echo Перезапуск сервиса...
adb shell "am force-stop %PKG%" >nul 2>&1
timeout /t 2 /nobreak >nul
adb shell "am start-foreground-service --user 0 -n %PKG%/.VoboostVoiceService" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] Сервис запущен
) else (
    echo [WARN] Автозапуск не сработал. Запустите приложение вручную.
)
echo.

echo ============================================================================
echo  Готово! Конфиг обновлен, сервис перезапущен.
echo ============================================================================
pause