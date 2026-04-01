@echo off
REM ========================================
REM Быстрая установка VoboostVoiceAssistant
REM ========================================
REM Обновление APK БЕЗ удаления библиотек
REM ========================================

setlocal enabledelayedexpansion

set APK_PATH=app\build\outputs\apk\release\app-release-unsigned.apk
set DEVICE_APK_PATH=/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
set DEVICE_LIB_PATH=/system/priv-app/VoboostVoiceAssistant/lib/arm64

echo ========================================
echo  VoboostVoiceAssistant - Установка
echo ========================================
echo.

REM Проверка наличия APK
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала выполните сборку: gradlew.bat assembleRelease
    pause
    exit /b 1
)

echo [1/6] Получение root-прав...
adb root
timeout /t 2 /nobreak >nul

echo [2/6] Перемонтирование /system в режим записи...
adb remount
timeout /t 2 /nobreak >nul

echo [3/6] Проверка наличия библиотек...
adb shell ls %DEVICE_LIB_PATH% >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Библиотеки не найдены! Запустите copy-libs-to-device.bat
) else (
    echo [OK] Библиотеки на месте
)

echo [4/6] Копирование APK в систему...
adb push %APK_PATH% %DEVICE_APK_PATH%
if errorlevel 1 (
    echo [ERROR] Не удалось скопировать APK!
    pause
    exit /b 1
)

echo [5/6] Установка прав доступа (chmod 644)...
adb shell chmod 644 %DEVICE_APK_PATH%

echo [6/6] Перезапуск приложения...
adb shell am force-stop com.voboost.voiceassistant
timeout /t 2 /nobreak >nul
adb shell am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService

echo.
echo ========================================
echo  ✅ Готово! Приложение обновлено.
echo ========================================
echo.
echo Библиотеки сохранены в: %DEVICE_LIB_PATH%
echo.
echo Для проверки:
echo   adb shell ps -ef ^| grep voboost
echo   adb logcat -s VoboostVoiceService:*
echo.

pause
