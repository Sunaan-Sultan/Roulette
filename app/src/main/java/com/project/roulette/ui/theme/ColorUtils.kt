package com.project.roulette.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return if (la > lb) la / lb else lb / la
}

private val DarkContent = Color(0xFF14141A)

fun contentColorOn(background: Color): Color =
    if (contrastRatio(DarkContent, background) >= contrastRatio(Color.White, background)) {
        DarkContent
    } else {
        Color.White
    }

fun Color.ensureContrast(on: Color, ratio: Float = 4.5f): Color {
    if (contrastRatio(this, on) >= ratio) return this
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    val goingDarker = on.luminance() > 0.5f
    var v = hsv[2]
    repeat(25) {
        v = (v + if (goingDarker) -0.04f else 0.04f).coerceIn(0f, 1f)
        val candidate = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], v)))
        if (contrastRatio(candidate, on) >= ratio) return candidate
    }
    return if (goingDarker) Color.Black else Color.White
}

fun adaptiveAccent(base: Color, onBackground: Color): Color = base.ensureContrast(onBackground, 4.5f)

@Composable
fun rememberAccentOnSurface(base: Color): Color {
    val colors = RouletteTheme.colors
    return remember(base, colors.isLight, colors.surface) {
        if (colors.isLight) {
            base.ensureContrast(colors.surface, 4.5f)
        } else {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(base.toArgb(), hsv)
            hsv[1] *= 0.6f
            hsv[2] = (hsv[2] + 1f) / 2f
            Color(android.graphics.Color.HSVToColor(hsv))
        }
    }
}
