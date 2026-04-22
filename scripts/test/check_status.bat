@echo off
chcp 65001 >nul
echo ============================================
echo   VoboostVoiceAssistant - Полная проверка
echo ============================================
echo.

echo [1/5] Проверка подключения устройства...
adb devices | findstr device
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Устройство не подключено!
    pause
    exit /b 1
)
echo   ✓ Устройство подключено
echo.

echo [2/5] Проверка установки приложения...
adb shell pm list packages | findstr voboost
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Приложение не установлено!
    echo Запустите VoboostVoiceAssistant-install.bat
    pause
    exit /b 1
)
echo   ✓ Приложение установлено
echo.

echo [3/5] Проверка разрешений...
adb shell dumpsys package ru.voboost.voiceassistant | findstr "granted=true"
echo   ✓ Разрешения проверены
echo.

echo [4/5] Проверка Accessibility Service...
adb shell settings get secure enabled_accessibility_services | findstr voboost
if %ERRORLEVEL% EQU 0 (
    echo   ✓ Accessibility Service включен
) else (
    echo   ⚠ Accessibility Service выключен
    echo   Запустите grant_permissions.bat
)
echo.

echo [5/5] Проверка автозапуска...
adb shell dumpsys package ru.voboost.voiceassistant | findstr "enabled=true"
echo   ✓ Автозапуск настроен
echo.

echo ============================================
echo   ПРОВЕРКА ЗАВЕРШЕНА!
echo ============================================
echo.
echo Для тестирования голосовых команд:
echo   1. Скажите "Привет, Вобуст"
echo   2. Скажите "Открой лючок зарядки"
echo.
echo Для просмотра логов:
echo   adb logcat ^| grep -i "voboost"
echo.
pause
