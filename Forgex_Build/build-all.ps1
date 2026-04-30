param(
    [string]$Version = "1.0.0",
    [string]$WindowsJreHome = "",
    [switch]$AllowDistFallback,
    [switch]$CompileWindowsInstaller
)

$ErrorActionPreference = "Stop"

& "$PSScriptRoot\collect-artifacts.ps1" -Version $Version -WindowsJreHome $WindowsJreHome -AllowDistFallback:$AllowDistFallback
& "$PSScriptRoot\build-windows.ps1" -Version $Version -CompileInstaller:$CompileWindowsInstaller
& "$PSScriptRoot\build-linux.ps1" -Version $Version
