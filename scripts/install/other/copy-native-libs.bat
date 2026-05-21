@echo off
chcp 65001 >nul
REM ============================================================================
REM  Копирование нативных библиотек для системного приложения
REM ============================================================================

setlocal enabledelayedexpansion

REM Путь к ADB
set "ADB_PATH=D:\Projects\Android\MM\6.11.1\export\adb"
set "PATH=%ADB_PATH%;%PATH%"

REM Архитектура (arm64 для большинства современных устройств)
set "ARCH=arm64-v8a"
set "LIB_DIR=lib/arm64"

echo.
echo ============================================================================
echo  Copying Native Libraries to /system/priv-app/
echo ============================================================================
echo.

REM Проверка подключения
adb devices >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB не найден!
    pause
    exit /b 1
)

REM Получение root
echo [1/6] Получение root прав...
adb root
timeout /t 2 /nobreak >nul
adb remount
echo [OK] Root получен, /system перемонтирован
echo.

REM Создание директории для библиотек
echo [2/6] Создание директории для библиотек...
adb shell "mkdir -p /system/priv-app/VoboostVoiceAssistant/%LIB_DIR%"
echo [OK] Директория создана: /system/priv-app/VoboostVoiceAssistant/%LIB_DIR%
echo.

REM Извлечение библиотек из APK
echo [3/6] Извлечение библиотек из APK...

set "APK_PATH=D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant\app\build\outputs\apk\debug\app-debug.apk"
set "TEMP_DIR=%TEMP%\voboost_libs"

if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"

REM Используем PowerShell для извлечения
powershell -Command "Add-Type -Assembly System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::OpenRead('%APK_PATH%'); $libs = $zip.Entries | Where-Object {$_.FullName -like 'lib/%ARCH%/*.so'}; foreach ($lib in $libs) { $path = '%TEMP_DIR%\' + [System.IO.Path]::GetFileName($lib.FullName); $stream = $lib.Open(); $file = [System.IO.File]::Create($path); $stream.CopyTo($file); $file.Close(); $stream.Close(); Write-Host \"Extracted: $path\" }"

echo [OK] Библиотеки извлечены в %TEMP_DIR%
echo.

REM Копирование библиотек на устройство
echo [4/6] Копирование библиотек на устройство...
for %%f in ("%TEMP_DIR%\*.so") do (
    echo       Копирование %%~nxf...
    adb push "%%f" "/system/priv-app/VoboostVoiceAssistant/%LIB_DIR%/%%~nxf"
)
echo [OK] Библиотеки скопированы
echo.

REM Установка прав
echo [5/6] Установка прав доступа...
adb shell "chmod 755 /system/priv-app/VoboostVoiceAssistant/lib"
adb shell "chmod 755 /system/priv-app/VoboostVoiceAssistant/lib/arm64"
adb shell "chmod 644 /system/priv-app/VoboostVoiceAssistant/lib/arm64/*.so"
echo [OK] Права установлены
echo.

REM Проверка
echo [6/6] Проверка установленных библиотек...
adb shell "ls -la /system/priv-app/VoboostVoiceAssistant/lib/arm64/"
echo.

REM Очистка
rmdir /s /q "%TEMP_DIR%"

echo ============================================================================
echo  Готово!
echo ============================================================================
echo.
echo Теперь нужно перезагрузить устройство:
echo   adb reboot
echo.
pause

