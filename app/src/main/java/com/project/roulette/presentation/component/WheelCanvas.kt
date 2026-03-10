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
    wheelSize: Dp = 300.dp
) {
    Box(
        modifier = modifier.size(wheelSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(wheelSize)) {
            val segments = wheel.getActiveSegments()
            if (segments.isEmpty()) return@Canvas

            val radius = wheelSize.toPx() / 2f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Compute sweep angles using segment weights
            val totalWeight = segments.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(0.0001f)
            val angles = segments.map { (it.weight / totalWeight) * 360f }

            // Apply rotation
            rotate(rotation, Offset(centerX, centerY)) {
                var startAngle = -90f // start at top
                segments.forEachIndexed { index, segment ->
                    val sweep = angles[index]

                    // Draw filled arc sector
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )

                    // Draw border (stroke)
                    drawArc(
                        color = Color.Black,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        style = Stroke(width = 2f)
                    )

                    // Draw label at midpoint of sector
                    val midAngle = startAngle + sweep / 2f
                    // Pass the number of segments to adjust text size and placement
                    drawSectorLabel(midAngle, centerX, centerY, radius, segment.name, segments.size)

                    startAngle += sweep
                }
            }

            // Draw center circle
            drawCircle(
                color = Color.White,
                radius = radius * 0.1f,
                center = Offset(centerX, centerY)
            )

            // Draw pointer at top edge of wheel
            drawPointerAtTop(centerX, centerY, radius)
        }
    }
}

private fun DrawScope.drawSectorLabel(
    angleDegrees: Float,
    centerX: Float,
    centerY: Float,
    radius: Float,
    text: String,
    segmentCount: Int
) {
    // Dynamically adjust text size based on number of segments
    // For many segments (e.g. 20+), we need smaller text.
    val baseTextSize = 32f
    val adjustedTextSize = when {
        segmentCount <= 6 -> baseTextSize
        segmentCount <= 12 -> 28f
        segmentCount <= 20 -> 24f
        segmentCount <= 30 -> 20f
        else -> 16f
    }

    // Adjust text position: pull it further out if there are many segments
    val radiusFactor = when {
        segmentCount <= 12 -> 0.65f
        segmentCount <= 20 -> 0.75f
        else -> 0.85f
    }

    val angleRad = (angleDegrees) * PI.toFloat() / 180f
    val textX = centerX + (radius * radiusFactor) * cos(angleRad)
    val textY = centerY + (radius * radiusFactor) * sin(angleRad)

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = adjustedTextSize
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            // Add a subtle shadow for better readability on various colors
            setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
        }
        
        save()
        translate(textX, textY)
        
        // Rotate text to align with the radius (radial text)
        // For many segments, radial text is much more legible than upright text.
        rotate(angleDegrees)
        
        // If the text is on the left side of the wheel, flip it so it's not upside down
        val normalizedAngle = (angleDegrees % 360f + 360f) % 360f
        if (normalizedAngle > 90f && normalizedAngle < 270f) {
            rotate(180f)
        }
        
        // Truncate long text for small segments
        val maxChars = when {
            segmentCount <= 10 -> 15
            segmentCount <= 20 -> 10
            else -> 6
        }
        val displayName = if (text.length > maxChars) text.take(maxChars - 1) + "…" else text

        drawText(displayName, 0f, 0f, paint)
        restore()
    }
}

private fun DrawScope.drawPointerAtTop(centerX: Float, centerY: Float, radius: Float) {
    // Increased pointer size for better visibility
    val pointerSize = radius * 0.12f
    val halfWidth = pointerSize / 2f

    // Apex (top center) of the pointer
    val apexX = centerX
    val apexY = centerY - radius + pointerSize / 2f

    // Coordinates for outer (outline) triangle
    val outerLeftX = apexX - halfWidth
    val outerRightX = apexX + halfWidth
    val outerBottomY = apexY + pointerSize

    val outerPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(outerLeftX, apexY)
        lineTo(outerRightX, apexY)
        lineTo(apexX, outerBottomY)
        close()
    }

    // Draw outer outline (dark) to create strong contrast
    drawPath(path = outerPath, color = Color.Black)

    // Inner triangle slightly inset for a bright, noticeable fill
    val inset = pointerSize * 0.18f
    val innerLeftX = outerLeftX + inset
    val innerRightX = outerRightX - inset
    val innerBottomY = outerBottomY - inset

    val innerPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(innerLeftX, apexY + inset * 0.1f)
        lineTo(innerRightX, apexY + inset * 0.1f)
        lineTo(apexX, innerBottomY)
        close()
    }

    // Bright fill for high contrast (use Yellow) and a subtle inner stroke
    drawPath(path = innerPath, color = Color(0xFFFFD54F)) // Amber/Yellow
    drawPath(path = innerPath, color = Color.Black, style = Stroke(width = 2f))

    // Draw small tip circle for extra visibility and to ensure pointer center is easy to spot
    val tipRadius = pointerSize * 0.18f
    drawCircle(color = Color.White, radius = tipRadius, center = Offset(apexX, apexY + tipRadius))
    drawCircle(color = Color.Black, radius = tipRadius, center = Offset(apexX, apexY + tipRadius), style = Stroke(width = 2f))
}
