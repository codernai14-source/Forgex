param(
    [string]$InstallRoot = "",
    [string]$PackageRoot = "",
    [switch]$SkipStop,
    [switch]$SkipStart,
    [switch]$NoBackup
)

$ErrorActionPreference = "Stop"

function Resolve-InstallRoot {
    if (-not [string]::IsNullOrWhiteSpace($InstallRoot)) {
        return [System.IO.Path]::GetFullPath($InstallRoot)
    }

    $scriptRoot = Split-Path -Parent $PSCommandPath
    $scriptInstallRoot = [System.IO.Path]::GetFullPath((Join-Path $scriptRoot ".."))
    if (Test-ForgexInstallRoot -Root $scriptInstallRoot) {
        return $scriptInstallRoot
    }

    $candidates = @(Find-ForgexInstallRoots)
    if ($candidates.Count -eq 1) {
        Write-Host "Auto detected Forgex install root: $($candidates[0])"
        return $candidates[0]
    }

    if ($candidates.Count -gt 1) {
        $candidateText = ($candidates | ForEach-Object { "  $_" }) -join [Environment]::NewLine
        throw "Multiple Forgex install roots found. Please pass -InstallRoot explicitly:$([Environment]::NewLine)$candidateText"
    }

    throw "Forgex install root was not found. Please pass -InstallRoot explicitly."
}

function Resolve-PackageRoot {
    if (-not [string]::IsNullOrWhiteSpace($PackageRoot)) {
        return [System.IO.Path]::GetFullPath($PackageRoot)
    }

    $scriptRoot = Split-Path -Parent $PSCommandPath
    return [System.IO.Path]::GetFullPath((Join-Path $scriptRoot ".."))
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

function Get-LockingProcesses {
    param(
        [string]$Path
    )

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) {
        return @()
    }

    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    $result = New-Object System.Collections.Generic.List[object]
    Get-Process -ErrorAction SilentlyContinue | ForEach-Object {
        try {
            if ($null -ne $_.Path -and [System.StringComparer]::OrdinalIgnoreCase.Equals([System.IO.Path]::GetFullPath($_.Path), $resolvedPath)) {
                $result.Add($_)
            }
        } catch {
        }
    }

    return $result.ToArray()
}

function Wait-FileUnlocked {
    param(
        [string]$Path,
        [int]$TimeoutSeconds = 60
    )

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) {
        return
    }

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $stream = $null
        try {
            $stream = [System.IO.File]::Open($Path, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
            return
        } catch [System.IO.IOException] {
            $lockingProcesses = @(Get-LockingProcesses -Path $Path)
            if ($lockingProcesses.Count -gt 0) {
                $processText = ($lockingProcesses | ForEach-Object { "$($_.ProcessName)(pid:$($_.Id))" }) -join ", "
                Write-Host "Waiting for file to be released: $Path"
                Write-Host "Please close: $processText"
            } else {
                Write-Host "Waiting for file to be released: $Path"
            }
            Start-Sleep -Seconds 2
        } finally {
            if ($null -ne $stream) {
                $stream.Dispose()
            }
        }
    }

    throw "File is still in use after $TimeoutSeconds seconds. Please close the running program and rerun upgrade: $Path"
}

function Copy-ControlCenterDirectory {
    param(
        [string]$Source,
        [string]$Destination
    )

    if (-not (Test-Path -LiteralPath $Source)) {
        return
    }

    $targetExe = Join-Path $Destination "ForgexControlCenter.exe"
    Wait-FileUnlocked -Path $targetExe -TimeoutSeconds 60
    Copy-DirectoryContents -Source $Source -Destination $Destination
}

