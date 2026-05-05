@echo off
setlocal

if not "%~1"=="" (
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0upgrade.ps1" %*
  goto :done
)

for %%I in ("%~dp0..") do set "PACKAGE_ROOT=%%~fI"

if exist "%PACKAGE_ROOT%\config\forgex-control.json" (
  echo This script is running from an installed Forgex directory.
  set /p PACKAGE_ROOT=Please input the upgrade package root:
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0upgrade.ps1" -PackageRoot "%PACKAGE_ROOT%"

:done
if errorlevel 1 (
  echo Upgrade failed.
) else (
  echo Upgrade completed.
)
pause
endlocal
