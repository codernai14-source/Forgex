param(
    [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"

& "$PSScriptRoot\\collect-artifacts.ps1" -Version $Version
& "$PSScriptRoot\\build-windows.ps1" -Version $Version
& "$PSScriptRoot\\build-linux.ps1" -Version $Version
