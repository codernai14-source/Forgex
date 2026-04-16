@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "TOOLS_ROOT=%SCRIPT_DIR%"
for %%I in ("%TOOLS_ROOT%..\..") do set "REPO_ROOT=%%~fI"

set "ACTIVE_PROFILE=%FORGEX_PROFILE%"
if "%ACTIVE_PROFILE%"=="" (
    set "ACTIVE_PROFILE=dev"
) else if /I not "%ACTIVE_PROFILE%"=="dev" (
    echo [Forgex] Current profile is not dev. Script stopped.
    echo [Forgex] Current FORGEX_PROFILE=%FORGEX_PROFILE%
    echo [Forgex] This script is only for local development use.
    exit /b 1
)

if /I not "%ACTIVE_PROFILE%"=="dev" (
    echo [Forgex] Active profile is not dev. Script stopped.
    exit /b 1
)

where dotnet >nul 2>nul
if errorlevel 1 (
    echo [Forgex] dotnet SDK was not found. Please install .NET SDK 9 first.
    exit /b 1
)

if "%FORGEX_LICENSE_DIR%"=="" (
    set "FORGEX_LICENSE_DIR=%REPO_ROOT%\forgex\license"
)

if "%FORGEX_INSTANCE_CODE%"=="" (
    set "FORGEX_INSTANCE_CODE=DEV_LOCAL"
)

set "DEV_KEYS_DIR=%TOOLS_ROOT%dev-local-keys"
set "REQUEST_INFO_PATH=%FORGEX_LICENSE_DIR%\request-info.json"
set "LICENSE_PATH=%FORGEX_LICENSE_DIR%\license.lic"
set "PUBLIC_KEY_PATH=%FORGEX_LICENSE_DIR%\public-key.base64"
set "GENERATOR_PROJECT=%TOOLS_ROOT%issuer\FxLicenseGenerator\FxLicenseGenerator.csproj"
set "REQUEST_PROJECT=%TOOLS_ROOT%request-client\FxLicenseRequest\FxLicenseRequest.csproj"
set "SOLUTION_PATH=%TOOLS_ROOT%FxLicenseTools.sln"

if not exist "%FORGEX_LICENSE_DIR%" (
    mkdir "%FORGEX_LICENSE_DIR%"
)

echo [Forgex] Building license tools...
dotnet build "%SOLUTION_PATH%" >nul
if errorlevel 1 (
    echo [Forgex] License tools build failed.
    exit /b 1
)

if not exist "%DEV_KEYS_DIR%\private-key.pkcs8.base64" (
    echo [Forgex] Generating local dev key pair...
    dotnet run --project "%GENERATOR_PROJECT%" -- gen-keypair --out-dir "%DEV_KEYS_DIR%"
    if errorlevel 1 (
        echo [Forgex] Failed to generate local dev key pair.
        exit /b 1
    )
)

if exist "%REQUEST_INFO_PATH%" (
    echo [Forgex] Reusing existing request-info.json to avoid machine-code mismatch.
) else (
    echo [Forgex] Generating development request-info.json...
    dotnet run --project "%REQUEST_PROJECT%" -- generate-request --output "%REQUEST_INFO_PATH%" --instance-code "%FORGEX_INSTANCE_CODE%" --edition dev
    if errorlevel 1 (
        echo [Forgex] Failed to generate request-info.json.
        exit /b 1
    )
)

echo [Forgex] Issuing local development license...
dotnet run --project "%GENERATOR_PROJECT%" -- issue --request-info "%REQUEST_INFO_PATH%" --private-key "%DEV_KEYS_DIR%\private-key.pkcs8.base64" --output "%LICENSE_PATH%" --customer-name "Forgex Development" --edition dev --modules gateway,auth,sys,basic,job,integration,workflow,report --duration-days 3650 --remark "DEV_ONLY_LOCAL_LICENSE"
if errorlevel 1 (
    echo [Forgex] Failed to issue development license.
    exit /b 1
)

echo [Forgex] Importing development license...
dotnet run --project "%REQUEST_PROJECT%" -- import-license --license-file "%LICENSE_PATH%" --target-dir "%FORGEX_LICENSE_DIR%"
if errorlevel 1 (
    echo [Forgex] Failed to import development license.
    exit /b 1
)

set /p FORGEX_LICENSE_PUBLIC_KEY=<"%DEV_KEYS_DIR%\public-key.base64"
set "FORGEX_LICENSE_ENABLED=true"
copy /Y "%DEV_KEYS_DIR%\public-key.base64" "%PUBLIC_KEY_PATH%" >nul
if errorlevel 1 (
    echo [Forgex] Failed to copy public key to license directory.
    exit /b 1
)

setx FORGEX_PROFILE "dev" >nul
setx FORGEX_INSTANCE_CODE "%FORGEX_INSTANCE_CODE%" >nul
setx FORGEX_LICENSE_ENABLED "true" >nul
setx FORGEX_LICENSE_DIR "%FORGEX_LICENSE_DIR%" >nul
setx FORGEX_LICENSE_PUBLIC_KEY "%FORGEX_LICENSE_PUBLIC_KEY%" >nul
setx FORGEX_LICENSE_PUBLIC_KEY_FILE_NAME "public-key.base64" >nul

echo.
echo [Forgex] Development license activation completed.
echo [Forgex] Active profile: dev
echo [Forgex] Instance code: %FORGEX_INSTANCE_CODE%
echo [Forgex] License directory: %FORGEX_LICENSE_DIR%
echo [Forgex] Request file: %REQUEST_INFO_PATH%
echo [Forgex] License file: %LICENSE_PATH%
echo [Forgex] Public key file: %PUBLIC_KEY_PATH%
echo [Forgex] FORGEX_PROFILE has been written as user env var: dev
echo [Forgex] FORGEX_LICENSE_PUBLIC_KEY has been written to user env vars.
echo [Forgex] Restart Gateway and Sys services before login.

exit /b 0
