param(
    [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"
$distDir = Join-Path $PSScriptRoot "dist\\linux"
$stagingDir = Join-Path $PSScriptRoot "staging\\linux"
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null

$bundleRoot = Join-Path $distDir ("forgex-linux-bundle-{0}" -f $Version)
if (Test-Path $bundleRoot) {
    Remove-Item -LiteralPath $bundleRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $bundleRoot | Out-Null

Copy-Item -Recurse -Force (Join-Path $stagingDir "*") $bundleRoot

$zipPath = Join-Path $distDir ("forgex-linux-bundle-{0}.zip" -f $Version)
if (Test-Path $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
Compress-Archive -Path (Join-Path $bundleRoot "*") -DestinationPath $zipPath

$tarGzPath = Join-Path $distDir ("forgex-linux-bundle-{0}.tar.gz" -f $Version)
if (Test-Path $tarGzPath) {
    Remove-Item -LiteralPath $tarGzPath -Force
}

Push-Location $distDir
try {
    tar -czf ("forgex-linux-bundle-{0}.tar.gz" -f $Version) ("forgex-linux-bundle-{0}" -f $Version)
} finally {
    Pop-Location
}

Write-Host "Linux 部署包骨架已生成。版本: $Version"
