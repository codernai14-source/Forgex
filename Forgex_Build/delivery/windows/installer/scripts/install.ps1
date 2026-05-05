param(
    [string]$InstanceCode = "ACME_PROD",
    [string]$InstallRoot = "",
    [string]$DeployProfile = "yanshi",
    [string]$NacosAddr = "127.0.0.1:8848",
    [string]$NacosNamespace = "",
    [string]$NacosGroup = "DEFAULT_GROUP",
    [string]$RedisAddr = "127.0.0.1:6379",
    [string]$RocketMqAddr = "127.0.0.1:9876",
    [string]$MysqlUrl = "",
    [string]$LogDir = "",
    [string]$DatasourceConfig = "datasource-forgex-dev.yml",
    [string]$IntegrationDatasourceConfig = "datasource-forgex-integration-dev.yml",
    [int]$GatewayPort = 9000,
    [int]$AuthPort = 9001,
    [int]$SysPort = 9002,
    [int]$BasicPort = 9003,
    [int]$JobPort = 9004,
    [int]$IntegrationPort = 9007,
    [int]$WorkflowPort = 9005,
    [int]$ReportPort = 8084,
    [int]$FrontendPort = 18080
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($InstallRoot)) {
    $InstallRoot = "C:\Forgex_$InstanceCode"
}

$allowedDeployProfiles = @("prod", "yanshi")
if ($DeployProfile -notin $allowedDeployProfiles) {
    throw "Windows customer installer only supports DeployProfile=prod or DeployProfile=yanshi. Current value: $DeployProfile"
}

if ([string]::IsNullOrWhiteSpace($NacosNamespace)) {
    $NacosNamespace = $DeployProfile
}

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $InstallRoot "logs"
}

$LogDir = [System.IO.Path]::GetFullPath($LogDir)

$licenseDir = Join-Path $InstallRoot "license"
$uploadDir = Join-Path $InstallRoot "data\uploads"
$logDir = $LogDir
$backupDir = Join-Path $InstallRoot "backup"
$servicesDir = Join-Path $InstallRoot "services"
$frontendDir = Join-Path $InstallRoot "frontend"
$configDir = Join-Path $InstallRoot "config"
$toolsDir = Join-Path $InstallRoot "tools"
$nginxDir = Join-Path $InstallRoot "nginx"
$nginxConfPath = Join-Path $nginxDir "forgex.conf"
$serviceStateDir = Join-Path $InstallRoot "data\service-state"

$dirs = @(
    "config",
    "data",
    "data\uploads",
    "data\service-state",
    "license",
    "database-init",
    "scripts",
    "backup",
    "tools",
    "services",
    "frontend",
    "nginx",
    "nginx\temp",
    "nginx\temp\client_body_temp",
    "nginx\temp\proxy_temp",
    "nginx\temp\fastcgi_temp",
    "nginx\temp\uwsgi_temp",
    "nginx\temp\scgi_temp"
)

