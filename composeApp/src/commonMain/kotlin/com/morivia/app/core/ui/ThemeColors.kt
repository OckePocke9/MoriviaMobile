package com.morivia.app.core.ui

import androidx.compose.ui.graphics.Color

data class ThemeColorPalette(
    val secondary: Color,
    val secondaryVariant: Color,
    val accentGradient: List<Color> = listOf(secondary),
    val nativeAccentHex: String,
    val onSecondary: Color = Color.White,
    val onSecondaryVariant: Color = Color.White,
    val focusRing: Color,
    val focusBackground: Color,
    val background: Color = Color(0xFF0D0D0D),
    val backgroundElevated: Color = Color(0xFF1A1A1A),
    val backgroundCard: Color = Color(0xFF242424),
)

object ThemeColors {

    // The new app icon's colours (owner, 2026-11-23): magenta #F23BFF, violet, blue #037EFE, cyan #00CFFD.
    val Original = ThemeColorPalette(
        secondary = Color(0xFF6B4BF5),
        secondaryVariant = Color(0xFF00CFFD),
        accentGradient = listOf(Color(0xFFF23BFF), Color(0xFFA80CFE), Color(0xFF4B2BFD), Color(0xFF037EFE), Color(0xFF00CFFD)),
        nativeAccentHex = "#6B4BF5",
        onSecondary = Color.White,
        onSecondaryVariant = Color(0xFF111111),
        focusRing = Color(0xFF00CFFD),
        focusBackground = Color(0xFF261B4A),
        background = Color(0xFF0C0B14),
        backgroundElevated = Color(0xFF161424),
        backgroundCard = Color(0xFF1C1930),
    )

    val Gold = ThemeColorPalette(
        secondary = Color(0xFFE8A91C),
        secondaryVariant = Color(0xFF9A6200),
        accentGradient = listOf(
            Color(0xFF8A5700),
            Color(0xFFE8A91C),
            Color(0xFFFFF1A8),
            Color(0xFFFFD45C),
            Color(0xFF9A6200),
        ),
        nativeAccentHex = "#FFD45C",
        onSecondary = Color(0xFF111111),
        onSecondaryVariant = Color.White,
        focusRing = Color(0xFFFFD45C),
        focusBackground = Color(0xFF3D2D1A),
        background = Color(0xFF0F0E0B),
        backgroundElevated = Color(0xFF1D1A14),
        backgroundCard = Color(0xFF262116),
    )

    val Jade = ThemeColorPalette(
        secondary = Color(0xFF22D37C),
        secondaryVariant = Color(0xFF0BBF9A),
        accentGradient = listOf(Color(0xFF7BF08D), Color(0xFF22D37C), Color(0xFF0BBF9A)),
        nativeAccentHex = "#7BF08D",
        onSecondary = Color(0xFF111111),
        onSecondaryVariant = Color(0xFF111111),
        focusRing = Color(0xFF7BF08D),
        focusBackground = Color(0xFF153A2C),
        background = Color(0xFF0B0F0D),
        backgroundElevated = Color(0xFF141D18),
        backgroundCard = Color(0xFF16251D),
    )

    val RoseGold = ThemeColorPalette(
        secondary = Color(0xFFEC70A9),
        secondaryVariant = Color(0xFFB75AFF),
        accentGradient = listOf(Color(0xFFB75AFF), Color(0xFFEC70A9), Color(0xFFFFB37A)),
        nativeAccentHex = "#FFB37A",
        onSecondary = Color(0xFF111111),
        onSecondaryVariant = Color.White,
        focusRing = Color(0xFFFFB37A),
        focusBackground = Color(0xFF442037),
        background = Color(0xFF100C0F),
        backgroundElevated = Color(0xFF1F161D),
        backgroundCard = Color(0xFF281A24),
    )

