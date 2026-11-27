<#
.SYNOPSIS
  Recolours the original Morivia logo for every theme, keeping its details.

.DESCRIPTION
  The ribbons, their shading and the white play button of the original logo stay as they are;
  only the colour changes. Each pixel takes the theme gradient's colour at its place on the
  diagonal (top left to bottom right) and keeps its own lightness difference from the original
  logo's average at that place, so the darker ribbon and the light play button stay visible.

  Reads the *_original images and writes, for every theme colour:
    assets/branding/theme-symbols/morivia_symbol_<c>.png (master) and composeResources/drawable/morivia_symbol_<c>.webp, app_icon_<c>.webp, morivia_brand_symbol_<c>.webp
    androidMain/res/drawable-nodpi/morivia_foreground_<c>.webp, morivia_launcher_<c>.webp
  The WebP files are lossless and need Google's cwebp (see scripts/webp.ps1).
  Run scripts/generate-wordmarks.ps1 afterwards to redraw the wordmarks.

.EXAMPLE
  .\scripts\recolor-theme-icons.ps1
#>
param([string[]]$Only)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webp.ps1')
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;

public static class MoriviaRecolor {
    static double Lum(double r, double g, double b) { return 0.3 * r + 0.59 * g + 0.11 * b; }

    static void SetLum(ref double r, ref double g, ref double b, double l) {
        double d = l - Lum(r, g, b);
        r += d; g += d; b += d;
        double L = Lum(r, g, b);
        double n = Math.Min(r, Math.Min(g, b)), x = Math.Max(r, Math.Max(g, b));
        if (n < 0 && L - n > 1e-9) { r = L + (r - L) * L / (L - n); g = L + (g - L) * L / (L - n); b = L + (b - L) * L / (L - n); }
        if (x > 1 && x - L > 1e-9) { r = L + (r - L) * (1 - L) / (x - L); g = L + (g - L) * (1 - L) / (x - L); b = L + (b - L) * (1 - L) / (x - L); }
    }

    static void PaletteAt(int[] stops, double s, out double r, out double g, out double b) {
        if (stops.Length == 1) { r = ((stops[0] >> 16) & 255) / 255.0; g = ((stops[0] >> 8) & 255) / 255.0; b = (stops[0] & 255) / 255.0; return; }
        double p = Math.Max(0, Math.Min(1, s)) * (stops.Length - 1);
        int i = Math.Min((int)Math.Floor(p), stops.Length - 2);
        double t = p - i;
        int a = stops[i], c = stops[i + 1];
        r = (((a >> 16) & 255) * (1 - t) + ((c >> 16) & 255) * t) / 255.0;
        g = (((a >> 8) & 255) * (1 - t) + ((c >> 8) & 255) * t) / 255.0;
        b = ((a & 255) * (1 - t) + (c & 255) * t) / 255.0;
    }

