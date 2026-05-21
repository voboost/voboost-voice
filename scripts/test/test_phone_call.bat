@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "ADB=d:\Projects\Android\MM\6.11.1\export\adb\adb.exe"

if "%~1"=="" (
    echo.
    echo ========================================
    echo Voboost Phone Call Test Tool
    echo ========================================
    echo.
    echo Usage: %~nx0 PHONE_NUMBER_OR_CONTACT
    echo.
    echo Examples:
    echo   %~nx0 4008888488
    echo   %~nx0 89001234567
    echo.
    echo Monitor logs: %~nx0 logcat
    echo.
    echo Broadcast params:
    echo   Action: com.qinggan.broadcast.action.ivokaphonecall
    echo   Extra:  Ivoka_CallInfo = [number/contact]
    echo   Extra:  screen_int     = 0
    echo   Extra:  mac            = ""
    echo ========================================
    echo.
    exit /b 1
)

if "%~1"=="logcat" (
    echo Monitoring logs... Press Ctrl+C to stop.
    echo.
    %ADB% logcat -s HeadSetProfileManager:I MakeCallBroadcastReceiver:I BluetoothPhone:I VoboostVoiceService:D IntentHandler:D
    exit /b 0
)

set "PHONE_NUMBER=%~1"

echo.
echo ========================================
echo Sending phone call command
echo ========================================
echo Action: com.qinggan.broadcast.action.ivokaphonecall
echo Ivoka_CallInfo: %PHONE_NUMBER%
echo.

%ADB% shell "am broadcast -a com.qinggan.broadcast.action.ivokaphonecall --es Ivoka_CallInfo '%PHONE_NUMBER%' --ei screen_int 0 --es mac ''"

if !errorlevel! equ 0 (
    echo Broadcast sent successfully!
) else (
    echo ERROR: Failed to send broadcast!
    %ADB% devices
)

