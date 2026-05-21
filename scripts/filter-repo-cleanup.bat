@echo off
chcp 65001 >nul

REM Script to remove large model files from git history
cd /d %~dp0..
python -m git_filter_repo --path models/ --invert-paths --force

echo.
echo Cleanup complete!
pause
