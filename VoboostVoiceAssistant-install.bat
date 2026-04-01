@echo off
REM ============================================================================
REM  VoboostVoiceAssistant - Установка и тестирование
REM ============================================================================

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Установка
echo ============================================================================
echo.

REM Проверка ADB
echo [1/7] Проверка ADB...
adb devices >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB не найден! Установите Android SDK Platform Tools.
    pause
    exit /b 1
)
echo [OK] ADB найден
echo.

REM Сборка проекта
echo [2/7] Сборка проекта...
cd /d "%~dp0VoboostVoiceAssistant"
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo [ERROR] Ошибка сборки!
    pause
    exit /b 1
)
echo [OK] Сборка завершена
echo.

REM Установка APK
echo [3/7] Установка APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo [ERROR] Ошибка установки!
    pause
    exit /b 1
)
echo [OK] APK установлен
echo.

REM Разрешения
echo [4/7] Выдача разрешений...
adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
adb shell pm grant com.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant com.voboost.voiceassistant android.permission.FOREGROUND_SERVICE
echo [OK] Разрешения выданы
echo.

REM Отключение стандартных сервисов
echo [5/7] Отключение стандартных голосовых ассистентов...
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
adb shell pm disable com.qinggan.sttservice


adb shell pm enable com.voboost.voiceassistant

echo [OK] Стандартные сервисы отключены
echo.

REM Запуск сервиса
echo [6/7] Запуск VoboostVoiceService...
adb shell am startservice com.voboost.voiceassistant/.VoboostVoiceService
.\adb shell am start-foreground-service com.voboost.voiceassistant/.VoboostVoiceService

adb shell settings put global hidden_api_policy 1

adb.exe shell "setenforce 0"
adb.exe shell "getenforce"
adb.exe shell "am force-stop com.voboost.voiceassistant"

adb.exe shell "pm disable com.voboost.voiceassistant"

adb shell pm list permissions -f | grep -A 1 WRITE_CANBUS



adb shell am start-foreground-service -a com.voboost.voiceassistant.START com.voboost.voiceassistant/.VoboostVoiceService


adb shell am force-stop com.voboost.voiceassistant

adb.exe shell am start-foreground-service com.voboost.voiceassistant/.VoboostVoiceService

 Starting service: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.v    │
  │    oboost.voiceassistant/.VoboostVoiceService }  
  
   # 2. Выдать разрешения
      5 adb shell pm grant com.voboost.voiceassistant android.permission.RECORD_AUDIO
      6 adb shell pm grant com.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW
      7 adb shell pm grant com.voboost.voiceassistant android.permission.FOREGROUND_SERVICE
      8
      9 # 3. Запустить сервис (ПРАВИЛЬНАЯ КОМАНДА)
     10 adb shell am start-foreground-service -a com.voboost.voiceassistant.START com.voboost.voiceassistant/.VoboostVoiceService
     11
     12 # ИЛИ через компонент:
     13 adb shell am start-foreground-service -n com.voboost.voiceassistant/.VoboostVoiceService



echo [OK] Сервис запущен
echo.

REM Проверка логов
echo [7/7] Проверка логов (нажмите Ctrl+C для остановки)...
timeout /t 2 >nul
adb logcat -c
echo.
echo Ожидание логов Voboost (Ctrl+C для остановки)...
echo.
adb logcat | findstr /i "voboost SoundEffect"

echo.
echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Дальнейшие шаги:
echo   1. Запустите Frida скрипт:
echo      frida -U -f com.qinggan.sttservice -l ..\frida-voice-button.js --no-pause
echo.
echo   2. Нажмите кнопку на руле
echo.
echo   3. Проверьте логи
echo.
pause
