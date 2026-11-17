<#
.SYNOPSIS
  Builds the Morivia Android APK on this PC and publishes the release "Morivia X.Y.Z" on GitHub.

.DESCRIPTION
  GitHub Actions is turned off for this repository, so releases are published from a PC.
  The script:
    1. checks that the version, README ("What's new in X.Y.Z") and CHANGELOG ("## X.Y.Z") are
       written, that the work is committed and pushed, and that the version is not published yet;
    2. runs the Android host tests and builds the signed release APK, then checks its app ID,
       version, name and signature;
    3. creates the release vX.Y.Z on the pushed commit with the APK, an optional iOS IPA built on a
       Mac (-IpaPath), README.md, CHANGELOG.md, LICENSE and SHA256SUMS.txt, marked Latest.
  It signs in with the GitHub login Git Credential Manager already has. A published version is
  never replaced.

.EXAMPLE
  .\scripts\publish-release.ps1
.EXAMPLE
  .\scripts\publish-release.ps1 -ApkPath androidApp\build\outputs\apk\full\release\androidApp-full-arm64-v8a-release.apk
.EXAMPLE
  .\scripts\publish-release.ps1 -IpaPath C:\Downloads\Morivia-iOS-1.4.0.ipa
#>
param(
    # An APK already built in Android Studio (full release variant). It is checked but not
    # rebuilt. Without it the script runs the tests and builds the APK itself.
    [string]$ApkPath,
    # An unsigned iOS IPA of the same version, built on a Mac with scripts/build-ios-ipa.sh.
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
$version = [regex]::Match($properties, '(?m)^morivia\.versionName=(\d+\.\d+\.\d+)\r?$').Groups[1].Value
$code = [regex]::Match($properties, '(?m)^morivia\.versionCode=(\d+)\r?$').Groups[1].Value
if (-not $version -or -not $code) { Fail 'gradle.properties has no valid morivia.versionName / morivia.versionCode.' }
$xcconfig = Read-Text 'iosApp/Configuration/Version.xcconfig'
if ($xcconfig -notmatch "(?m)^MARKETING_VERSION=$([regex]::Escape($version))\r?$" -or
    $xcconfig -notmatch "(?m)^CURRENT_PROJECT_VERSION=$code\r?$") {
    Fail 'Version.xcconfig does not match gradle.properties. Use scripts/bump-version.ps1.'
}
$readme = Read-Text 'README.md'
$changelog = Read-Text 'CHANGELOG.md'
if ($changelog -notmatch "(?m)^## $([regex]::Escape($version))\r?$") { Fail "CHANGELOG.md has no '## $version' section." }
if ($readme -notmatch "(?m)^## What's new in $([regex]::Escape($version))\r?$") { Fail "README.md has no 'What's new in $version' section." }
if ($readme -notmatch "<!-- version -->$([regex]::Escape($version))<!-- /version -->") { Fail "README.md shows another version than $version." }
if ($readme -match 'TODO: describe this update') { Fail 'Describe the update in README.md before releasing.' }

# Committed and pushed, so the tag points at exactly what is built.
if (& $git status --porcelain --untracked-files=no -- . ':!MPVKit') { Fail 'Commit your changes first.' }
& $git fetch --quiet origin main
$head = (& $git rev-parse HEAD).Trim()
$remote = (& $git rev-parse origin/main).Trim()
if ($head -ne $remote) { Fail 'Push main to GitHub first (HEAD is not origin/main).' }

# GitHub login from Git Credential Manager.
$request = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllText($request, "protocol=https`nhost=github.com`n`n", (New-Object System.Text.UTF8Encoding($false)))
$credential = cmd /c "`"$git`" credential fill < `"$request`" 2>nul"
Remove-Item $request -ErrorAction SilentlyContinue
$token = ($credential | Where-Object { $_ -like 'password=*' }) -replace '^password=', ''
if (-not $token) { Fail 'No GitHub login found. Sign in once with: git fetch' }
$headers = @{ Authorization = "Bearer $token"; Accept = 'application/vnd.github+json' }
$api = "https://api.github.com/repos/$repository"
try {
    Invoke-RestMethod -ErrorAction Stop -Headers $headers -Uri "$api/releases/tags/v$version" | Out-Null
    Fail "Morivia $version is already published. Raise the version with scripts/bump-version.ps1."
} catch [System.Net.WebException] {
    if ($_.Exception.Response.StatusCode.value__ -ne 404) { throw }
}

if ($IpaPath -and -not (Test-Path $IpaPath)) { Fail "IPA not found: $IpaPath" }
if ($ApkPath -and -not (Test-Path $ApkPath)) { Fail "APK not found: $ApkPath" }

# 2. Build and check the Android APK
if (-not $env:JAVA_HOME) { $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr' }
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not (Test-Path 'local.properties')) { New-Item -ItemType File 'local.properties' | Out-Null }
if ($ApkPath) {
    $apk = Get-Item $ApkPath
} else {
    $tasks = @(':androidApp:assembleFullRelease')
    if (-not $SkipTests) { $tasks = @(':composeApp:testAndroidHostTest') + $tasks }
    & .\gradlew.bat @tasks '-Pnuvio.android.distribution=full' '-PreleaseMinifyEnabled=false' --console=plain
    if ($LASTEXITCODE -ne 0) { Fail 'The Android build or its tests failed; nothing was published.' }
    $apk = Get-ChildItem 'androidApp/build/outputs/apk/full/release' -Filter '*arm64-v8a-release.apk' | Select-Object -First 1
}
if (-not $apk) { Fail 'No APK was built.' }
$tools = Get-ChildItem (Join-Path $env:ANDROID_HOME 'build-tools') | Sort-Object { [version]($_.Name -replace '[^0-9.].*$', '') } | Select-Object -Last 1
$badging = & (Join-Path $tools.FullName 'aapt.exe') dump badging $apk.FullName
foreach ($expected in "name='com.morivia.app'", "versionName='$version'", "versionCode='$code'", "application-label:'Morivia'") {
    if (-not ($badging | Select-String -SimpleMatch $expected)) { Fail "The APK does not contain $expected." }
}
& (Join-Path $tools.FullName 'apksigner.bat') verify $apk.FullName
if ($LASTEXITCODE -ne 0) { Fail 'The APK signature does not verify.' }

# 3. Publish
$dist = Join-Path $root "build/release-$version"
if (Test-Path $dist) { Remove-Item -Recurse -Force $dist }
New-Item -ItemType Directory -Force $dist | Out-Null
$apkName = "Morivia-Android-$version-arm64-v8a.apk"
Copy-Item $apk.FullName (Join-Path $dist $apkName)
$ipaName = "Morivia-iOS-$version.ipa"
if ($IpaPath) { Copy-Item $IpaPath (Join-Path $dist $ipaName) }
foreach ($file in 'README.md', 'CHANGELOG.md', 'LICENSE') { Copy-Item $file (Join-Path $dist $file) }
$sums = Get-ChildItem $dist -Filter 'Morivia-*' | ForEach-Object {
    "$((Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower())  $($_.Name)"
}
[System.IO.File]::WriteAllText((Join-Path $dist 'SHA256SUMS.txt'), (($sums -join "`n") + "`n"), $utf8)

$notes = [regex]::Match($changelog, "(?ms)^## $([regex]::Escape($version))\r?\n(.*?)(?=^## |\z)").Groups[1].Value.Trim()
$download = "`n`n## Download`n`n- **Android phones and tablets:** ``$apkName```n"
if ($IpaPath) {
    $download += "- **iPhone and iPad:** ``$ipaName`` (unsigned, see README for how to install)`n"
} else {
    $download += "- **iPhone and iPad:** no IPA in this release. iOS builds need a Mac, and GitHub Actions is turned off for this repository.`n"
}
$download += "`nSHA256SUMS.txt lists the checksums. Built and published from a PC with scripts/publish-release.ps1 from commit $($head.Substring(0, 8)).`n"

$body = @{ tag_name = "v$version"; target_commitish = $head; name = "Morivia $version"; body = $notes + $download; draft = $true } |
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
