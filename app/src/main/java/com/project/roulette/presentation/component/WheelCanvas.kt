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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.roulette.domain.model.Segment
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
    rotation: Float = 0f,
    modifier: Modifier = Modifier,
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
                    drawSectorLabel(midAngle, centerX, centerY, radius, segment.name)

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
    text: String
) {
    val angleRad = (angleDegrees) * PI.toFloat() / 180f
    val textX = centerX + (radius * 0.65f) * cos(angleRad)
    val textY = centerY + (radius * 0.65f) * sin(angleRad)

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        save()
        translate(textX, textY)
        // rotate so text reads outward along radius; subtract 90 so text is upright
        rotate(angleDegrees - 90f)
        drawText(text, 0f, 0f, paint)
        restore()
    }
}

private fun DrawScope.drawPointerAtTop(centerX: Float, centerY: Float, radius: Float) {
    val pointerSize = radius * 0.08f
    val apexX = centerX
    val apexY = centerY - radius + pointerSize / 2f

    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(apexX - pointerSize / 2f, apexY)
        lineTo(apexX + pointerSize / 2f, apexY)
        lineTo(apexX, apexY + pointerSize)
        close()
    }

    drawPath(path = path, color = Color.Red)
    drawPath(path = path, color = Color.Black, style = Stroke(width = 1f))
}