    public static Bitmap Apply(Bitmap source, int[] stops, double maxBaseLum) {
        int w = source.Width, h = source.Height;
        Bitmap src = new Bitmap(w, h, PixelFormat.Format32bppArgb);
        using (Graphics gr = Graphics.FromImage(src)) { gr.DrawImage(source, new Rectangle(0, 0, w, h)); }
        BitmapData data = src.LockBits(new Rectangle(0, 0, w, h), ImageLockMode.ReadWrite, PixelFormat.Format32bppArgb);
        byte[] px = new byte[data.Stride * h];
        Marshal.Copy(data.Scan0, px, 0, px.Length);

        int minX = w, minY = h, maxX = 0, maxY = 0;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            if (px[y * data.Stride + x * 4 + 3] >= 64) { minX = Math.Min(minX, x); maxX = Math.Max(maxX, x); minY = Math.Min(minY, y); maxY = Math.Max(maxY, y); }
        }
        double bw = Math.Max(1, maxX - minX), bh = Math.Max(1, maxY - minY);
        const int bins = 64;
        double[] sum = new double[bins]; int[] count = new int[bins];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int o = y * data.Stride + x * 4;
            if (px[o + 3] < 200) continue;
            double b = px[o] / 255.0, g = px[o + 1] / 255.0, r = px[o + 2] / 255.0;
            if (Math.Min(r, Math.Min(g, b)) > 0.75) continue; // the white play button
            double s = ((x - minX) / bw + (y - minY) / bh) / 2;
            int k = Math.Max(0, Math.Min(bins - 1, (int)(s * bins)));
            sum[k] += Lum(r, g, b); count[k]++;
        }
        double[] mean = new double[bins];
        for (int k = 0; k < bins; k++) {
            int lo = k, hi = k;
            while (count[lo] == 0 && lo > 0) lo--;
            while (count[hi] == 0 && hi < bins - 1) hi++;
            double a = count[lo] > 0 ? sum[lo] / count[lo] : 0.5, c = count[hi] > 0 ? sum[hi] / count[hi] : a;
            mean[k] = count[k] > 0 ? sum[k] / count[k] : (a + c) / 2;
        }
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int o = y * data.Stride + x * 4;
            if (px[o + 3] == 0) continue;
            double ob = px[o] / 255.0, og = px[o + 1] / 255.0, or = px[o + 2] / 255.0;
            double s = ((x - minX) / bw + (y - minY) / bh) / 2;
            int k = Math.Max(0, Math.Min(bins - 1, (int)(s * bins)));
            double r, g, b;
            PaletteAt(stops, s, out r, out g, out b);
            double baseLum = Math.Min(Lum(r, g, b), maxBaseLum);
            double target = Math.Max(0, Math.Min(1, baseLum + (Lum(or, og, ob) - mean[k])));
            SetLum(ref r, ref g, ref b, target);
            px[o + 2] = (byte)Math.Round(Math.Max(0, Math.Min(1, r)) * 255);
            px[o + 1] = (byte)Math.Round(Math.Max(0, Math.Min(1, g)) * 255);
            px[o] = (byte)Math.Round(Math.Max(0, Math.Min(1, b)) * 255);
        }
        Marshal.Copy(px, 0, data.Scan0, px.Length);
        src.UnlockBits(data);
        return src;
    }
}
'@

$root = Split-Path -Parent $PSScriptRoot
$drawable = Join-Path $root 'composeApp/src/commonMain/composeResources/drawable'
$symbols = Join-Path $root 'assets/branding/theme-symbols'
$nodpi = Join-Path $root 'composeApp/src/androidMain/res/drawable-nodpi'
$themeSource = Get-Content -Raw (Join-Path $root 'composeApp/src/commonMain/kotlin/com/morivia/app/core/ui/ThemeColors.kt')

