param(
    [string]$OutputFile = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Add-Line {
    param(
        [System.Collections.Generic.List[string]]$Buffer,
        [string]$Text = ""
    )
    [void]$Buffer.Add($Text)
}

function Resolve-OutputFile {
    param([string]$Value)
    if (-not [string]::IsNullOrWhiteSpace($Value)) {
        return [System.IO.Path]::GetFullPath($Value)
    }
    $desktop = [Environment]::GetFolderPath("Desktop")
    if ([string]::IsNullOrWhiteSpace($desktop) -or -not (Test-Path -LiteralPath $desktop)) {
        $desktop = (Get-Location).Path
    }
    $name = "forgex-license-diagnostics-{0}.md" -f (Get-Date -Format "yyyyMMdd-HHmmss")
    return (Join-Path $desktop $name)
}

function Get-EnvValue {
    param(
        [string]$Name,
        [string]$Scope
    )
    try {
        return [Environment]::GetEnvironmentVariable($Name, $Scope)
    } catch {
        return $null
    }
}

function Try-ReadText {
    param([string]$Path)
    try {
        return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    } catch {
        try {
            return [System.IO.File]::ReadAllText($Path)
        } catch {
            return ""
        }
    }
}

function Normalize-Base64UrlAndDecode {
    param([string]$Value)
    $normalized = $Value.Replace("-", "+").Replace("_", "/")
    $padding = (4 - ($normalized.Length % 4)) % 4
    if ($padding -gt 0) {
        $normalized = $normalized + ("=" * $padding)
    }
    return [Convert]::FromBase64String($normalized)
}

function Summarize-File {
    param(
        [string]$FilePath,
        [System.Collections.Generic.List[string]]$Buffer
    )

    if (-not (Test-Path -LiteralPath $FilePath)) {
        Add-Line $Buffer ("- `{0}`: MISSING" -f $FilePath)
        return
    }

    $item = Get-Item -LiteralPath $FilePath
    Add-Line $Buffer ("- `{0}`: exists, {1} bytes, LastWrite={2}" -f $FilePath, $item.Length, $item.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss"))

    if ($item.Length -le 0) {
        return
    }

    $raw = Try-ReadText -Path $FilePath
    $text = $raw.Replace([string][char]0xFEFF, "").Trim()
    if ($text.Length -gt 0) {
        $headLen = [Math]::Min(70, $text.Length)
        $tailLen = [Math]::Min(70, $text.Length)
        $head = $text.Substring(0, $headLen)
        $tail = $text.Substring($text.Length - $tailLen, $tailLen)
        Add-Line $Buffer ("  - text length={0}" -f $text.Length)
        Add-Line $Buffer ("  - head='{0}'" -f $head)
        Add-Line $Buffer ("  - tail='{0}'" -f $tail)
    }

    if ($item.Name -ieq "license.lic") {
        $parts = $text.Split(".")
        Add-Line $Buffer ("  - segment count={0}" -f $parts.Length)
        if ($parts.Length -eq 2) {
            for ($i = 0; $i -lt 2; $i++) {
                $seg = $parts[$i]
                Add-Line $Buffer ("  - seg{0}: len={1}, mod4={2}" -f $i, $seg.Length, ($seg.Length % 4))
                try {
                    $bytes = Normalize-Base64UrlAndDecode -Value $seg
                    Add-Line $Buffer ("    - decode: OK ({0} bytes)" -f $bytes.Length)
                } catch {
                    Add-Line $Buffer ("    - decode: ERR ({0})" -f $_.Exception.Message)
                }
            }
        }
    }

    if ($item.Name -ieq "public-key.base64") {
        try {
            $pk = [Convert]::FromBase64String($text)
            Add-Line $Buffer ("  - base64 decode: OK ({0} bytes)" -f $pk.Length)
        } catch {
            Add-Line $Buffer ("  - base64 decode: ERR ({0})" -f $_.Exception.Message)
        }
    }
}

$report = New-Object 'System.Collections.Generic.List[string]'
$now = Get-Date
$outputPath = Resolve-OutputFile -Value $OutputFile

Add-Line $report "# Forgex License Diagnostics"
Add-Line $report ""
Add-Line $report ("- GeneratedAt: {0}" -f $now.ToString("yyyy-MM-dd HH:mm:ss zzz"))
Add-Line $report ("- ComputerName: {0}" -f $env:COMPUTERNAME)
Add-Line $report ("- UserName: {0}" -f $env:USERNAME)
Add-Line $report ("- CurrentDir: {0}" -f (Get-Location).Path)
Add-Line $report ""

Add-Line $report "## 1) FORGEX Environment Variables"
Add-Line $report ""
$envNames = @(
    "FORGEX_PROFILE",
    "FORGEX_HOME",
    "FORGEX_LICENSE_DIR",
    "FORGEX_LICENSE_ENABLED",
    "FORGEX_LICENSE_PUBLIC_KEY",
    "FORGEX_LICENSE_PUBLIC_KEY_FILE_NAME",
    "FORGEX_LICENSE_FILE_NAME",
    "FORGEX_REQUEST_INFO_FILE_NAME"
)
$scopes = @("Process", "User", "Machine")
foreach ($name in $envNames) {
    Add-Line $report ("### {0}" -f $name)
    foreach ($scope in $scopes) {
        $val = Get-EnvValue -Name $name -Scope $scope
        if ([string]::IsNullOrWhiteSpace($val)) {
            Add-Line $report ("- " + $scope + ": <empty>")
        } elseif ($name -eq "FORGEX_LICENSE_PUBLIC_KEY") {
            $head = $val.Substring(0, [Math]::Min(30, $val.Length))
            Add-Line $report ("- " + $scope + ": len=" + $val.Length + ", head='" + $head + "'")
        } else {
            Add-Line $report ("- " + $scope + ": '" + $val + "'")
        }
    }
    Add-Line $report ""
}

Add-Line $report "## 2) Java Process Snapshot"
Add-Line $report ""
try {
    $javaProcesses = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'"
} catch {
    $javaProcesses = @()
    Add-Line $report ("- WARN: unable to query java processes: {0}" -f $_.Exception.Message)
}

if ($javaProcesses.Count -eq 0) {
    Add-Line $report "- No java.exe process found."
    Add-Line $report ""
} else {
    foreach ($p in $javaProcesses) {
        Add-Line $report ("### PID {0}" -f $p.ProcessId)
        Add-Line $report ("- ExecutablePath: '" + $p.ExecutablePath + "'")
        Add-Line $report ("- CommandLine: '" + $p.CommandLine + "'")
        $cmd = $p.CommandLine
        if ($cmd) {
            if ($cmd -match "-Dforgex\.deployment\.license-dir=([^\\s]+)") {
                Add-Line $report ("- Detected JVM license-dir: '" + $matches[1] + "'")
            }
            if ($cmd -match "-Dforgex\.deployment\.home=([^\\s]+)") {
                Add-Line $report ("- Detected JVM home: '" + $matches[1] + "'")
            }
            if ($cmd -match "-Dspring\.profiles\.active=([^\\s]+)") {
                Add-Line $report ("- Detected spring profile: '" + $matches[1] + "'")
            }
        }
        Add-Line $report ""
    }
}

Add-Line $report "## 3) Candidate License Directories"
Add-Line $report ""
$candidateDirs = @(
    (Join-Path (Get-Location).Path "forgex\license"),
    "D:\forgex\forgex\license",
    "D:\mine_product\forgex\forgex\license",
    "D:\mine_product\forgex\Forgex_MOM\Forgex_Backend\Forgex_Gateway\forgex\license",
    "D:\mine_product\forgex\Forgex_MOM\Forgex_Backend\Forgex_Sys\forgex\license",
    "D:\mine_product\forgex\Forgex_MOM\Forgex_Backend\Forgex_Job\forgex\license"
) | Select-Object -Unique

foreach ($dir in $candidateDirs) {
    Add-Line $report ("### '" + $dir + "'")
    if (-not (Test-Path -LiteralPath $dir)) {
        Add-Line $report "- directory: MISSING"
        Add-Line $report ""
        continue
    }
    Add-Line $report "- directory: EXISTS"
    Summarize-File -FilePath (Join-Path $dir "license.lic") -Buffer $report
    Summarize-File -FilePath (Join-Path $dir "public-key.base64") -Buffer $report
    Summarize-File -FilePath (Join-Path $dir "request-info.json") -Buffer $report
    Summarize-File -FilePath (Join-Path $dir "activation-history.json") -Buffer $report
    Add-Line $report ""
}

Add-Line $report "## 4) Quick Judgment Tips"
Add-Line $report ""
Add-Line $report "- If process env `FORGEX_LICENSE_PUBLIC_KEY` is non-empty but malformed, decode will fail before signature check."
Add-Line $report "- If process points to one directory but files were fixed in another directory, issue persists."
Add-Line $report "- Compare `CommandLine` detected JVM `-D...license-dir` with actual fixed directory."
Add-Line $report ""

$parent = Split-Path -Parent $outputPath
if (-not [string]::IsNullOrWhiteSpace($parent) -and -not (Test-Path -LiteralPath $parent)) {
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
}
[System.IO.File]::WriteAllLines($outputPath, $report, [System.Text.Encoding]::UTF8)
Write-Host ("[Forgex] diagnostics written: {0}" -f $outputPath)
