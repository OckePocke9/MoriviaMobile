# Shared by recolor-theme-icons.ps1 and generate-wordmarks.ps1 (dot-sourced).
# Turns a PNG the script has just written into a lossless WebP next to it and removes the PNG.
# WebP keeps every pixel and is about half the size. cwebp comes from Google's libwebp
# (https://developers.google.com/speed/webp/download): set CWEBP to cwebp.exe, put it on PATH,
# or unpack libwebp into a "tools" folder next to the repository (C:\workspace\tools\libwebp-1.5.0).
function Get-Cwebp {
    if ($env:CWEBP -and (Test-Path $env:CWEBP)) { return $env:CWEBP }
    $onPath = Get-Command cwebp -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }
    $repoRoot = Split-Path -Parent $PSScriptRoot
    $local = Get-ChildItem (Join-Path (Split-Path -Parent $repoRoot) 'tools') -Recurse -Filter cwebp.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($local) { return $local.FullName }
    throw 'cwebp.exe not found. Install libwebp (see scripts/webp.ps1) or set CWEBP.'
}

function Convert-ToWebp([string]$png) {
    $webp = [System.IO.Path]::ChangeExtension($png, '.webp')
    & (Get-Cwebp) -quiet -lossless -exact -z 9 -mt $png -o $webp
    if ($LASTEXITCODE -ne 0) { throw "cwebp failed for $png" }
    Remove-Item $png
}
