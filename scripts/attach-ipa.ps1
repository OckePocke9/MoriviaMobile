<#
.SYNOPSIS
  Adds the iPhone and iPad IPA from a finished "Morivia iPhone and iPad" workflow run to the
  published Android release of the same version.

.DESCRIPTION
  Android releases are published from this PC with publish-release.ps1; the IPA is built later on
  GitHub's free macOS runners. This script downloads the run's IPA, checks it, and adds it to the
  release vX.Y with a download line and its SHA-256 in the release notes. Files already in the
  release are never replaced, and a release that already has an IPA is left alone.

.EXAMPLE
  .\scripts\attach-ipa.ps1 -RunId 45983420316
#>
param(
    [Parameter(Mandatory)] [long]$RunId
)

$ErrorActionPreference = 'Stop'
$repository = 'OckePocke9/MoriviaMobile'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$utf8 = New-Object System.Text.UTF8Encoding($false)
function Fail([string]$message) { Write-Host $message -ForegroundColor Red; exit 1 }
$git = 'git'
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { $git = 'C:\Program Files\Git\cmd\git.exe' }

$properties = [System.IO.File]::ReadAllText((Join-Path $root 'gradle.properties'), $utf8)
$version = [regex]::Match($properties, '(?m)^morivia\.versionName=(\d+\.\d+)\r?$').Groups[1].Value
if (-not $version) { Fail 'gradle.properties has no valid morivia.versionName.' }

# GitHub login from Git Credential Manager, as in publish-release.ps1.
$credentialRequest = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllText($credentialRequest, "protocol=https`nhost=github.com`npath=$repository.git`n`n", $utf8)
$credential = cmd /c "`"$git`" -c credential.interactive=never credential fill < `"$credentialRequest`""
Remove-Item $credentialRequest -ErrorAction SilentlyContinue
$token = ($credential | Where-Object { $_ -like 'password=*' }) -replace '^password=', ''
if (-not $token) { Fail 'No GitHub login found. Sign in once with: git fetch' }
$headers = @{ Authorization = "Bearer $token"; Accept = 'application/vnd.github+json' }
$api = "https://api.github.com/repos/$repository"

$run = Invoke-RestMethod -Headers $headers -Uri "$api/actions/runs/$RunId"
if ($run.name -ne 'Morivia iPhone and iPad' -or $run.conclusion -ne 'success') { Fail "Run $RunId is not a successful iPhone and iPad build." }
$release = Invoke-RestMethod -Headers $headers -Uri "$api/releases/tags/v$version"
if ($release.assets | Where-Object { $_.name -like '*.ipa' }) { Fail "Morivia $version already has an IPA; nothing was changed." }

$artifact = (Invoke-RestMethod -Headers $headers -Uri "$api/actions/runs/$RunId/artifacts").artifacts |
    Where-Object { $_.name -eq 'morivia-ios-Release' } | Select-Object -First 1
if (-not $artifact) { Fail 'The run has no Release IPA artifact.' }

$work = Join-Path $root "build/ipa-$RunId"
New-Item -ItemType Directory -Force $work | Out-Null
$zip = Join-Path $work 'artifact.zip'
Invoke-WebRequest -Headers $headers -Uri $artifact.archive_download_url -OutFile $zip -UseBasicParsing
Expand-Archive -Path $zip -DestinationPath $work -Force
$ipa = Get-ChildItem $work -Filter "Morivia-iOS-$version-release.ipa" | Select-Object -First 1
if (-not $ipa) { Fail "The artifact has no Morivia-iOS-$version-release.ipa." }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$package = [System.IO.Compression.ZipFile]::OpenRead($ipa.FullName)
try {
    foreach ($entry in 'Info.plist', 'Morivia') {
        if (-not $package.GetEntry("Payload/Morivia.app/$entry")) { Fail "The IPA lacks Payload/Morivia.app/$entry." }
    }
} finally { $package.Dispose() }

Write-Host "Uploading $($ipa.Name) ($([math]::Round($ipa.Length / 1MB, 1)) MB)..."
Invoke-RestMethod -Method Post -Headers @{ Authorization = "Bearer $token" } `
    -Uri "https://uploads.github.com/repos/$repository/releases/$($release.id)/assets?name=$($ipa.Name)" `
    -InFile $ipa.FullName -ContentType 'application/octet-stream' -TimeoutSec 1800 | Out-Null

$sha = (Get-FileHash $ipa.FullName -Algorithm SHA256).Hash.ToLower()
$line = "- **iPhone and iPad** (iOS or iPadOS 16.1 or newer): ``$($ipa.Name)``. Install the unsigned IPA with [Sideloadly](https://sideloadly.io/). SHA-256: ``$sha``"
$body = $release.body -replace '(?m)^(SHA256SUMS\.txt lists the checksums)', "$line`n`n`$1"
if ($body -eq $release.body) { $body = $release.body.TrimEnd() + "`n`n$line`n" }
Invoke-RestMethod -Method Patch -Headers $headers -Uri "$api/releases/$($release.id)" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes((@{ body = $body } | ConvertTo-Json))) -ContentType 'application/json; charset=utf-8' | Out-Null
Write-Host "Added $($ipa.Name) to Morivia ${version}: $($release.html_url)" -ForegroundColor Green
