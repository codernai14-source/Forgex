param(
    [switch]$DryRun,
    [switch]$SkipReactivate,
    [string]$TargetHome
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Write-Step {
    param([string]$Message)
    Write-Host "[Forgex] $Message"
}

function Set-EnvValue {
    param(
        [string]$Name,
        [string]$Value
    )
    Set-Item -Path "Env:$Name" -Value $Value
    if (-not $DryRun) {
        [System.Environment]::SetEnvironmentVariable($Name, $Value, 'User')
    }
}

function Copy-IfExists {
    param(
        [string]$Source,
        [string]$Target
    )
    if (-not (Test-Path -LiteralPath $Source)) {
        return
    }
    $targetDir = Split-Path -Parent $Target
    if (-not (Test-Path -LiteralPath $targetDir)) {
        if ($DryRun) {
            Write-Step "DRY RUN mkdir: $targetDir"
        } else {
            New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        }
    }
    if ($DryRun) {
        Write-Step "DRY RUN copy: $Source -> $Target"
        return
    }
    Copy-Item -LiteralPath $Source -Destination $Target -Force
}

function Get-LicenseSnapshot {
    param([string]$Directory)

    $licenseFile = Join-Path $Directory 'license.lic'
    $publicKeyFile = Join-Path $Directory 'public-key.base64'
    $requestInfoFile = Join-Path $Directory 'request-info.json'
    $historyFile = Join-Path $Directory 'activation-history.json'

    $score = 0
    if ((Test-Path -LiteralPath $licenseFile) -and ((Get-Item -LiteralPath $licenseFile).Length -gt 0)) { $score += 4 }
    if ((Test-Path -LiteralPath $publicKeyFile) -and ((Get-Item -LiteralPath $publicKeyFile).Length -gt 0)) { $score += 2 }
    if ((Test-Path -LiteralPath $requestInfoFile) -and ((Get-Item -LiteralPath $requestInfoFile).Length -gt 0)) { $score += 1 }

    $lastWrite = Get-Date '2000-01-01'
    foreach ($candidate in @($licenseFile, $publicKeyFile, $requestInfoFile, $historyFile)) {
        if (Test-Path -LiteralPath $candidate) {
            $candidateTime = (Get-Item -LiteralPath $candidate).LastWriteTime
            if ($candidateTime -gt $lastWrite) {
                $lastWrite = $candidateTime
            }
        }
    }

    [PSCustomObject]@{
        Directory = $Directory
        LicenseFile = $licenseFile
        PublicKeyFile = $publicKeyFile
        RequestInfoFile = $requestInfoFile
        HistoryFile = $historyFile
        Score = $score
        LastWriteTime = $lastWrite
    }
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $scriptDir '..\..'))
$activateScript = Join-Path $scriptDir 'activate-dev-license.bat'

$processHome = [System.Environment]::GetEnvironmentVariable('FORGEX_HOME', 'Process')
$userHome = [System.Environment]::GetEnvironmentVariable('FORGEX_HOME', 'User')
$machineHome = [System.Environment]::GetEnvironmentVariable('FORGEX_HOME', 'Machine')

if ([string]::IsNullOrWhiteSpace($TargetHome) -and -not [string]::IsNullOrWhiteSpace($processHome)) {
    $TargetHome = $processHome
}
if ([string]::IsNullOrWhiteSpace($TargetHome) -and -not [string]::IsNullOrWhiteSpace($userHome)) {
    $TargetHome = $userHome
}
if ([string]::IsNullOrWhiteSpace($TargetHome) -and -not [string]::IsNullOrWhiteSpace($machineHome)) {
    $TargetHome = $machineHome
}
if ([string]::IsNullOrWhiteSpace($TargetHome)) {
    $TargetHome = Join-Path $repoRoot 'forgex'
}

$canonicalHome = [System.IO.Path]::GetFullPath($TargetHome)
$canonicalLicenseDir = Join-Path $canonicalHome 'license'

$candidateDirs = @(
    $canonicalLicenseDir,
    (Join-Path $repoRoot 'forgex\license'),
    (Join-Path $repoRoot 'Forgex_MOM\Forgex_Backend\Forgex_Gateway\forgex\license'),
    (Join-Path $repoRoot 'Forgex_MOM\Forgex_Backend\Forgex_Sys\forgex\license'),
    (Join-Path $repoRoot 'Forgex_MOM\Forgex_Backend\Forgex_Job\forgex\license'),
    'D:\forgex\forgex\license'
) | Select-Object -Unique

$source = $candidateDirs |
    ForEach-Object { Get-LicenseSnapshot -Directory $_ } |
    Sort-Object Score, LastWriteTime -Descending |
    Select-Object -First 1

