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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.roulette.domain.model.Wheel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Composable for drawing the spinning wheel.
 * Uses drawArc and weights to compute sector sizes and fixes text rotation.
 */
@Composable
fun WheelCanvas(
    wheel: Wheel,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    wheelSize: Dp = 260.dp
) {
    val jewelTones = listOf(
        Color(0xFF4A148C), // Deep Violet
        Color(0xFF0D47A1), // Steel Blue
        Color(0xFF004D40), // Forest Teal
        Color(0xFF880E4F), // Muted Rose
        Color(0xFF1B5E20), // Dark Green
        Color(0xFFBF360C)  // Burnt Orange
    )

    Box(
        modifier = modifier.size(wheelSize + 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(wheelSize + 20.dp)) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val outerRingRadius = (wheelSize.toPx() / 2f) + 4f

            // Outer Ring
            drawCircle(
                color = Color(0xFF212121),
                radius = outerRingRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 8f)
            )
        }

        Canvas(modifier = Modifier.size(wheelSize)) {
            val segments = wheel.getActiveSegments()
            if (segments.isEmpty()) return@Canvas

            val radius = wheelSize.toPx() / 2f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Compute sweep angles - the requirement says "equal slices"
            val sweep = 360f / segments.size

            // Apply rotation
            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = -90f - (sweep / 2f) // center first slice at top
                segments.forEachIndexed { index, segment ->
                    val color = jewelTones[index % jewelTones.size]

                    // Draw filled arc sector
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )

                    // Draw label
                    val midAngle = startAngle + sweep / 2f
                    drawSectorLabel(midAngle, centerX, centerY, radius, segment.name)

                    startAngle += sweep
                }
            }

            // Hub
            drawCircle(
                color = Color(0xFF673AB7), // Violet border ring
                radius = radius * 0.12f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF121212), // Dark filled
                radius = radius * 0.08f,
                center = Offset(centerX, centerY)
            )
        }

        // Pointer (Fixed)
        Canvas(modifier = Modifier.size(wheelSize)) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = wheelSize.toPx() / 2f
            
            drawPointerAtTop(centerX, centerY, radius)
        }
    }
}

private fun DrawScope.drawSectorLabel(
    angleDegrees: Float,
    centerX: Float,
    centerY: Float,
    radius: Float,
    text: String
) {
    val angleRad = (angleDegrees) * PI.toFloat() / 180f
    
    // Text starts from near the center and goes outward
    val startRadius = radius * 0.25f
    val textX = centerX + startRadius * cos(angleRad)
    val textY = centerY + startRadius * sin(angleRad)

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = android.graphics.Paint.Align.LEFT
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        
        save()
        translate(textX, textY)
        rotate(angleDegrees)
        
        val maxChars = 12
        val displayName = if (text.length > maxChars) text.take(maxChars - 1) + "…" else text

        drawText(displayName, 0f, 12f, paint) // 12f to vertically center text on the line
        restore()
    }
}

private fun DrawScope.drawPointerAtTop(centerX: Float, centerY: Float, radius: Float) {
    val pointerSize = 24.dp.toPx()
    val apexY = centerY - radius

    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(centerX - pointerSize / 2, apexY - 10f)
        lineTo(centerX + pointerSize / 2, apexY - 10f)
        lineTo(centerX, apexY + pointerSize * 0.8f)
        close()
    }

    // Glow effect (simplified with a slightly larger blurred version if possible, 
    // but here we'll just use a soft colored stroke or layer)
    drawPath(
        path = path,
        color = Color(0xFF9575CD),
        style = Stroke(width = 4f)
    )
    drawPath(
        path = path,
        color = Color(0xFF673AB7)
    )
}
