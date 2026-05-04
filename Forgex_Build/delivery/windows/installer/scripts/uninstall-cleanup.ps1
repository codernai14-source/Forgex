param(
    [string]$InstallRoot = ""
)

$ErrorActionPreference = "Continue"

function Resolve-InstallRoot {
    if (-not [string]::IsNullOrWhiteSpace($InstallRoot)) {
        return [System.IO.Path]::GetFullPath($InstallRoot)
    }

    $scriptRoot = Split-Path -Parent $PSCommandPath
    return [System.IO.Path]::GetFullPath((Join-Path $scriptRoot ".."))
}

function Test-IsUnderPath {
    param(
        [string]$Path,
        [string]$Root
    )

    if ([string]::IsNullOrWhiteSpace($Path) -or [string]::IsNullOrWhiteSpace($Root)) {
        return $false
    }

    try {
        $fullPath = [System.IO.Path]::GetFullPath($Path).TrimEnd('\')
        $fullRoot = [System.IO.Path]::GetFullPath($Root).TrimEnd('\')
        return $fullPath.Equals($fullRoot, [System.StringComparison]::OrdinalIgnoreCase) -or
            $fullPath.StartsWith("$fullRoot\", [System.StringComparison]::OrdinalIgnoreCase)
    } catch {
        return $false
    }
}

function Get-ControlConfigServiceNames {
    param(
        [string]$Root
    )

    $configPath = Join-Path $Root "config\forgex-control.json"
    if (-not (Test-Path -LiteralPath $configPath)) {
        return @()
    }

    try {
        $config = Get-Content -Raw -Encoding UTF8 -LiteralPath $configPath | ConvertFrom-Json
        return @($config.services | ForEach-Object { [string]$_.serviceName } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    } catch {
        return @()
    }
}

function Stop-MatchingServices {
    param(
        [string]$Root
    )

    $configuredNames = @(Get-ControlConfigServiceNames -Root $Root)
    $serviceQuery = Get-CimInstance Win32_Service -ErrorAction SilentlyContinue | Where-Object {
        $configuredNames -contains $_.Name -or
        ($_.PathName -and $_.PathName.IndexOf($Root, [System.StringComparison]::OrdinalIgnoreCase) -ge 0)
    }

    foreach ($service in @($serviceQuery)) {
        try {
            if ($service.State -ne "Stopped") {
                Stop-Service -Name $service.Name -Force -ErrorAction SilentlyContinue
                (Get-Service -Name $service.Name -ErrorAction SilentlyContinue).WaitForStatus("Stopped", [TimeSpan]::FromSeconds(30))
            }
        } catch {
            Write-Host "Stop service failed: $($service.Name). $($_.Exception.Message)"
        }
    }
}

function Uninstall-Wrappers {
    param(
        [string]$Root
    )

    $wrapperDir = Join-Path $Root "services\wrappers"
    if (-not (Test-Path -LiteralPath $wrapperDir)) {
        return
    }

    foreach ($wrapper in Get-ChildItem -LiteralPath $wrapperDir -Filter "*.exe" -File -ErrorAction SilentlyContinue) {
        try {
            & $wrapper.FullName uninstall | Out-Host
        } catch {
            Write-Host "WinSW uninstall failed: $($wrapper.FullName). $($_.Exception.Message)"
        }
    }
}

function Remove-MatchingServices {
    param(
        [string]$Root
    )

    $configuredNames = @(Get-ControlConfigServiceNames -Root $Root)
    $serviceQuery = Get-CimInstance Win32_Service -ErrorAction SilentlyContinue | Where-Object {
        $configuredNames -contains $_.Name -or
        ($_.PathName -and $_.PathName.IndexOf($Root, [System.StringComparison]::OrdinalIgnoreCase) -ge 0)
    }

    foreach ($service in @($serviceQuery)) {
        try {
            & sc.exe delete $service.Name | Out-Host
        } catch {
            Write-Host "Service delete failed: $($service.Name). $($_.Exception.Message)"
        }
    }
}

function Stop-MatchingProcesses {
    param(
        [string]$Root
    )

    $currentPid = $PID
    $processes = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | Where-Object {
        $_.ProcessId -ne $currentPid -and
        $_.Name -notlike "unins*.exe" -and
        $_.Name -notin @("cmd.exe", "powershell.exe", "powershell_ise.exe", "pwsh.exe") -and
        (
            ($_.ExecutablePath -and (Test-IsUnderPath -Path $_.ExecutablePath -Root $Root)) -or
            ($_.CommandLine -and $_.CommandLine.IndexOf($Root, [System.StringComparison]::OrdinalIgnoreCase) -ge 0)
        )
    }

    foreach ($process in @($processes)) {
        try {
            Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
            Write-Host "Stopped process: $($process.ProcessId) $($process.Name)"
        } catch {
            Write-Host "Stop process failed: $($process.ProcessId). $($_.Exception.Message)"
        }
    }
}

function Remove-StaleRuntimeDirectories {
    param(
        [string]$Root
    )

    $targets = @(
        (Join-Path $Root "app\jre"),
        (Join-Path $Root "app"),
        (Join-Path $Root "services\wrappers")
    )

    foreach ($target in $targets) {
        if ((Test-IsUnderPath -Path $target -Root $Root) -and (Test-Path -LiteralPath $target)) {
            try {
                Remove-Item -LiteralPath $target -Recurse -Force -ErrorAction Stop
                Write-Host "Removed: $target"
            } catch {
                Write-Host "Remove failed: $target. $($_.Exception.Message)"
            }
        }
    }
}

$resolvedInstallRoot = Resolve-InstallRoot
Write-Host "Forgex uninstall cleanup: $resolvedInstallRoot"

Stop-MatchingServices -Root $resolvedInstallRoot
Uninstall-Wrappers -Root $resolvedInstallRoot
Remove-MatchingServices -Root $resolvedInstallRoot
Stop-MatchingProcesses -Root $resolvedInstallRoot
Start-Sleep -Seconds 2
Remove-StaleRuntimeDirectories -Root $resolvedInstallRoot

Write-Host "Forgex uninstall cleanup completed."
