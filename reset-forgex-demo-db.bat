@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ============================================================================
rem Forgex demo database reset script
rem
rem Usage:
rem   reset-forgex-demo-db.bat /run         Reset databases from .\forgex\*.sql
rem   reset-forgex-demo-db.bat /register    Register Windows task at 00:00 daily
rem   reset-forgex-demo-db.bat /unregister  Delete the Windows task
rem
rem Put Navicat-exported full database SQL files next to this script:
rem   .\forgex\forgex_admin.sql
rem   .\forgex\forgex_common.sql
rem   .\forgex\forgex_history.sql
rem   .\forgex\forgex_integration.sql
rem   .\forgex\forgex_job.sql
rem   .\forgex\forgex_scada.sql
rem   .\forgex\forgex_workflow.sql
rem
rem The database name is taken from the SQL file name, without ".sql".
rem ============================================================================

set "MYSQL_HOST=127.0.0.1"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
set "MYSQL_PASSWORD=123456"
set "MYSQL_CHARSET=utf8mb4"
set "MYSQL_COLLATION=utf8mb4_unicode_ci"
set "MYSQL_EXE=mysql.exe"
set "SQL_DIR=%~dp0forgex"
set "LOG_DIR=%~dp0logs"
set "TASK_NAME=Forgex Demo Database Reset"
set "TASK_TIME=00:00"

if /I "%~1"=="/register" goto :register
if /I "%~1"=="register" goto :register
if /I "%~1"=="/unregister" goto :unregister
if /I "%~1"=="unregister" goto :unregister
if /I "%~1"=="/run" goto :run
if /I "%~1"=="run" goto :run
if "%~1"=="" goto :run
goto :usage

:register
call :ensure_dirs
set "SCRIPT_PATH=%~f0"
echo Registering Windows scheduled task: %TASK_NAME%
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$action = New-ScheduledTaskAction -Execute 'cmd.exe' -Argument ('/c ""{0}"" /run' -f $env:SCRIPT_PATH);" ^
  "$trigger = New-ScheduledTaskTrigger -Daily -At ([datetime]::ParseExact($env:TASK_TIME, 'HH:mm', $null));" ^
  "Register-ScheduledTask -TaskName $env:TASK_NAME -Action $action -Trigger $trigger -Force | Out-Null"
if errorlevel 1 (
    echo Failed to register task. Try running this script as Administrator.
    exit /b 1
)
echo Registered. It will run every day at %TASK_TIME%.
exit /b 0

:unregister
echo Deleting Windows scheduled task: %TASK_NAME%
schtasks /Delete /TN "%TASK_NAME%" /F
exit /b %errorlevel%

:run
call :ensure_dirs
set "SCRIPT_PATH=%~f0"
call :find_mysql
if errorlevel 1 exit /b 1

call :set_log_file
call :log ============================================================
call :log Forgex demo database reset started at %DATE% %TIME%
call :log SQL directory: %SQL_DIR%
call :log MySQL: %MYSQL_EXE%

if not exist "%SQL_DIR%\" (
    call :log ERROR: SQL directory does not exist: %SQL_DIR%
    exit /b 1
)

set "SQL_COUNT=0"
for %%F in ("%SQL_DIR%\*.sql") do (
    if exist "%%~fF" (
        set /a SQL_COUNT+=1
    )
)

if "%SQL_COUNT%"=="0" (
    call :log ERROR: No .sql files found in %SQL_DIR%.
    call :log Put Navicat exported files such as forgex_admin.sql in this directory.
    exit /b 1
)

set "FAILED=0"
for %%F in ("%SQL_DIR%\*.sql") do (
    if exist "%%~fF" (
        call :reset_one "%%~fF"
        if errorlevel 1 set "FAILED=1"
    )
)

if "%FAILED%"=="1" (
    call :log Forgex demo database reset finished with errors at %DATE% %TIME%
    exit /b 1
)

call :log Forgex demo database reset finished successfully at %DATE% %TIME%
exit /b 0

:reset_one
set "SQL_FILE=%~1"
set "DB_NAME=%~n1"
call :log ------------------------------------------------------------
call :log Resetting database: %DB_NAME%
call :log SQL file: %SQL_FILE%

"%MYSQL_EXE%" -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=%MYSQL_CHARSET% -e "SET SESSION sql_log_bin=0; DROP DATABASE IF EXISTS \`%DB_NAME%\`; CREATE DATABASE \`%DB_NAME%\` DEFAULT CHARACTER SET %MYSQL_CHARSET% COLLATE %MYSQL_COLLATION%;" >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    call :log ERROR: Failed to recreate database %DB_NAME%.
    exit /b 1
)

"%MYSQL_EXE%" -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=%MYSQL_CHARSET% --max_allowed_packet=1G "%DB_NAME%" < "%SQL_FILE%" >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    call :log ERROR: Failed to import %SQL_FILE% into %DB_NAME%.
    exit /b 1
)

call :log OK: %DB_NAME% imported.
exit /b 0

:find_mysql
where "%MYSQL_EXE%" >nul 2>nul
if not errorlevel 1 exit /b 0

if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    exit /b 0
)

call :log ERROR: mysql.exe was not found. Add MySQL bin to PATH or set MYSQL_EXE in this script.
exit /b 1

:ensure_dirs
if not exist "%LOG_DIR%\" mkdir "%LOG_DIR%" >nul 2>nul
exit /b 0

:set_log_file
for /f %%D in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Date -Format yyyyMMdd"') do set "LOG_DATE=%%D"
if not defined LOG_DATE set "LOG_DATE=latest"
set "LOG_FILE=%LOG_DIR%\reset-forgex-demo-db-%LOG_DATE%.log"
exit /b 0

:log
echo %*
if defined LOG_FILE echo %*>> "%LOG_FILE%"
exit /b 0

:usage
echo Usage:
echo   %~nx0 /run
echo   %~nx0 /register
echo   %~nx0 /unregister
exit /b 1
