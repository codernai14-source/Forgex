param(
    [string]$InstallRoot = $PSScriptRoot | Split-Path -Parent
)

$ErrorActionPreference = "Stop"

$controlCenter = Join-Path $InstallRoot "tools\control-center\ForgexControlCenter.exe"
if (-not (Test-Path -LiteralPath $controlCenter)) {
    throw "Forgex Control Center not found: $controlCenter"
}

& $controlCenter --install-root $InstallRoot --start-all
