param(
    [string]$InstallRoot = "",
    [string]$DeployProfile = "yanshi",
    [string]$NacosNamespace = "",
    [string]$NacosGroup = "",
    [string]$LogDir = "",
    [string]$DatasourceConfig = "datasource-forgex-dev.yml",
    [string]$IntegrationDatasourceConfig = "datasource-forgex-integration-dev.yml"
)

$ErrorActionPreference = "Stop"

function Resolve-InstallRoot {
    if (-not [string]::IsNullOrWhiteSpace($InstallRoot)) {
        return [System.IO.Path]::GetFullPath($InstallRoot)
    }

    $scriptRoot = Split-Path -Parent $PSCommandPath
    $scriptInstallRoot = [System.IO.Path]::GetFullPath((Join-Path $scriptRoot ".."))
    if (Test-ForgexInstallRoot -Root $scriptInstallRoot) {
        return $scriptInstallRoot
    }

    $candidates = @(Find-ForgexInstallRoots)
    if ($candidates.Count -eq 1) {
        Write-Host "Auto detected Forgex install root: $($candidates[0])"
        return $candidates[0]
    }

    if ($candidates.Count -gt 1) {
        $candidateText = ($candidates | ForEach-Object { "  $_" }) -join [Environment]::NewLine
        throw "Multiple Forgex install roots found. Please run this script from the installed scripts directory, or pass -InstallRoot explicitly:$([Environment]::NewLine)$candidateText"
    }

    return $scriptInstallRoot
}

function Test-ForgexInstallRoot {
    param(
        [string]$Root
    )

    if ([string]::IsNullOrWhiteSpace($Root)) {
        return $false
    }

    $controlConfigPath = Join-Path $Root "config\forgex-control.json"
    return Test-Path -LiteralPath $controlConfigPath
}

function Find-ForgexInstallRoots {
    $result = New-Object System.Collections.Generic.List[string]
    $seen = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)
    $drives = @(Get-PSDrive -PSProvider FileSystem -ErrorAction SilentlyContinue)

    foreach ($drive in $drives) {
        if ([string]::IsNullOrWhiteSpace($drive.Root) -or -not (Test-Path -LiteralPath $drive.Root)) {
            continue
        }

        $dirs = @(Get-ChildItem -LiteralPath $drive.Root -Directory -Filter "Forgex_*" -ErrorAction SilentlyContinue)
        foreach ($dir in $dirs) {
            if (-not (Test-ForgexInstallRoot -Root $dir.FullName)) {
                continue
            }

            $fullPath = [System.IO.Path]::GetFullPath($dir.FullName)
            if ($seen.Add($fullPath)) {
                $result.Add($fullPath)
            }
        }
    }

    return $result.ToArray()
}

function Set-JsonProperty {
    param(
        [object]$Object,
        [string]$Name,
        [object]$Value
    )

    if ($Object.PSObject.Properties.Name -contains $Name) {
        $Object.$Name = $Value
        return
    }

    Add-Member -InputObject $Object -NotePropertyName $Name -NotePropertyValue $Value
}

function Set-YamlScalar {
    param(
        [string]$Content,
        [string]$Name,
        [string]$Value
    )

    $escapedName = [regex]::Escape($Name)
    if ($Content -match "(?m)^$escapedName\s*:") {
        return ($Content -replace "(?m)^$escapedName\s*:.*$", "$Name`: $Value")
    }

    return $Content.TrimEnd() + [Environment]::NewLine + "$Name`: $Value" + [Environment]::NewLine
}

function ConvertTo-XmlAttributeValue {
    param(
        [string]$Value
    )

    return [System.Security.SecurityElement]::Escape($Value)
}

function Set-WrapperEnv {
    param(
        [string]$Content,
        [string]$Name,
        [string]$Value,
        [string]$InsertAfter
    )

    $escapedName = [regex]::Escape($Name)
    $escapedValue = ConvertTo-XmlAttributeValue -Value $Value
    $line = "  <env name=""$Name"" value=""$escapedValue"" />"

    if ($Content -match "<env\s+name=""$escapedName""\s+value=""[^""]*""\s*/>") {
        return ($Content -replace "<env\s+name=""$escapedName""\s+value=""[^""]*""\s*/>", $line)
    }

    $escapedAnchor = [regex]::Escape($InsertAfter)
    if ($Content -match "<env\s+name=""$escapedAnchor""\s+value=""[^""]*""\s*/>") {
        return ($Content -replace "(<env\s+name=""$escapedAnchor""\s+value=""[^""]*""\s*/>)", "`$1`r`n$line")
    }

    return ($Content -replace "</service>", "$line`r`n</service>")
}

