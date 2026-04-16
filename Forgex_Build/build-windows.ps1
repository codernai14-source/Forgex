param(
    [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"
$distDir = Join-Path $PSScriptRoot "dist\\windows"
$stagingDir = Join-Path $PSScriptRoot "staging\\windows"
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null

$packageRoot = Join-Path $distDir ("Forgex-Windows-Package-{0}" -f $Version)
if (Test-Path $packageRoot) {
    Remove-Item -LiteralPath $packageRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $packageRoot | Out-Null

Copy-Item -Recurse -Force (Join-Path $stagingDir "*") $packageRoot
Copy-Item -Force (Join-Path $PSScriptRoot "delivery\\windows\\installer\\ForgexSetup.iss") (Join-Path $packageRoot "ForgexSetup.iss")
Copy-Item -Recurse -Force (Join-Path $PSScriptRoot "delivery\\windows\\installer\\winsw") (Join-Path $packageRoot "winsw")

$zipPath = Join-Path $distDir ("Forgex-Windows-Package-{0}.zip" -f $Version)
if (Test-Path $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
Compress-Archive -Path (Join-Path $packageRoot "*") -DestinationPath $zipPath

Write-Host "Windows 构建骨架已生成，可使用 Inno Setup 编译安装包。版本: $Version"
