param(
    [string]$InstanceCode = "ACME_PROD",
    [string]$InstallRoot = "",
    [string]$DeployProfile = "prod",
    [string]$NacosAddr = "127.0.0.1:8848",
    [string]$RedisAddr = "127.0.0.1:6379",
    [string]$MysqlUrl = "",
    [int]$GatewayPort = 9000,
    [int]$AuthPort = 9001,
    [int]$SysPort = 9002,
    [int]$BasicPort = 9003,
    [int]$JobPort = 9004,
    [int]$IntegrationPort = 9007,
    [int]$WorkflowPort = 9005,
    [int]$ReportPort = 8084
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($InstallRoot)) {
    $InstallRoot = "D:\Forgex_$InstanceCode"
}

$licenseDir = Join-Path $InstallRoot "license"
$uploadDir = Join-Path $InstallRoot "data\uploads"
$logDir = Join-Path $InstallRoot "logs"
$backupDir = Join-Path $InstallRoot "backup"

$dirs = @(
    "app",
    "config",
    "data",
    "data\uploads",
    "license",
    "logs",
    "scripts",
    "backup",
    "tools"
)

foreach ($dir in $dirs) {
    $fullPath = Join-Path $InstallRoot $dir
    if (-not (Test-Path -LiteralPath $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath | Out-Null
    }
}

$installConfig = @"
instanceCode: $InstanceCode
deployProfile: $DeployProfile
forgexHome: $InstallRoot
licenseDir: $licenseDir
uploadDir: $uploadDir
logDir: $logDir
backupDir: $backupDir
nacosAddr: $NacosAddr
redisAddr: $RedisAddr
mysqlUrl: $MysqlUrl
ports:
  gateway: $GatewayPort
  auth: $AuthPort
  sys: $SysPort
  basic: $BasicPort
  job: $JobPort
  integration: $IntegrationPort
  workflow: $WorkflowPort
  report: $ReportPort
"@

Set-Content -Encoding UTF8 -Path (Join-Path $InstallRoot "config\install-config.yml") -Value $installConfig

Write-Host "Forgex Windows install root initialized: $InstallRoot"
Write-Host "Deployment profile: $DeployProfile"
Write-Host "Generated install config: $(Join-Path $InstallRoot 'config\install-config.yml')"
