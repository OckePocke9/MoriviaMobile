param([string[]]$Only)

<#
.SYNOPSIS
  Draws the Morivia wordmarks (app icon + "morivia" lettering) for every icon colour.

.DESCRIPTION
  The lettering follows the style of Nuvio's wordmark: heavy, rounded, lowercase strokes.
  The letters are drawn as strokes (no font file needed), white on a transparent background,
  to the right of the matching morivia_symbol_<colour>.png. Writes
  composeApp/src/commonMain/composeResources/drawable/app_logo_wordmark*.webp (lossless; needs
  Google's cwebp, see scripts/webp.ps1).

.EXAMPLE
  .\scripts\generate-wordmarks.ps1
#>
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webp.ps1')
Add-Type -AssemblyName System.Drawing
$root = Split-Path -Parent $PSScriptRoot
$drawable = Join-Path $root 'composeApp/src/commonMain/composeResources/drawable'

# Letter geometry in units: stroke centre lines, x-height from y=13 to y=100, stroke 26.
$stroke = 33.0
function New-Letter([string]$ch) {
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $dots = @()
    switch ($ch) {
        'm' {
            $p.StartFigure(); $p.AddLine(13, 100, 13, 44)
            $p.AddArc(13, 13, 62, 62, 180, 180); $p.AddLine(75, 44, 75, 100)
            $p.StartFigure(); $p.AddLine(75, 44, 75, 44)
            $p.AddArc(75, 13, 62, 62, 180, 180); $p.AddLine(137, 44, 137, 100)
            $width = 150
        }
        'o' { $p.AddEllipse(13, 13, 87, 87); $width = 113 }
        'r' {
            $p.StartFigure(); $p.AddLine(13, 100, 13, 46)
            $p.AddArc(13, 13, 66, 66, 180, 95)
            $width = 74
        }
        'i' { $p.StartFigure(); $p.AddLine(13, 100, 13, 13); $dots += ,@(13, -27); $width = 26 }
        'v' {
            $p.StartFigure(); $p.AddLine(13, 13, 53, 100); $p.AddLine(53, 100, 93, 13)
            $width = 106
        }
        'a' {
            $p.AddEllipse(13, 13, 87, 87)
            $p.StartFigure(); $p.AddLine(100, 13, 100, 100)
            $width = 113
        }
    }
    [pscustomobject]@{ Path = $p; Width = $width; Dots = $dots }
}

function Write-Wordmark([string]$symbolName, [string]$outName) {
    $symbol = [System.Drawing.Image]::FromFile((Join-Path $drawable "$symbolName.png"))
    $height = 282
    # Like Nuvio's wordmark: the logo fills the height, the lettering's x-height matches Nuvio's (about 31 % of the logo height).
    $logoHeight = 260
    # The symbol images have empty margins: use only the visible logo.
    $sb = New-Object System.Drawing.Bitmap $symbol
    $minX = $sb.Width; $minY = $sb.Height; $maxX = 0; $maxY = 0
    for ($yy = 0; $yy -lt $sb.Height; $yy += 2) {
        for ($xx = 0; $xx -lt $sb.Width; $xx += 2) {
            if ($sb.GetPixel($xx, $yy).A -gt 24) {
                if ($xx -lt $minX) { $minX = $xx }; if ($xx -gt $maxX) { $maxX = $xx }
                if ($yy -lt $minY) { $minY = $yy }; if ($yy -gt $maxY) { $maxY = $yy }
            }
        }
    }
    $sb.Dispose()
    $srcRect = New-Object System.Drawing.Rectangle $minX, $minY, ($maxX - $minX + 2), ($maxY - $minY + 2)
    $logoWidth = [int]($srcRect.Width * $logoHeight / $srcRect.Height)

    $letters = 'morivia'.ToCharArray() | ForEach-Object { New-Letter $_ }
    $gap = 27
    $textUnits = ($letters | Measure-Object -Property Width -Sum).Sum + $gap * ($letters.Count - 1)
    $scale = ($logoHeight * 0.315) / 113.0
    $textWidth = [int][math]::Ceiling(($textUnits + 6) * $scale)
    $iconGap = [int]($logoHeight * 0.24)
    $width = $logoWidth + $iconGap + $textWidth

    $bmp = New-Object System.Drawing.Bitmap $width, $height, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = 'AntiAlias'; $g.InterpolationMode = 'HighQualityBicubic'; $g.PixelOffsetMode = 'HighQuality'
    $g.Clear([System.Drawing.Color]::Transparent)
    $dest = New-Object System.Drawing.Rectangle 0, ([int](($height - $logoHeight) / 2)), $logoWidth, $logoHeight
    $g.DrawImage($symbol, $dest, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
    $iconSize = $logoWidth

    $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::White), ($stroke * $scale)
    $pen.StartCap = 'Round'; $pen.EndCap = 'Round'; $pen.LineJoin = 'Round'
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
    $x = $iconSize + $iconGap
    $top = ($height - 113 * $scale) / 2 + 12 * $scale
    foreach ($letter in $letters) {
        $m = New-Object System.Drawing.Drawing2D.Matrix
        $m.Translate($x, $top); $m.Scale($scale, $scale)
        $path = $letter.Path.Clone(); $path.Transform($m)
        $g.DrawPath($pen, $path)
        foreach ($d in $letter.Dots) {
            $r = 15 * $scale
            $g.FillEllipse($brush, $x + $d[0] * $scale - $r, $top + $d[1] * $scale - $r, 2 * $r, 2 * $r)
        }
        $x += ($letter.Width + $gap) * $scale
    }
    $g.Dispose()
    $bmp.Save((Join-Path $drawable "$outName.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose(); $symbol.Dispose()
    Convert-ToWebp (Join-Path $drawable "$outName.png")
    Write-Host "$outName.webp ($width x $height)"
}

if (-not $Only) {
    Write-Wordmark 'morivia_symbol_original' 'app_logo_wordmark'
    $Only = @(Get-ChildItem $drawable -Filter 'morivia_symbol_*.png' | ForEach-Object { $_.BaseName.Substring(15) })
}
foreach ($c in $Only) {
    Write-Wordmark "morivia_symbol_$c" "app_logo_wordmark_$c"
}
