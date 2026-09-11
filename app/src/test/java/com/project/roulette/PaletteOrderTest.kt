package com.project.roulette

import androidx.compose.ui.graphics.Color
import com.project.roulette.ui.theme.Palettes
import com.project.roulette.ui.theme.ThemePalette
import org.junit.Assert.assertEquals
import org.junit.Test

class PaletteOrderTest {

    private val frozenNames = listOf(
        "Purple", "Teal", "Rose", "Ocean", "Amber", "Coral", "Mint", "Crimson"
    )

    private val frozenPrimaries = listOf(
        Color(0xFF6C5CE7), Color(0xFF00B894), Color(0xFFE84393), Color(0xFF0984E3),
        Color(0xFFFFA000), Color(0xFFFF7675), Color(0xFF27AE60), Color(0xFFD63031)
    )

    @Test
    fun paletteNamesAndOrderAreFrozen() {
        assertEquals(frozenNames, Palettes.map { it.name })
    }

    @Test
    fun palettePrimariesAreFrozen() {
        assertEquals(frozenPrimaries, Palettes.map { it.primary })
    }

    @Test
    fun themePaletteMirrorsPalettePrimaries() {
        assertEquals(frozenPrimaries, ThemePalette)
    }
}
