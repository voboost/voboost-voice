@echo off
chcp 65001 >nul
REM ============================================================================
REM  Копирование privapp-permissions.xml в систему
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

echo.
echo ============================================================================
echo  Install privapp-permissions.xml to /system/etc/permissions/
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

REM Копирование файла разрешений
echo [2/4] Копирование privapp-permissions.xml...
adb push "app\src\main\assets\privapp-permissions-voboost.xml" "/system/etc/permissions/privapp-permissions-voboost.xml"
if errorlevel 1 (
    echo [ERROR] Ошибка копирования!
    pause
    exit /b 1
)
echo [OK] Файл скопирован
echo.

REM Установка прав
echo [3/4] Установка прав доступа...
adb shell "chmod 644 /system/etc/permissions/privapp-permissions-voboost.xml"
adb shell "chown root:root /system/etc/permissions/privapp-permissions-voboost.xml"
echo [OK] Права установлены
echo.

REM Проверка
echo [4/4] Проверка...
adb shell "ls -la /system/etc/permissions/privapp-permissions-voboost.xml"
echo.

echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Теперь нужно ПЕРЕЗАГРУЗИТЬ устройство для применения разрешений:
echo   adb reboot
echo.
echo После перезагрузки проверьте:
echo   adb shell dumpsys package ru.voboost.voice ^| grep WRITE_CANBUS
echo.
pause

