param(
    [string]$InstallRoot = "",
    [string]$NacosAddr = "",
    [string]$Namespace = "",
    [string]$NamespaceName = "",
    [string]$Group = "",
    [string]$ConfigDir = "",
    [switch]$SkipNamespaceCreate
)

$ErrorActionPreference = "Stop"

function Resolve-InstallRoot {
    if (-not [string]::IsNullOrWhiteSpace($InstallRoot)) {
        return [System.IO.Path]::GetFullPath($InstallRoot)
    }

    $scriptRoot = Split-Path -Parent $PSCommandPath
    return [System.IO.Path]::GetFullPath((Join-Path $scriptRoot ".."))
}

function Read-ControlConfig {
    param(
        [string]$Root
    )

    $configPath = Join-Path $Root "config\forgex-control.json"
    if (-not (Test-Path -LiteralPath $configPath)) {
        return $null
    }

    return Get-Content -Raw -Encoding UTF8 -LiteralPath $configPath | ConvertFrom-Json
}

function Resolve-NacosBaseUrl {
    param(
        [string]$Address
    )

    $resolved = $Address.Trim()
    if ([string]::IsNullOrWhiteSpace($resolved)) {
        $resolved = "127.0.0.1:8848"
    }

    if ($resolved -notmatch '^https?://') {
        $resolved = "http://$resolved"
    }

    return $resolved.TrimEnd('/')
}

function Invoke-NacosRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Body = $null
    )

    if ($null -eq $Body) {
        return Invoke-RestMethod -Method $Method -Uri $Uri -TimeoutSec 20
    }

    return Invoke-RestMethod -Method $Method -Uri $Uri -Body $Body -ContentType "application/x-www-form-urlencoded; charset=utf-8" -TimeoutSec 20
}

function Ensure-Namespace {
    param(
        [string]$BaseUrl,
        [string]$NamespaceId,
        [string]$ResolvedNamespaceName
    )

    if ([string]::IsNullOrWhiteSpace($NamespaceId)) {
        Write-Host "Use public namespace."
        return
    }

    $namespaceList = Invoke-NacosRequest -Method "Get" -Uri "$BaseUrl/nacos/v1/console/namespaces"
    $existingNamespaces = @($namespaceList.data)
    $exists = $existingNamespaces | Where-Object {
        $_.namespace -eq $NamespaceId -or $_.namespaceShowName -eq $NamespaceId
    } | Select-Object -First 1

    if ($null -ne $exists) {
        Write-Host "Namespace exists: $NamespaceId"
        return
    }

    if ($SkipNamespaceCreate) {
        throw "Nacos namespace does not exist: $NamespaceId"
    }

    $body = @{
        customNamespaceId = $NamespaceId
        namespaceName = $ResolvedNamespaceName
        namespaceDesc = "Forgex $ResolvedNamespaceName"
    }

    $result = Invoke-NacosRequest -Method "Post" -Uri "$BaseUrl/nacos/v1/console/namespaces" -Body $body
    Write-Host "Namespace created: $NamespaceId ($result)"
}

function Publish-Config {
    param(
        [string]$BaseUrl,
        [string]$Tenant,
        [string]$ResolvedGroup,
        [System.IO.FileInfo]$File
    )

    $dataId = $File.Name
    $content = Get-Content -Raw -Encoding UTF8 -LiteralPath $File.FullName
    $type = if ($File.Extension.Equals(".yaml", [System.StringComparison]::OrdinalIgnoreCase)) { "yaml" } else { "yaml" }
    $body = @{
        dataId = $dataId
        group = $ResolvedGroup
        content = $content
        type = $type
    }

    if (-not [string]::IsNullOrWhiteSpace($Tenant)) {
        $body.tenant = $Tenant
    }

    $result = Invoke-NacosRequest -Method "Post" -Uri "$BaseUrl/nacos/v1/cs/configs" -Body $body
    if ($result -ne $true -and "$result".ToLowerInvariant() -ne "true") {
        throw "Nacos rejected config $dataId. Response: $result"
    }

    Write-Host "Published: $dataId -> namespace=$Tenant group=$ResolvedGroup"
}

$resolvedInstallRoot = Resolve-InstallRoot
$controlConfig = Read-ControlConfig -Root $resolvedInstallRoot

if ([string]::IsNullOrWhiteSpace($NacosAddr) -and $null -ne $controlConfig) {
    $NacosAddr = [string]$controlConfig.nacosAddr
}

if ([string]::IsNullOrWhiteSpace($Namespace) -and $null -ne $controlConfig) {
    $Namespace = [string]$controlConfig.nacosNamespace
}

if ([string]::IsNullOrWhiteSpace($Group) -and $null -ne $controlConfig) {
    $Group = [string]$controlConfig.nacosGroup
}

if ([string]::IsNullOrWhiteSpace($Namespace)) {
    $Namespace = "yanshi"
}

if ([string]::IsNullOrWhiteSpace($NamespaceName)) {
    $NamespaceName = $Namespace
}

if ([string]::IsNullOrWhiteSpace($Group)) {
    $Group = "DEFAULT_GROUP"
}

if ([string]::IsNullOrWhiteSpace($ConfigDir)) {
    $ConfigDir = Join-Path $resolvedInstallRoot "nacos\$Group"
}

if (-not (Test-Path -LiteralPath $ConfigDir)) {
    throw "Nacos config directory not found: $ConfigDir"
}

$baseUrl = Resolve-NacosBaseUrl -Address $NacosAddr
Write-Host "InstallRoot: $resolvedInstallRoot"
Write-Host "Nacos: $baseUrl"
Write-Host "Namespace: $Namespace"
Write-Host "Group: $Group"
Write-Host "ConfigDir: $ConfigDir"

Invoke-NacosRequest -Method "Get" -Uri "$baseUrl/nacos/v1/console/namespaces" | Out-Null
Ensure-Namespace -BaseUrl $baseUrl -NamespaceId $Namespace -ResolvedNamespaceName $NamespaceName

$configFiles = Get-ChildItem -LiteralPath $ConfigDir -File |
    Where-Object { $_.Extension -in @(".yml", ".yaml", ".properties") } |
    Sort-Object Name

if ($configFiles.Count -eq 0) {
    throw "No Nacos config files found in $ConfigDir"
}

foreach ($file in $configFiles) {
    Publish-Config -BaseUrl $baseUrl -Tenant $Namespace -ResolvedGroup $Group -File $file
}

Write-Host "Imported $($configFiles.Count) Nacos config file(s)."
