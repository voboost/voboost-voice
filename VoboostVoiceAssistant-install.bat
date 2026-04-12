@echo off
REM ============================================================================
REM  VoboostVoiceAssistant - Установка и запуск
REM ============================================================================
REM  Этот скрипт:
REM  1. Отключает стандартные голосовые ассистенты (Qinggan IVoka, STT)
REM  2. Собирает и устанавливает APK
REM  3. Копирует модели (Vosk + Sherpa TTS)
REM  4. Выдаёт разрешения
REM  5. Запускает сервис
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

REM Путь к проекту
set "PROJECT_DIR=%~dp0"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Развёртывание на устройстве
echo ============================================================================
echo.
echo ВНИМАНИЕ: Этот скрипт НЕ собирает проект!
echo Убедитесь что APK уже собран (через Android Studio или build-project.bat)
echo.
timeout /t 2 /nobreak

REM ============================================================================
REM  Шаг 1: Отключение стандартных голосовых ассистентов
REM ============================================================================
echo [1/7] Отключение стандартных голосовых ассистентов...
adb shell pm disable com.qinggan.ivoka       >nul 2>&1
if errorlevel 0 echo   [OK] com.qinggan.ivoka отключён
adb shell pm disable com.qinggan.ivoka1      >nul 2>&1
if errorlevel 0 echo   [OK] com.qinggan.ivoka1 отключён
adb shell pm disable com.qinggan.sttservice  >nul 2>&1
if errorlevel 0 echo   [OK] com.qinggan.sttservice отключён
echo.

REM ============================================================================
REM  Шаг 2: Проверка наличия APK
REM ============================================================================
echo [2/7] Проверка наличия APK...
set "APK_PATH=app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала соберите проект:
    echo   - Через Android Studio: Build ^> Make Project
    echo   - Или запустите: build-project.bat
    pause
    exit /b 1
)
echo [OK] APK найден: %APK_PATH%
echo.

REM ============================================================================
REM  Шаг 3: Установка APK
REM ============================================================================
echo [3/7] Установка APK...
adb root >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell "mount -o rw,remount /" >nul 2>&1

REM Удаляем старую версию и ставим новую
adb shell rm -rf /system/priv-app/VoboostVoiceAssistant >nul 2>&1
adb push app\build\outputs\apk\debug\app-debug.apk ^
  /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk >nul 2>&1

if errorlevel 1 (
    echo [ERROR] Ошибка установки APK!
    pause
    exit /b 1
)
adb shell chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk >nul 2>&1
echo [OK] APK установлен как системное приложение
echo.

REM ============================================================================
REM  Шаг 4: Копирование моделей
REM ============================================================================
echo [4/7] Копирование моделей (Vosk + Sherpa TTS)...

REM Создаём директории
adb shell mkdir -p /data/user/0/ru.voboost.voiceassistant/files/models/vosk >nul 2>&1
adb shell mkdir -p /data/user/0/ru.voboost.voiceassistant/files/models/sherpa >nul 2>&1

REM Копируем Vosk модель (русский язык, ~50MB)
if exist "models\vosk\vosk-model-small-ru-0.22" (
    adb push models\vosk\vosk-model-small-ru-0.22 ^
      /data/user/0/ru.voboost.voiceassistant/files/models/vosk/vosk-model-small-ru-0.22 >nul 2>&1
    if errorlevel 0 (
        echo   [OK] Vosk модель скопирована
    ) else (
        echo   [WARN] Ошибка копирования Vosk модели
    )
) else (
    echo   [WARN] Vosk модель не найдена: models\vosk\vosk-model-small-ru-0.22
)

REM Копируем Sherpa TTS модель (русская Piper, ~80MB)
if exist "models\sherpa\tts-ru-model-temp\tts-ru-model" (
    adb push models\sherpa\tts-ru-model-temp\tts-ru-model ^
      /data/user/0/ru.voboost.voiceassistant/files/models/sherpa/tts-ru-model >nul 2>&1
    if errorlevel 0 (
        echo   [OK] Sherpa TTS модель скопирована
    ) else (
        echo   [WARN] Ошибка копирования Sherpa TTS модели
    )
) else (
    echo   [WARN] Sherpa TTS модель не найдена: models\sherpa\tts-ru-model-temp\tts-ru-model
)

