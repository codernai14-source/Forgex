param([string]$BackupDir = "$env:FORGEX_HOME\backup")
$ErrorActionPreference = 'Stop'
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$target = Join-Path $BackupDir "forgex-backup-$stamp"
New-Item -ItemType Directory -Force -Path $target | Out-Null
if ($env:FORGEX_HOME -and (Test-Path "$env:FORGEX_HOME\data")) { Compress-Archive -Path "$env:FORGEX_HOME\data" -DestinationPath "$target\data.zip" -Force }
if ($env:FORGEX_HOME -and (Test-Path "$env:FORGEX_HOME\config")) { Compress-Archive -Path "$env:FORGEX_HOME\config" -DestinationPath "$target\config.zip" -Force }
Get-ChildItem $BackupDir -Directory -Filter 'forgex-backup-*' | Sort-Object Name -Descending | Select-Object -Skip 7 | Remove-Item -Recurse -Force