    val ArcticBlue = ThemeColorPalette(
        secondary = Color(0xFF3185F5),
        secondaryVariant = Color(0xFF4D55E8),
        accentGradient = listOf(Color(0xFF4DE3FF), Color(0xFF3185F5), Color(0xFF4D55E8)),
        nativeAccentHex = "#4DE3FF",
        onSecondary = Color.White,
        onSecondaryVariant = Color.White,
        focusRing = Color(0xFF4DE3FF),
        focusBackground = Color(0xFF172844),
        background = Color(0xFF0B0E14),
        backgroundElevated = Color(0xFF141A24),
        backgroundCard = Color(0xFF161E2A),
    )

    val Graphite = ThemeColorPalette(
        secondary = Color(0xFFAAB2BE),
        secondaryVariant = Color(0xFF687381),
        accentGradient = listOf(Color(0xFFF3F5F7), Color(0xFFAAB2BE), Color(0xFF687381)),
        nativeAccentHex = "#F3F5F7",
        onSecondary = Color(0xFF111111),
        onSecondaryVariant = Color(0xFFFFFFFF),
        focusRing = Color(0xFFF3F5F7),
        focusBackground = Color(0xFF30343A),
        background = Color(0xFF0C0D0F),
        backgroundElevated = Color(0xFF17191D),
        backgroundCard = Color(0xFF20242A),
    )

    val Crimson = ThemeColorPalette(
        secondary = Color(0xFFE53935),
        secondaryVariant = Color(0xFFC62828),
        nativeAccentHex = "#E53935",
        focusRing = Color(0xFFFF5252),
        focusBackground = Color(0xFF3D1A1A),
        background = Color(0xFF0D0D0D),
        backgroundElevated = Color(0xFF1A1A1A),
        backgroundCard = Color(0xFF241A1A),
    )

    val Ocean = ThemeColorPalette(
        secondary = Color(0xFF1E88E5),
        secondaryVariant = Color(0xFF1565C0),
        nativeAccentHex = "#1E88E5",
        focusRing = Color(0xFF42A5F5),
        focusBackground = Color(0xFF1A2D3D),
        background = Color(0xFF0D0D0F),
        backgroundElevated = Color(0xFF1A1A1E),
        backgroundCard = Color(0xFF1A1F24),
    )

    val Violet = ThemeColorPalette(
        secondary = Color(0xFF8E24AA),
        secondaryVariant = Color(0xFF6A1B9A),
        nativeAccentHex = "#8E24AA",
        focusRing = Color(0xFFAB47BC),
        focusBackground = Color(0xFF2D1A3D),
        background = Color(0xFF0D0D0F),
        backgroundElevated = Color(0xFF1A1A1E),
        backgroundCard = Color(0xFF1F1A24),
    )

    val Emerald = ThemeColorPalette(
        secondary = Color(0xFF22C55E),
        secondaryVariant = Color(0xFF2E7D32),
        nativeAccentHex = "#22C55E",
        focusRing = Color(0xFF22C55E),
        focusBackground = Color(0xFF1A3D1E),
        background = Color(0xFF0D0D0D),
        backgroundElevated = Color(0xFF1A1A1A),
        backgroundCard = Color(0xFF1A241A),
    )

    val Amber = ThemeColorPalette(
        secondary = Color(0xFFFFB300),
        secondaryVariant = Color(0xFFEF6C00),
        nativeAccentHex = "#FFB300",
        focusRing = Color(0xFFFFB300),
        focusBackground = Color(0xFF3D2D1A),
        background = Color(0xFF0F0D0D),
        backgroundElevated = Color(0xFF1E1A1A),
        backgroundCard = Color(0xFF24201A),
    )

    val Rose = ThemeColorPalette(
        secondary = Color(0xFFD81B60),
        secondaryVariant = Color(0xFFC2185B),
        nativeAccentHex = "#D81B60",
        focusRing = Color(0xFFEC407A),
        focusBackground = Color(0xFF3D1A2D),
        background = Color(0xFF0D0D0D),
        backgroundElevated = Color(0xFF1A1A1A),
        backgroundCard = Color(0xFF241A1F),
    )