REM Устанавливаем права
adb shell chown -R 10068:10068 /data/user/0/ru.voboost.voiceassistant/files/models >nul 2>&1
adb shell chmod -R 755 /data/user/0/ru.voboost.voiceassistant/files/models >nul 2>&1
echo [OK] Модели установлены
echo.

REM ============================================================================
REM  Шаг 5: Инициализация приложения и разрешения
REM ============================================================================
echo [5/7] Инициализация приложения и выдача разрешений...

REM Инициализируем данные приложения (создаёт /data/user_de/0/...)
adb shell cmd package install-existing ru.voboost.voiceassistant >nul 2>&1
echo   [OK] Данные приложения инициализированы

REM Выдаём разрешения
adb shell pm grant ru.voboost.voiceassistant android.permission.RECORD_AUDIO >nul 2>&1
adb shell pm grant ru.voboost.voiceassistant android.permission.READ_CONTACTS >nul 2>&1
adb shell pm grant ru.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW >nul 2>&1
adb shell pm grant ru.voboost.voiceassistant android.permission.FOREGROUND_SERVICE >nul 2>&1
echo   [OK] Разрешения выданы (RECORD_AUDIO, SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE)
echo.

REM ============================================================================
REM  Шаг 6: Перезагрузка для регистрации системного приложения
REM ============================================================================
echo [6/7] Перезагрузка устройства для регистрации системного приложения...
echo   ВНИМАНИЕ: Устройство будет перезагружено через 3 секунды!
echo   (Нажмите Ctrl+C для отмены)
timeout /t 3 /nobreak

adb shell reboot
echo   Ожидание перезагрузки...
timeout /t 30 /nobreak

REM Ждём полного запуска системы
echo   Ожидание загрузки системы (ещё 30 секунд)...
timeout /t 30 /nobreak

REM Проверяем, что устройство подключено
adb wait-for-device
timeout /t 5 /nobreak
echo [OK] Устройство загружено
echo.

REM ============================================================================
REM  Шаг 7: Запуск сервиса
REM ============================================================================
echo [7/7] Запуск VoboostVoiceService...

REM Убеждаемся что приложение включено
adb shell pm enable ru.voboost.voiceassistant >nul 2>&1

REM Останавливаем если было запущено
adb shell am force-stop ru.voboost.voiceassistant >nul 2>&1
timeout /t 2 /nobreak

REM Запускаем foreground сервис
adb shell am start-foreground-service --user 0 ^
  -n ru.voboost.voiceassistant/.VoboostVoiceService

if errorlevel 1 (
    echo [ERROR] Ошибка запуска сервиса!
    pause
    exit /b 1
)

REM Ждём инициализации
timeout /t 5 /nobreak

REM Проверяем что процесс запущен
adb shell ps | findstr voboost >nul 2>&1
if errorlevel 1 (
    echo [WARN] Процесс не найден! Проверьте логи:
    adb logcat -d -t 50 | findstr /i "voboost fatal error"
    pause
    exit /b 1
)
echo [OK] Сервис запущен и работает
echo.

REM ============================================================================
REM  Проверка логов
REM ============================================================================
echo ============================================================================
echo  Проверка логов (нажмите Ctrl+C для остановки)...
echo ============================================================================
echo.
adb logcat -c
timeout /t 2 /nobreak
echo Ожидание логов VoboostVoiceService...
echo.
adb logcat | findstr /i "VoboostVoiceService StateMachine SpeechRecognizer"

echo.
echo ============================================================================
echo  Готово! VoboostVoiceAssistant развёрнут и работает!
echo ============================================================================
echo.
echo Как использовать:
echo   1. Нажмите кнопку голосового управления на руле
echo   2. Дождитесь звукового сигнала и фразы "Слушаю вас"
echo   3. Скажите команду (например: "открой окно", "закрой окно")
echo.
echo Просмотр логов:
echo   adb logcat -s VoboostVoiceService:* StateMachine:* SpeechRecognizer:*
echo.
echo Остановка сервиса:
echo   adb shell am force-stop ru.voboost.voiceassistant
echo.
pause
