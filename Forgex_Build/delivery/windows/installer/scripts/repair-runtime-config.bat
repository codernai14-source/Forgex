@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%repair-runtime-config.ps1" %*
if errorlevel 1 (
  echo.
  echo Runtime config repair failed.
  pause
  exit /b 1
)

echo.
echo Runtime config repair completed.
pause
