param(
    [string]$Version = "1.0.0",
    [string]$WindowsJreHome = "",
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

function Resolve-WindowsJreHome {
    param(
        [string]$ConfiguredHome
    )

    if (-not [string]::IsNullOrWhiteSpace($ConfiguredHome) -and (Test-Path -LiteralPath $ConfiguredHome)) {
        return $ConfiguredHome
    }

    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME) -and (Test-Path -LiteralPath $env:JAVA_HOME)) {
        return $env:JAVA_HOME
    }

    $java = Get-Command "java.exe" -ErrorAction SilentlyContinue
    if ($null -eq $java) {
        return ""
    }

    try {
        $candidateHome = Split-Path (Split-Path $java.Source -Parent) -Parent
        if (-not [string]::IsNullOrWhiteSpace($candidateHome) -and (Test-Path -LiteralPath $candidateHome)) {
            return $candidateHome
        }
    } catch {
    }

    try {
        $javaSettingsOutput = (& $java.Source -XshowSettings:properties -version) 2>&1 | Out-String
        $javaHomeLine = $javaSettingsOutput -split "`r?`n" |
            Where-Object { $_ -match '^\s*java\.home\s*=' } |
            Select-Object -First 1

        if ($null -eq $javaHomeLine) {
            return ""
        }

        $candidateHome = ($javaHomeLine -split '=', 2)[1].Trim()
        if ([string]::IsNullOrWhiteSpace($candidateHome)) {
            return ""
        }

        if (Test-Path -LiteralPath $candidateHome) {
            return $candidateHome
        }
    } catch {
        return ""
    }

    return ""
}

