param(
    [string]$TaskName = "ForgexBackup",
    [string]$BackupScript = "",
    [string]$StartTime = "02:30"
)

# 注册 Windows 计划任务，每日调用 backup.ps1。
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($BackupScript)) {
    $BackupScript = Join-Path $PSScriptRoot "backup.ps1"
}
if (-not (Test-Path $BackupScript)) {
    throw "backup.ps1 not found: $BackupScript"
}

$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$BackupScript`""
$trigger = New-ScheduledTaskTrigger -Daily -At $StartTime
$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -AllowStartIfOnBatteries
Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Settings $settings -Force | Out-Null
Write-Host "Scheduled task $TaskName registered at $StartTime"
