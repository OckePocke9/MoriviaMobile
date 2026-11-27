<#
.SYNOPSIS
  Builds the Morivia Android APK on this PC and publishes the release "Morivia Android vX.Y" on GitHub.

.DESCRIPTION
  Android releases are built and published from this PC; the iPhone and iPad IPA comes from
  the manual GitHub Actions workflow morivia-ios.yml.
  The script:
    1. checks that the version, README ("What's new in X.Y") and CHANGELOG ("## X.Y") are
       written, that the work is committed and pushed, and that the version is not published yet;
    2. runs the Android host tests and builds the signed release APKs (one per processor type and a
       universal one), then checks each one's app ID, version, name and signature;
    3. creates the release vX.Y on the pushed commit with the APKs, README.md, CHANGELOG.md, LICENSE
       and SHA256SUMS.txt, marked Latest. The app's update banner reads this repository's
       latest release.
  It signs in with the GitHub login Git Credential Manager already has. A published version is
  never replaced.

.EXAMPLE
  .\scripts\publish-release.ps1
.EXAMPLE
  .\scripts\publish-release.ps1 -ApkPath androidApp\build\outputs\apk\full\release
#>
param(
    # A folder with APKs already built (full release variant, every processor type). They are
    # checked but not rebuilt. Without it the script runs the tests and builds the APKs itself.
    [string]$ApkPath,
    # Matching unsigned device IPA, built and version-checked from the same commit.
    [string]$IpaPath,
    # Skip the Android host tests (about 10 minutes). Not recommended.
    [switch]$SkipTests
)

# Native tools (git, Gradle) write warnings to stderr; failures are checked through their exit codes.
$ErrorActionPreference = 'Continue'
$repository = 'OckePocke9/MoriviaMobile'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$utf8 = New-Object System.Text.UTF8Encoding($false)
function Read-Text([string]$path) { [System.IO.File]::ReadAllText((Join-Path $root $path), $utf8) }
function Fail([string]$message) { Write-Host $message -ForegroundColor Red; exit 1 }
$git = 'git'
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { $git = 'C:\Program Files\Git\cmd\git.exe' }

# 1. Version, README and CHANGELOG
$properties = Read-Text 'gradle.properties'
$version = [regex]::Match($properties, '(?m)^morivia\.versionName=(\d+\.\d+(?:\.\d+)?)\r?$').Groups[1].Value
$code = [regex]::Match($properties, '(?m)^morivia\.versionCode=(\d+)\r?$').Groups[1].Value
if (-not $version -or -not $code) { Fail 'gradle.properties has no valid morivia.versionName / morivia.versionCode.' }
$readme = Read-Text 'README.md'
$changelog = Read-Text 'CHANGELOG.md'
if ($changelog -notmatch "(?m)^## $([regex]::Escape($version))\r?$") { Fail "CHANGELOG.md has no '## $version' section." }
if ($readme -notmatch "(?m)^## What's new in $([regex]::Escape($version))\r?$") { Fail "README.md has no 'What's new in $version' section." }
if ($readme -notmatch "<!-- version -->$([regex]::Escape($version))<!-- /version -->") { Fail "README.md shows another version than $version." }
if ($readme -match 'TODO: describe this update') { Fail 'Describe the update in README.md before releasing.' }

# Committed and pushed, so the tag points at exactly what is built.
if (& $git status --porcelain --untracked-files=no) { Fail 'Commit your changes first.' }
& $git fetch --quiet origin main
$head = (& $git rev-parse HEAD).Trim()
$remote = (& $git rev-parse origin/main).Trim()
if ($head -ne $remote) { Fail 'Push main to GitHub first (HEAD is not origin/main).' }