function Resolve-ServiceIdFromWrapper {
    param(
        [string]$WrapperName,
        [string]$Content
    )

    foreach ($serviceId in @("auth", "sys", "basic", "job", "workflow", "integration", "report", "gateway")) {
        if ($WrapperName -match "(?i)(^|[-_])$serviceId($|[-_])") {
            return $serviceId
        }
    }

    if ($Content -match '(?i)Forgex_([A-Za-z]+).*?\.jar') {
        $candidate = $Matches[1].ToLowerInvariant()
        if ($candidate -eq "gateway") { return "gateway" }
        if ($candidate -eq "auth") { return "auth" }
        if ($candidate -eq "sys") { return "sys" }
        if ($candidate -eq "basic") { return "basic" }
        if ($candidate -eq "job") { return "job" }
        if ($candidate -eq "workflow") { return "workflow" }
        if ($candidate -eq "integration") { return "integration" }
        if ($candidate -eq "report") { return "report" }
    }

    return [System.IO.Path]::GetFileNameWithoutExtension($WrapperName)
}

function Set-JavaSystemPropertyArgument {
    param(
        [string]$Content,
        [string]$Name,
        [string]$Value
    )

    $escapedName = [regex]::Escape($Name)
    $argument = "-D$Name=""$Value"""

    if ($Content -match "-D$escapedName=") {
        return ($Content -replace "-D$escapedName=(?:""[^""]*""|[^\s<""]+)", $argument)
    }

    return ($Content -replace '(\s-jar\s+)', " $argument`$1")
}

if ($DeployProfile -notin @("prod", "yanshi")) {
    throw "DeployProfile must be prod or yanshi. Current value: $DeployProfile"
}

if ([string]::IsNullOrWhiteSpace($NacosNamespace)) {
    $NacosNamespace = $DeployProfile
}

$resolvedInstallRoot = Resolve-InstallRoot
if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $resolvedInstallRoot "logs"
}

$LogDir = [System.IO.Path]::GetFullPath($LogDir)

$controlConfigPath = Join-Path $resolvedInstallRoot "config\forgex-control.json"
if (-not (Test-Path -LiteralPath $controlConfigPath)) {
    throw "Forgex control config not found: $controlConfigPath"
}

$controlConfig = Get-Content -Raw -Encoding UTF8 -LiteralPath $controlConfigPath | ConvertFrom-Json
$serviceIds = @("auth", "sys", "basic", "job", "workflow", "integration", "report", "gateway")
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
foreach ($serviceId in $serviceIds) {
    New-Item -ItemType Directory -Force -Path (Join-Path $LogDir $serviceId) | Out-Null
}
New-Item -ItemType Directory -Force -Path (Join-Path $LogDir "nginx") | Out-Null

Set-JsonProperty -Object $controlConfig -Name "deployProfile" -Value $DeployProfile
Set-JsonProperty -Object $controlConfig -Name "nacosNamespace" -Value $NacosNamespace
if (-not [string]::IsNullOrWhiteSpace($NacosGroup)) {
    Set-JsonProperty -Object $controlConfig -Name "nacosGroup" -Value $NacosGroup
}
Set-JsonProperty -Object $controlConfig -Name "logDir" -Value $LogDir
Set-JsonProperty -Object $controlConfig -Name "datasourceConfig" -Value $DatasourceConfig
Set-JsonProperty -Object $controlConfig -Name "integrationDatasourceConfig" -Value $IntegrationDatasourceConfig

if ($controlConfig.PSObject.Properties.Name -contains "services") {
    foreach ($service in @($controlConfig.services)) {
        if ($null -eq $service -or -not ($service.PSObject.Properties.Name -contains "serviceId")) {
            continue
        }

        Set-JsonProperty -Object $service -Name "logDir" -Value (Join-Path $LogDir ([string]$service.serviceId))
    }
}

function Convert-ToNginxPath {
    param(
        [string]$Path
    )

    return $Path.Replace("\", "/")
}

function Get-ConfigStringValue {
    param(
        [object]$Object,
        [string]$Name,
        [string]$DefaultValue
    )

    if ($null -ne $Object -and $Object.PSObject.Properties.Name -contains $Name) {
        $value = [string]$Object.$Name
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            return $value
        }
    }

    return $DefaultValue
}

