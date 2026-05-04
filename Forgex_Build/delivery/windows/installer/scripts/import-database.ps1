param(
    [string]$InstallRoot = "",
    [string]$SqlDir = "",
    [string]$MysqlExe = "",
    [string]$MysqlHost = "",
    [int]$MysqlPort = 0,
    [string]$User = "",
    [string]$Password = "",
    [switch]$PromptPassword,
    [switch]$ForceImport,
    [switch]$ResetDatabase
)

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$OutputEncoding = $utf8NoBom
try {
    [Console]::OutputEncoding = $utf8NoBom
    [Console]::InputEncoding = $utf8NoBom
} catch {
}

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

function Resolve-MysqlEndpointFromJdbcUrl {
    param(
        [string]$JdbcUrl
    )

    if ([string]::IsNullOrWhiteSpace($JdbcUrl)) {
        return $null
    }

    if ($JdbcUrl -match '^jdbc:mysql://([^/:?]+)(?::([0-9]+))?/') {
        $resolvedPort = 3306
        if (-not [string]::IsNullOrWhiteSpace($Matches[2])) {
            $resolvedPort = [int]$Matches[2]
        }

        return [pscustomobject]@{
            Host = $Matches[1]
            Port = $resolvedPort
        }
    }

    return $null
}

function Read-DefaultDatasourceCredential {
    param(
        [string]$Root
    )

    $candidateFiles = @(
        (Join-Path $Root "nacos\DEFAULT_GROUP\datasource-forgex-dev.yml"),
        (Join-Path $Root "nacos\DEFAULT_GROUP\datasource-forgex-integration-dev.yml")
    )

    foreach ($candidate in $candidateFiles) {
        if (-not (Test-Path -LiteralPath $candidate)) {
            continue
        }

        $resolvedUser = ""
        $resolvedPassword = ""
        foreach ($line in Get-Content -Encoding UTF8 -LiteralPath $candidate) {
            if ([string]::IsNullOrWhiteSpace($resolvedUser) -and $line -match '^\s*username:\s*(.*)\s*$') {
                $resolvedUser = $Matches[1].Trim().Trim("'").Trim('"')
                continue
            }

            if ([string]::IsNullOrWhiteSpace($resolvedPassword) -and $line -match '^\s*password:\s*(.*)\s*$') {
                $resolvedPassword = $Matches[1].Trim().Trim("'").Trim('"')
                continue
            }

            if (-not [string]::IsNullOrWhiteSpace($resolvedUser) -and -not [string]::IsNullOrWhiteSpace($resolvedPassword)) {
                break
            }
        }

        if (-not [string]::IsNullOrWhiteSpace($resolvedUser)) {
            return [pscustomobject]@{
                User = $resolvedUser
                Password = $resolvedPassword
            }
        }
    }

    return $null
}

function Resolve-MysqlExecutable {
    param(
        [string]$ConfiguredPath
    )

    if (-not [string]::IsNullOrWhiteSpace($ConfiguredPath) -and (Test-Path -LiteralPath $ConfiguredPath)) {
        return [System.IO.Path]::GetFullPath($ConfiguredPath)
    }

    $command = Get-Command "mysql.exe" -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }

    $candidates = @(
        "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.3\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.2\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\Program Files\MariaDB 11.4\bin\mysql.exe",
        "C:\Program Files\MariaDB 11.3\bin\mysql.exe",
        "C:\Program Files\MariaDB 11.2\bin\mysql.exe",
        "C:\Program Files\MariaDB 10.11\bin\mysql.exe",
        "C:\xampp\mysql\bin\mysql.exe"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw "mysql.exe was not found. Add MySQL bin to PATH or rerun with -MysqlExe <path-to-mysql.exe>."
}

function ConvertTo-MysqlIdentifier {
    param(
        [string]$Name
    )

    $tick = [char]96
    return $tick + $Name.Replace("$tick", "$tick$tick") + $tick
}

