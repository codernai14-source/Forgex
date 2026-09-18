<#!
.SYNOPSIS
Runs the local equivalent of the repository verification gate.
#>
param()

$ErrorActionPreference = 'Stop'

function Invoke-VerificationStep {
    param(
        [string]$Name,
        [string]$WorkingDirectory,
        [scriptblock]$Command
    )

    Write-Host "==> $Name"
    Push-Location $WorkingDirectory
    try {
        & $Command
        if ($LASTEXITCODE -ne 0) {
            throw "$Name failed with exit code $LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
}

$repoRoot = $PSScriptRoot
$backendRoot = Join-Path $repoRoot 'Forgex_MOM\Forgex_Backend'
$frontendRoot = Join-Path $repoRoot 'Forgex_MOM\Forgex_Fronted'
$androidRoot = Join-Path $repoRoot 'Forgex_MOM\Forgex_Mobile_Android'

Invoke-VerificationStep 'backend tests' $backendRoot { mvn -q test }
Invoke-VerificationStep 'backend module boundaries' $backendRoot {
    powershell -ExecutionPolicy Bypass -File scripts\verify-common-module-boundaries.ps1
}
Invoke-VerificationStep 'frontend build' $frontendRoot { npm run build }
Invoke-VerificationStep 'frontend i18n audit' $frontendRoot { npm run i18n:audit }
Invoke-VerificationStep 'frontend tests' $frontendRoot { npm test }
Invoke-VerificationStep 'doc freshness tests' $repoRoot { node --test tools\doc-freshness\tests\doc-freshness.test.mjs }
Invoke-VerificationStep 'doc freshness' $repoRoot { node tools\doc-freshness\doc-freshness.mjs }
$androidSdk = $env:ANDROID_HOME
if ([string]::IsNullOrWhiteSpace($androidSdk)) {
    $androidSdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}
if (Test-Path -LiteralPath $androidSdk) {
    $env:ANDROID_HOME = $androidSdk
    $env:ANDROID_SDK_ROOT = $androidSdk
}
Invoke-VerificationStep 'android network tests and compile' $androidRoot {
    .\gradlew.bat :core:network:testDebugUnitTest :core:network:compileDebugKotlin :feature:auth:testDebugUnitTest
}
