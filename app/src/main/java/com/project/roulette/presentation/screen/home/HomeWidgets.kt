package com.project.roulette.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.design.ActivityBarChart
import com.project.roulette.presentation.component.design.ChartBar
import com.project.roulette.presentation.component.design.ChartSlice
import com.project.roulette.presentation.component.design.GroupedCard
import com.project.roulette.presentation.component.design.LegendRow
import com.project.roulette.presentation.component.design.MetricBar
import com.project.roulette.presentation.component.design.Pill
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.RingChart
import com.project.roulette.presentation.component.design.SegmentedToggle
import com.project.roulette.presentation.model.ChartType
import com.project.roulette.presentation.model.DashboardUiState
import com.project.roulette.presentation.model.DistributionItem
import com.project.roulette.presentation.model.QuickWheel
import com.project.roulette.presentation.model.RecentSpinItem
import com.project.roulette.presentation.model.ShareSlice
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.util.TimeUtils

private val QuickTileHeight = 164.dp

@Composable
internal fun ActivityHeroCard(state: DashboardUiState.Success) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    val bars = state.weekActivity.map { day ->
        ChartBar(label = day.label, value = day.count, highlighted = day.isToday)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RouletteTheme.shapes.card)
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primary.copy(alpha = if (colors.isLight) 0.16f else 0.26f),
                        colors.primary.copy(alpha = if (colors.isLight) 0.05f else 0.10f)
                    )
                )
            )
            .border(dimens.borderWidth, colors.primaryBorder, RouletteTheme.shapes.card)
    ) {
        Column(modifier = Modifier.padding(dimens.space20)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL SPINS",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(dimens.space4))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.totalSpins.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = colors.primary
                        )
                        if (state.spinsToday > 0) {
                            Spacer(Modifier.width(dimens.space8))
                            Pill(text = "+${state.spinsToday} today", accent = colors.success)
                        }
                    }
                }
                StreakBadge(state.dayStreak)
            }

            Spacer(Modifier.height(dimens.space20))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LAST 7 DAYS",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (state.spinsThisWeek == 1) "1 spin" else "${state.spinsThisWeek} spins",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary
                )
            }

            Spacer(Modifier.height(dimens.space16))

            ActivityBarChart(bars = bars, accent = colors.primary)
        }
    }
}

