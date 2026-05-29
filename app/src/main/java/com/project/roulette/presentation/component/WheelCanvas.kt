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
import com.project.roulette.domain.model.Wheel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Composable for drawing the spinning wheel.
 * High-fidelity design matching reference images.
 */
@Composable
fun WheelCanvas(
    wheel: Wheel,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    wheelSize: Dp = 260.dp,
    themeColor: Color = Color(0xFF6C5CE7)
) {
    val segments = wheel.getActiveSegments()
    
    // Calculate readable text colors based on the segment backgrounds
    val textColors = segments.map { segment ->
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(segment.color.toArgb(), hsv)
        hsv[1] *= 0.3f // Desaturate
        hsv[2] = (hsv[2] + 1f) / 2f // Brighten
        Color(android.graphics.Color.HSVToColor(hsv))
    }

    Box(
        modifier = modifier.size(wheelSize + 60.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Outer Halo / Ambient Pop (The dark background glow seen in reference)
        Canvas(modifier = Modifier.size(wheelSize + 48.dp)) {
            drawCircle(
                color = Color(0xFF141424), // Dark Navy/Purple Pop
                radius = size.width / 2f
            )
        }

        // 2. Multi-color ambient spill
        Canvas(modifier = Modifier.size(wheelSize + 36.dp)) {
            if (segments.isEmpty()) return@Canvas
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val sweep = 360f / segments.size

            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = -90f - (sweep / 2f)
                segments.forEach { segment ->
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
            val sweep = 360f / segments.size

            // Pass 1: Draw Arcs
            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = -90f - (sweep / 2f)
                segments.forEachIndexed { index, segment ->
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
                var startAngle = -90f - (sweep / 2f)
                segments.forEach { _ ->
                    val lineAngleRad = startAngle * PI.toFloat() / 180f
                    drawLine(
                        color = Color.Black.copy(alpha = 0.8f),
                        start = Offset(centerX, centerY),
                        end = Offset(
                            centerX + radius * cos(lineAngleRad),
                            centerY + radius * sin(lineAngleRad)
                        ),
                        strokeWidth = 6f
                    )
                    startAngle += sweep
                }
            }

            // Pass 3: Draw Text
            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = -90f - (sweep / 2f)
                segments.forEachIndexed { index, segment ->
                    val midAngle = startAngle + sweep / 2f
                    drawSectorLabel(midAngle, centerX, centerY, radius, segment.name, textColors[index])
                    startAngle += sweep
                }
            }

            // Pass 4: Draw Border Flush to Edge
            drawCircle(
                color = Color(0xFF050505),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 10f)
            )
            // Inner highlight ring
            drawCircle(
                color = Color(0xFF1E1E2C),
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
                color = Color(0xFF121212),
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
                color = themeColor // Match theme
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
        
        save()
        translate(textX, textY)
        rotate(angleDegrees)
        val maxChars = 10
        val displayName = if (text.length > maxChars) text.take(maxChars - 1) + "…" else text
        drawText(displayName, 0f, 10f, paint)
        restore()
    }
}
