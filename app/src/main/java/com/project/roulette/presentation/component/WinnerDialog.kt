package com.project.roulette.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.model.Wheel
import java.util.*
import kotlin.random.Random
import androidx.compose.ui.graphics.painter.Painter
import com.project.roulette.ui.theme.RouletteTheme

@Composable
fun WinnerDialog(
    result: SpinResult,
    wheel: Wheel,
    spinsToday: Int,
    themeColor: Color,
    lighterThemeColor: Color,
    onDismiss: () -> Unit,
    onSpinAgain: () -> Unit,
    onRemoveFromWheel: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RouletteTheme.colors.scrim),
            contentAlignment = Alignment.Center
        ) {
            ConfettiEffect(themeColor = themeColor)

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(32.dp)),
                color = RouletteTheme.colors.surfaceElevated
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Rainbow accent bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        themeColor,
                                        Color(0xFF00BCD4),
                                        RouletteTheme.colors.warning,
                                        RouletteTheme.colors.danger
                                    )
                                )
                            )
                    )

                    Spacer(Modifier.height(32.dp))

                    // Glowing Trophy Area
                    Box(contentAlignment = Alignment.Center) {
                        val infiniteTransition = rememberInfiniteTransition(label = "glow")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 0.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(scale)
                                .background(themeColor.copy(alpha = alpha), CircleShape)
                        )

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(RouletteTheme.colors.surfaceElevated, CircleShape)
                                .border(2.dp, themeColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎉", style = MaterialTheme.typography.displayLarge)
                            
                            // Checkmark badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .size(24.dp)
                                    .background(RouletteTheme.colors.success, CircleShape)
                                    .border(2.dp, RouletteTheme.colors.surfaceElevated, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(AppIcons.Check, contentDescription = null, tint = RouletteTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "WE HAVE A WINNER!",
                        style = MaterialTheme.typography.labelMedium,
                        color = RouletteTheme.colors.textSecondary,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = result.selectedSegmentName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = RouletteTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(Modifier.height(24.dp))

                    // Meta Chips
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        MetaChip(
                            icon = AppIcons.History,
                            text = "Spin #$spinsToday",
                            containerColor = themeColor.copy(alpha = 0.2f),
                            contentColor = lighterThemeColor
                        )
                        MetaChip(
                            icon = AppIcons.Timer,
                            text = String.format(Locale.US, "%.1fs", result.spinDuration / 1000f),
                            containerColor = RouletteTheme.colors.successSubtle,
                            contentColor = RouletteTheme.colors.success
                        )
                        
                        val selectedSegment = wheel.segments.find { it.id == result.selectedSegmentId }
                        val totalWeight = wheel.getTotalWeight()
                        val probability = if (selectedSegment != null && totalWeight > 0) {
                            (selectedSegment.weight / totalWeight * 100).toInt()
                        } else 0

                        MetaChip(
                            icon = AppIcons.Percent,
                            text = "$probability%",
                            containerColor = RouletteTheme.colors.warningSubtle,
                            contentColor = RouletteTheme.colors.warning
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = RouletteTheme.colors.divider, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSpinAgain,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RouletteTheme.colors.textSecondary.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RouletteTheme.colors.textPrimary)
                        ) {
                            Icon(AppIcons.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Spin again", style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RouletteTheme.colors.textPrimary, contentColor = RouletteTheme.colors.surface)
                        ) {
                            Icon(
                                AppIcons.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = RouletteTheme.colors.surface
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Got It!",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = RouletteTheme.colors.surface)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (wheel.getActiveSegments().size > 2) {
                        TextButton(onClick = onRemoveFromWheel) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    AppIcons.PersonRemove,
                                    contentDescription = null,
                                    tint = RouletteTheme.colors.danger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Remove from wheel", color = RouletteTheme.colors.danger, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MetaChip(icon: Painter, text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = contentColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ConfettiEffect(themeColor: Color) {
    val confettiCount = 20
    val colors = listOf(themeColor, Color(0xFF00BCD4), RouletteTheme.colors.warning, RouletteTheme.colors.danger)
    
    repeat(confettiCount) {
        val xProgress = remember { Random.nextFloat() }
        val duration = remember { 3000 + Random.nextInt(2000) }
        val delay = remember { Random.nextInt(5000) }

        val infiniteTransition = rememberInfiniteTransition(label = "confetti")
        val yOffset by infiniteTransition.animateFloat(
            initialValue = 1.2f,
            targetValue = -0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, delayMillis = delay, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "yOffset"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val color = colors[Random.nextInt(colors.size)]
            drawCircle(
                color = color.copy(alpha = 0.6f),
                radius = 4.dp.toPx(),
                center = Offset(size.width * xProgress, size.height * yOffset)
            )
        }
    }
}
