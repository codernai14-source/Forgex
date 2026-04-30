param(
    [string]$Version = "1.0.0",
    [string]$InnoSetupCompiler = "",
    [switch]$CompileInstaller
)

$ErrorActionPreference = "Stop"

$distDir = Join-Path $PSScriptRoot "dist\windows"
$stagingDir = Join-Path $PSScriptRoot "staging\windows"
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null

function Copy-DirectoryContents {
    param(
        [string]$Source,
        [string]$Destination
    )

    if (-not (Test-Path -LiteralPath $Source)) {
        return
    }

    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    Get-ChildItem -Force -LiteralPath $Source | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $Destination -Recurse -Force
    }
}

$staleRootIssPath = Join-Path $distDir "ForgexSetup.iss"
if (Test-Path -LiteralPath $staleRootIssPath) {
    Remove-Item -LiteralPath $staleRootIssPath -Force
}

$packageRoot = Join-Path $distDir ("Forgex-Windows-Package-{0}" -f $Version)
if (Test-Path -LiteralPath $packageRoot) {
    Remove-Item -LiteralPath $packageRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $packageRoot | Out-Null

Copy-Item -Recurse -Force (Join-Path $stagingDir "*") $packageRoot

$sourceScriptsDir = Join-Path $PSScriptRoot "delivery\windows\installer\scripts"
Copy-DirectoryContents -Source $sourceScriptsDir -Destination (Join-Path $packageRoot "scripts")

$sourceWinSwDir = Join-Path $PSScriptRoot "delivery\windows\installer\winsw"
$packageWinSwDir = Join-Path $packageRoot "winsw"
Copy-DirectoryContents -Source $sourceWinSwDir -Destination $packageWinSwDir

$sourceNginxDir = Join-Path $PSScriptRoot "shared\nginx\windows"
$packageNginxDir = Join-Path $packageRoot "nginx"
Copy-DirectoryContents -Source $sourceNginxDir -Destination $packageNginxDir
$sourceNginxTemplatePath = Join-Path $PSScriptRoot "shared\nginx\forgex.conf.template"
if (Test-Path -LiteralPath $sourceNginxTemplatePath) {
    New-Item -ItemType Directory -Force -Path $packageNginxDir | Out-Null
    Copy-Item -LiteralPath $sourceNginxTemplatePath -Destination (Join-Path $packageNginxDir "forgex.conf.template") -Force
}

$sourceIssPath = Join-Path $PSScriptRoot "delivery\windows\installer\ForgexSetup.iss"
$packageIssPath = Join-Path $packageRoot "ForgexSetup.iss"
$sourceLanguageDir = Join-Path $PSScriptRoot "delivery\windows\installer\languages"
$packageLanguageDir = Join-Path $packageRoot "languages"
if (Test-Path -LiteralPath $sourceLanguageDir) {
    New-Item -ItemType Directory -Force -Path $packageLanguageDir | Out-Null
    Copy-Item -Recurse -Force (Join-Path $sourceLanguageDir "*") $packageLanguageDir
}

$packageIss = [System.IO.File]::ReadAllText($sourceIssPath, [System.Text.Encoding]::UTF8)
$packageIss = $packageIss.Replace('..\..\..\staging\windows\*', '*')
$packageIss = $packageIss.Replace('..\..\..\dist\windows', '..')
$utf8Bom = New-Object System.Text.UTF8Encoding($true)
[System.IO.File]::WriteAllText($packageIssPath, $packageIss, $utf8Bom)

$zipPath = Join-Path $distDir ("Forgex-Windows-Package-{0}.zip" -f $Version)
if (Test-Path -LiteralPath $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
Compress-Archive -Path (Join-Path $packageRoot "*") -DestinationPath $zipPath

$packagedJavaExe = Join-Path $packageRoot "app\\jre\\bin\\java.exe"
if (-not (Test-Path -LiteralPath $packagedJavaExe)) {
    throw "Windows package is missing app\\jre\\bin\\java.exe. Run collect-artifacts.ps1 with a valid JRE before packaging."
}

if ($CompileInstaller) {
    $isccPath = $InnoSetupCompiler
    if ([string]::IsNullOrWhiteSpace($isccPath)) {
        $iscc = Get-Command "ISCC.exe" -ErrorAction SilentlyContinue
        if ($null -ne $iscc) {
            $isccPath = $iscc.Source
        }
    }

    if ([string]::IsNullOrWhiteSpace($isccPath)) {
        $candidatePaths = @(
            "D:\APP\Inno Setup 6\ISCC.exe",
            (Join-Path ${env:ProgramFiles(x86)} "Inno Setup 6\ISCC.exe"),
            (Join-Path $env:ProgramFiles "Inno Setup 6\ISCC.exe")
        )

        foreach ($candidatePath in $candidatePaths) {
            if (-not [string]::IsNullOrWhiteSpace($candidatePath) -and (Test-Path -LiteralPath $candidatePath)) {
                $isccPath = $candidatePath
                break
            }
        }
    }

    if ([string]::IsNullOrWhiteSpace($isccPath) -or -not (Test-Path -LiteralPath $isccPath)) {
        throw "ISCC.exe not found. Install Inno Setup, add ISCC.exe to PATH, or pass -InnoSetupCompiler <path>."
    }

    Push-Location $packageRoot
    try {
        & $isccPath $packageIssPath
    } finally {
        Pop-Location
    }
}

Write-Host "Windows package generated. Compile ForgexSetup.iss with Inno Setup to create the installer exe. Version: $Version"
