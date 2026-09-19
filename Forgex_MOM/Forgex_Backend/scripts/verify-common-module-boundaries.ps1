$ErrorActionPreference = 'Stop'
$backendRoot = Split-Path -Parent $PSScriptRoot
$errors = [System.Collections.Generic.List[string]]::new()
$modules = @{}
$owners = @{}

# 按实际 POM 坐标索引模块，目录分组不参与模块身份判断。
foreach ($file in Get-ChildItem -LiteralPath $backendRoot -Recurse -Filter pom.xml) {
    if ($file.FullName -match '[\\/]target[\\/]') { continue }
    [xml]$pom = Get-Content -Raw -Encoding UTF8 -LiteralPath $file.FullName
    $id = [string]$pom.project.artifactId
    if ($modules.ContainsKey($id)) { $errors.Add("Duplicate artifact: $id"); continue }
    $deps = @($pom.project.dependencies.dependency | Where-Object { $_.scope -ne 'test' })
    $modules[$id] = @{ Root = $file.DirectoryName; Pom = $pom; Dependencies = $deps }
    $source = Join-Path $file.DirectoryName 'src/main/java'
    if (Test-Path -LiteralPath $source) {
        foreach ($java in Get-ChildItem -LiteralPath $source -Recurse -Filter '*.java') {
            $text = Get-Content -Raw -Encoding UTF8 -LiteralPath $java.FullName
            $package = [regex]::Match($text, '(?m)^package\s+([\w.]+);')
            if ($package.Success) {
                $class = "$($package.Groups[1].Value).$($java.BaseName)"
                if ($owners.ContainsKey($class)) { $errors.Add("Duplicate source: $class") }
                $owners[$class] = $id
            }
        }
    }
}

$commonIds = @($modules.Keys | Where-Object { $modules[$_].Root.StartsWith((Join-Path $backendRoot 'forgex-common'), [StringComparison]::OrdinalIgnoreCase) })
$serviceIds = @('Forgex_Auth','Forgex_Sys','Forgex_Basic','Forgex_Job','Forgex_Workflow','Forgex_Integration','Forgex_Report','Forgex_Gateway')
foreach ($required in @('forgex-common','forgex-common-parent','forgex-common-bom','forgex-common-starter','forgex-admin','forgex-admin-client','forgex-admin-runtime','forgex-business','forgex-business-basic') + $serviceIds) {
    if (-not $modules.ContainsKey($required)) { $errors.Add("Missing module: $required") }
}

foreach ($id in $modules.Keys) {
    $module = $modules[$id]
    $direct = @($module.Dependencies | ForEach-Object { [string]$_.artifactId })
    foreach ($dep in $module.Dependencies) {
        $artifact = [string]$dep.artifactId
        if ($id -in $commonIds -and $dep.groupId -eq 'com.forgex' -and $artifact -notin $commonIds) {
            $errors.Add("$id depends on non-common artifact: $artifact")
        }
        if ($artifact -in $serviceIds) { $errors.Add("$id depends on runnable service implementation: $artifact") }
        if (($id -like '*_Api' -or $id -in @('Forgex_Common_Core','Forgex_Common_Contract','Forgex_Domain_Contract')) -and
            $artifact -match 'spring-boot-starter|spring-cloud-starter|mybatis-plus-spring|dynamic-datasource|rocketmq|poi-ooxml|fastexcel|bcprov') {
            $errors.Add("$id has heavy contract dependency: $artifact")
        }
    }
    if ($id -in $serviceIds -and 'Forgex_Common' -in $direct) { $errors.Add("$id uses compatibility aggregate") }
    $source = Join-Path $module.Root 'src/main/java'
    if (-not (Test-Path -LiteralPath $source)) { continue }
    foreach ($java in Get-ChildItem -LiteralPath $source -Recurse -Filter '*.java') {
        $text = Get-Content -Raw -Encoding UTF8 -LiteralPath $java.FullName
        if ($id -in $commonIds -and $text -match '@(?:EnableFeignClients|FeignClient|TableName)\b|@DS\("(?:admin|common)"\)') {
            # 通用基础实体不映射任何平台表；平台 Feign 和持久化仅存在于 Admin。
            $errors.Add("Common source contains platform binding: $($java.FullName)")
        }
        foreach ($match in [regex]::Matches($text, '(?m)^import\s+(?:static\s+)?(com\.forgex\.[\w.]+)(?:\.\*)?;')) {
            $class = $match.Groups[1].Value
            if (-not $owners.ContainsKey($class)) { continue }
            $owner = $owners[$class]
            if ($owner -eq $id) { continue }
            if ($id -in $commonIds -and $owner -notin $commonIds) { $errors.Add("$id imports platform source: $class") }
            if ($id -in $serviceIds -and $owner -notin $direct) { $errors.Add("$id uses $owner without direct dependency ($class)") }
        }
    }
}
$starterDeps = @($modules['forgex-common-starter'].Dependencies | ForEach-Object { [string]$_.artifactId })
foreach ($forbidden in @('forgex-admin-runtime','forgex-admin-client','Forgex_Common_Infra','Forgex_Common_Data','Forgex_Common_Excel')) {
    if ($forbidden -in $starterDeps) { $errors.Add("Starter requires optional runtime: $forbidden") }
}
if ($errors.Count -gt 0) {
    $errors | Select-Object -Unique | ForEach-Object { Write-Error $_ -ErrorAction Continue }
    exit 1
}
Write-Host "Common/Admin boundary verification passed ($($modules.Count) modules)."
