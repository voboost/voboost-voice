@echo off
REM ========================================
REM Быстрая установка VoboostVoiceAssistant
REM ========================================

set "ADB=d:\Projects\Android\MM\6.11.1\export\adb\adb.exe"
REM Обновление APK БЕЗ удаления библиотек
REM ========================================
REM ВНИМАНИЕ: Этот скрипт НЕ собирает проект!
REM Используйте build-project.bat или Android Studio для сборки
REM ========================================

setlocal enabledelayedexpansion

set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
set DEVICE_APK_PATH=/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
set DEVICE_LIB_PATH=/system/priv-app/VoboostVoiceAssistant/lib/arm64

echo ========================================
echo  VoboostVoiceAssistant - Быстрая установка
echo ========================================
echo.

REM Проверка наличия APK
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала выполните сборку:
    echo   - build-project.bat
    echo   - Или через Android Studio: Build ^> Make Project
    pause
    exit /b 1
)

echo [1/5] Отключение стандартных голосовых ассистентов...
%ADB% shell pm disable com.qinggan.ivoka       >nul 2>&1
%ADB% shell pm disable com.qinggan.ivoka1      >nul 2>&1
%ADB% shell pm disable com.qinggan.sttservice  >nul 2>&1
echo   [OK] Стандартные ассистенты отключены
echo.

echo [2/5] Получение root-прав...
%ADB% root
timeout /t 2 /nobreak >nul

echo [3/5] Перемонтирование /system в режим записи...
%ADB% shell "mount -o rw,remount /"
timeout /t 2 /nobreak >nul

echo [4/5] Копирование APK в систему...
%ADB% push %APK_PATH% %DEVICE_APK_PATH%
if errorlevel 1 (
    echo [ERROR] Не удалось скопировать APK!
    pause
    exit /b 1
)

echo [5/5] Установка прав доступа и запуск...
%ADB% shell chmod 644 %DEVICE_APK_PATH%

REM Разрешения
%ADB% shell pm grant ru.voboost.voiceassistant android.permission.RECORD_AUDIO >nul 2>&1
%ADB% shell pm grant ru.voboost.voiceassistant android.permission.READ_CONTACTS >nul 2>&1
%ADB% shell pm grant ru.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW >nul 2>&1
%ADB% shell pm grant ru.voboost.voiceassistant android.permission.FOREGROUND_SERVICE >nul 2>&1

REM Перезапуск приложения
%ADB% shell am force-stop ru.voboost.voiceassistant
timeout /t 2 /nobreak >nul
%ADB% shell am start-foreground-service -n ru.voboost.voiceassistant/.VoboostVoiceService

echo.
echo ========================================
echo  ✅ Готово! Приложение обновлено и запущено.
echo ========================================
echo.
echo Для проверки:
echo   adb shell ps -ef ^| findstr voboost
echo   adb logcat -s VoboostVoiceService:*
echo.
echo Для полной установки с моделями:
echo   VoboostVoiceAssistant-install.bat
echo.
pause
