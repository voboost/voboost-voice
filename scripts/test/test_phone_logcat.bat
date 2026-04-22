@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM ADB утилита для мониторинга логов телефонных команд
Показывает логи:
  - BluetoothPhone (HeadSetProfileManager, MakeCallBroadcastReceiver)
  - VoboostVoiceService (отправка команд)
  - IntentHandler (наши обработчики)
============================================================

REM Путь к adb
set "ADB=d:\Projects\Android\MM\6.11.1\export\adb\adb.exe"

echo.
echo ========================================
echo Мониторинг логов телефонных команд
echo ========================================
echo.
echo Отслеживаемые теги:
echo   HeadSetProfileManager:I     - BluetoothPhone обработка звонков
echo   MakeCallBroadcastReceiver:I - BluetoothPhone приём broadcast
echo   BluetoothPhone:I            - Общие логи BluetoothPhone
echo   VoboostVoiceService:D       - Наш сервис
echo   IntentHandler:D             - Наши Intent-обработчики
echo.
echo Нажмите Ctrl+C для остановки
echo ========================================
echo.

%ADB% logcat -s ^
    HeadSetProfileManager:I ^
    MakeCallBroadcastReceiver:I ^
    BluetoothPhone:I ^
    VoboostVoiceService:D ^
    IntentHandler:D ^
    VoboostVoiceService:I ^
    IntentHandler:I ^
    PhoneCommand:I
