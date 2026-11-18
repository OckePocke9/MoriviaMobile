<#
.SYNOPSIS
  Copies a published Morivia release to the public OckePocke9/MoriviaUpdates repository.

.DESCRIPTION
  MoriviaMobile is private, so the app cannot read its releases. The in-app updater reads the
  latest release of the public MoriviaUpdates repository instead, which holds only the Android
  APK, the checksums and the release notes (no source code). publish-release.ps1 runs this script
  after every release; it can also be run on its own for a version already built in
  build/release-X.Y.Z. A version that already exists in MoriviaUpdates is never replaced.

.EXAMPLE
  .\scripts\publish-update-mirror.ps1 -Version 1.5.0
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Version,
    [string]$Commit
)

$ErrorActionPreference = 'Continue'
$mirror = 'OckePocke9/MoriviaUpdates'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$utf8 = New-Object System.Text.UTF8Encoding($false)
function Fail([string]$message) { Write-Host $message -ForegroundColor Red; exit 1 }
$git = 'git'
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { $git = 'C:\Program Files\Git\cmd\git.exe' }

$dist = Join-Path $root "build/release-$Version"
$apkName = "Morivia-Android-$Version-arm64-v8a.apk"
$apk = Join-Path $dist $apkName
if (-not (Test-Path $apk)) { Fail "No $apkName in build/release-$Version. Run publish-release.ps1 first." }

$request = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllText($request, "protocol=https`nhost=github.com`n`n", $utf8)
$credential = cmd /c "`"$git`" credential fill < `"$request`" 2>nul"
Remove-Item $request -ErrorAction SilentlyContinue
$token = ($credential | Where-Object { $_ -like 'password=*' }) -replace '^password=', ''
if (-not $token) { Fail 'No GitHub login found. Sign in once with: git fetch' }
$headers = @{ Authorization = "Bearer $token"; Accept = 'application/vnd.github+json' }
$api = "https://api.github.com/repos/$mirror"

try {
    Invoke-RestMethod -ErrorAction Stop -Headers $headers -Uri "$api/releases/tags/v$Version" | Out-Null
    Write-Host "Morivia $Version is already in $mirror." -ForegroundColor Yellow
    exit 0
} catch [System.Net.WebException] {
    if ($_.Exception.Response.StatusCode.value__ -ne 404) { throw }
}

$changelog = [System.IO.File]::ReadAllText((Join-Path $root 'CHANGELOG.md'), $utf8)
$notes = [regex]::Match($changelog, "(?ms)^## $([regex]::Escape($Version))\r?\n(.*?)(?=^## |\z)").Groups[1].Value.Trim()
$body = $notes + "`n`n## Download`n`n- **Android phones and tablets:** ``$apkName```n`nInstall it over the earlier version; your data stays. SHA256SUMS.txt lists the checksum."

$release = Invoke-RestMethod -ErrorAction Stop -Method Post -Headers $headers -Uri "$api/releases" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes((@{
        tag_name = "v$Version"; name = "Morivia $Version"; body = $body; draft = $true
    } | ConvertTo-Json -Depth 3))) -ContentType 'application/json; charset=utf-8'

$sums = Join-Path $dist 'SHA256SUMS-android.txt'
[System.IO.File]::WriteAllText($sums, "$((Get-FileHash $apk -Algorithm SHA256).Hash.ToLower())  $apkName`n", $utf8)
foreach ($file in @(@{ Path = $apk; Name = $apkName; Type = 'application/vnd.android.package-archive' },
                    @{ Path = $sums; Name = 'SHA256SUMS.txt'; Type = 'text/plain' })) {
    Write-Host "Uploading $($file.Name) to $mirror..."
    Invoke-RestMethod -ErrorAction Stop -Method Post -Headers @{ Authorization = "Bearer $token" } `
        -Uri "https://uploads.github.com/repos/$mirror/releases/$($release.id)/assets?name=$($file.Name)" `
        -InFile $file.Path -ContentType $file.Type -TimeoutSec 1800 | Out-Null
}
$published = Invoke-RestMethod -ErrorAction Stop -Method Patch -Headers $headers -Uri "$api/releases/$($release.id)" `
    -Body '{"draft":false,"make_latest":"true"}' -ContentType 'application/json'
Write-Host "Published $($published.name) for the in-app updater: $($published.html_url)" -ForegroundColor Green
