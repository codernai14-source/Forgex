param(
    [string]$Version = "1.0.0",
    [switch]$AllowDistFallback
)

$ErrorActionPreference = "Stop"

function Reset-Directory {
    param(
        [string]$Path
    )

    if (Test-Path -LiteralPath $Path) {
        Remove-Item -LiteralPath $Path -Recurse -Force
    }

    New-Item -ItemType Directory -Force -Path $Path | Out-Null
}

function Copy-DirectoryContents {
    param(
        [string]$Source,
        [string]$Destination
    )

    if (-not (Test-Path -LiteralPath $Source)) {
        return
    }

    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    Get-ChildItem -Force -LiteralPath $Source | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $Destination -Recurse -Force
    }
}

$repoRoot = Split-Path $PSScriptRoot -Parent
$stagingRoot = Join-Path $PSScriptRoot "staging"
$windowsStaging = Join-Path $stagingRoot "windows"
$linuxStaging = Join-Path $stagingRoot "linux"
$sharedRoot = Join-Path $PSScriptRoot "shared"
$windowsDeliveryRoot = Join-Path $PSScriptRoot "delivery\\windows\\installer"
$linuxDeliveryRoot = Join-Path $PSScriptRoot "delivery\\linux\\scripts"
$licenseRequestClientRoot = Join-Path $PSScriptRoot "license-tools\\request-client"
$licensePublicKeyPath = Join-Path $PSScriptRoot "license-tools\\keys\\public-key.base64"
$controlCenterProject = Join-Path $PSScriptRoot "tools\\control-center\\ForgexControlCenter\\ForgexControlCenter.csproj"
$controlCenterPublishDir = Join-Path $PSScriptRoot "tools\\control-center\\publish\\win-x64"
$nginxTemplatePath = Join-Path $sharedRoot "nginx\\forgex.conf.template"
$windowsNginxRuntimeRoot = Join-Path $sharedRoot "nginx\\windows"

Reset-Directory -Path $windowsStaging
Reset-Directory -Path $linuxStaging

$frontendRoot = Join-Path $repoRoot "Forgex_MOM\\Forgex_Fronted"
$frontendDist = Join-Path $frontendRoot "dist"
$backendRoot = Join-Path $repoRoot "Forgex_MOM\\Forgex_Backend"

function Invoke-FrontendBuild {
    if (-not (Test-Path -LiteralPath (Join-Path $frontendRoot "package.json"))) {
        throw "Frontend project not found: $frontendRoot"
    }

    $npm = Get-Command "npm.cmd" -ErrorAction SilentlyContinue
    if ($null -eq $npm) {
        $npm = Get-Command "npm" -ErrorAction SilentlyContinue
    }

    if ($null -eq $npm) {
        throw "npm not found. Install Node.js before collecting frontend artifacts."
    }

    Push-Location $frontendRoot
    try {
        if (-not (Test-Path -LiteralPath (Join-Path $frontendRoot "node_modules"))) {
            Write-Host "Frontend node_modules not found. Running npm ci..."
            & $npm.Source ci
        }

        Write-Host "Building frontend dist..."
        & $npm.Source run build
    } finally {
        Pop-Location
    }

    $indexPath = Join-Path $frontendDist "index.html"
    if (-not (Test-Path -LiteralPath $indexPath)) {
        throw "Frontend build did not produce dist/index.html: $indexPath"
    }
}

Invoke-FrontendBuild
Copy-DirectoryContents -Source $frontendDist -Destination (Join-Path $windowsStaging "frontend")
Copy-DirectoryContents -Source $frontendDist -Destination (Join-Path $linuxStaging "frontend")

$moduleDirs = @(
    "Forgex_Gateway",
    "Forgex_Auth",
    "Forgex_Sys",
    "Forgex_Basic",
    "Forgex_Job",
    "Forgex_Integration",
    "Forgex_Workflow",
    "Forgex_Report"
)

foreach ($module in $moduleDirs) {
    $targetDir = Join-Path $backendRoot "$module\\target"
    if (-not (Test-Path -LiteralPath $targetDir)) {
        continue
    }

    $jar = Get-ChildItem -LiteralPath $targetDir -Filter *.jar -File |
        Where-Object { $_.Name -notmatch 'sources|javadoc|original' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -ne $jar) {
        $windowsServices = Join-Path $windowsStaging "services"
        $linuxServices = Join-Path $linuxStaging "services"
        New-Item -ItemType Directory -Force -Path $windowsServices | Out-Null
        New-Item -ItemType Directory -Force -Path $linuxServices | Out-Null
        Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $windowsServices $jar.Name) -Force
        Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $linuxServices $jar.Name) -Force
    }
}

