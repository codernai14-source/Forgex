param(
    [string]$RuntimeIdentifier = "win-x64",
    [string]$Configuration = "Release",
    [string]$OutputDirectory = "",
    [switch]$FrameworkDependent
)

$ErrorActionPreference = "Stop"

$issuerRoot = Join-Path $PSScriptRoot "issuer\FxLicenseIssuer"
$projectPath = Join-Path $issuerRoot "FxLicenseIssuer.csproj"
$publishRoot = Join-Path $PSScriptRoot "issuer\publish"
$outputPath = if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    Join-Path $publishRoot $RuntimeIdentifier
} else {
    $OutputDirectory
}

if (-not (Test-Path -LiteralPath $projectPath)) {
    throw "FxLicenseIssuer project not found: $projectPath"
}

$resolvedPublishRoot = [System.IO.Path]::GetFullPath($publishRoot)
$resolvedOutputPath = [System.IO.Path]::GetFullPath($outputPath)
if (-not $resolvedOutputPath.StartsWith($resolvedPublishRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refuse to clean publish directory outside expected root: $resolvedOutputPath"
}

if (Test-Path -LiteralPath $resolvedOutputPath) {
    Remove-Item -LiteralPath $resolvedOutputPath -Recurse -Force
}

$selfContained = -not $FrameworkDependent
dotnet publish $projectPath -c $Configuration -r $RuntimeIdentifier --self-contained:$selfContained `
    -p:PublishSingleFile=true `
    -p:IncludeNativeLibrariesForSelfExtract=true `
    -p:EnableCompressionInSingleFile=true `
    -p:DebugType=None `
    -p:DebugSymbols=false `
    -o $resolvedOutputPath

if ($LASTEXITCODE -ne 0) {
    throw "dotnet publish failed for FxLicenseIssuer."
}

$exePath = Join-Path $resolvedOutputPath "FxLicenseIssuer.exe"
if (-not (Test-Path -LiteralPath $exePath)) {
    throw "Published executable not found: $exePath"
}

Write-Host "FxLicenseIssuer published: $exePath"