function Publish-LicenseRequestClient {
    param(
        [string]$ProjectPath,
        [string]$RuntimeIdentifier,
        [string]$PublishDirectory,
        [string]$ExpectedBinaryName
    )

    if (-not (Test-Path -LiteralPath $ProjectPath)) {
        throw "License request client project not found: $ProjectPath"
    }

    if (Test-Path -LiteralPath $PublishDirectory) {
        Remove-Item -LiteralPath $PublishDirectory -Recurse -Force
    }

    & dotnet publish $ProjectPath -c Release -r $RuntimeIdentifier --self-contained false `
        -p:PublishSingleFile=true `
        -p:IncludeNativeLibrariesForSelfExtract=true `
        -p:DebugType=None `
        -p:DebugSymbols=false `
        -o $PublishDirectory | Out-Host

    if ($LASTEXITCODE -ne 0) {
        throw "dotnet publish failed for license request client: $ProjectPath ($RuntimeIdentifier)"
    }

    $expectedBinaryPath = Join-Path $PublishDirectory $ExpectedBinaryName
    if (-not (Test-Path -LiteralPath $expectedBinaryPath)) {
        throw "Published license request client binary not found: $expectedBinaryPath"
    }

    return $PublishDirectory
}

function Get-ServiceJarCount {
    param(
        [string]$Path
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return 0
    }

    return @(Get-ChildItem -LiteralPath $Path -Filter *.jar -File -ErrorAction SilentlyContinue).Count
}

$repoRoot = Split-Path $PSScriptRoot -Parent
$stagingRoot = Join-Path $PSScriptRoot "staging"
$windowsStaging = Join-Path $stagingRoot "windows"
$linuxStaging = Join-Path $stagingRoot "linux"
$sharedRoot = Join-Path $PSScriptRoot "shared"
$artifactCacheRoot = Join-Path $PSScriptRoot "artifact-cache"
$servicesCacheRoot = Join-Path $artifactCacheRoot "services"
$windowsDeliveryRoot = Join-Path $PSScriptRoot "delivery\\windows\\installer"
$linuxDeliveryRoot = Join-Path $PSScriptRoot "delivery\\linux\\scripts"
$licenseRequestClientRoot = Join-Path $PSScriptRoot "license-tools\\request-client"
$licenseRequestClientProject = Join-Path $licenseRequestClientRoot "FxLicenseRequest\\FxLicenseRequest.csproj"
$licenseRequestClientReadmePath = Join-Path $licenseRequestClientRoot "FxLicenseRequest\\README.md"
$licenseRequestClientWindowsPublishDir = Join-Path $licenseRequestClientRoot "publish\\win-x64"
$licenseRequestClientLinuxPublishDir = Join-Path $licenseRequestClientRoot "publish\\linux-x64"
$licensePublicKeyPath = Join-Path $PSScriptRoot "license-tools\\keys\\public-key.base64"
$controlCenterProject = Join-Path $PSScriptRoot "tools\\control-center\\ForgexControlCenter\\ForgexControlCenter.csproj"
$controlCenterPublishDir = Join-Path $PSScriptRoot "tools\\control-center\\publish\\win-x64"
$nginxTemplatePath = Join-Path $sharedRoot "nginx\\forgex.conf.template"
$docNacosConfigRoot = Join-Path $repoRoot "Forgex_Doc\\部署\\nacos配置"
$docDatabaseInitRoot = Join-Path $repoRoot "Forgex_Doc\\部署\\数据库初始化脚本"
$docDatabaseUpgradeRoot = Join-Path $repoRoot "doc\\sql\\upgrade"
$windowsNginxRuntimeRoot = Join-Path $sharedRoot "nginx\\windows"
$resolvedWindowsJreHome = Resolve-WindowsJreHome -ConfiguredHome $WindowsJreHome

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
        New-Item -ItemType Directory -Force -Path $servicesCacheRoot | Out-Null
        Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $windowsServices $jar.Name) -Force
        Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $linuxServices $jar.Name) -Force
        Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $servicesCacheRoot $jar.Name) -Force
    }
}

$windowsServices = Join-Path $windowsStaging "services"
$linuxServices = Join-Path $linuxStaging "services"
$collectedJarCount = 0
if (Test-Path -LiteralPath $windowsServices) {
    $collectedJarCount = @(Get-ChildItem -LiteralPath $windowsServices -Filter *.jar -File).Count
}

if ($collectedJarCount -lt $moduleDirs.Count) {
    $fallbackServiceSources = @(
        $servicesCacheRoot,
        (Join-Path $PSScriptRoot "scratch\\installer-smoke\\services"),
        (Join-Path $PSScriptRoot "dist\\windows\\Forgex-Windows-Package-$Version\\services"),
        (Join-Path $PSScriptRoot "dist\\linux\\forgex-linux-bundle-$Version\\services")
    )

    $resolvedFallbackServices = $fallbackServiceSources |
        Where-Object { (Get-ServiceJarCount -Path $_) -ge $moduleDirs.Count } |
        Select-Object -First 1

    if ($AllowDistFallback -and -not [string]::IsNullOrWhiteSpace($resolvedFallbackServices)) {
        Copy-DirectoryContents -Source $resolvedFallbackServices -Destination $windowsServices
        Copy-DirectoryContents -Source $resolvedFallbackServices -Destination $linuxServices
        New-Item -ItemType Directory -Force -Path $servicesCacheRoot | Out-Null
        Copy-DirectoryContents -Source $resolvedFallbackServices -Destination $servicesCacheRoot
        Write-Host "Backend jars were loaded from fallback source: $resolvedFallbackServices"
    } else {
        $fallbackSummary = $fallbackServiceSources |
            ForEach-Object { "{0} ({1} jars)" -f $_, (Get-ServiceJarCount -Path $_) }
        throw "Backend jars are missing. Build backend modules first, or rerun with -AllowDistFallback to reuse one of: $($fallbackSummary -join '; ')."
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
Copy-DirectoryContents -Source $docNacosConfigRoot -Destination (Join-Path $windowsStaging "nacos")
Copy-DirectoryContents -Source $docNacosConfigRoot -Destination (Join-Path $linuxStaging "nacos")
Copy-DirectoryContents -Source $docDatabaseInitRoot -Destination (Join-Path $windowsStaging "database-init")
Copy-DirectoryContents -Source $docDatabaseInitRoot -Destination (Join-Path $linuxStaging "database-init")
Copy-DirectoryContents -Source $docDatabaseUpgradeRoot -Destination (Join-Path $windowsStaging "database-upgrade")
Copy-DirectoryContents -Source $docDatabaseUpgradeRoot -Destination (Join-Path $linuxStaging "database-upgrade")
if (Test-Path -LiteralPath $windowsNginxRuntimeRoot) {
    Copy-DirectoryContents -Source $windowsNginxRuntimeRoot -Destination (Join-Path $windowsStaging "nginx")
}
if ([string]::IsNullOrWhiteSpace($resolvedWindowsJreHome)) {
    throw "Windows JRE home could not be resolved. Install Java locally or pass -WindowsJreHome <path>."
}

$resolvedWindowsJavaExe = Join-Path $resolvedWindowsJreHome "bin\\java.exe"
if (-not (Test-Path -LiteralPath $resolvedWindowsJavaExe)) {
    throw "Resolved Windows JRE does not contain bin\\java.exe: $resolvedWindowsJavaExe"
}

Copy-DirectoryContents -Source $resolvedWindowsJreHome -Destination (Join-Path $windowsStaging "app\\jre")
if (Test-Path -LiteralPath $nginxTemplatePath) {
    New-Item -ItemType Directory -Force -Path (Join-Path $windowsStaging "nginx") | Out-Null
    New-Item -ItemType Directory -Force -Path (Join-Path $linuxStaging "nginx") | Out-Null
    Copy-Item -LiteralPath $nginxTemplatePath -Destination (Join-Path $windowsStaging "nginx\\forgex.conf.template") -Force
    Copy-Item -LiteralPath $nginxTemplatePath -Destination (Join-Path $linuxStaging "nginx\\forgex.conf.template") -Force
}

$publishedWindowsLicenseRequestClient = Publish-LicenseRequestClient `
    -ProjectPath $licenseRequestClientProject `
    -RuntimeIdentifier "win-x64" `
    -PublishDirectory $licenseRequestClientWindowsPublishDir `
    -ExpectedBinaryName "FxLicenseRequest.exe"

$publishedLinuxLicenseRequestClient = Publish-LicenseRequestClient `
    -ProjectPath $licenseRequestClientProject `
    -RuntimeIdentifier "linux-x64" `
    -PublishDirectory $licenseRequestClientLinuxPublishDir `
    -ExpectedBinaryName "FxLicenseRequest"

$windowsLicenseRequestClientPackageDir = Join-Path $windowsStaging "license-tools\\request-client"
$linuxLicenseRequestClientPackageDir = Join-Path $linuxStaging "license-tools\\request-client"
Copy-DirectoryContents -Source $publishedWindowsLicenseRequestClient -Destination $windowsLicenseRequestClientPackageDir
Copy-DirectoryContents -Source $publishedLinuxLicenseRequestClient -Destination $linuxLicenseRequestClientPackageDir
if (Test-Path -LiteralPath $licenseRequestClientReadmePath) {
    New-Item -ItemType Directory -Force -Path $windowsLicenseRequestClientPackageDir | Out-Null
    New-Item -ItemType Directory -Force -Path $linuxLicenseRequestClientPackageDir | Out-Null
    Copy-Item -LiteralPath $licenseRequestClientReadmePath -Destination (Join-Path $windowsLicenseRequestClientPackageDir "README.md") -Force
    Copy-Item -LiteralPath $licenseRequestClientReadmePath -Destination (Join-Path $linuxLicenseRequestClientPackageDir "README.md") -Force
}

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

if (-not (Test-Path -LiteralPath (Join-Path $windowsStaging "app\\jre\\bin\\java.exe"))) {
    throw "Windows staging is missing app\\jre\\bin\\java.exe after JRE copy."
}

Write-Host "Artifacts collected successfully. Version: $Version"