function Resolve-GatewayPort {
    param(
        [object]$Config
    )

    if ($null -ne $Config -and $Config.PSObject.Properties.Name -contains "services") {
        foreach ($service in @($Config.services)) {
            if ($null -eq $service -or -not ($service.PSObject.Properties.Name -contains "serviceId")) {
                continue
            }

            if ([string]$service.serviceId -ne "gateway") {
                continue
            }

            $port = 0
            if ($service.PSObject.Properties.Name -contains "port" -and [int]::TryParse([string]$service.port, [ref]$port) -and $port -gt 0) {
                return $port
            }
        }
    }

    return 9000
}

function Write-NginxConfig {
    param(
        [string]$Root,
        [object]$Config,
        [string]$RuntimeLogDir,
        [System.Text.Encoding]$Encoding
    )

    $nginxDir = Get-ConfigStringValue -Object $Config -Name "nginxDir" -DefaultValue (Join-Path $Root "nginx")
    $frontendDir = Get-ConfigStringValue -Object $Config -Name "frontendDir" -DefaultValue (Join-Path $Root "frontend")
    $serviceStateDir = Get-ConfigStringValue -Object $Config -Name "serviceStateDir" -DefaultValue (Join-Path $Root "data\service-state")
    $nginxConfPath = Get-ConfigStringValue -Object $Config -Name "nginxConfPath" -DefaultValue (Join-Path $nginxDir "forgex.conf")
    $gatewayPort = Resolve-GatewayPort -Config $Config

    $frontendPort = 18080
    if ($Config.PSObject.Properties.Name -contains "frontendPort") {
        [int]::TryParse([string]$Config.frontendPort, [ref]$frontendPort) | Out-Null
    }
    if ($frontendPort -le 0) {
        $frontendPort = 18080
    }

    New-Item -ItemType Directory -Force -Path $nginxDir | Out-Null
    New-Item -ItemType Directory -Force -Path $serviceStateDir | Out-Null

    Set-JsonProperty -Object $Config -Name "nginxDir" -Value $nginxDir
    Set-JsonProperty -Object $Config -Name "nginxConfPath" -Value $nginxConfPath
    Set-JsonProperty -Object $Config -Name "frontendDir" -Value $frontendDir
    Set-JsonProperty -Object $Config -Name "serviceStateDir" -Value $serviceStateDir
    Set-JsonProperty -Object $Config -Name "frontendPort" -Value $frontendPort

    $templatePath = Join-Path $nginxDir "forgex.conf.template"
    if (Test-Path -LiteralPath $templatePath) {
        $nginxConfig = Get-Content -Raw -Encoding UTF8 -LiteralPath $templatePath
        $nginxConfig = $nginxConfig.
            Replace("__FORGEX_HOME__", (Convert-ToNginxPath -Path $Root)).
            Replace("__FRONTEND_DIR__", (Convert-ToNginxPath -Path $frontendDir)).
            Replace("__NGINX_DIR__", (Convert-ToNginxPath -Path $nginxDir)).
            Replace("__LOG_DIR__", (Convert-ToNginxPath -Path $RuntimeLogDir)).
            Replace("__STATE_DIR__", (Convert-ToNginxPath -Path $serviceStateDir)).
            Replace("__FRONTEND_PORT__", [string]$frontendPort).
            Replace("__FORGEX_GATEWAY_PORT__", [string]$gatewayPort)
        $nginxConfig = $nginxConfig -replace '(proxy_pass\s+http://127\.0\.0\.1:\d+)/;', '$1;'
        [System.IO.File]::WriteAllText($nginxConfPath, $nginxConfig, $Encoding)
        Write-Host "Updated: $nginxConfPath"
        return
    }

    if (Test-Path -LiteralPath $nginxConfPath) {
        $nginxConfig = Get-Content -Raw -Encoding UTF8 -LiteralPath $nginxConfPath
        $nginxConfig = $nginxConfig -replace '(proxy_pass\s+http://127\.0\.0\.1:\d+)/;', '$1;'
        [System.IO.File]::WriteAllText($nginxConfPath, $nginxConfig, $Encoding)
        Write-Host "Patched existing nginx config: $nginxConfPath"
        return
    }

    Write-Host "Nginx config template not found: $templatePath"
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
Write-NginxConfig -Root $resolvedInstallRoot -Config $controlConfig -RuntimeLogDir $LogDir -Encoding $utf8NoBom
[System.IO.File]::WriteAllText($controlConfigPath, ($controlConfig | ConvertTo-Json -Depth 8), $utf8NoBom)
Write-Host "Updated: $controlConfigPath"

$installConfigPath = Join-Path $resolvedInstallRoot "config\install-config.yml"
if (Test-Path -LiteralPath $installConfigPath) {
    $installConfig = Get-Content -Raw -Encoding UTF8 -LiteralPath $installConfigPath
    $installConfig = Set-YamlScalar -Content $installConfig -Name "deployProfile" -Value $DeployProfile
    $installConfig = Set-YamlScalar -Content $installConfig -Name "nacosNamespace" -Value $NacosNamespace
    if (-not [string]::IsNullOrWhiteSpace($NacosGroup)) {
        $installConfig = Set-YamlScalar -Content $installConfig -Name "nacosGroup" -Value $NacosGroup
    }
    $installConfig = Set-YamlScalar -Content $installConfig -Name "logDir" -Value $LogDir
    $installConfig = Set-YamlScalar -Content $installConfig -Name "datasourceConfig" -Value $DatasourceConfig
    $installConfig = Set-YamlScalar -Content $installConfig -Name "integrationDatasourceConfig" -Value $IntegrationDatasourceConfig
    [System.IO.File]::WriteAllText($installConfigPath, $installConfig, $utf8NoBom)
    Write-Host "Updated: $installConfigPath"
}

$wrapperDir = Join-Path $resolvedInstallRoot "services\wrappers"
if (Test-Path -LiteralPath $wrapperDir) {
    $wrapperFiles = @(Get-ChildItem -LiteralPath $wrapperDir -Filter "*.xml" -File)
    foreach ($wrapper in $wrapperFiles) {
        $content = Get-Content -Raw -Encoding UTF8 -LiteralPath $wrapper.FullName
        $serviceId = Resolve-ServiceIdFromWrapper -WrapperName $wrapper.Name -Content $content
        $wrapperLogDir = Join-Path $LogDir $serviceId
        New-Item -ItemType Directory -Force -Path $wrapperLogDir | Out-Null
        $content = $content -replace '(\s--spring\.profiles\.active=)[^\s<"]+', "`${1}$DeployProfile"
        $content = Set-JavaSystemPropertyArgument -Content $content -Name "forgex.deployment.log-dir" -Value $LogDir
        $content = Set-JavaSystemPropertyArgument -Content $content -Name "LOG_DIR" -Value $LogDir
        $content = Set-JavaSystemPropertyArgument -Content $content -Name "logging.file.path" -Value $LogDir
        $content = $content -replace '<logpath>.*?</logpath>', "<logpath>$([System.Security.SecurityElement]::Escape($wrapperLogDir))</logpath>"
        $content = Set-WrapperEnv -Content $content -Name "FORGEX_PROFILE" -Value $DeployProfile -InsertAfter "FORGEX_INSTANCE_CODE"
        $content = Set-WrapperEnv -Content $content -Name "FORGEX_DEPLOYMENT_PROFILE" -Value $DeployProfile -InsertAfter "FORGEX_PROFILE"
        $content = Set-WrapperEnv -Content $content -Name "FORGEX_LOG_DIR" -Value $LogDir -InsertAfter "FORGEX_UPLOAD_DIR"
        $content = Set-WrapperEnv -Content $content -Name "LOG_DIR" -Value $LogDir -InsertAfter "FORGEX_LOG_DIR"
        $content = Set-WrapperEnv -Content $content -Name "LOG_PATH" -Value $LogDir -InsertAfter "LOG_DIR"
        $content = Set-WrapperEnv -Content $content -Name "LOGGING_FILE_PATH" -Value $LogDir -InsertAfter "LOG_PATH"
        $content = Set-WrapperEnv -Content $content -Name "FORGEX_NACOS_NAMESPACE" -Value $NacosNamespace -InsertAfter "FORGEX_NACOS_ADDR"
        if (-not [string]::IsNullOrWhiteSpace($NacosGroup)) {
            $content = Set-WrapperEnv -Content $content -Name "FORGEX_NACOS_GROUP" -Value $NacosGroup -InsertAfter "FORGEX_NACOS_NAMESPACE"
        }
        $content = Set-WrapperEnv -Content $content -Name "FORGEX_DATASOURCE_CONFIG" -Value $DatasourceConfig -InsertAfter "FORGEX_NACOS_GROUP"
        $content = Set-WrapperEnv -Content $content -Name "FORGEX_INTEGRATION_DATASOURCE_CONFIG" -Value $IntegrationDatasourceConfig -InsertAfter "FORGEX_DATASOURCE_CONFIG"
        [System.IO.File]::WriteAllText($wrapper.FullName, $content, $utf8NoBom)
        Write-Host "Updated: $($wrapper.FullName)"
    }
}

Write-Host "Runtime config repaired. LogDir: $LogDir. Stop all Forgex services, then start them again."
