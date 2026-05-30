package com.project.roulette.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Premium Dark Theme Colors
val DeepNavyBlack = Color(0xFF0D0D12)
val SurfaceDark = Color(0xFF1A1A24)
val SurfaceDarker = Color(0xFF14141A)
val PrimaryPurple = Color(0xFF6C5CE7)
val TextSecondary = Color(0xFF8E8E93)

// Accent Colors for Tiles
val TilePurple = Color(0xFF6C5CE7)
val TileTeal = Color(0xFF00B894)
val TileCoral = Color(0xFFFF7675)
val TileBlue = Color(0xFF0984E3)
val TilePink = Color(0xFFE84393)
val TileOrange = Color(0xFFE17055)

// Theme Palettes
data class RoulettePalette(
    val name: String,
    val primary: Color,
    val accent: Color,
    val surface: Color = SurfaceDark
)

val Palettes = listOf(
    RoulettePalette("Purple", Color(0xFF6C5CE7), Color(0xFFA29BFE)),
    RoulettePalette("Teal", Color(0xFF00B894), Color(0xFF55E6C1)),
    RoulettePalette("Rose", Color(0xFFE84393), Color(0xFFFD79A8)),
    RoulettePalette("Ocean", Color(0xFF0984E3), Color(0xFF74B9FF)),
    RoulettePalette("Amber", Color(0xFFFFA000), Color(0xFFFFC107)),
    RoulettePalette("Coral", Color(0xFFFF7675), Color(0xFFFAB1A0)),
    RoulettePalette("Mint", Color(0xFF27AE60), Color(0xFF2ECC71)),
    RoulettePalette("Crimson", Color(0xFFD63031), Color(0xFFFF7675))
)

val ThemePalette = Palettes.map { it.primary }
