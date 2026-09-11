package com.project.roulette.presentation.component.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.roulette.ui.theme.RouletteTheme

data class ChartBar(
    val label: String,
    val value: Int,
    val highlighted: Boolean = false
)

data class ChartSlice(
    val label: String,
    val value: Int,
    val color: Color
)

@Composable
fun ActivityBarChart(
    bars: List<ChartBar>,
    modifier: Modifier = Modifier,
    accent: Color = RouletteTheme.colors.primary,
    plotHeight: Dp = 132.dp
) {
    if (bars.isEmpty()) return

    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val peak = bars.maxOf { it.value }.coerceAtLeast(1)
    val trackColor = if (colors.isLight) {
        Color.Black.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.07f)
    }

    var play by remember(bars) { mutableStateOf(false) }
    LaunchedEffect(bars) { play = true }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(plotHeight),
            horizontalArrangement = Arrangement.spacedBy(dimens.space8),
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEachIndexed { index, bar ->
                val fraction by animateFloatAsState(
                    targetValue = if (play) bar.value.toFloat() / peak else 0f,
                    animationSpec = tween(
                        durationMillis = 620,
                        delayMillis = index * 55,
                        easing = FastOutSlowInEasing
                    ),
                    label = "bar_${bar.label}_$index"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (bar.value > 0) bar.value.toString() else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bar.highlighted) accent else colors.textTertiary,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(dimens.space4))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(trackColor),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (bar.highlighted) {
                                        accent
                                    } else {
                                        accent.copy(alpha = if (colors.isLight) 0.35f else 0.42f)
                                    }
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(dimens.space8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.space8)
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (bar.highlighted) accent else colors.textTertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RingChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    thickness: Dp = 26.dp,
    gapDegrees: Float = 2.5f,
    center: @Composable () -> Unit = {}
) {
    val colors = RouletteTheme.colors
    val total = slices.sumOf { it.value }.coerceAtLeast(1)
    val trackColor = if (colors.isLight) {
        Color.Black.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.07f)
    }

    var play by remember(slices) { mutableStateOf(false) }
    LaunchedEffect(slices) { play = true }

    val sweepProgress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "ring_sweep"
    )

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = thickness.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke),
                size = arcSize,
                topLeft = topLeft
            )

            var startAngle = -90f
            slices.forEach { slice ->
                val fullSweep = slice.value.toFloat() / total * 360f
                val gap = if (slices.size > 1 && fullSweep > gapDegrees * 2) gapDegrees else 0f
                val sweep = ((fullSweep - gap) * sweepProgress).coerceAtLeast(0f)

                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                        size = arcSize,
                        topLeft = topLeft
                    )
                }
                startAngle += fullSweep
            }
        }
        center()
    }
}

@Composable
fun LegendRow(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RouletteTheme.shapes.cardSmall)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = dimens.space8, horizontal = dimens.space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimens.space12)
                .background(color, RouletteTheme.shapes.avatar)
        )
        Spacer(Modifier.width(dimens.space12))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary
        )
    }
}

@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    Surface(
        modifier = modifier.height(dimens.buttonHeightSmall),
        shape = RouletteTheme.shapes.chip,
        color = if (colors.isLight) colors.background else colors.surfacePressed,
        border = BorderStroke(dimens.borderWidth, colors.divider)
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val background by animateColorAsState(
                    targetValue = if (selected) colors.primary else Color.Transparent,
                    animationSpec = tween(220),
                    label = "toggle_bg_$index"
                )
                val content by animateColorAsState(
                    targetValue = if (selected) colors.onPrimary else colors.textSecondary,
                    animationSpec = tween(220),
                    label = "toggle_fg_$index"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RouletteTheme.shapes.chip)
                        .background(background)
                        .clickable(enabled = !selected) { onSelect(index) }
                        .padding(horizontal = dimens.space16),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelSmall,
                        color = content
                    )
                }
            }
        }
    }
}
