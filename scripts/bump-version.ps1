<#
.SYNOPSIS
  Raises the Morivia version for Android and iOS (PowerShell version of bump-version.py).

.DESCRIPTION
  gradle.properties holds the version. This script raises it, increases the build number by one,
  writes the same values to iosApp/Configuration/Version.xcconfig and starts a new
  "What's new" section in README.md. Describe the update there and in CHANGELOG.md afterwards.

.EXAMPLE
  .\scripts\bump-version.ps1 patch
#>
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('patch', 'minor', 'major')]
    [string]$Kind
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Read-Text([string]$path) { [System.IO.File]::ReadAllText($path, $utf8) }
function Write-Text([string]$path, [string]$text) { [System.IO.File]::WriteAllText($path, $text, $utf8) }

$propertiesPath = Join-Path $root 'gradle.properties'
$text = Read-Text $propertiesPath
$name = [regex]::Match($text, '(?m)^morivia\.versionName=(\d+)\.(\d+)\.(\d+)\r?$')
$code = [regex]::Match($text, '(?m)^morivia\.versionCode=(\d+)\r?$')
if (-not $name.Success -or -not $code.Success) {
    throw 'gradle.properties must contain morivia.versionName and morivia.versionCode.'
}

$major = [int]$name.Groups[1].Value
$minor = [int]$name.Groups[2].Value
$patch = [int]$name.Groups[3].Value
switch ($Kind) {
    'major' { $major++; $minor = 0; $patch = 0 }
    'minor' { $minor++; $patch = 0 }
    'patch' { $patch++ }
}
$version = "$major.$minor.$patch"
$build = [int]$code.Groups[1].Value + 1

$text = [regex]::Replace($text, '(?m)^morivia\.versionName=.*?(\r?)$', "morivia.versionName=$version`$1")
$text = [regex]::Replace($text, '(?m)^morivia\.versionCode=.*?(\r?)$', "morivia.versionCode=$build`$1")
Write-Text $propertiesPath $text

Write-Text (Join-Path $root 'iosApp/Configuration/Version.xcconfig') (
    "// Written by scripts/bump-version.py from gradle.properties. Do not edit by hand.`n" +
    "MARKETING_VERSION=$version`n" +
    "CURRENT_PROJECT_VERSION=$build`n")

# README: the version under the logo and a new "What's new" section for this version.
$readmePath = Join-Path $root 'README.md'
$readme = Read-Text $readmePath
$marker = [regex]::Matches($readme, '<!-- version -->[^<]*<!-- /version -->')
if ($marker.Count -ne 1) { throw 'README.md must contain one <!-- version -->X.Y.Z<!-- /version --> marker.' }
$readme = $readme.Replace($marker[0].Value, "<!-- version -->$version<!-- /version -->")
$section = [regex]::Match($readme, "(?ms)^## What's new in .*?(?=^See the \[changelog\])")
if (-not $section.Success) { throw "README.md must contain a '## What's new in X.Y.Z' section followed by the changelog link." }
$readme = $readme.Substring(0, $section.Index) + "## What's new in $version`n`n- TODO: describe this update.`n`n" +
    $readme.Substring($section.Index + $section.Length)
Write-Text $readmePath $readme

Write-Host "Morivia $version (build $build)."
Write-Host "Before committing, describe the update in README.md ('What's new in $version') and in a '## $version' section of CHANGELOG.md."
