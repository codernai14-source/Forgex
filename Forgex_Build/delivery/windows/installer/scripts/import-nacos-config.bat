@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%import-nacos-config.ps1" %*
if errorlevel 1 (
  echo.
  echo Nacos config import failed.
  pause
  exit /b 1
)

echo.
echo Nacos config import completed.
pause