# GitHub login from Git Credential Manager.
# Windows PowerShell pipes text to native programs with a byte-order mark and CRLF, which git
# rejects; a plain LF file through cmd works in every PowerShell.
$credentialRequest = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllText($credentialRequest, "protocol=https`nhost=github.com`npath=$repository.git`n`n", $utf8)
$credential = cmd /c "`"$git`" -c credential.interactive=never credential fill < `"$credentialRequest`""
Remove-Item $credentialRequest -ErrorAction SilentlyContinue
$token = ($credential | Where-Object { $_ -like 'password=*' }) -replace '^password=', ''
if (-not $token) { Fail 'No GitHub login found. Sign in once with: git fetch' }
$headers = @{ Authorization = "Bearer $token"; Accept = 'application/vnd.github+json' }
$api = "https://api.github.com/repos/$repository"
try {
    Invoke-RestMethod -ErrorAction Stop -Headers $headers -Uri "$api/releases/tags/v$version" | Out-Null
    Fail "Morivia Android v$version is already published. Raise the version with scripts/bump-version.ps1."
} catch {
    if ([int]$_.Exception.Response.StatusCode -ne 404) { throw }
}

if ($ApkPath -and -not (Test-Path $ApkPath)) { Fail "APK not found: $ApkPath" }

$ipa = $null
if ($IpaPath) {
    if (-not (Test-Path -LiteralPath $IpaPath -PathType Leaf)) { Fail "IPA not found: $IpaPath" }
    $ipa = Get-Item -LiteralPath $IpaPath
    if ($ipa.Name -cne "Morivia-iOS-$version-release.ipa") { Fail "Use the Release IPA from the iOS build for $version." }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $package = [System.IO.Compression.ZipFile]::OpenRead($ipa.FullName)
    try {
        foreach ($entry in 'Info.plist', 'Morivia') {
            if (-not $package.GetEntry("Payload/Morivia.app/$entry")) { Fail "IPA lacks the Morivia iOS app $entry." }
        }
        if ($package.GetEntry('Payload/Morivia.app/embedded.mobileprovision') -or
            $package.GetEntry('Payload/Morivia.app/_CodeSignature/CodeResources')) {
            Fail 'Use the unsigned IPA for Sideloadly.'
        }
    } finally {
        $package.Dispose()
    }
}

# 2. Build and check the Android APK
if (-not $env:JAVA_HOME) { $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr' }
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not (Test-Path 'local.properties')) { New-Item -ItemType File 'local.properties' | Out-Null }
# One APK per processor type and a universal one, so Morivia runs on every phone and tablet
# (owner, 2026-11-26). The in-app updater picks the right one by its name.
$abis = @('arm64-v8a', 'armeabi-v7a', 'x86_64', 'universal')
$apkDirectory = 'androidApp/build/outputs/apk/full/release'
if ($ApkPath) {
    $apkDirectory = $ApkPath
} else {
    $tasks = @(':androidApp:assembleFullRelease')
    if (-not $SkipTests) { $tasks = @(':composeApp:testAndroidHostTest') + $tasks }
    & .\gradlew.bat @tasks '-Pmorivia.android.distribution=full' --console=plain
    if ($LASTEXITCODE -ne 0) { Fail 'The Android build or its tests failed; nothing was published.' }
}
$tools = Get-ChildItem (Join-Path $env:ANDROID_HOME 'build-tools') | Sort-Object { [version]($_.Name -replace '[^0-9.].*$', '') } | Select-Object -Last 1
# Since 1.6 releases are signed with Morivia's private release key (kept outside Git, set up in
# local.properties). This is the public SHA-256 fingerprint of its certificate.
$releaseKeySha256 = 'b7132668066d058409aca81eb0aaa23441b9cf71ad01582e572311d94fa85900'
$apks = @{}
foreach ($abi in $abis) {
    $apk = Get-ChildItem $apkDirectory -Filter "*-$abi-release.apk" | Select-Object -First 1
    if (-not $apk) { Fail "No $abi APK was built." }
    $badging = & (Join-Path $tools.FullName 'aapt.exe') dump badging $apk.FullName
    foreach ($expected in "name='com.morivia.app'", "versionName='$version'", "versionCode='$code'", "application-label:'Morivia'") {
        if (-not ($badging | Select-String -SimpleMatch $expected)) { Fail "The $abi APK does not contain $expected." }
    }
    $signer = & (Join-Path $tools.FullName 'apksigner.bat') verify --print-certs $apk.FullName
    if ($LASTEXITCODE -ne 0) { Fail "The $abi APK signature does not verify." }
    if (-not ($signer | Select-String -SimpleMatch "certificate SHA-256 digest: $releaseKeySha256")) {
        Fail "The $abi APK is not signed with the Morivia release key. Set MORIVIA_RELEASE_* in local.properties."
    }
    $apks[$abi] = $apk
}

