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
 * Uses drawArc and weights to compute sector sizes and fixes text rotation.
 */
@Composable
fun WheelCanvas(
    wheel: Wheel,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    wheelSize: Dp = 260.dp
) {
    val segments = wheel.getActiveSegments()
    
    // Exact colors from the reference image and expanded variety
    val jewelTones = listOf(
        Color(0xFF1E2736), // Deep Slate Blue
        Color(0xFF2D264D), // Deep Purple
        Color(0xFF1A332B), // Dark Forest Green
        Color(0xFF3B241A), // Deep Rust Brown
        Color(0xFF3D1B1B), // Deep Burgundy
        Color(0xFF0D2C33), // Dark Teal
        Color(0xFF2A1A2F), // Deep Plum
        Color(0xFF1A1F2B), // Midnight Navy
        Color(0xFF2E2B1A), // Dark Olive
        Color(0xFF3D2A1B)  // Dark Sienna
    )
    
    // Lighter versions for text to match the image style
    val textColors = listOf(
        Color(0xFF82B1FF), // Light Blue
        Color(0xFFB39DDB), // Light Purple
        Color(0xFF81C784), // Light Green
        Color(0xFFFFAB91), // Light Peach/Rust
        Color(0xFFFF8A80), // Light Red/Coral
        Color(0xFF80DEEA), // Light Cyan
        Color(0xFFF48FB1), // Light Pink
        Color(0xFF9FA8DA), // Light Indigo
        Color(0xFFE6EE9C), // Light Lime/Yellow
        Color(0xFFFFCC80)  // Light Orange
    )

    Box(
        modifier = modifier.size(wheelSize + 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Ring
        Canvas(modifier = Modifier.size(wheelSize + 12.dp)) {
            drawCircle(
                color = Color(0xFF121212),
                radius = size.width / 2f,
                style = Stroke(width = 8f)
            )
            drawCircle(
                color = Color(0xFF1E1E2C),
                radius = size.width / 2f - 4f,
                style = Stroke(width = 2f)
            )
        }

        Canvas(modifier = Modifier.size(wheelSize)) {
            if (segments.isEmpty()) return@Canvas

            val radius = wheelSize.toPx() / 2f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            val sweep = 360f / segments.size

            // Apply rotation
            rotate(rotation, Offset(centerX, centerY)) {
                val startAngleOffset = -90f - (sweep / 2f)
                
                // First pass: Draw all background sectors
                var currentAngle = startAngleOffset
                segments.forEachIndexed { index, _ ->
                    drawArc(
                        color = jewelTones[index % jewelTones.size],
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                    currentAngle += sweep
                }

                // Second pass: Draw all divider lines on top of the sectors
                currentAngle = startAngleOffset
                segments.forEachIndexed { _, _ ->
                    val lineAngleRad = currentAngle * PI.toFloat() / 180f
                    drawLine(
                        color = Color.Black.copy(alpha = 0.7f),
                        start = Offset(centerX, centerY),
                        end = Offset(
                            centerX + radius * cos(lineAngleRad),
                            centerY + radius * sin(lineAngleRad)
                        ),
                        strokeWidth = 4f
                    )
                    currentAngle += sweep
                }

                // Third pass: Draw all labels on top of everything
                currentAngle = startAngleOffset
                segments.forEachIndexed { index, segment ->
                    val midAngle = currentAngle + sweep / 2f
                    drawSectorLabel(
                        midAngle, 
                        centerX, 
                        centerY, 
                        radius, 
                        segment.name, 
                        textColors[index % textColors.size]
                    )
                    currentAngle += sweep
                }
            }

            // Hub - Purple center with dark border
            drawCircle(
                color = Color(0xFF45408A), // Inner Purple
                radius = radius * 0.1f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF121212), // Border
                radius = radius * 0.1f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f)
            )
        }

        // Pointer (Small light triangle at top)
        Canvas(modifier = Modifier.size(wheelSize)) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = wheelSize.toPx() / 2f
            val pointerWidth = 16.dp.toPx()
            val pointerHeight = 12.dp.toPx()
            
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(centerX - pointerWidth / 2, centerY - radius - 2f)
                lineTo(centerX + pointerWidth / 2, centerY - radius - 2f)
                lineTo(centerX, centerY - radius + pointerHeight)
                close()
            }
            
            drawPath(
                path = path,
                color = Color(0xFFB3B8D3) // Pale Lavender/Grey from image
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
    
    // Start text further from center
    val startRadius = radius * 0.4f
    val textX = centerX + startRadius * cos(angleRad)
    val textY = centerY + startRadius * sin(angleRad)

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = 32f
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