$windowsServices = Join-Path $windowsStaging "services"
$linuxServices = Join-Path $linuxStaging "services"
$collectedJarCount = 0
if (Test-Path -LiteralPath $windowsServices) {
    $collectedJarCount = @(Get-ChildItem -LiteralPath $windowsServices -Filter *.jar -File).Count
}

if ($collectedJarCount -lt $moduleDirs.Count) {
    $fallbackServices = Join-Path $PSScriptRoot "dist\\windows\\Forgex-Windows-Package-$Version\\services"
    if ($AllowDistFallback -and (Test-Path -LiteralPath $fallbackServices)) {
        Copy-DirectoryContents -Source $fallbackServices -Destination $windowsServices
        Copy-DirectoryContents -Source $fallbackServices -Destination $linuxServices
        Write-Host "Backend jars were loaded from previous dist fallback: $fallbackServices"
    } else {
        throw "Backend jars are missing. Build backend modules first, or rerun with -AllowDistFallback to reuse $fallbackServices."
    }
}

Copy-DirectoryContents -Source (Join-Path $sharedRoot "config-templates") -Destination (Join-Path $windowsStaging "config-templates")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "config-templates") -Destination (Join-Path $linuxStaging "config-templates")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "license-templates") -Destination (Join-Path $windowsStaging "license")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "license-templates") -Destination (Join-Path $linuxStaging "license")
if (Test-Path -LiteralPath $licensePublicKeyPath) {
    Copy-Item -LiteralPath $licensePublicKeyPath -Destination (Join-Path $windowsStaging "license\\public-key.base64") -Force
    Copy-Item -LiteralPath $licensePublicKeyPath -Destination (Join-Path $linuxStaging "license\\public-key.base64") -Force
}
Copy-DirectoryContents -Source (Join-Path $sharedRoot "nacos") -Destination (Join-Path $windowsStaging "nacos")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "nacos") -Destination (Join-Path $linuxStaging "nacos")
if (Test-Path -LiteralPath $windowsNginxRuntimeRoot) {
    Copy-DirectoryContents -Source $windowsNginxRuntimeRoot -Destination (Join-Path $windowsStaging "nginx")
}
if (Test-Path -LiteralPath $nginxTemplatePath) {
    New-Item -ItemType Directory -Force -Path (Join-Path $windowsStaging "nginx") | Out-Null
    New-Item -ItemType Directory -Force -Path (Join-Path $linuxStaging "nginx") | Out-Null
    Copy-Item -LiteralPath $nginxTemplatePath -Destination (Join-Path $windowsStaging "nginx\\forgex.conf.template") -Force
    Copy-Item -LiteralPath $nginxTemplatePath -Destination (Join-Path $linuxStaging "nginx\\forgex.conf.template") -Force
}
Copy-DirectoryContents -Source $licenseRequestClientRoot -Destination (Join-Path $windowsStaging "license-tools\\request-client")
Copy-DirectoryContents -Source $licenseRequestClientRoot -Destination (Join-Path $linuxStaging "license-tools\\request-client")

if (Test-Path -LiteralPath $controlCenterProject) {
    if (Test-Path -LiteralPath $controlCenterPublishDir) {
        Remove-Item -LiteralPath $controlCenterPublishDir -Recurse -Force
    }

    dotnet publish $controlCenterProject -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true -o $controlCenterPublishDir
    Copy-DirectoryContents -Source $controlCenterPublishDir -Destination (Join-Path $windowsStaging "tools\\control-center")
}

Copy-DirectoryContents -Source (Join-Path $windowsDeliveryRoot "scripts") -Destination (Join-Path $windowsStaging "scripts")
Copy-DirectoryContents -Source (Join-Path $windowsDeliveryRoot "winsw") -Destination (Join-Path $windowsStaging "winsw")
Copy-DirectoryContents -Source $linuxDeliveryRoot -Destination $linuxStaging

Write-Host "Artifacts collected successfully. Version: $Version"