# 3. Publish
$dist = Join-Path $root "build/release-$version"
if (Test-Path $dist) { $dist = "$dist-" + [guid]::NewGuid().ToString("N") }
New-Item -ItemType Directory -Force $dist | Out-Null
foreach ($abi in $abis) { Copy-Item $apks[$abi].FullName (Join-Path $dist "Morivia-Android-$version-$abi.apk") }
if ($ipa) { Copy-Item -LiteralPath $ipa.FullName -Destination (Join-Path $dist $ipa.Name) }
foreach ($file in 'README.md', 'CHANGELOG.md', 'LICENSE') { Copy-Item $file (Join-Path $dist $file) }
$sums = Get-ChildItem $dist -Filter 'Morivia-*' | ForEach-Object {
    "$((Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower())  $($_.Name)"
}
[System.IO.File]::WriteAllText((Join-Path $dist 'SHA256SUMS.txt'), (($sums -join "`n") + "`n"), $utf8)

$notes = [regex]::Match($changelog, "(?ms)^## $([regex]::Escape($version))\r?\n(.*?)(?=^## |\z)").Groups[1].Value.Trim()
$download = "`n`n## Download`n`nAndroid phones and tablets with Android 7.0 or newer:`n`n" +
    "- ``Morivia-Android-$version-arm64-v8a.apk``: almost every phone and tablet from about 2017 on (Samsung Galaxy, Pixel, OnePlus, Xiaomi and others). Take this one if unsure.`n" +
    "- ``Morivia-Android-$version-armeabi-v7a.apk``: older or low-cost phones, tablets and TV boxes with 32-bit Android.`n" +
    "- ``Morivia-Android-$version-x86_64.apk``: Chromebooks with Intel or AMD processors and Android emulators.`n" +
    "- ``Morivia-Android-$version-universal.apk``: works on all of them, but is larger.`n`n" +
    "Morivia's in-app updates pick the right file for the device by themselves.`n"
if ($ipa) {
    $download += "- **iPhone and iPad with iOS or iPadOS 16.1 or newer:** ``$($ipa.Name)``. Install the unsigned IPA with [Sideloadly](https://sideloadly.io/).`n"
}
$download += "`nSHA256SUMS.txt lists the checksums. Built and published from a PC with scripts/publish-release.ps1 from commit $($head.Substring(0, 8)).`n"

$body = @{ tag_name = "v$version"; target_commitish = $head; name = "Morivia Android v$version"; body = $notes + $download; draft = $true } |
    ConvertTo-Json -Depth 3
$release = Invoke-RestMethod -ErrorAction Stop -Method Post -Headers $headers -Uri "$api/releases" `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -ContentType 'application/json; charset=utf-8'
foreach ($file in Get-ChildItem $dist) {
    $type = if ($file.Extension -in '.apk', '.ipa') { 'application/octet-stream' } else { 'text/plain' }
    Write-Host "Uploading $($file.Name)..."
    Invoke-RestMethod -ErrorAction Stop -Method Post -Headers @{ Authorization = "Bearer $token" } `
        -Uri "https://uploads.github.com/repos/$repository/releases/$($release.id)/assets?name=$($file.Name)" `
        -InFile $file.FullName -ContentType $type -TimeoutSec 1800 | Out-Null
}
$published = Invoke-RestMethod -ErrorAction Stop -Method Patch -Headers $headers -Uri "$api/releases/$($release.id)" `
    -Body '{"draft":false,"make_latest":"true"}' -ContentType 'application/json'
Write-Host "Published $($published.name): $($published.html_url)" -ForegroundColor Green
