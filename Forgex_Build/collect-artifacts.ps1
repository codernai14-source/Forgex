param(
    [string]$Version = "1.0.0"
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

Reset-Directory -Path $windowsStaging
Reset-Directory -Path $linuxStaging

$frontendDist = Join-Path $repoRoot "Forgex_MOM\\Forgex_Fronted\\dist"
$backendRoot = Join-Path $repoRoot "Forgex_MOM\\Forgex_Backend"

if (Test-Path -LiteralPath $frontendDist) {
    Copy-DirectoryContents -Source $frontendDist -Destination (Join-Path $windowsStaging "frontend")
    Copy-DirectoryContents -Source $frontendDist -Destination (Join-Path $linuxStaging "frontend")
}

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

Copy-DirectoryContents -Source (Join-Path $sharedRoot "config-templates") -Destination (Join-Path $windowsStaging "config-templates")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "config-templates") -Destination (Join-Path $linuxStaging "config-templates")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "license-templates") -Destination (Join-Path $windowsStaging "license")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "license-templates") -Destination (Join-Path $linuxStaging "license")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "nacos") -Destination (Join-Path $windowsStaging "nacos")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "nacos") -Destination (Join-Path $linuxStaging "nacos")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "nginx") -Destination (Join-Path $windowsStaging "nginx")
Copy-DirectoryContents -Source (Join-Path $sharedRoot "nginx") -Destination (Join-Path $linuxStaging "nginx")
Copy-DirectoryContents -Source $licenseRequestClientRoot -Destination (Join-Path $windowsStaging "license-tools\\request-client")
Copy-DirectoryContents -Source $licenseRequestClientRoot -Destination (Join-Path $linuxStaging "license-tools\\request-client")
Copy-DirectoryContents -Source (Join-Path $windowsDeliveryRoot "scripts") -Destination (Join-Path $windowsStaging "scripts")
Copy-DirectoryContents -Source $linuxDeliveryRoot -Destination $linuxStaging

Write-Host "Artifacts collected successfully. Version: $Version"
