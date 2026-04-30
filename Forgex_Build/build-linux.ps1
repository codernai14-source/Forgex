param(
    [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"
$distDir = Join-Path $PSScriptRoot "dist\\linux"
$stagingDir = Join-Path $PSScriptRoot "staging\\linux"
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null

$bundleName = "forgex-linux-bundle-{0}" -f $Version
$bundleRoot = Join-Path $distDir $bundleName
if (Test-Path -LiteralPath $bundleRoot) {
    Remove-Item -LiteralPath $bundleRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $bundleRoot | Out-Null

Copy-Item -Recurse -Force (Join-Path $stagingDir "*") $bundleRoot

$zipPath = Join-Path $distDir ("{0}.zip" -f $bundleName)
if (Test-Path -LiteralPath $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
Compress-Archive -Path (Join-Path $bundleRoot "*") -DestinationPath $zipPath

$tarGzPath = Join-Path $distDir ("{0}.tar.gz" -f $bundleName)
if (Test-Path -LiteralPath $tarGzPath) {
    Remove-Item -LiteralPath $tarGzPath -Force
}

$python = Get-Command "python.exe" -ErrorAction SilentlyContinue
if ($null -eq $python) {
    $python = Get-Command "python" -ErrorAction SilentlyContinue
}

if ($null -eq $python) {
    throw "python not found. Python is required to build the Linux tar.gz bundle with executable permissions."
}

$pythonScript = @'
import pathlib
import tarfile
import sys

dist_dir = pathlib.Path(sys.argv[1])
bundle_name = sys.argv[2]
bundle_root = dist_dir / bundle_name
tar_path = dist_dir / f"{bundle_name}.tar.gz"

exec_paths = {
    "install.sh",
    "upgrade.sh",
    "rollback.sh",
    "backup.sh",
    "restore.sh",
    "license-tools/request-client/FxLicenseRequest",
}

with tarfile.open(tar_path, "w:gz") as archive:
    for path in sorted(bundle_root.rglob("*")):
        relative_path = path.relative_to(bundle_root).as_posix()
        archive_path = pathlib.PurePosixPath(bundle_name) / pathlib.PurePosixPath(relative_path)
        info = archive.gettarinfo(str(path), str(archive_path))

        if info.isdir():
            info.mode = 0o755
            archive.addfile(info)
            continue

        if info.isfile():
            info.mode = 0o755 if relative_path in exec_paths else 0o644
            with path.open("rb") as handle:
                archive.addfile(info, handle)
'@

@"
$pythonScript
"@ | & $python.Source - $distDir $bundleName

if ($LASTEXITCODE -ne 0) {
    throw "Failed to build Linux tar.gz bundle."
}

Write-Host "Linux 部署包骨架已生成。版本: $Version"