    val White = ThemeColorPalette(
        secondary = Color(0xFFF5F5F5),
        secondaryVariant = Color(0xFFE0E0E0),
        nativeAccentHex = "#F5F5F5",
        onSecondary = Color(0xFF111111),
        onSecondaryVariant = Color(0xFF111111),
        focusRing = Color(0xFFFFFFFF),
        focusBackground = Color(0xFF303030),
        background = Color(0xFF0D0D0D),
        backgroundElevated = Color(0xFF1A1A1A),
        backgroundCard = Color(0xFF222222),
    )

    val Copper = ThemeColorPalette(
        secondary = Color(0xFFD9772E),
        secondaryVariant = Color(0xFF914407),
        nativeAccentHex = "#D9772E",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFD9772E),
        focusBackground = Color(0xFF3D261A),
    )

    val Amethyst = ThemeColorPalette(
        secondary = Color(0xFFAD7DE0),
        secondaryVariant = Color(0xFF60309A),
        accentGradient = listOf(Color(0xFF7A1FE0), Color(0xFFA24BF5), Color(0xFFC27BFF), Color(0xFFE0A6FF)),
        nativeAccentHex = "#AD7DE0",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFAD7DE0),
        focusBackground = Color(0xFF2D203A),
        background = Color(0xFF0D0911),
        backgroundElevated = Color(0xFF191220),
        backgroundCard = Color(0xFF23192D),
    )

    val Apricot = ThemeColorPalette(
        secondary = Color(0xFFFFA077),
        secondaryVariant = Color(0xFFCB5C3B),
        accentGradient = listOf(Color(0xFFFF6A3D), Color(0xFFFF8C5A), Color(0xFFFFAE6B), Color(0xFFFFD08A)),
        nativeAccentHex = "#FFA077",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFFFA077),
        focusBackground = Color(0xFF422A1F),
        background = Color(0xFF130C09),
        backgroundElevated = Color(0xFF251711),
        backgroundCard = Color(0xFF332018),
    )

    val Aubergine = ThemeColorPalette(
        secondary = Color(0xFFA672A3),
        secondaryVariant = Color(0xFF542956),
        accentGradient = listOf(Color(0xFF6A1B9A), Color(0xFF9C27B0), Color(0xFFC13FB8), Color(0xFFE05FC8)),
        nativeAccentHex = "#A672A3",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFA672A3),
        focusBackground = Color(0xFF2B1E2A),
        background = Color(0xFF0C090C),
        backgroundElevated = Color(0xFF181118),
        backgroundCard = Color(0xFF211721),
    )

    val Azure = ThemeColorPalette(
        secondary = Color(0xFF2F8DFF),
        secondaryVariant = Color(0xFF205BBC),
        nativeAccentHex = "#2F8DFF",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF2F8DFF),
        focusBackground = Color(0xFF112D41),
        background = Color(0xFF050D13),
        backgroundElevated = Color(0xFF0A1924),
        backgroundCard = Color(0xFF0D2232),
    )

    val Basil = ThemeColorPalette(
        secondary = Color(0xFF76A851),
        secondaryVariant = Color(0xFF325D26),
        accentGradient = listOf(Color(0xFF2E9E3E), Color(0xFF48B83C), Color(0xFF7BCF3F), Color(0xFFA8E05A)),
        nativeAccentHex = "#76A851",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF76A851),
        focusBackground = Color(0xFF1F2C15),
        background = Color(0xFF090D06),
        backgroundElevated = Color(0xFF11180C),
        backgroundCard = Color(0xFF182210),
    )

    val Berry = ThemeColorPalette(
        secondary = Color(0xFFFF2E88),
        secondaryVariant = Color(0xFF932452),
        nativeAccentHex = "#FF2E88",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFFF2E88),
        focusBackground = Color(0xFF381523),
        background = Color(0xFF10060A),
        backgroundElevated = Color(0xFF1F0C13),
        backgroundCard = Color(0xFF2B101B),
    )

    val Blush = ThemeColorPalette(
        secondary = Color(0xFFF49B9E),
        secondaryVariant = Color(0xFFB05A72),
        accentGradient = listOf(Color(0xFFFF5C8A), Color(0xFFFF7A9C), Color(0xFFFF9AAF), Color(0xFFFFBFC8)),
        nativeAccentHex = "#F49B9E",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFF49B9E),
        focusBackground = Color(0xFF3F2829),
        background = Color(0xFF120C0C),
        backgroundElevated = Color(0xFF231617),
        backgroundCard = Color(0xFF311F20),
    )

    val Cerulean = ThemeColorPalette(
        secondary = Color(0xFF00B0F0),
        secondaryVariant = Color(0xFF087493),
        nativeAccentHex = "#00B0F0",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF00B0F0),
        focusBackground = Color(0xFF0C333D),
        background = Color(0xFF040F11),
        backgroundElevated = Color(0xFF071C22),
        backgroundCard = Color(0xFF0A272F),
    )

    val Cherry = ThemeColorPalette(
        secondary = Color(0xFFF0213F),
        secondaryVariant = Color(0xFFAD203A),
        nativeAccentHex = "#F0213F",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFF0213F),
        focusBackground = Color(0xFF3E1318),
        background = Color(0xFF120507),
        backgroundElevated = Color(0xFF230A0D),
        backgroundCard = Color(0xFF300E13),
    )

    val Cobalt = ThemeColorPalette(
        secondary = Color(0xFF3D5AFE),
        secondaryVariant = Color(0xFF2A3DA9),
        nativeAccentHex = "#3D5AFE",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF3D5AFE),
        focusBackground = Color(0xFF181E3E),
        background = Color(0xFF070912),
        backgroundElevated = Color(0xFF0D1123),
        backgroundCard = Color(0xFF131730),
    )

    val Coral = ThemeColorPalette(
        secondary = Color(0xFFFF6B5A),
        secondaryVariant = Color(0xFFCC5048),
        nativeAccentHex = "#FF6B5A",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFFF6B5A),
        focusBackground = Color(0xFF42201C),
        background = Color(0xFF130908),
        backgroundElevated = Color(0xFF251210),
        backgroundCard = Color(0xFF331916),
    )

    val Denim = ThemeColorPalette(
        secondary = Color(0xFF688DB4),
        secondaryVariant = Color(0xFF3B5476),
        accentGradient = listOf(Color(0xFF1D4ED8), Color(0xFF2F6BD8), Color(0xFF4F8FE0), Color(0xFF7BB2EE)),
        nativeAccentHex = "#688DB4",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF688DB4),
        focusBackground = Color(0xFF1B252F),
        background = Color(0xFF080B0E),
        backgroundElevated = Color(0xFF0F141A),
        backgroundCard = Color(0xFF151C24),
    )

    val Fern = ThemeColorPalette(
        secondary = Color(0xFFA8C65D),
        secondaryVariant = Color(0xFF65782A),
        accentGradient = listOf(Color(0xFF3FA34D), Color(0xFF6BBF45), Color(0xFF98D63E), Color(0xFFC2E85A)),
        nativeAccentHex = "#A8C65D",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFA8C65D),
        focusBackground = Color(0xFF2C3318),
        background = Color(0xFF0D0F07),
        backgroundElevated = Color(0xFF181D0D),
        backgroundCard = Color(0xFF222813),
    )

    val Glacier = ThemeColorPalette(
        secondary = Color(0xFFA9E3F0),
        secondaryVariant = Color(0xFF518FAF),
        accentGradient = listOf(Color(0xFF00B4E6), Color(0xFF2CCBF2), Color(0xFF6EE0FA), Color(0xFFA8F0FF)),
        nativeAccentHex = "#A9E3F0",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFA9E3F0),
        focusBackground = Color(0xFF2C3B3E),
        background = Color(0xFF0D1112),
        backgroundElevated = Color(0xFF192123),
        backgroundCard = Color(0xFF222D30),
    )

    val Grape = ThemeColorPalette(
        secondary = Color(0xFFA033F0),
        secondaryVariant = Color(0xFF7534B4),
        nativeAccentHex = "#A033F0",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFA033F0),
        focusBackground = Color(0xFF311B3E),
        background = Color(0xFF0E0812),
        backgroundElevated = Color(0xFF1C0F23),
        backgroundCard = Color(0xFF261530),
    )

    val Heather = ThemeColorPalette(
        secondary = Color(0xFFB89FBF),
        secondaryVariant = Color(0xFF786384),
        accentGradient = listOf(Color(0xFF9B4DCA), Color(0xFFB565D8), Color(0xFFCC85E5), Color(0xFFE3A8F2)),
        nativeAccentHex = "#B89FBF",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFB89FBF),
        focusBackground = Color(0xFF302932),
        background = Color(0xFF0E0C0E),
        backgroundElevated = Color(0xFF1B171C),
        backgroundCard = Color(0xFF252026),
    )

    val Indigo = ThemeColorPalette(
        secondary = Color(0xFF6A4CFF),
        secondaryVariant = Color(0xFF413591),
        nativeAccentHex = "#6A4CFF",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF6A4CFF),
        focusBackground = Color(0xFF1F1E39),
        background = Color(0xFF090910),
        backgroundElevated = Color(0xFF111120),
        backgroundCard = Color(0xFF18172C),
    )

    val Lagoon = ThemeColorPalette(
        secondary = Color(0xFF30C6AC),
        secondaryVariant = Color(0xFF087C73),
        accentGradient = listOf(Color(0xFF00A896), Color(0xFF00C2A8), Color(0xFF2EDCC0), Color(0xFF6FF0D8)),
        nativeAccentHex = "#30C6AC",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF30C6AC),
        focusBackground = Color(0xFF0C332D),
        background = Color(0xFF040F0D),
        backgroundElevated = Color(0xFF071D19),
        backgroundCard = Color(0xFF0A2822),
    )

    val Lavender = ThemeColorPalette(
        secondary = Color(0xFFC6AEFA),
        secondaryVariant = Color(0xFF8673C4),
        accentGradient = listOf(Color(0xFF8A63F5), Color(0xFFA27BFF), Color(0xFFBE9BFF), Color(0xFFD9BFFF)),
        nativeAccentHex = "#C6AEFA",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFC6AEFA),
        focusBackground = Color(0xFF332D41),
        background = Color(0xFF0F0D13),
        backgroundElevated = Color(0xFF1D1924),
        backgroundCard = Color(0xFF282332),
    )

    val Lemon = ThemeColorPalette(
        secondary = Color(0xFFFFE01A),
        secondaryVariant = Color(0xFFAB831D),
        nativeAccentHex = "#FFE01A",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFFFE01A),
        focusBackground = Color(0xFF413919),
        background = Color(0xFF131107),
        backgroundElevated = Color(0xFF24200E),
        backgroundCard = Color(0xFF322C13),
    )

    val Lilac = ThemeColorPalette(
        secondary = Color(0xFFDEA6E7),
        secondaryVariant = Color(0xFF9965AF),
        accentGradient = listOf(Color(0xFFC44FD8), Color(0xFFD86BE6), Color(0xFFE78CF0), Color(0xFFF4B3FA)),
        nativeAccentHex = "#DEA6E7",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFDEA6E7),
        focusBackground = Color(0xFF3A2B3C),
        background = Color(0xFF110C11),
        backgroundElevated = Color(0xFF201821),
        backgroundCard = Color(0xFF2C212E),
    )

    val Lime = ThemeColorPalette(
        secondary = Color(0xFF9BDB10),
        secondaryVariant = Color(0xFF7AA621),
        nativeAccentHex = "#9BDB10",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF9BDB10),
        focusBackground = Color(0xFF333D14),
        background = Color(0xFF0F1106),
        backgroundElevated = Color(0xFF1D220B),
        backgroundCard = Color(0xFF272F10),
    )

    val Mango = ThemeColorPalette(
        secondary = Color(0xFFFF8A00),
        secondaryVariant = Color(0xFFD17A20),
        nativeAccentHex = "#FF8A00",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFFF8A00),
        focusBackground = Color(0xFF423317),
        background = Color(0xFF130F07),
        backgroundElevated = Color(0xFF251D0D),
        backgroundCard = Color(0xFF332812),
    )

    val Mint = ThemeColorPalette(
        secondary = Color(0xFF2EE6B0),
        secondaryVariant = Color(0xFF329E80),
        nativeAccentHex = "#2EE6B0",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF2EE6B0),
        focusBackground = Color(0xFF1E3C33),
        background = Color(0xFF09110F),
        backgroundElevated = Color(0xFF11211C),
        backgroundCard = Color(0xFF172E27),
    )

    val Moss = ThemeColorPalette(
        secondary = Color(0xFF9AA16D),
        secondaryVariant = Color(0xFF5F673F),
        accentGradient = listOf(Color(0xFF5A8F1E), Color(0xFF7AAA24), Color(0xFF9CC23A), Color(0xFFBFD95A)),
        nativeAccentHex = "#9AA16D",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF9AA16D),
        focusBackground = Color(0xFF282A1C),
        background = Color(0xFF0C0C08),
        backgroundElevated = Color(0xFF161710),
        backgroundCard = Color(0xFF1F2016),
    )

    val Orchid = ThemeColorPalette(
        secondary = Color(0xFFE040D8),
        secondaryVariant = Color(0xFFAA449D),
        nativeAccentHex = "#E040D8",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFE040D8),
        focusBackground = Color(0xFF3F243C),
        background = Color(0xFF120A11),
        backgroundElevated = Color(0xFF231422),
        backgroundCard = Color(0xFF311C2E),
    )

    val Peach = ThemeColorPalette(
        secondary = Color(0xFFFFC1A1),
        secondaryVariant = Color(0xFFCB8267),
        accentGradient = listOf(Color(0xFFFF7E5F), Color(0xFFFF9672), Color(0xFFFFB088), Color(0xFFFFCBA4)),
        nativeAccentHex = "#FFC1A1",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFFFC1A1),
        focusBackground = Color(0xFF42322A),
        background = Color(0xFF130E0C),
        backgroundElevated = Color(0xFF251C17),
        backgroundCard = Color(0xFF332720),
    )

    val Periwinkle = ThemeColorPalette(
        secondary = Color(0xFF9AABFF),
        secondaryVariant = Color(0xFF5763B9),
        accentGradient = listOf(Color(0xFF5C6CFF), Color(0xFF7484FF), Color(0xFF8EA0FF), Color(0xFFAFBDFF)),
        nativeAccentHex = "#9AABFF",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF9AABFF),
        focusBackground = Color(0xFF282C42),
        background = Color(0xFF0C0D13),
        backgroundElevated = Color(0xFF161925),
        backgroundCard = Color(0xFF1F2233),
    )

    val Pine = ThemeColorPalette(
        secondary = Color(0xFF2B9D83),
        secondaryVariant = Color(0xFF15584C),
        accentGradient = listOf(Color(0xFF00796B), Color(0xFF009B7D), Color(0xFF1FB98E), Color(0xFF52D3A6)),
        nativeAccentHex = "#2B9D83",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF2B9D83),
        focusBackground = Color(0xFF0B2922),
        background = Color(0xFF030C0A),
        backgroundElevated = Color(0xFF061713),
        backgroundCard = Color(0xFF091F1A),
    )

    val Saffron = ThemeColorPalette(
        secondary = Color(0xFFF4AA49),
        secondaryVariant = Color(0xFFB46725),
        accentGradient = listOf(Color(0xFFFF9500), Color(0xFFFFAA1A), Color(0xFFFFC233), Color(0xFFFFD866)),
        nativeAccentHex = "#F4AA49",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFFF4AA49),
        focusBackground = Color(0xFF3F2C13),
        background = Color(0xFF120D05),
        backgroundElevated = Color(0xFF23190B),
        backgroundCard = Color(0xFF31220F),
    )

    val Teal = ThemeColorPalette(
        secondary = Color(0xFF00BFC8),
        secondaryVariant = Color(0xFF167880),
        nativeAccentHex = "#00BFC8",
        onSecondary = Color(0xFF111111),
        focusRing = Color(0xFF00BFC8),
        focusBackground = Color(0xFF113335),
        background = Color(0xFF050F0F),
        backgroundElevated = Color(0xFF0A1D1D),
        backgroundCard = Color(0xFF0D2729),
    )

    fun getColorPalette(
        theme: AppTheme,
        customColors: CustomThemeColors = CustomThemeColors.Default,
    ): ThemeColorPalette = when (theme) {
        AppTheme.CUSTOM -> customColors.toColorPalette()
        AppTheme.ORIGINAL -> Original
        AppTheme.GOLD -> Gold
        AppTheme.JADE -> Jade
        AppTheme.ROSE_GOLD -> RoseGold
        AppTheme.ARCTIC_BLUE -> ArcticBlue
        AppTheme.GRAPHITE -> Graphite
        AppTheme.CRIMSON -> Crimson
        AppTheme.OCEAN -> Ocean
        AppTheme.VIOLET -> Violet
        AppTheme.EMERALD -> Emerald
        AppTheme.AMBER -> Amber
        AppTheme.ROSE -> Rose
        AppTheme.WHITE -> White
        AppTheme.COPPER -> Copper
        AppTheme.AMETHYST -> Amethyst
        AppTheme.APRICOT -> Apricot
        AppTheme.AUBERGINE -> Aubergine
        AppTheme.AZURE -> Azure
        AppTheme.BASIL -> Basil
        AppTheme.BERRY -> Berry
        AppTheme.BLUSH -> Blush
        AppTheme.CERULEAN -> Cerulean
        AppTheme.CHERRY -> Cherry
        AppTheme.COBALT -> Cobalt
        AppTheme.CORAL -> Coral
        AppTheme.DENIM -> Denim
        AppTheme.FERN -> Fern
        AppTheme.GLACIER -> Glacier
        AppTheme.GRAPE -> Grape
        AppTheme.HEATHER -> Heather
        AppTheme.INDIGO -> Indigo
        AppTheme.LAGOON -> Lagoon
        AppTheme.LAVENDER -> Lavender
        AppTheme.LEMON -> Lemon
        AppTheme.LILAC -> Lilac
        AppTheme.LIME -> Lime
        AppTheme.MANGO -> Mango
        AppTheme.MINT -> Mint
        AppTheme.MOSS -> Moss
        AppTheme.ORCHID -> Orchid
        AppTheme.PEACH -> Peach
        AppTheme.PERIWINKLE -> Periwinkle
        AppTheme.PINE -> Pine
        AppTheme.SAFFRON -> Saffron
        AppTheme.TEAL -> Teal
    }
}

/**
 * AMOLED black in Morivia: cards and other surfaces are pure black, and the screen background
 * takes a deep tint of the selected theme's accent so the theme stays visible around them.
 */
internal fun ThemeColorPalette.amoledBackground(): Color =
    androidx.compose.ui.graphics.lerp(Color.Black, secondary, 0.24f)
