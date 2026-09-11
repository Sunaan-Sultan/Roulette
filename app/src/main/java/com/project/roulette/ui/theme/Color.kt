package com.project.roulette.ui.theme

import androidx.compose.ui.graphics.Color

val GroupedBgLight = Color(0xFFF2F3F5)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFFFFFFF)
val SurfacePressedLight = Color(0xFFE9EAEE)
val DividerLight = Color(0xFFE8E8E8)
val OutlineLight = Color(0xFFD8DADF)
val TextPrimaryLight = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF8A8F99)
val TextTertiaryLight = Color(0xFFBFC3CB)
val ScrimLight = Color(0x66000000)

val GroupedBgDark = Color(0xFF000000)
val SurfaceDarkNew = Color(0xFF1C1C1E)
val SurfaceElevatedDark = Color(0xFF2C2C2E)
val SurfacePressedDark = Color(0xFF2C2C2E)
val DividerDark = Color(0xFF2C2C2E)
val OutlineDark = Color(0xFF3A3A3C)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF8E8E93)
val TextTertiaryDark = Color(0xFF48484A)
val ScrimDark = Color(0x99000000)

val DangerLight = Color(0xFFD32F2F)
val DangerDark = Color(0xFFFF6B6B)
val SuccessLight = Color(0xFF1B7F46)
val SuccessDark = Color(0xFF3DDC84)
val WarningLight = Color(0xFFA15C00)
val WarningDark = Color(0xFFFFB300)
val InfoLight = Color(0xFF0B6BC0)
val InfoDark = Color(0xFF4DA3FF)

data class RoulettePalette(
    val name: String,
    val primary: Color,
    val onLight: Color,
    val onDark: Color
)

val Palettes = listOf(
    RoulettePalette("Purple", Color(0xFF6C5CE7), Color(0xFF6C5CE7), Color(0xFF9B8CFF)),
    RoulettePalette("Teal", Color(0xFF00B894), Color(0xFF00775E), Color(0xFF22D3A5)),
    RoulettePalette("Rose", Color(0xFFE84393), Color(0xFFC2185B), Color(0xFFFF6FB0)),
    RoulettePalette("Ocean", Color(0xFF0984E3), Color(0xFF0B6BC0), Color(0xFF4DA3FF)),
    RoulettePalette("Amber", Color(0xFFFFA000), Color(0xFFA15C00), Color(0xFFFFB300)),
    RoulettePalette("Coral", Color(0xFFFF7675), Color(0xFFC74240), Color(0xFFFF8E8D)),
    RoulettePalette("Mint", Color(0xFF27AE60), Color(0xFF1B7F46), Color(0xFF3DDC84)),
    RoulettePalette("Crimson", Color(0xFFD63031), Color(0xFFD63031), Color(0xFFFF5A5A))
)

val ThemePalette = Palettes.map { it.primary }

val TilePurple = Color(0xFF6C5CE7)
val TileTeal = Color(0xFF00B894)
val TileCoral = Color(0xFFFF7675)
val TileBlue = Color(0xFF0984E3)
val TilePink = Color(0xFFE84393)
val TileOrange = Color(0xFFE17055)
val TileAmber = Color(0xFFFFA000)
val TileGreen = Color(0xFF27AE60)

@Deprecated("Use RouletteTheme.colors.background", ReplaceWith("RouletteTheme.colors.background"))
val DeepNavyBlack = GroupedBgDark

@Deprecated("Use RouletteTheme.colors.surface", ReplaceWith("RouletteTheme.colors.surface"))
val SurfaceDark = SurfaceDarkNew

@Deprecated("Use RouletteTheme.colors.surfaceElevated", ReplaceWith("RouletteTheme.colors.surfaceElevated"))
val SurfaceDarker = SurfaceElevatedDark

@Deprecated("Use RouletteTheme.colors.textSecondary", ReplaceWith("RouletteTheme.colors.textSecondary"))
val TextSecondary = TextSecondaryDark

@Deprecated("Use RouletteTheme.colors.primary", ReplaceWith("RouletteTheme.colors.primary"))
val PrimaryPurple = Color(0xFF6C5CE7)
