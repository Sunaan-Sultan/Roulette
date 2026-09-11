package com.project.roulette.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import com.project.roulette.domain.model.Wheel
import com.project.roulette.ui.theme.RouletteTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Composable for drawing the spinning wheel.
 * High-fidelity design matching reference images.
 */
@Immutable
data class WheelCanvasColors(
    val halo: Color,
    val divider: Color,
    val rim: Color,
    val innerRing: Color,
    val hubStroke: Color,
    val pointerOutline: Color
)

@Composable
fun rememberWheelCanvasColors(): WheelCanvasColors {
    val isLight = RouletteTheme.colors.isLight
    return remember(isLight) {
        if (isLight) {
            WheelCanvasColors(
                halo = Color(0xFFE9EAEE),
                divider = Color.White.copy(alpha = 0.85f),
                rim = Color(0xFFD5D7DD),
                innerRing = Color.White.copy(alpha = 0.6f),
                hubStroke = Color.White,
                pointerOutline = Color.White
            )
        } else {
            WheelCanvasColors(
                halo = Color(0xFF141424),
                divider = Color.Black.copy(alpha = 0.8f),
                rim = Color(0xFF050505),
                innerRing = Color(0xFF1E1E2C),
                hubStroke = Color(0xFF121212),
                pointerOutline = Color(0xFF0B0B14)
            )
        }
    }
}

@Composable
fun WheelCanvas(
    wheel: Wheel,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    wheelSize: Dp = 260.dp,
    themeColor: Color = Color(0xFF6C5CE7),
    canvasColors: WheelCanvasColors = rememberWheelCanvasColors()
) {
    val segments = wheel.getActiveSegments()
    val isLight = RouletteTheme.colors.isLight
    val segmentColors = segments.map { it.color }
    val pointerColor = remember(segmentColors, themeColor, isLight) {
        distinctPointerColor(segmentColors, themeColor, isLight)
    }
    
    // Calculate readable text colors based on the segment backgrounds
    val textColors = segments.map { segment ->
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(segment.color.toArgb(), hsv)
        hsv[1] *= 0.3f
        hsv[2] = if (segment.color.luminance() < 0.45f) (hsv[2] + 1f) / 2f else hsv[2] * 0.35f
        Color(android.graphics.Color.HSVToColor(hsv))
    }

    Box(
        modifier = modifier.size(wheelSize + 60.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Outer Halo / Ambient Pop (The dark background glow seen in reference)
        Canvas(modifier = Modifier.size(wheelSize + 48.dp)) {
            drawCircle(
                color = canvasColors.halo,
                radius = size.width / 2f
            )
        }

        // 2. Multi-color ambient spill
        Canvas(modifier = Modifier.size(wheelSize + 36.dp)) {
            if (segments.isEmpty()) return@Canvas
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val totalWeight = wheel.getTotalWeight()

            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = -90f - (360f / segments.size / 2f) // Approximation for start
                // Actually, let's just use -90 for consistency
                startAngle = -90f - (360f / segments.size / 2f) 
                
                segments.forEach { segment ->
                    val sweep = (segment.weight / totalWeight) * 360f
                    drawArc(
                        color = segment.color.copy(alpha = 0.2f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                    startAngle += sweep
                }
            }
        }

        // 3. Main Wheel (Segments + Divider + Flush Border)
        Canvas(modifier = Modifier.size(wheelSize + 12.dp)) {
            if (segments.isEmpty()) return@Canvas

            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = wheelSize.toPx() / 2f
            val totalWeight = wheel.getTotalWeight()
            val initialOffset = -90f - (360f / segments.size / 2f)

            // Pass 1: Draw Arcs
            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = initialOffset
                segments.forEach { segment ->
                    val sweep = (segment.weight / totalWeight) * 360f
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = Offset(centerX - radius, centerY - radius)
                    )
                    startAngle += sweep
                }
            }

            // Pass 2: Draw Dividers on top of arcs
            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = initialOffset
                segments.forEach { segment ->
                    val lineAngleRad = startAngle * PI.toFloat() / 180f
                    drawLine(
                        color = canvasColors.divider,
                        start = Offset(centerX, centerY),
                        end = Offset(
                            centerX + radius * cos(lineAngleRad),
                            centerY + radius * sin(lineAngleRad)
                        ),
                        strokeWidth = 6f
                    )
                    val sweep = (segment.weight / totalWeight) * 360f
                    startAngle += sweep
                }
            }

            // Pass 3: Draw Text
            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = initialOffset
                segments.forEachIndexed { index, segment ->
                    val sweep = (segment.weight / totalWeight) * 360f
                    val midAngle = startAngle + sweep / 2f
                    drawSectorLabel(midAngle, centerX, centerY, radius, segment.name, segment.weight, textColors[index])
                    startAngle += sweep
                }
            }

            // Pass 4: Draw Border Flush to Edge
            drawCircle(
                color = canvasColors.rim,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 10f)
            )
            // Inner highlight ring
            drawCircle(
                color = canvasColors.innerRing,
                radius = radius - 4f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )

            // Pass 5: Center Hub
            drawCircle(
                color = themeColor,
                radius = radius * 0.1f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = canvasColors.hubStroke,
                radius = radius * 0.1f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f)
            )
        }

        // 4. Fixed Pointer at the Top
        Canvas(modifier = Modifier.size(wheelSize)) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = wheelSize.toPx() / 2f
            val pointerWidth = 20.dp.toPx()
            val pointerHeight = 16.dp.toPx()

            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(centerX - pointerWidth / 2, centerY - radius - 4f)
                lineTo(centerX + pointerWidth / 2, centerY - radius - 4f)
                lineTo(centerX, centerY - radius + pointerHeight)
                close()
            }

            drawPath(
                path = path,
                color = pointerColor
            )
            drawPath(
                path = path,
                color = canvasColors.pointerOutline,
                style = Stroke(width = 5f)
            )
        }
    }
}

