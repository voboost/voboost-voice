@echo off
REM ========================================
REM Извлечение нативных библиотек из APK
REM ========================================
REM Примечание: Этот скрипт больше не нужен!
REM Библиотеки хранятся в native_libs\arm64-v8a\
REM Используйте copy-libs-to-device.bat для загрузки
REM ========================================

setlocal enabledelayedexpansion

set APK_PATH=app\build\outputs\apk\release\app-release-unsigned.apk
set TEMP_DIR=temp_extract
set OUTPUT_DIR=native_libs\arm64-v8a

echo ========================================
echo  Извлечение библиотек из APK
echo ========================================
echo.
echo [ПРИМЕЧАНИЕ] Этот скрипт устарел!
echo Библиотеки теперь хранятся в native_libs\
echo Используйте copy-libs-to-device.bat
echo.
echo Продолжить? (y/n)
set /p CONTINUE=
if /i not "%CONTINUE%"=="y" exit /b 0

REM Проверка наличия APK
if not exist "%APK_PATH%" (
    echo [ERROR] APK не найден: %APK_PATH%
    echo Сначала выполните сборку: gradlew.bat assembleRelease
    pause
    exit /b 1
)

REM Проверка наличия unzip
where unzip >nul 2>&1
if errorlevel 1 (
    echo [ERROR] unzip не найден! Установите Git Bash или GnuWin32 unzip.
    echo Или используйте 7-Zip для ручного извлечения.
    pause
    exit /b 1
)

echo [1/4] Создание временной директории...
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"

echo [2/4] Распаковка APK...
unzip -q "%APK_PATH%" -d "%TEMP_DIR%"

echo [3/4] Извлечение библиотек arm64-v8a...
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

copy "%TEMP_DIR%\lib\arm64-v8a\libjnidispatch.so" "%OUTPUT_DIR%\" >nul 2>&1 && echo   + libjnidispatch.so
copy "%TEMP_DIR%\lib\arm64-v8a\libonnxruntime.so" "%OUTPUT_DIR%\" >nul 2>&1 && echo   + libonnxruntime.so
copy "%TEMP_DIR%\lib\arm64-v8a\libsherpa-onnx-jni.so" "%OUTPUT_DIR%\" >nul 2>&1 && echo   + libsherpa-onnx-jni.so
copy "%TEMP_DIR%\lib\arm64-v8a\libvosk.so" "%OUTPUT_DIR%\" >nul 2>&1 && echo   + libvosk.so

echo [4/4] Очистка временных файлов...
rmdir /s /q "%TEMP_DIR%"

echo.
echo ========================================
echo  Готово! Библиотеки извлечены в:
echo  %OUTPUT_DIR%
echo ========================================
echo.

pause