foreach ($dir in $dirs) {
    $fullPath = Join-Path $InstallRoot $dir
    if (-not (Test-Path -LiteralPath $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath | Out-Null
    }
}

$runtimeDirs = @(
    $logDir,
    (Join-Path $logDir "nginx")
)

foreach ($dir in $runtimeDirs) {
    if (-not (Test-Path -LiteralPath $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
}

function Resolve-JarPath {
    param(
        [string]$Pattern
    )

    $jar = Get-ChildItem -LiteralPath $servicesDir -Filter $Pattern -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $jar) {
        return ""
    }

    return $jar.FullName
}

function New-ServiceConfig {
    param(
        [string]$ServiceId,
        [string]$DisplayName,
        [string]$JarPattern,
        [int]$Port,
        [int]$StartOrder
    )

    $serviceName = "forgex-$ServiceId-$InstanceCode".ToLowerInvariant()
    $safeInstanceCode = $InstanceCode.ToLowerInvariant()

    [ordered]@{
        serviceId = $ServiceId
        displayName = $DisplayName
        serviceName = $serviceName
        jarPath = Resolve-JarPath -Pattern $JarPattern
        port = $Port
        startOrder = $StartOrder
        workingDirectory = $InstallRoot
        logDir = Join-Path $logDir $ServiceId
        pidFile = Join-Path $serviceStateDir "$ServiceId.pid"
        wrapperExePath = Join-Path $servicesDir "wrappers\$serviceName.exe"
        wrapperXmlPath = Join-Path $servicesDir "wrappers\$serviceName.xml"
        instanceCode = $safeInstanceCode
    }
}

$services = @(
    New-ServiceConfig -ServiceId "auth" -DisplayName "Forgex Auth" -JarPattern "Forgex_Auth*.jar" -Port $AuthPort -StartOrder 10
    New-ServiceConfig -ServiceId "sys" -DisplayName "Forgex Sys" -JarPattern "Forgex_Sys*.jar" -Port $SysPort -StartOrder 20
    New-ServiceConfig -ServiceId "basic" -DisplayName "Forgex Basic" -JarPattern "Forgex_Basic*.jar" -Port $BasicPort -StartOrder 30
    New-ServiceConfig -ServiceId "job" -DisplayName "Forgex Job" -JarPattern "Forgex_Job*.jar" -Port $JobPort -StartOrder 40
    New-ServiceConfig -ServiceId "workflow" -DisplayName "Forgex Workflow" -JarPattern "Forgex_Workflow*.jar" -Port $WorkflowPort -StartOrder 50
    New-ServiceConfig -ServiceId "integration" -DisplayName "Forgex Integration" -JarPattern "Forgex_Integration*.jar" -Port $IntegrationPort -StartOrder 60
    New-ServiceConfig -ServiceId "report" -DisplayName "Forgex Report" -JarPattern "Forgex_Report*.jar" -Port $ReportPort -StartOrder 70
    New-ServiceConfig -ServiceId "gateway" -DisplayName "Forgex Gateway" -JarPattern "Forgex_Gateway*.jar" -Port $GatewayPort -StartOrder 90
)

foreach ($service in $services) {
    if (-not (Test-Path -LiteralPath $service.logDir)) {
        New-Item -ItemType Directory -Force -Path $service.logDir | Out-Null
    }
}

if ([string]::IsNullOrWhiteSpace($MysqlUrl)) {
    $MysqlUrl = "jdbc:mysql://127.0.0.1:3306/forgex_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
}

$installConfig = @"
instanceCode: $InstanceCode
deployProfile: $DeployProfile
forgexHome: $InstallRoot
licenseDir: $licenseDir
uploadDir: $uploadDir
logDir: $logDir
backupDir: $backupDir
frontendDir: $frontendDir
servicesDir: $servicesDir
nacosAddr: $NacosAddr
nacosNamespace: $NacosNamespace
nacosGroup: $NacosGroup
redisAddr: $RedisAddr
rocketMqAddr: $RocketMqAddr
mysqlUrl: $MysqlUrl
datasourceConfig: $DatasourceConfig
integrationDatasourceConfig: $IntegrationDatasourceConfig
ports:
  gateway: $GatewayPort
  auth: $AuthPort
  sys: $SysPort
  basic: $BasicPort
  job: $JobPort
  integration: $IntegrationPort
  workflow: $WorkflowPort
  report: $ReportPort
  frontend: $FrontendPort
"@

Set-Content -Encoding UTF8 -Path (Join-Path $configDir "install-config.yml") -Value $installConfig

function Convert-ToNginxPath {
    param(
        [string]$Path
    )

    return $Path.Replace("\", "/")
}

function Write-NginxConfig {
    $templatePath = Join-Path $nginxDir "forgex.conf.template"
    if (-not (Test-Path -LiteralPath $templatePath)) {
        Write-Host "Nginx config template not found: $templatePath"
        return
    }

    $nginxConfig = Get-Content -Raw -Path $templatePath
    $nginxConfig = $nginxConfig.
        Replace("__FORGEX_HOME__", (Convert-ToNginxPath -Path $InstallRoot)).
        Replace("__FRONTEND_DIR__", (Convert-ToNginxPath -Path $frontendDir)).
        Replace("__NGINX_DIR__", (Convert-ToNginxPath -Path $nginxDir)).
        Replace("__LOG_DIR__", (Convert-ToNginxPath -Path $logDir)).
        Replace("__STATE_DIR__", (Convert-ToNginxPath -Path $serviceStateDir)).
        Replace("__FRONTEND_PORT__", [string]$FrontendPort).
        Replace("__FORGEX_GATEWAY_PORT__", [string]$GatewayPort)

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($nginxConfPath, $nginxConfig, $utf8NoBom)
}

Write-NginxConfig

$javaExe = Join-Path $InstallRoot "app\jre\bin\java.exe"
if (-not (Test-Path -LiteralPath $javaExe)) {
    $javaExe = "java.exe"
}

$controlConfig = [ordered]@{
    product = "Forgex"
    instanceCode = $InstanceCode
    deployProfile = $DeployProfile
    installRoot = $InstallRoot
    javaExe = $javaExe
    licenseDir = $licenseDir
    uploadDir = $uploadDir
    logDir = $logDir
    backupDir = $backupDir
    frontendDir = $frontendDir
    servicesDir = $servicesDir
    nginxDir = $nginxDir
    nginxConfPath = $nginxConfPath
    serviceStateDir = $serviceStateDir
    nacosAddr = $NacosAddr
    nacosNamespace = $NacosNamespace
    nacosGroup = $NacosGroup
    redisAddr = $RedisAddr
    rocketMqAddr = $RocketMqAddr
    mysqlUrl = $MysqlUrl
    datasourceConfig = $DatasourceConfig
    integrationDatasourceConfig = $IntegrationDatasourceConfig
    frontendPort = $FrontendPort
    services = $services
}

$controlConfigPath = Join-Path $configDir "forgex-control.json"
$controlConfig | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 -Path $controlConfigPath

function Resolve-WinSwSource {
    $winswRoot = Join-Path $InstallRoot "winsw"
    if (-not (Test-Path -LiteralPath $winswRoot)) {
        return $null
    }

    return Get-ChildItem -LiteralPath $winswRoot -Filter "*.exe" -File -ErrorAction SilentlyContinue |
        Sort-Object Name |
        Select-Object -First 1
}

function Install-WinSwServices {
    $winswSource = Resolve-WinSwSource
    $templatePath = Join-Path $InstallRoot "winsw\winsw-service.xml.template"
    if ($null -eq $winswSource -or -not (Test-Path -LiteralPath $templatePath)) {
        Write-Host "WinSW executable not found. Services can still be controlled by Forgex Control Center process mode."
        return
    }

    $wrapperDir = Join-Path $servicesDir "wrappers"
    New-Item -ItemType Directory -Force -Path $wrapperDir | Out-Null
    $template = Get-Content -Raw -Path $templatePath

    foreach ($service in $services) {
        if ([string]::IsNullOrWhiteSpace($service.jarPath) -or -not (Test-Path -LiteralPath $service.jarPath)) {
            Write-Host "Skip WinSW registration for $($service.serviceId), jar not found."
            continue
        }

        $wrapperExePath = $service.wrapperExePath
        $wrapperXmlPath = $service.wrapperXmlPath
        Copy-Item -LiteralPath $winswSource.FullName -Destination $wrapperExePath -Force

        $xml = $template.
            Replace("__SERVICE_ID__", $service.serviceName).
            Replace("__SERVICE_NAME__", $service.displayName).
            Replace("__FORGEX_HOME__", $InstallRoot).
            Replace("__JAVA_EXE__", $javaExe).
            Replace("__JAR_PATH__", $service.jarPath).
            Replace("__DEPLOY_PROFILE__", $DeployProfile).
            Replace("__INSTANCE_CODE__", $InstanceCode).
            Replace("__LICENSE_DIR__", $licenseDir).
            Replace("__UPLOAD_DIR__", $uploadDir).
            Replace("__LOG_DIR__", $logDir).
            Replace("__BACKUP_DIR__", $backupDir).
            Replace("__NACOS_ADDR__", $NacosAddr).
            Replace("__NACOS_NAMESPACE__", $NacosNamespace).
            Replace("__NACOS_GROUP__", $NacosGroup).
            Replace("__DATASOURCE_CONFIG__", $DatasourceConfig).
            Replace("__INTEGRATION_DATASOURCE_CONFIG__", $IntegrationDatasourceConfig).
            Replace("__REDIS_ADDR__", $RedisAddr).
            Replace("__ROCKETMQ_ADDR__", $RocketMqAddr).
            Replace("__MYSQL_URL__", $MysqlUrl).
            Replace("__SERVICE_PORT__", [string]$service.port)

        Set-Content -Encoding UTF8 -Path $wrapperXmlPath -Value $xml

        & $wrapperExePath install | Out-Host
    }
}

Install-WinSwServices

Write-Host "Forgex Windows install root initialized: $InstallRoot"
Write-Host "Deployment profile: $DeployProfile"
Write-Host "Generated install config: $(Join-Path $configDir 'install-config.yml')"
Write-Host "Generated control config: $controlConfigPath"
Write-Host "Generated nginx config: $nginxConfPath"
