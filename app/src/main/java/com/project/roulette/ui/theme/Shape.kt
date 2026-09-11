package com.project.roulette.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Immutable
data class RouletteShapes(
    val card: Shape = RoundedCornerShape(16.dp),
    val cardSmall: Shape = RoundedCornerShape(12.dp),
    val groupTop: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    val groupBottom: Shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
    val groupMiddle: Shape = RoundedCornerShape(0.dp),
    val button: Shape = RoundedCornerShape(14.dp),
    val buttonLarge: Shape = RoundedCornerShape(16.dp),
    val textField: Shape = RoundedCornerShape(12.dp),
    val dialog: Shape = RoundedCornerShape(20.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val chip: Shape = CircleShape,
    val iconTile: Shape = RoundedCornerShape(8.dp),
    val avatar: Shape = CircleShape,
    val thumbnail: Shape = RoundedCornerShape(12.dp)
)

val LocalRouletteShapes = staticCompositionLocalOf { RouletteShapes() }
