@echo off
chcp 65001 >nul
echo ============================================
echo   VoboostVoiceAssistant - Разрешения
echo ============================================
echo.

echo [1/6] Microphone...
adb shell appops set ru.voboost.voice RECORD_AUDIO allow

echo [2/6] System Alert Window (Overlay)...
adb shell appops set ru.voboost.voice SYSTEM_ALERT_WINDOW allow

echo [3/6] Accessibility Service...
adb shell settings put secure enabled_accessibility_services ru.voboost.voice/ru.voboost.voice.VoiceActivationService
adb shell settings put secure accessibility_enabled 1

echo [4/6] Notifications...
adb shell appops set ru.voboost.voice POST_NOTIFICATION allow

echo [5/6] Auto-start (Boot Completed)...
adb shell appops set ru.voboost.voice RECEIVE_BOOT_COMPLETED allow

echo [6/6] Foreground Service...
adb shell appops set ru.voboost.voice FOREGROUND_SERVICE allow

echo.
echo ============================================
echo   Готово! Все разрешения предоставлены.
echo ============================================
echo.
echo Проверка разрешений:
echo   adb shell dumpsys package ru.voboost.voice
echo.
echo Тестирование автозапуска:
echo   adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p ru.voboost.voice
echo.
pause