private fun DrawScope.drawSectorLabel(
    angleDegrees: Float,
    centerX: Float,
    centerY: Float,
    radius: Float,
    text: String,
    weight: Float,
    color: Color
) {
    val angleRad = (angleDegrees) * PI.toFloat() / 180f
    val startRadius = radius * 0.4f
    val textX = centerX + startRadius * cos(angleRad)
    val textY = centerY + startRadius * sin(angleRad)

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = 34f
            textAlign = android.graphics.Paint.Align.LEFT
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }

        val weightPaint = android.graphics.Paint().apply {
            this.color = color.copy(alpha = 0.6f).toArgb()
            textSize = 24f
            textAlign = android.graphics.Paint.Align.LEFT
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            isAntiAlias = true
        }
        
        save()
        translate(textX, textY)
        rotate(angleDegrees)
        val maxChars = 10
        val displayName = if (text.length > maxChars) text.take(maxChars - 1) + "…" else text
        drawText(displayName, 0f, 0f, paint)
        
        if (weight > 1f) {
            val weightText = "${String.format(java.util.Locale.US, "%.1f", weight)}x"
            drawText(weightText, 0f, 30f, weightPaint)
        }
        restore()
    }
}

private const val MIN_POINTER_HUE_GAP = 45f

private fun distinctPointerColor(
    segmentColors: List<Color>,
    themeColor: Color,
    isLight: Boolean
): Color {
    val hsv = FloatArray(3)
    val segmentHues = segmentColors.mapNotNull { color ->
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        if (hsv[1] >= 0.15f && hsv[2] >= 0.15f) hsv[0] else null
    }
    if (segmentHues.isEmpty()) return themeColor

    android.graphics.Color.colorToHSV(themeColor.toArgb(), hsv)
    val themeHue = hsv[0]
    val themeSaturation = hsv[1]
    val themeValue = hsv[2]
    if (segmentHues.minOf { hueDistance(it, themeHue) } >= MIN_POINTER_HUE_GAP) return themeColor

    val sorted = segmentHues.sorted()
    var bestHue = themeHue
    var bestGap = -1f
    for (index in sorted.indices) {
        val current = sorted[index]
        val next = if (index == sorted.lastIndex) sorted[0] + 360f else sorted[index + 1]
        val gap = next - current
        if (gap > bestGap) {
            bestGap = gap
            bestHue = (current + gap / 2f) % 360f
        }
    }

    return Color(
        android.graphics.Color.HSVToColor(
            floatArrayOf(
                bestHue,
                themeSaturation.coerceAtLeast(0.75f),
                if (isLight) themeValue.coerceIn(0.55f, 0.8f) else themeValue.coerceAtLeast(0.9f)
            )
        )
    )
}

private fun hueDistance(a: Float, b: Float): Float {
    val diff = abs(a - b) % 360f
    return if (diff > 180f) 360f - diff else diff
}