# Theme gradients from ThemeColors.kt (Copper has no theme and keeps its own colours).
$themes = [ordered]@{
    arctic_blue = 'ArcticBlue'; emerald = 'Emerald'; rose_gold = 'RoseGold'; copper = $null; graphite = 'Graphite'
    gold = 'Gold'; jade = 'Jade'; crimson = 'Crimson'; ocean = 'Ocean'; violet = 'Violet'; amber = 'Amber'; rose = 'Rose'; white = 'White'
}
$themes['amethyst'] = 'Amethyst'
$themes['apricot'] = 'Apricot'
$themes['aubergine'] = 'Aubergine'
$themes['azure'] = 'Azure'
$themes['basil'] = 'Basil'
$themes['berry'] = 'Berry'
$themes['blush'] = 'Blush'
$themes['cerulean'] = 'Cerulean'
$themes['cherry'] = 'Cherry'
$themes['cobalt'] = 'Cobalt'
$themes['coral'] = 'Coral'
$themes['denim'] = 'Denim'
$themes['fern'] = 'Fern'
$themes['glacier'] = 'Glacier'
$themes['grape'] = 'Grape'
$themes['heather'] = 'Heather'
$themes['indigo'] = 'Indigo'
$themes['lagoon'] = 'Lagoon'
$themes['lavender'] = 'Lavender'
$themes['lemon'] = 'Lemon'
$themes['lilac'] = 'Lilac'
$themes['lime'] = 'Lime'
$themes['mango'] = 'Mango'
$themes['mint'] = 'Mint'
$themes['moss'] = 'Moss'
$themes['orchid'] = 'Orchid'
$themes['peach'] = 'Peach'
$themes['periwinkle'] = 'Periwinkle'
$themes['pine'] = 'Pine'
$themes['saffron'] = 'Saffron'
$themes['teal'] = 'Teal'
function Get-Stops([string]$name) {
    if (-not $name) { return @(0xFFD6A4, 0xC7793A, 0x80421D) }
    $block = ($themeSource -split "val $name = ThemeColorPalette\(")[1] -split "`n    val " | Select-Object -First 1
    if ($block -match 'accentGradient = listOf\(([\s\S]*?)nativeAccentHex') {
        $colors = [regex]::Matches($Matches[1], '0xFF([0-9A-Fa-f]{6})') | ForEach-Object { [Convert]::ToInt32($_.Groups[1].Value, 16) }
        if ($colors.Count -gt 1) { return @($colors) }
    }
    $sec = [regex]::Match($block, 'secondary = Color\(0xFF([0-9A-Fa-f]{6})\)').Groups[1].Value
    $var = [regex]::Match($block, 'secondaryVariant = Color\(0xFF([0-9A-Fa-f]{6})\)').Groups[1].Value
    return @([Convert]::ToInt32($sec, 16), [Convert]::ToInt32($var, 16))
}

# Light palettes are darkened a little so the white play button stays visible.
$maxBaseLum = 0.80

function Save-Recolored([string]$from, [string]$to, [int[]]$stops) {
    $img = [System.Drawing.Bitmap]::FromFile($from)
    $out = [MoriviaRecolor]::Apply($img, $stops, $maxBaseLum)
    $img.Dispose()
    $out.Save($to, [System.Drawing.Imaging.ImageFormat]::Png)
    return $out
}
function Save-OnBlack($symbol, [string]$to, [int]$size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Black)
    $g.DrawImage($symbol, 0, 0, $size, $size)
    $g.Dispose()
    $bmp.Save($to, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

foreach ($key in $themes.Keys) {
    if ($Only -and $key -notin $Only) { continue }
    $stops = Get-Stops $themes[$key]
    $symbol = Save-Recolored (Join-Path $symbols 'morivia_symbol_original.png') (Join-Path $symbols "morivia_symbol_$key.png") $stops
    Copy-Item (Join-Path $symbols "morivia_symbol_$key.png") (Join-Path $drawable "morivia_symbol_$key.png")
    Convert-ToWebp (Join-Path $drawable "morivia_symbol_$key.png")
    Save-OnBlack $symbol (Join-Path $drawable "app_icon_$key.png") 512
    Save-OnBlack $symbol (Join-Path $nodpi "morivia_launcher_$key.png") 512
    $symbol.Dispose()
    (Save-Recolored (Join-Path $nodpi 'morivia_foreground_original.png') (Join-Path $nodpi "morivia_foreground_$key.png") $stops).Dispose()
    (Save-Recolored (Join-Path $drawable 'morivia_brand_symbol.png') (Join-Path $drawable "morivia_brand_symbol_$key.png") $stops).Dispose()
    # The PNG masters of morivia_symbol_<c> stay in assets/branding/theme-symbols (generate-wordmarks.ps1 reads them); the app ships lossless WebP.
    Convert-ToWebp (Join-Path $drawable "app_icon_$key.png")
    Convert-ToWebp (Join-Path $nodpi "morivia_launcher_$key.png")
    Convert-ToWebp (Join-Path $nodpi "morivia_foreground_$key.png")
    Convert-ToWebp (Join-Path $drawable "morivia_brand_symbol_$key.png")
    Write-Host "$key : $(($stops | ForEach-Object { '#{0:X6}' -f $_ }) -join ' ')"
}
