@echo off
REM ============================================================================
REM  VoboostVoiceAssistant - Установка в /system/priv-app/ с моделями на SD-карте
REM ============================================================================
REM  Этот скрипт:
REM  1. Собирает APK
REM  2. Копирует config.json и модели на SD-карту
REM  3. Устанавливает APK в /system/priv-app/
REM  4. Выдаёт разрешения
REM  5. Перезагружает устройство
REM ============================================================================

setlocal enabledelayedexpansion

REM Добавляем путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - System App Installer (SD Card Models)
echo ============================================================================
echo.

REM Проверка ADB
echo [1/9] Проверка подключения ADB...
adb devices >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB не найден! Убедитесь что устройство подключено.
    pause
    exit /b 1
)

REM Проверка что устройство в режиме root
adb root >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Не удалось получить root права! Устройство должно быть рутировано.
    pause
    exit /b 1
)
echo [OK] ADB подключен, root получен
echo.

REM Сборка проекта
echo [2/9] Сборка проекта...
cd /d "%~dp0"
call gradlew.bat assembleRelease
if errorlevel 1 (
    echo [ERROR] Ошибка сборки! Проверьте логи выше.
    pause
    exit /b 1
)
echo [OK] Сборка завершена
echo.

REM Проверка APK
set "APK_PATH=app\build\outputs\apk\release\app-release.apk"
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Попробуйте собрать проект вручную: gradlew.bat assembleRelease
    pause
    exit /b 1
)
echo [OK] APK найден: %APK_PATH%
echo.

REM Копирование config.json и моделей на SD-карту
echo [3/9] Копирование файлов на SD-карту...
echo       Это может занять несколько минут...

REM Создаём директорию на SD-карте
adb shell "mkdir -p /sdcard/Android/data/ru.voboost.voiceassistant/files/models/vosk"
adb shell "mkdir -p /sdcard/Android/data/ru.voboost.voiceassistant/files/models/sherpa"

REM Копирование config.json
echo       Копирование config.json...
if exist "app\src\main\assets\config.json" (
    adb push "app\src\main\assets\config.json" "/sdcard/Android/data/ru.voboost.voiceassistant/files/config.json"
    if errorlevel 1 (
        echo [WARNING] Ошибка копирования config.json
    ) else (
        echo [OK] config.json скопирован
    )
) else (
    echo [WARNING] config.json не найден в assets
)

REM Копирование Vosk модели
echo       Копирование Vosk модели...
if exist "models\vosk\vosk-model-small-ru-0.22" (
    adb push "models\vosk\vosk-model-small-ru-0.22" "/sdcard/Android/data/ru.voboost.voiceassistant/files/models/vosk/vosk-model-small-ru-0.22"
    if errorlevel 1 (
        echo [WARNING] Ошибка копирования Vosk модели, пробуем архивом...
        goto :copy_vosk_archive
    )
    echo [OK] Vosk модель скопирована
) else if exist "models\vosk-model-small-ru-0.22.tar.gz" (
    :copy_vosk_archive
    echo       Копирование архива Vosk...
    adb push "models\vosk-model-small-ru-0.22.tar.gz" "/sdcard/Android/data/ru.voboost.voiceassistant/files/models/vosk/"
    adb shell "cd /sdcard/Android/data/ru.voboost.voiceassistant/files/models/vosk && tar -xzf vosk-model-small-ru-0.22.tar.gz && rm vosk-model-small-ru-0.22.tar.gz"
    echo [OK] Vosk модель распакована
) else (
    echo [WARNING] Vosk модель не найдена в models/
)