function Test-SamePath {
    param(
        [string]$Left,
        [string]$Right
    )

    $leftFull = [System.IO.Path]::GetFullPath($Left).TrimEnd('\', '/')
    $rightFull = [System.IO.Path]::GetFullPath($Right).TrimEnd('\', '/')
    return [System.StringComparer]::OrdinalIgnoreCase.Equals($leftFull, $rightFull)
}

function Test-ForgexInstallRoot {
    param(
        [string]$Root
    )

    if ([string]::IsNullOrWhiteSpace($Root)) {
        return $false
    }

    return Test-Path -LiteralPath (Join-Path $Root "config\forgex-control.json")
}

function Find-ForgexInstallRoots {
    $result = New-Object System.Collections.Generic.List[string]
    $seen = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)
    $drives = @(Get-PSDrive -PSProvider FileSystem -ErrorAction SilentlyContinue)

    foreach ($drive in $drives) {
        if ([string]::IsNullOrWhiteSpace($drive.Root) -or -not (Test-Path -LiteralPath $drive.Root)) {
            continue
        }

        $dirs = @(Get-ChildItem -LiteralPath $drive.Root -Directory -Filter "Forgex_*" -ErrorAction SilentlyContinue)
        foreach ($dir in $dirs) {
            if (-not (Test-ForgexInstallRoot -Root $dir.FullName)) {
                continue
            }

            $fullPath = [System.IO.Path]::GetFullPath($dir.FullName)
            if ($seen.Add($fullPath)) {
                $result.Add($fullPath)
            }
        }
    }

    return $result.ToArray()
}

function Backup-Path {
    param(
        [string]$Source,
        [string]$BackupRoot
    )

    if ($NoBackup -or -not (Test-Path -LiteralPath $Source)) {
        return
    }

    $name = Split-Path -Leaf $Source
    Copy-Item -LiteralPath $Source -Destination (Join-Path $BackupRoot $name) -Recurse -Force
}

function Set-JsonProperty {
    param(
        [object]$Object,
        [string]$Name,
        [object]$Value
    )

    if ($Object.PSObject.Properties.Name -contains $Name) {
        $Object.$Name = $Value
        return
    }

    Add-Member -InputObject $Object -NotePropertyName $Name -NotePropertyValue $Value
}

function Resolve-NewJarPath {
    param(
        [string]$PackageServicesDir,
        [string]$ServicesDir,
        [string]$ServiceId,
        [string]$JarPath
    )

    $moduleName = switch ($ServiceId) {
        "auth" { "Forgex_Auth" }
        "sys" { "Forgex_Sys" }
        "basic" { "Forgex_Basic" }
        "job" { "Forgex_Job" }
        "workflow" { "Forgex_Workflow" }
        "integration" { "Forgex_Integration" }
        "report" { "Forgex_Report" }
        "gateway" { "Forgex_Gateway" }
        default { "" }
    }

    if (-not [string]::IsNullOrWhiteSpace($moduleName) -and (Test-Path -LiteralPath $PackageServicesDir)) {
        $packageJar = Get-ChildItem -LiteralPath $PackageServicesDir -Filter "$moduleName*.jar" -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -notmatch 'sources|javadoc|original' } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        if ($null -ne $packageJar) {
            return Join-Path $ServicesDir $packageJar.Name
        }
    }

    $jarName = Split-Path -Leaf $JarPath
    if (-not [string]::IsNullOrWhiteSpace($jarName)) {
        $sameName = Join-Path $ServicesDir $jarName
        if (Test-Path -LiteralPath $sameName) {
            return $sameName
        }
    }

    $prefix = $jarName -replace '-?\d.*$', ''
    if ([string]::IsNullOrWhiteSpace($prefix)) {
        return $JarPath
    }

    $jar = Get-ChildItem -LiteralPath $ServicesDir -Filter "$prefix*.jar" -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc|original' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $jar) {
        return $JarPath
    }

    return $jar.FullName
}

function Set-WinSwJarPath {
    param(
        [string]$WrapperXmlPath,
        [string]$JarPath
    )

    if ([string]::IsNullOrWhiteSpace($WrapperXmlPath) -or -not (Test-Path -LiteralPath $WrapperXmlPath)) {
        return
    }

    $content = Get-Content -Raw -Encoding UTF8 -LiteralPath $WrapperXmlPath
    $escapedJar = [System.Security.SecurityElement]::Escape($JarPath)
    $content = $content -replace '(-jar\s+&quot;| -jar\s+")([^"&]+)(&quot;|")', "`$1$escapedJar`$3"
    [System.IO.File]::WriteAllText($WrapperXmlPath, $content, (New-Object System.Text.UTF8Encoding($false)))
}

function Invoke-ControlCenter {
    param(
        [string]$ControlCenter,
        [string]$Root,
        [string]$Argument
    )

    if (-not (Test-Path -LiteralPath $ControlCenter)) {
        Write-Host "Forgex Control Center not found, skip $Argument`: $ControlCenter"
        return
    }

    & $ControlCenter --install-root $Root $Argument | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Forgex Control Center command failed: $Argument"
    }
}

