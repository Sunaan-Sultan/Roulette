package com.project.roulette.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DeepNavyBlack,
    surface = SurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White,
    secondaryContainer = SurfaceDarker,
    onSecondaryContainer = TextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

data class RouletteColors(
    val primary: Color,
    val accent: Color,
    val accentBg: Color,
    val accentBorder: Color,
    val textSecondary: Color = TextSecondary
)

val LocalRouletteColors = staticCompositionLocalOf {
    RouletteColors(
        primary = PrimaryPurple,
        accent = PrimaryPurple,
        accentBg = PrimaryPurple.copy(alpha = 0.15f),
        accentBorder = PrimaryPurple.copy(alpha = 0.3f)
    )
}

object RouletteTheme {
    val colors: RouletteColors
        @Composable
        @ReadOnlyComposable
        get() = LocalRouletteColors.current
}

@Composable
fun RouletteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false to maintain the premium navy look regardless of system dynamic colors
    dynamicColor: Boolean = false,
    paletteIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val palette = Palettes.getOrElse(paletteIndex) { Palettes[0] }
    
    val rouletteColors = RouletteColors(
        primary = palette.primary,
        accent = palette.accent,
        accentBg = palette.primary.copy(alpha = 0.15f),
        accentBorder = palette.primary.copy(alpha = 0.3f)
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme.copy(
            primary = palette.primary,
            outline = rouletteColors.accentBorder
        )
        else -> LightColorScheme.copy(
            primary = palette.primary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            // Set light icons for dark theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalRouletteColors provides rouletteColors,
            content = content
        )
    }
}
