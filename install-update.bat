@echo off
REM ========================================
REM Быстрое обновление VoboostVoiceAssistant
REM Обновление APK БЕЗ перезагрузки и моделей
REM ========================================

set "ADB=d:\Projects\Android\MM\6.11.1\export\adb\adb.exe"
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
set DEVICE_APK_PATH=/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk
set PKG=ru.voboost.voiceassistant

echo ========================================
echo  VoboostVoiceAssistant - Быстрое обновление
echo ========================================
echo.

if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала: build-project.bat
    pause
    exit /b 1
)

echo [1/5] Отключение ассистентов...
%ADB% shell pm disable com.qinggan.ivoka       >nul 2>&1
%ADB% shell pm disable com.qinggan.ivoka1      >nul 2>&1
%ADB% shell pm disable com.qinggan.sttservice  >nul 2>&1
echo   [OK]

echo [2/5] Root...
%ADB% root >nul 2>&1
timeout /t 1 /nobreak >nul

echo [3/5] Remount /system...
%ADB% shell "mount -o rw,remount /" >nul 2>&1

echo [4/5] Обновление APK...
%ADB% push %APK_PATH% %DEVICE_APK_PATH% >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Не удалось скопировать APK!
    pause
    exit /b 1
)
%ADB% shell chmod 644 %DEVICE_APK_PATH% >nul 2>&1
echo   [OK]

echo [5/5] Перезапуск...
%ADB% shell "am force-stop %PKG%" >nul 2>&1
timeout /t 2 /nobreak >nul
%ADB% shell "am start-foreground-service -n %PKG%/.VoboostVoiceService" >nul 2>&1
echo   [OK]

echo.
echo ========================================
echo  ✅ APK обновлён и перезапущен
echo ========================================
echo.
echo Полная установка с моделями:
echo   VoboostVoiceAssistant-install.bat
echo.
echo Модели и конфиг хранятся на внешнем хранилище:
echo   /storage/emulated/0/Android/data/ru.voboost.voiceassistant/files/
echo.
pause