$resolvedInstallRoot = Resolve-InstallRoot
$resolvedPackageRoot = Resolve-PackageRoot
$controlConfigPath = Join-Path $resolvedInstallRoot "config\forgex-control.json"

if (-not (Test-Path -LiteralPath $controlConfigPath)) {
    throw "Forgex control config not found: $controlConfigPath"
}

if (Test-SamePath -Left $resolvedInstallRoot -Right $resolvedPackageRoot) {
    throw "PackageRoot cannot be the installed directory. Please pass the new upgrade package root, for example -PackageRoot D:\Forgex-Windows-Package-1.0.1"
}

$packageServicesDir = Join-Path $resolvedPackageRoot "services"
$packageFrontendDir = Join-Path $resolvedPackageRoot "frontend"

if (-not (Test-Path -LiteralPath $packageServicesDir) -and -not (Test-Path -LiteralPath $packageFrontendDir)) {
    throw "Upgrade package must contain services or frontend directory: $resolvedPackageRoot"
}

$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$backupRoot = Join-Path $resolvedInstallRoot "backup\upgrade-$timestamp"
if (-not $NoBackup) {
    New-Item -ItemType Directory -Force -Path $backupRoot | Out-Null
}

$controlCenter = Join-Path $resolvedInstallRoot "tools\control-center\ForgexControlCenter.exe"
if (-not $SkipStop) {
    Invoke-ControlCenter -ControlCenter $controlCenter -Root $resolvedInstallRoot -Argument "--stop-all"
}

$installServicesDir = Join-Path $resolvedInstallRoot "services"
$installFrontendDir = Join-Path $resolvedInstallRoot "frontend"

if (Test-Path -LiteralPath $packageServicesDir) {
    Backup-Path -Source $installServicesDir -BackupRoot $backupRoot
    Copy-DirectoryContents -Source $packageServicesDir -Destination $installServicesDir
}

if (Test-Path -LiteralPath $packageFrontendDir) {
    Backup-Path -Source $installFrontendDir -BackupRoot $backupRoot
    if (Test-Path -LiteralPath $installFrontendDir) {
        Remove-Item -LiteralPath $installFrontendDir -Recurse -Force
    }
    Copy-DirectoryContents -Source $packageFrontendDir -Destination $installFrontendDir
}

foreach ($dirName in @("tools\control-center", "nginx", "winsw", "scripts")) {
    $sourceDir = Join-Path $resolvedPackageRoot $dirName
    if (Test-Path -LiteralPath $sourceDir) {
        $destinationDir = Join-Path $resolvedInstallRoot $dirName
        if ($dirName -eq "tools\control-center") {
            Copy-ControlCenterDirectory -Source $sourceDir -Destination $destinationDir
        } else {
            Copy-DirectoryContents -Source $sourceDir -Destination $destinationDir
        }
    }
}

$controlConfig = Get-Content -Raw -Encoding UTF8 -LiteralPath $controlConfigPath | ConvertFrom-Json
if ($controlConfig.PSObject.Properties.Name -contains "services") {
    foreach ($service in @($controlConfig.services)) {
        if ($null -eq $service -or -not ($service.PSObject.Properties.Name -contains "jarPath")) {
            continue
        }

        $serviceId = ""
        if ($service.PSObject.Properties.Name -contains "serviceId") {
            $serviceId = [string]$service.serviceId
        }

        $jarPath = Resolve-NewJarPath -PackageServicesDir $packageServicesDir -ServicesDir $installServicesDir -ServiceId $serviceId -JarPath ([string]$service.jarPath)
        Set-JsonProperty -Object $service -Name "jarPath" -Value $jarPath
        if ($service.PSObject.Properties.Name -contains "wrapperXmlPath") {
            Set-WinSwJarPath -WrapperXmlPath ([string]$service.wrapperXmlPath) -JarPath $jarPath
        }
    }
}

[System.IO.File]::WriteAllText($controlConfigPath, ($controlConfig | ConvertTo-Json -Depth 8), (New-Object System.Text.UTF8Encoding($false)))

if (-not $SkipStart) {
    Invoke-ControlCenter -ControlCenter $controlCenter -Root $resolvedInstallRoot -Argument "--start-all"
}

Write-Host "Forgex upgrade completed."
Write-Host "InstallRoot: $resolvedInstallRoot"
if (-not $NoBackup) {
    Write-Host "BackupRoot: $backupRoot"
}