Write-Step "Repository root: $repoRoot"
Write-Step "Canonical FORGEX_HOME: $canonicalHome"
Write-Step "Canonical FORGEX_LICENSE_DIR: $canonicalLicenseDir"

if ($source.Score -gt 0) {
    Write-Step "Using existing license source: $($source.Directory)"
    foreach ($pair in @(
        @{ Source = $source.LicenseFile; Target = (Join-Path $canonicalLicenseDir 'license.lic') },
        @{ Source = $source.PublicKeyFile; Target = (Join-Path $canonicalLicenseDir 'public-key.base64') },
        @{ Source = $source.RequestInfoFile; Target = (Join-Path $canonicalLicenseDir 'request-info.json') },
        @{ Source = $source.HistoryFile; Target = (Join-Path $canonicalLicenseDir 'activation-history.json') }
    )) {
        Copy-IfExists -Source $pair.Source -Target $pair.Target
    }
} elseif (-not $DryRun -and -not (Test-Path -LiteralPath $canonicalLicenseDir)) {
    New-Item -ItemType Directory -Path $canonicalLicenseDir -Force | Out-Null
}

Set-EnvValue -Name 'FORGEX_PROFILE' -Value 'dev'
Set-EnvValue -Name 'FORGEX_HOME' -Value $canonicalHome
Set-EnvValue -Name 'FORGEX_LICENSE_DIR' -Value $canonicalLicenseDir
Set-EnvValue -Name 'FORGEX_LICENSE_ENABLED' -Value 'true'
Set-EnvValue -Name 'FORGEX_LICENSE_PUBLIC_KEY_FILE_NAME' -Value 'public-key.base64'
# 清空环境变量公钥，强制运行时优先使用磁盘 public-key.base64，避免 BOM/编码污染。
Set-EnvValue -Name 'FORGEX_LICENSE_PUBLIC_KEY' -Value ''

if (-not $DryRun -and -not $SkipReactivate) {
    if (-not (Test-Path -LiteralPath $activateScript)) {
        throw "Missing activation script: $activateScript"
    }
    $dotnetCommand = Get-Command dotnet -ErrorAction SilentlyContinue
    if ($null -ne $dotnetCommand) {
        Write-Step 'Reactivating development license in canonical directory...'
        & $activateScript
        if ($LASTEXITCODE -ne 0) {
            throw "Development license activation failed with exit code $LASTEXITCODE."
        }
    } else {
        Write-Step 'dotnet SDK not found, skip reactivation and keep copied license bundle.'
    }
}

$canonicalFiles = @(
    (Join-Path $canonicalLicenseDir 'license.lic'),
    (Join-Path $canonicalLicenseDir 'public-key.base64'),
    (Join-Path $canonicalLicenseDir 'request-info.json'),
    (Join-Path $canonicalLicenseDir 'activation-history.json')
)

foreach ($targetDir in $candidateDirs) {
    if ($targetDir -eq $canonicalLicenseDir) {
        continue
    }
    foreach ($file in $canonicalFiles) {
        Copy-IfExists -Source $file -Target (Join-Path $targetDir (Split-Path -Leaf $file))
    }
}

$licenseFile = Join-Path $canonicalLicenseDir 'license.lic'
$publicKeyFile = Join-Path $canonicalLicenseDir 'public-key.base64'
$requestInfoFile = Join-Path $canonicalLicenseDir 'request-info.json'

if (-not $DryRun) {
    if (-not (Test-Path -LiteralPath $licenseFile) -or (Get-Item -LiteralPath $licenseFile).Length -le 0) {
        throw "Canonical license file is missing or empty: $licenseFile"
    }
    if (-not (Test-Path -LiteralPath $publicKeyFile) -or (Get-Item -LiteralPath $publicKeyFile).Length -le 0) {
        throw "Canonical public key file is missing or empty: $publicKeyFile"
    }
    if (-not (Test-Path -LiteralPath $requestInfoFile) -or (Get-Item -LiteralPath $requestInfoFile).Length -le 0) {
        throw "Canonical request-info file is missing or empty: $requestInfoFile"
    }

    $licenseText = (Get-Content -LiteralPath $licenseFile -Raw).Trim()
    $segments = $licenseText.Split('.')
    if ($segments.Length -ne 2) {
        throw "Canonical license text format is invalid: expected 2 segments, got $($segments.Length)."
    }
}

Write-Step 'License repair completed.'
Write-Step "License file: $licenseFile"
Write-Step "Public key: $publicKeyFile"
Write-Step "Request info: $requestInfoFile"
Write-Step 'Please restart Gateway and Sys services before logging in again.'