@Composable
private fun StreakBadge(dayStreak: Int) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val active = dayStreak > 0
    val accent = if (active) colors.warning else colors.textTertiary

    Surface(
        shape = RouletteTheme.shapes.cardSmall,
        color = colors.surface,
        border = BorderStroke(dimens.borderWidth, if (active) accent.copy(alpha = 0.3f) else colors.divider)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimens.space12, vertical = dimens.space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = AppIcons.Whatshot,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(dimens.iconSizeSmall)
            )
            Spacer(Modifier.width(dimens.space8))
            Column {
                Text(
                    text = if (active) "$dayStreak day${if (dayStreak == 1) "" else "s"}" else "No streak",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = if (active) "on a streak" else "spin to start",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
internal fun SpinShareCard(
    slices: List<ShareSlice>,
    totalSpins: Int,
    chartType: ChartType,
    onToggle: () -> Unit,
    onSliceClick: (String) -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    GroupedCard {
        Column(modifier = Modifier.padding(dimens.space16)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Where spins go",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Split across your wheels",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                SegmentedToggle(
                    options = listOf("Ring", "Bars"),
                    selectedIndex = if (chartType == ChartType.RING) 0 else 1,
                    onSelect = { onToggle() }
                )
            }

            Spacer(Modifier.height(dimens.space20))

            if (chartType == ChartType.RING) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RingChart(
                        slices = slices.map { ChartSlice(it.label, it.count, it.color) },
                        modifier = Modifier.size(196.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = totalSpins.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "spins",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimens.space16))

                Column {
                    slices.forEach { slice ->
                        LegendRow(
                            label = slice.label,
                            value = "${slice.count} · ${slice.percentage}%",
                            color = slice.color,
                            onClick = slice.wheelId?.let { id -> { onSliceClick(id) } }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.space16)) {
                    slices.forEach { slice ->
                        val accent = rememberAccentOnSurface(slice.color)
                        MetricBar(
                            name = slice.label,
                            value = "${slice.count} · ${slice.percentage}%",
                            progress = slice.percentage / 100f,
                            accent = accent
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TopPicksCard(picks: List<DistributionItem>) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    GroupedCard {
        Column(modifier = Modifier.padding(dimens.space16)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RouletteTheme.shapes.thumbnail)
                        .background(colors.warningSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = AppIcons.Trophy,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(dimens.iconSizeSmall)
                    )
                }
                Spacer(Modifier.width(dimens.space12))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Top winners",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Names that keep coming up",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(dimens.space20))

            Column(verticalArrangement = Arrangement.spacedBy(dimens.space16)) {
                picks.forEachIndexed { index, pick ->
                    val accent = rememberAccentOnSurface(pick.color)
                    val medal = when (index) {
                        0 -> "🥇 "
                        1 -> "🥈 "
                        2 -> "🥉 "
                        else -> ""
                    }
                    MetricBar(
                        name = "$medal${pick.name}",
                        value = "${pick.count} · ${pick.percentage}%",
                        progress = pick.percentage / 100f,
                        accent = accent
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuickWheelCard(
    wheel: QuickWheel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val accent = rememberAccentOnSurface(wheel.color)

    Surface(
        modifier = modifier
            .width(150.dp)
            .height(QuickTileHeight)
            .clickable(onClick = onClick),
        shape = RouletteTheme.shapes.card,
        color = colors.surface,
        border = BorderStroke(dimens.borderWidth, colors.divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.space16),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                SegmentRing(
                    segmentColors = wheel.segmentColors,
                    fallback = accent,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.weight(1f))
                if (wheel.isFavorite) {
                    Icon(
                        painter = AppIcons.FavoriteFilled,
                        contentDescription = null,
                        tint = colors.danger,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(dimens.space12))

            Text(
                text = wheel.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${wheel.segmentCount} segments",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(dimens.space12))

            Pill(
                text = if (wheel.spinCount == 1) "1 spin" else "${wheel.spinCount} spins",
                accent = accent,
                showDot = true
            )
        }
    }
}

@Composable
internal fun RecentSpinRow(
    item: RecentSpinItem,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val accent = rememberAccentOnSurface(item.color)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = dimens.space16, vertical = dimens.space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RouletteTheme.shapes.thumbnail)
                    .background(accent.copy(alpha = if (colors.isLight) 0.12f else 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dimens.space12)
                        .background(accent, RouletteTheme.shapes.avatar)
                )
            }

            Spacer(Modifier.width(dimens.rowIconGap))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.segmentName,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.wheelName} · ${TimeUtils.getRelativeTime(item.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                painter = AppIcons.ChevronRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(dimens.chevronSize)
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .padding(start = dimens.dividerInsetWithIcon)
                    .fillMaxWidth()
                    .height(dimens.borderWidth)
                    .background(colors.divider)
            )
        }
    }
}

@Composable
internal fun WelcomeCard(onCreate: () -> Unit) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RouletteTheme.shapes.card,
        color = colors.primarySubtle,
        border = BorderStroke(dimens.borderWidth, colors.primaryBorder)
    ) {
        Column(modifier = Modifier.padding(dimens.space24)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colors.surface, RouletteTheme.shapes.card),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = AppIcons.Wheel,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(dimens.space16))

            Text(
                text = "Make your first wheel",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(dimens.space4))
            Text(
                text = "Add names, spin, and this dashboard fills up with charts of every result.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(dimens.space20))

            PrimaryButton(
                text = "Create wheel",
                icon = AppIcons.Add,
                onClick = onCreate
            )
        }
    }
}

@Composable
internal fun FirstSpinCard(onSpin: (() -> Unit)?) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    GroupedCard(border = BorderStroke(dimens.borderWidth, colors.primaryBorder)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RouletteTheme.shapes.thumbnail)
                    .background(colors.primarySubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = AppIcons.Casino,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(dimens.iconSize)
                )
            }

            Spacer(Modifier.width(dimens.rowIconGap))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "No spins yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "Take your first spin to unlock charts and history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            if (onSpin != null) {
                Spacer(Modifier.width(dimens.space12))
                Pill(text = "Spin now", accent = colors.primary, modifier = Modifier.clickable(onClick = onSpin))
            }
        }
    }
}

@Composable
internal fun SegmentRing(
    segmentColors: List<Color>,
    fallback: Color,
    modifier: Modifier = Modifier
) {
    val slices = segmentColors.ifEmpty { listOf(fallback) }
    val track = RouletteTheme.colors.divider

    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.28f
        val diameter = size.minDimension - stroke
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val sweep = 360f / slices.size
        val gap = if (slices.size > 1) (sweep * 0.12f).coerceAtMost(6f) else 0f

        drawArc(
            color = track,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = stroke),
            size = arcSize,
            topLeft = topLeft
        )

        slices.forEachIndexed { index, color ->
            drawArc(
                color = color,
                startAngle = -90f + index * sweep + gap / 2f,
                sweepAngle = sweep - gap,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )
        }
    }
}

@Composable
internal fun CreateWheelTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    Surface(
        modifier = modifier
            .width(150.dp)
            .height(QuickTileHeight)
            .clickable(onClick = onClick),
        shape = RouletteTheme.shapes.card,
        color = colors.primarySubtle,
        border = BorderStroke(dimens.borderWidth, colors.primaryBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.space16),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RouletteTheme.shapes.avatar)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = AppIcons.Add,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(dimens.iconSize)
                )
            }

            Column {
                Text(
                    text = "Create a wheel",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = "Pick a template or start blank",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            Pill(text = "Tap to start", accent = colors.primary)
        }
    }
}

@Composable
internal fun HeaderActionButton(
    icon: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = RouletteTheme.colors.textPrimary,
    container: Color = RouletteTheme.colors.surface,
    border: Color = RouletteTheme.colors.divider
) {
    val dimens = RouletteTheme.dimens
    Surface(
        modifier = modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        shape = RouletteTheme.shapes.avatar,
        color = container,
        border = BorderStroke(dimens.borderWidth, border)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(dimens.iconSize)
            )
        }
    }
}