function ConvertTo-MysqlStringLiteral {
    param(
        [string]$Value
    )

    return "'" + $Value.Replace("\", "\\").Replace("'", "''") + "'"
}

function Invoke-Mysql {
    param(
        [string[]]$Arguments,
        [string]$InputText = $null
    )

    $previousMysqlPwd = [Environment]::GetEnvironmentVariable("MYSQL_PWD", "Process")
    try {
        if ([string]::IsNullOrEmpty($script:ResolvedPassword)) {
            [Environment]::SetEnvironmentVariable("MYSQL_PWD", $null, "Process")
        } else {
            [Environment]::SetEnvironmentVariable("MYSQL_PWD", $script:ResolvedPassword, "Process")
        }

        if ($null -eq $InputText) {
            $output = & $script:ResolvedMysqlExe @Arguments 2>&1
        } else {
            $output = $InputText | & $script:ResolvedMysqlExe @Arguments 2>&1
        }

        if ($LASTEXITCODE -ne 0) {
            $message = ($output | ForEach-Object { "$_" }) -join [Environment]::NewLine
            if ([string]::IsNullOrWhiteSpace($message)) {
                $message = "mysql.exe exited with code $LASTEXITCODE."
            }

            throw $message
        }

        return $output
    } finally {
        [Environment]::SetEnvironmentVariable("MYSQL_PWD", $previousMysqlPwd, "Process")
    }
}

function Get-DatabaseTableCount {
    param(
        [string]$DatabaseName
    )

    $schemaLiteral = ConvertTo-MysqlStringLiteral -Value $DatabaseName
    $query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = $schemaLiteral;"
    $output = Invoke-Mysql -Arguments ($script:BaseMysqlArgs + @("--batch", "--skip-column-names", "--execute=$query"))
    $numericLine = $output | Where-Object { "$_" -match '^\s*[0-9]+\s*$' } | Select-Object -Last 1
    if ($null -eq $numericLine) {
        return 0
    }

    return [int]("$numericLine".Trim())
}

function Invoke-MysqlStatement {
    param(
        [string]$Sql
    )

    Invoke-Mysql -Arguments $script:BaseMysqlArgs -InputText $Sql | Out-Null
}

function Get-OrderedSqlFiles {
    param(
        [string]$Directory
    )

    $preferredOrder = @(
        "forgex_common",
        "forgex_admin",
        "forgex_history",
        "forgex_job",
        "forgex_workflow",
        "forgex_scada",
        "forgex_integration"
    )

    return Get-ChildItem -LiteralPath $Directory -Filter "*.sql" -File |
        Sort-Object `
            @{ Expression = {
                $index = [Array]::IndexOf($preferredOrder, $_.BaseName)
                if ($index -lt 0) { 999 } else { $index }
            } },
            @{ Expression = { $_.Name } }
}

$resolvedInstallRoot = Resolve-InstallRoot
$controlConfig = Read-ControlConfig -Root $resolvedInstallRoot

if ([string]::IsNullOrWhiteSpace($SqlDir)) {
    $SqlDir = Join-Path $resolvedInstallRoot "database-init"
}

if (-not (Test-Path -LiteralPath $SqlDir)) {
    throw "SQL directory not found: $SqlDir"
}

if (($MysqlHost -eq "" -or $MysqlPort -eq 0) -and $null -ne $controlConfig) {
    $endpoint = Resolve-MysqlEndpointFromJdbcUrl -JdbcUrl ([string]$controlConfig.mysqlUrl)
    if ($null -ne $endpoint) {
        if ([string]::IsNullOrWhiteSpace($MysqlHost)) {
            $MysqlHost = $endpoint.Host
        }

        if ($MysqlPort -eq 0) {
            $MysqlPort = $endpoint.Port
        }
    }
}

if ([string]::IsNullOrWhiteSpace($MysqlHost)) {
    $MysqlHost = "127.0.0.1"
}

if ($MysqlPort -eq 0) {
    $MysqlPort = 3306
}

$credential = Read-DefaultDatasourceCredential -Root $resolvedInstallRoot
if ([string]::IsNullOrWhiteSpace($User)) {
    if ($null -ne $credential -and -not [string]::IsNullOrWhiteSpace($credential.User)) {
        $User = $credential.User
    } else {
        $User = "root"
    }
}

if ([string]::IsNullOrWhiteSpace($Password)) {
    if ($null -ne $credential) {
        $Password = [string]$credential.Password
    } else {
        $Password = "123456"
    }
}

if ($PromptPassword) {
    $securePassword = Read-Host -Prompt "MySQL password" -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $Password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

$script:ResolvedMysqlExe = Resolve-MysqlExecutable -ConfiguredPath $MysqlExe
$script:ResolvedPassword = $Password
$script:BaseMysqlArgs = @(
    "--protocol=tcp",
    "--host=$MysqlHost",
    "--port=$MysqlPort",
    "--user=$User",
    "--default-character-set=utf8mb4",
    "--binary-mode"
)

$sqlFiles = @(Get-OrderedSqlFiles -Directory $SqlDir)
if ($sqlFiles.Count -eq 0) {
    throw "No .sql files found in $SqlDir"
}

Write-Host "InstallRoot: $resolvedInstallRoot"
Write-Host "SQL directory: $SqlDir"
Write-Host "MySQL: $MysqlHost`:$MysqlPort"
Write-Host "MySQL user: $User"
Write-Host "mysql.exe: $script:ResolvedMysqlExe"
Write-Host "SQL files: $($sqlFiles.Count)"
if ($ResetDatabase) {
    Write-Host "Mode: reset database before import"
} elseif ($ForceImport) {
    Write-Host "Mode: force import into existing databases"
} else {
    Write-Host "Mode: import only when target databases are empty"
}

Invoke-Mysql -Arguments ($script:BaseMysqlArgs + @("--batch", "--skip-column-names", "--execute=SELECT VERSION();")) | Out-Null

foreach ($file in $sqlFiles) {
    $databaseName = $file.BaseName
    if ($databaseName -notmatch '^forgex_[a-z0-9_]+$') {
        throw "SQL file name is not a valid Forgex database name: $($file.Name)"
    }

    $databaseIdentifier = ConvertTo-MysqlIdentifier -Name $databaseName

    if ($ResetDatabase) {
        Write-Host "Reset database: $databaseName"
        Invoke-MysqlStatement -Sql "DROP DATABASE IF EXISTS $databaseIdentifier; CREATE DATABASE $databaseIdentifier DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    } else {
        $tableCount = Get-DatabaseTableCount -DatabaseName $databaseName
        if ($tableCount -gt 0 -and -not $ForceImport) {
            throw "Database $databaseName already has $tableCount table(s). Rerun with -ForceImport to execute the SQL anyway, or -ResetDatabase to drop and rebuild it."
        }

        Invoke-MysqlStatement -Sql "CREATE DATABASE IF NOT EXISTS $databaseIdentifier DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    }

    Write-Host "Importing $($file.Name) -> $databaseName"
    $sourceSql = [System.IO.File]::ReadAllText($file.FullName, $utf8NoBom)
    $importSql = @"
SET NAMES utf8mb4;
USE $databaseIdentifier;
SET FOREIGN_KEY_CHECKS=0;
$sourceSql

SET FOREIGN_KEY_CHECKS=1;
"@

    Invoke-Mysql -Arguments $script:BaseMysqlArgs -InputText $importSql | Out-Null
    Write-Host "Imported: $databaseName"
}

Write-Host "Database import completed. Imported $($sqlFiles.Count) database script(s)."
