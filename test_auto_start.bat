@echo off
chcp 65001 >nul
echo ============================================
echo   VoboostVoiceAssistant - Тест автозапуска
echo ============================================
echo.

echo [1/2] Эмуляция загрузки системы...
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p com.voboost.voiceassistant

echo.
echo [2/2] Ожидание запуска сервиса (5 сек)...
timeout /t 5 /nobreak >nul

echo.
echo Проверка логов:
echo ============================================
adb logcat -d | findstr /i "BootReceiver VoboostVoiceService SpeechRecognition"
echo ============================================
echo.

echo Проверка запущенных сервисов:
adb shell dumpsys activity services | findstr /i "voboost"
echo.

echo ============================================
echo   Готово!
echo ============================================
echo.
echo Если видите "Vosk initialized successfully" - всё работает!
echo.
pause
