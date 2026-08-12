$ErrorActionPreference = 'Stop'

$backendRoot = Split-Path -Parent $PSScriptRoot
$requiredModules = @(
    'Forgex_Common_Contract',
    'Forgex_Common_Core',
    'Forgex_Domain_Contract',
    'Forgex_Auth_Api',
    'Forgex_Sys_Api',
    'Forgex_Basic_Api',
    'Forgex_Job_Api',
    'Forgex_Workflow_Api',
    'Forgex_Integration_Api',
    'Forgex_Common_Web',
    'Forgex_Common_Data',
    'Forgex_Common_Crypto',
    'Forgex_Common_Excel',
    'Forgex_Common_Infra'
)

$errors = [System.Collections.Generic.List[string]]::new()

foreach ($module in $requiredModules) {
    $pomPath = Join-Path $backendRoot "$module/pom.xml"
    if (-not (Test-Path -LiteralPath $pomPath)) {
        $errors.Add("Missing module POM: $module/pom.xml")
    }
}

$forbiddenDependencies = @(
    'spring-boot-starter-web',
    'spring-cloud-starter-openfeign',
    'mybatis-plus',
    'spring-boot-starter-data-redis',
    'rocketmq',
    'poi-ooxml',
    'fastexcel',
    'bcprov'
)

foreach ($module in @('Forgex_Common_Contract', 'Forgex_Common_Core')) {
    $pomPath = Join-Path $backendRoot "$module/pom.xml"
    if (-not (Test-Path -LiteralPath $pomPath)) {
        continue
    }

    $pomContent = Get-Content -Raw -LiteralPath $pomPath
    foreach ($dependency in $forbiddenDependencies) {
        if ($pomContent -match [regex]::Escape($dependency)) {
            $errors.Add("$module contains forbidden heavy dependency: $dependency")
        }
    }
}

$apiModules = @(
    'Forgex_Domain_Contract',
    'Forgex_Auth_Api',
    'Forgex_Sys_Api',
    'Forgex_Basic_Api',
    'Forgex_Job_Api',
    'Forgex_Workflow_Api',
    'Forgex_Integration_Api'
)
$apiForbiddenDependencies = @(
    'spring-boot-starter',
    'mybatis-plus-spring-boot',
    'dynamic-datasource',
    'spring-boot-starter-data-redis',
    'rocketmq',
    'poi-ooxml',
    'fastexcel',
    'bcprov'
)

foreach ($module in $apiModules) {
    $pomPath = Join-Path $backendRoot "$module/pom.xml"
    if (-not (Test-Path -LiteralPath $pomPath)) {
        continue
    }

    $pomContent = Get-Content -Raw -LiteralPath $pomPath
    foreach ($dependency in $apiForbiddenDependencies) {
        if ($pomContent -match [regex]::Escape($dependency)) {
            $errors.Add("$module contains forbidden runtime dependency: $dependency")
        }
    }
}

$webPomPath = Join-Path $backendRoot 'Forgex_Common_Web/pom.xml'
if (Test-Path -LiteralPath $webPomPath) {
    $webPomContent = Get-Content -Raw -LiteralPath $webPomPath
    foreach ($module in @('Forgex_Common_Infra', 'Forgex_Common_Data', 'Forgex_Common_Crypto', 'Forgex_Common_Excel')) {
        if ($webPomContent -match [regex]::Escape("<artifactId>$module</artifactId>")) {
            $errors.Add("Forgex_Common_Web must not depend on heavy capability module: $module")
        }
    }
}

$sourceOwners = @{}
foreach ($module in $requiredModules) {
    $sourceRoot = Join-Path $backendRoot "$module/src/main/java"
    if (-not (Test-Path -LiteralPath $sourceRoot)) {
        continue
    }

    foreach ($sourceFile in Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java') {
        $relativePath = $sourceFile.FullName.Substring($sourceRoot.Length + 1)
        if ($sourceOwners.ContainsKey($relativePath)) {
            $errors.Add("Duplicate Java source $relativePath in $($sourceOwners[$relativePath]) and $module")
        } else {
            $sourceOwners[$relativePath] = $module
        }
    }
}

$aggregateSource = Join-Path $backendRoot 'Forgex_Common/src/main/java'
if (Test-Path -LiteralPath $aggregateSource) {
    $remainingSources = @(Get-ChildItem -LiteralPath $aggregateSource -Recurse -Filter '*.java')
    if ($remainingSources.Count -gt 0) {
        $errors.Add("Forgex_Common aggregate still contains $($remainingSources.Count) Java source files")
    }
}

$businessModules = @(
    'Forgex_Auth',
    'Forgex_Sys',
    'Forgex_Basic',
    'Forgex_Gateway',
    'Forgex_Job',
    'Forgex_Workflow',
    'Forgex_Report',
    'Forgex_Integration'
)

$classOwners = @{}
foreach ($module in $requiredModules) {
    $sourceRoot = Join-Path $backendRoot "$module/src/main/java"
    if (-not (Test-Path -LiteralPath $sourceRoot)) {
        continue
    }

    foreach ($sourceFile in Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java') {
        $content = Get-Content -Raw -LiteralPath $sourceFile.FullName
        $packageMatch = [regex]::Match($content, '(?m)^package\s+([\w.]+);')
        if ($packageMatch.Success) {
            $classOwners["$($packageMatch.Groups[1].Value).$($sourceFile.BaseName)"] = $module
        }
    }
}

foreach ($module in $businessModules) {
    $pomPath = Join-Path $backendRoot "$module/pom.xml"
    if (-not (Test-Path -LiteralPath $pomPath)) {
        $errors.Add("Missing business module POM: $module/pom.xml")
        continue
    }

    [xml]$pom = Get-Content -Raw -Encoding UTF8 -LiteralPath $pomPath
    $namespace = [System.Xml.XmlNamespaceManager]::new($pom.NameTable)
    $namespace.AddNamespace('m', 'http://maven.apache.org/POM/4.0.0')
    $declaredDependencies = @(
        $pom.SelectNodes('/m:project/m:dependencies/m:dependency/m:artifactId', $namespace) |
            ForEach-Object { $_.InnerText }
    )

    if ('Forgex_Common' -in $declaredDependencies) {
        $errors.Add("$module must use precise common dependencies instead of Forgex_Common")
    }

    $usedModules = [System.Collections.Generic.HashSet[string]]::new()
    $sourceRoot = Join-Path $backendRoot "$module/src"
    if (Test-Path -LiteralPath $sourceRoot) {
        foreach ($sourceFile in Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java') {
            $content = Get-Content -Raw -LiteralPath $sourceFile.FullName
            foreach ($importMatch in [regex]::Matches(
                $content,
                '(?m)^import\s+(?:static\s+)?(com\.forgex\.common\.[\w.]+)(?:\.\*)?;'
            )) {
                $className = $importMatch.Groups[1].Value
                if ($classOwners.ContainsKey($className)) {
                    [void]$usedModules.Add($classOwners[$className])
                }
            }
        }
    }

    foreach ($usedModule in $usedModules) {
        if ($usedModule -notin $declaredDependencies) {
            $errors.Add("$module uses $usedModule classes without a direct Maven dependency")
        }
    }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ -ErrorAction Continue }
    exit 1
}

Write-Host "Common module boundary verification passed ($($requiredModules.Count) modules)."
