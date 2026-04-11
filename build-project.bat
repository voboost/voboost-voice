@echo off
REM ============================================================================
REM  VoboostVoiceAssistant - Сборка проекта
REM ============================================================================
REM  Этот скрипт только собирает APK (debug и release).
REM  Запускать перед install-update.bat или VoboostVoiceAssistant-install.bat
REM ============================================================================

setlocal enabledelayedexpansion

set "PROJECT_DIR=%~dp0"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Сборка проекта
echo ============================================================================
echo.

echo Выберите тип сборки:
echo   1. Debug   (для разработки и тестирования)
echo   2. Release (для финальной версии)
echo   3. Оба варианта
echo.
set /p BUILD_TYPE="Ваш выбор (1/2/3) [по умолчанию 1]: "

if "%BUILD_TYPE%"=="" set BUILD_TYPE=1
cd /d "%PROJECT_DIR%"

if "%BUILD_TYPE%"=="1" (
    echo.
    echo [1/1] Сборка Debug APK...
    call gradlew.bat assembleDebug
    if errorlevel 1 (
        echo [ERROR] Ошибка сборки Debug!
        pause
        exit /b 1
    )
    echo.
    echo [OK] Debug APK собран:
    echo     app\build\outputs\apk\debug\app-debug.apk
    echo.

) else if "%BUILD_TYPE%"=="2" (
    echo.
    echo [1/1] Сборка Release APK...
    call gradlew.bat assembleRelease
    if errorlevel 1 (
        echo [ERROR] Ошибка сборки Release!
        pause
        exit /b 1
    )
    echo.
    echo [OK] Release APK собран:
    echo     app\build\outputs\apk\release\app-release-unsigned.apk
    echo.

) else if "%BUILD_TYPE%"=="3" (
    echo.
    echo [1/2] Сборка Debug APK...
    call gradlew.bat assembleDebug
    if errorlevel 1 (
        echo [ERROR] Ошибка сборки Debug!
        pause
        exit /b 1
    )
    echo [OK] Debug APK собран
    echo.
    
    echo [2/2] Сборка Release APK...
    call gradlew.bat assembleRelease
    if errorlevel 1 (
        echo [ERROR] Ошибка сборки Release!
        pause
        exit /b 1
    )
    echo [OK] Release APK собран
    echo.
    
    echo [OK] Оба APK собраны:
    echo     Debug:   app\build\outputs\apk\debug\app-debug.apk
    echo     Release: app\build\outputs\apk\release\app-release-unsigned.apk
    echo.

) else (
    echo [ERROR] Неверный выбор: %BUILD_TYPE%
    pause
    exit /b 1
)

echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Следующий шаг:
echo   - Для быстрой установки:  install-update.bat
echo   - Для полного развёртывания: VoboostVoiceAssistant-install.bat
echo.
pause