REM Копирование Sherpa TTS модели
echo       Копирование Sherpa TTS модели...
if exist "models\sherpa\tts-ru-model" (
    adb push "models\sherpa\tts-ru-model" "/sdcard/Android/data/ru.voboost.voiceassistant/files/models/sherpa/tts-ru-model"
    if errorlevel 1 (
        echo [WARNING] Ошибка копирования Sherpa модели, пробуем архивом...
        goto :copy_sherpa_archive
    )
    echo [OK] Sherpa TTS модель скопирована
) else if exist "models\sherpa\tts-ru-model.tar.gz.bin" (
    :copy_sherpa_archive
    echo       Копирование архива Sherpa...
    adb push "models\sherpa\tts-ru-model.tar.gz.bin" "/sdcard/Android/data/ru.voboost.voiceassistant/files/models/sherpa/"
    adb shell "cd /sdcard/Android/data/ru.voboost.voiceassistant/files/models/sherpa && mv tts-ru-model.tar.gz.bin tts-ru-model.tar.gz && tar -xzf tts-ru-model.tar.gz && rm tts-ru-model.tar.gz"
    echo [OK] Sherpa TTS модель распакована
) else (
    echo [WARNING] Sherpa TTS модель не найдена в models/
)

echo [OK] Копирование завершено
echo.

REM Удаление старой версии
echo [4/9] Удаление старой версии приложения...
adb uninstall ru.voboost.voiceassistant 2>nul
if errorlevel 1 (
    echo [INFO] Приложение не было установлено ранее
) else (
    echo [OK] Старая версия удалена
)
echo.

REM Установка в /system/priv-app/
echo [5/9] Установка в /system/priv-app/...
adb remount >nul 2>&1

REM Создаём директорию в system
adb shell "mkdir -p /system/priv-app/VoboostVoiceAssistant"

REM Копируем APK
adb push "%APK_PATH%" "/system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk"
if errorlevel 1 (
    echo [ERROR] Ошибка копирования APK в system!
    pause
    exit /b 1
)

REM Устанавливаем правильные права
adb shell "chmod 644 /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk"
adb shell "chown root:root /system/priv-app/VoboostVoiceAssistant/VoboostVoiceAssistant.apk"
echo [OK] APK установлен в /system/priv-app/
echo.

REM Выдача разрешений
echo [6/9] Выдача разрешений...
adb shell pm grant ru.voboost.voiceassistant android.permission.RECORD_AUDIO
adb shell pm grant ru.voboost.voiceassistant android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant ru.voboost.voiceassistant android.permission.FOREGROUND_SERVICE
adb shell pm grant ru.voboost.voiceassistant android.permission.RECEIVE_BOOT_COMPLETED
echo [OK] Разрешения выданы
echo.

REM Проверка установки
echo [7/9] Проверка установки...
adb shell dumpsys package ru.voboost.voiceassistant | findstr "system"
echo.

REM Проверка файлов на SD-карте
echo [8/9] Проверка файлов на SD-карте...
adb shell "ls -la /sdcard/Android/data/ru.voboost.voiceassistant/files/config.json" 2>nul || echo "  config.json не найден"
adb shell "ls -la /sdcard/Android/data/ru.voboost.voiceassistant/files/models/" 2>nul || echo "  models/ не найдена"
echo.

REM Перезагрузка
echo [9/9] Перезагрузка устройства...
echo.
echo !!! ВНИМАНИЕ !!!
echo Устройство будет перезапущено через 3 секунды.
echo.
echo Если вы хотите отменить перезагрузку, нажмите Ctrl+C сейчас!
echo.
timeout /t 3 /nobreak >nul

adb reboot

echo.
echo ============================================================================
echo  Установка завершена!
echo ============================================================================
echo.
echo После перезагрузки:
echo   1. Дождитесь полной загрузки системы
echo   2. Проверьте логи:
echo      adb logcat -s VoboostVoiceService:V ConfigManager:V
echo.
echo   3. Проверьте что конфиг загружен с SD-карты:
echo      adb shell ls -la /sdcard/Android/data/ru.voboost.voiceassistant/files/
echo.
echo   4. Протестируйте команду:
echo      adb shell am broadcast -a ru.voboost.voiceassistant.COMMAND ^
echo        --es "target" "Window" --ei "classify" 2 --ei "command" 0
echo.
echo ============================================================================
echo   Чтобы обновить конфиг в будущем:
echo     1. Отредактируйте app/src/main/assets/config.json
echo     2. Запустите: adb push app/src/main/assets/config.json ^
echo        /sdcard/Android/data/ru.voboost.voiceassistant/files/config.json
echo     3. Перезапустите приложение
echo ============================================================================
echo.
pause
