@echo off
chcp 65001 >nul
REM ============================================================================
REM  VoboostVoiceAssistant - Сборка проекта
REM ============================================================================
REM  Этот скрипт только собирает APK (debug и release).
REM  Запускать перед install-update.bat или VoboostVoiceAssistant-install.bat
REM 
REM  Использование:
REM    build-project.bat [1|2|3]   - Выбор типа сборки:
REM                                   1 = Debug (по умолчанию)
REM                                   2 = Release
REM                                   3 = Оба варианта
REM ============================================================================

set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..
set GRADLEW="%SCRIPT_DIR%gradlew.bat"

echo.
echo ============================================================================
echo  VoboostVoiceAssistant - Сборка проекта
echo ============================================================================
echo.

REM Проверка параметров командной строки - если есть параметр, сразу используем его
if not "%1"=="" (
    set BUILD_TYPE=%1
    goto skip_prompt
)

echo Выберите тип сборки:
echo   1. Debug   (для разработки и тестирования)
echo   2. Release (для финальной версии)
echo   3. Оба варианта
echo.
set /p BUILD_TYPE="Ваш выбор (1/2/3) [по умолчанию 1]: "
if "%BUILD_TYPE%"=="" set BUILD_TYPE=1

:skip_prompt
if "%BUILD_TYPE%"=="1" goto build_debug
if "%BUILD_TYPE%"=="2" goto build_release
if "%BUILD_TYPE%"=="3" goto build_both

echo [ERROR] Неверный выбор: %BUILD_TYPE%
pause
exit /b 1

:build_debug
echo.
echo [1/1] Сборка Debug APK...
call %GRADLEW% -p "%PROJECT_DIR%" assembleDebug
if errorlevel 1 (
    echo [ERROR] Ошибка сборки Debug!
    pause
    exit /b 1
)
echo.
echo [OK] Debug APK собран:
echo     app\build\outputs\apk\debug\app-debug.apk
goto done

:build_release
echo.
echo [1/1] Сборка Release APK...
call %GRADLEW% -p "%PROJECT_DIR%" assembleRelease
if errorlevel 1 (
    echo [ERROR] Ошибка сборки Release!
    pause
    exit /b 1
)
echo.
echo [OK] Release APK собран:
echo     app\build\outputs\apk\release\app-release-unsigned.apk
goto done

:build_both
echo.
echo [1/2] Сборка Debug APK...
call %GRADLEW% -p "%PROJECT_DIR%" assembleDebug
if errorlevel 1 (
    echo [ERROR] Ошибка сборки Debug!
    pause
    exit /b 1
)
echo [OK] Debug APK собран
echo.

echo [2/2] Сборка Release APK...
call %GRADLEW% -p "%PROJECT_DIR%" assembleRelease
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
goto done

:done
echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Следующий шаг:
echo   - Для быстрой установки:  install-update.bat
echo   - Для полного развёртывания: VoboostVoiceAssistant-install.bat

