@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%import-database.ps1" %*
if errorlevel 1 (
  echo.
  echo Database import failed.
  pause
  exit /b 1
)

echo.
echo Database import completed.
pause
