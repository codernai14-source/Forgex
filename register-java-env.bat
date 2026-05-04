@echo off
setlocal EnableExtensions DisableDelayedExpansion

rem Register JAVA_HOME and Path for Windows system environment.
rem Usage:
rem   register-java-env.bat
rem   register-java-env.bat "C:\Program Files\Java\jdk-17"

title Register Java Environment

if /i "%~1"=="/?" goto :usage
if /i "%~1"=="-h" goto :usage
if /i "%~1"=="--help" goto :usage

net session >nul 2>&1
if not "%errorlevel%"=="0" (
    echo Requesting administrator privileges...
    if "%~1"=="" (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    ) else (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath '%~f0' -ArgumentList '\"%~1\"' -Verb RunAs"
    )
    exit /b
)

set "JAVA_HOME_INPUT=%~1"
set "SCRIPT_DIR=%~dp0"
set "DETECTED_JAVA_HOME="

echo Searching JDK...

for /f "usebackq delims=" %%J in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='SilentlyContinue'; function Get-JdkHome([string]$p){ if([string]::IsNullOrWhiteSpace($p)){ return $null }; $full=(Resolve-Path -LiteralPath $p -ErrorAction SilentlyContinue).Path; if(-not $full){ return $null }; if(Test-Path -LiteralPath (Join-Path $full 'bin\java.exe')){ return $full }; if($full.EndsWith('\bin') -and (Test-Path -LiteralPath (Join-Path $full 'java.exe'))){ return (Split-Path $full -Parent) }; if($full.EndsWith('java.exe')){ return (Split-Path (Split-Path $full -Parent) -Parent) }; return $null }; $homes=@(); $homes += Get-JdkHome $env:JAVA_HOME_INPUT; $homes += Get-JdkHome $env:JAVA_HOME; $base=$env:SCRIPT_DIR.TrimEnd('\'); $quick=@($base, (Join-Path $base 'jdk'), (Join-Path $base 'jre'), (Join-Path $base 'java'), (Join-Path $base 'runtime'), (Join-Path $base 'runtime\jdk'), (Join-Path $base 'tools\jdk'), 'C:\Java', 'D:\Java', 'D:\jdk'); foreach($p in $quick){ $homes += Get-JdkHome $p }; $roots=@($base,'C:\Forgex_ACME_PROD','D:\Forgex_ACME_PROD','C:\Program Files\Java','C:\Program Files\Eclipse Adoptium','C:\Program Files\Microsoft','C:\Program Files\Zulu','C:\Program Files\Amazon Corretto','C:\Program Files\BellSoft\LibericaJDK','C:\Program Files\RedHat'); foreach($root in $roots){ if(Test-Path -LiteralPath $root){ Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue | ForEach-Object { $h=Get-JdkHome $_.FullName; if($h){ $homes += $h } }; if($root -like '*Forgex*'){ Get-ChildItem -LiteralPath $root -Directory -Recurse -Depth 5 -ErrorAction SilentlyContinue | ForEach-Object { $h=Get-JdkHome $_.FullName; if($h){ $homes += $h } } } } }; $jdk=$homes | Where-Object { $_ } | Select-Object -First 1; if($jdk){ [Console]::WriteLine($jdk) }"`) do set "DETECTED_JAVA_HOME=%%J"

if not defined DETECTED_JAVA_HOME (
    echo.
    echo ERROR: java.exe was not found.
    echo Please install or unzip a JDK, then run:
    echo   %~nx0 "C:\Path\To\Your\JDK"
    echo.
    pause
    exit /b 1
)

echo Found JDK: %DETECTED_JAVA_HOME%

if not exist "%DETECTED_JAVA_HOME%\bin\java.exe" (
    echo.
    echo ERROR: java.exe does not exist:
    echo   %DETECTED_JAVA_HOME%\bin\java.exe
    echo.
    echo Please run this script with the real JDK/JRE directory:
    echo   %~nx0 "C:\Forgex_ACME_PROD\app\jre"
    echo.
    pause
    exit /b 1
)

set "JAVA_HOME=%DETECTED_JAVA_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$jdk=$env:DETECTED_JAVA_HOME; $bin=(Join-Path $jdk 'bin').TrimEnd('\'); [Environment]::SetEnvironmentVariable('JAVA_HOME',$jdk,'Machine'); $machinePath=[Environment]::GetEnvironmentVariable('Path','Machine'); if([string]::IsNullOrWhiteSpace($machinePath)){ $parts=@() } else { $parts=$machinePath -split ';' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } }; $clean=@(); foreach($p in $parts){ $item=$p.Trim().TrimEnd('\'); if($item -ine $bin -and $item -ine '%%JAVA_HOME%%\bin'){ $clean += $p } }; $newPath=(@($bin) + $clean) -join ';'; [Environment]::SetEnvironmentVariable('Path',$newPath,'Machine'); try { Add-Type -Namespace Win32 -Name NativeMethods -MemberDefinition '[DllImport(\"user32.dll\", SetLastError=true, CharSet=CharSet.Auto)] public static extern IntPtr SendMessageTimeout(IntPtr hWnd, uint Msg, UIntPtr wParam, string lParam, uint flags, uint timeout, out UIntPtr result);'; $result=[UIntPtr]::Zero; [Win32.NativeMethods]::SendMessageTimeout([IntPtr]0xffff,0x1A,[UIntPtr]::Zero,'Environment',2,5000,[ref]$result) | Out-Null } catch {}"

echo.
echo JAVA_HOME has been registered:
echo   %JAVA_HOME%
echo.
echo Current window java version:
where java
java -version
echo.
echo Done. Please reopen CMD, PowerShell, Nacos, RocketMQ, and Forgex Control Center.
pause
exit /b 0

:usage
echo Register JAVA_HOME and system Path.
echo.
echo Usage:
echo   %~nx0
echo   %~nx0 "C:\Path\To\Your\JDK"
echo.
echo Example:
echo   %~nx0 "C:\Program Files\Java\jdk-17"
exit /b 0
