package com.project.roulette.presentation.screen.statistics

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.model.WheelStatistics
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppTopBar
import com.project.roulette.presentation.component.design.GroupedCard
import com.project.roulette.presentation.component.design.MetricBar
import com.project.roulette.presentation.component.design.Pill
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.SectionHeader
import com.project.roulette.presentation.component.design.SettingsRow
import com.project.roulette.presentation.component.design.StatCard
import com.project.roulette.presentation.component.design.StatCardStyle
import com.project.roulette.presentation.model.ChartType
import com.project.roulette.presentation.model.StatisticsUiState
import com.project.roulette.presentation.model.StreakInfo
import com.project.roulette.presentation.model.TimelineItem
import com.project.roulette.presentation.viewmodel.StatisticsEffect
import com.project.roulette.presentation.viewmodel.StatisticsViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.util.PdfExporter

@Composable
fun StatisticsScreen(
    wheelId: String,
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    LaunchedEffect(wheelId) {
        viewModel.loadStatistics(wheelId)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StatisticsEffect.SharePdf -> {
                    val uri = PdfExporter.generateAndSharePdf(
                        context,
                        effect.wheelName,
                        effect.results,
                        effect.stats
                    )
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_SUBJECT, "${effect.wheelName} Statistics Report")
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Statistics Report"))
                    }
                }
            }
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Statistics",
                eyebrow = (uiState as? StatisticsUiState.Success)?.wheel?.name ?: "Wheel",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.exportHistory() }) {
                        Icon(
                            painter = AppIcons.Share,
                            contentDescription = "Share report",
                            tint = colors.textPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is StatisticsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            is StatisticsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = dimens.screenPadding,
                        end = dimens.screenPadding,
                        top = dimens.listTopPadding,
                        bottom = dimens.listBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(dimens.space16)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.space12)
                        ) {
                            StatCard(
                                value = state.statistics.totalSpins.toString(),
                                label = "Total spins",
                                accent = colors.primary,
                                style = StatCardStyle.Tinted,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = state.statistics.mostFrequent ?: "—",
                                label = "Most picked",
                                accent = colors.success,
                                style = StatCardStyle.Tinted,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = state.statistics.leastFrequent ?: "—",
                                label = "Least picked",
                                accent = colors.warning,
                                style = StatCardStyle.Tinted,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        FairnessScoreCard(state.fairnessScore, state.fairnessMessage)
                    }

                    item {
                        SelectionDistributionCard(
                            statistics = state.statistics,
                            chartType = state.chartType,
                            onToggle = { viewModel.toggleChartType() },
                            wheel = state.wheel
                        )
                    }

                    if (state.streaks.isNotEmpty()) {
                        item { LuckyStreaksSection(state.streaks) }
                    }

                    if (state.timelineData.isNotEmpty()) {
                        item { SpinTimelineCard(state.timelineData) }
                    }

                    item {
                        GroupedCard {
                            SettingsRow(
                                leadingIcon = AppIcons.Autorenew,
                                leadingIconTint = colors.danger,
                                title = "Reset statistics",
                                subtitle = "Clears all spin history for this wheel",
                                titleColor = colors.danger,
                                onClick = { viewModel.clearStatistics(wheelId) }
                            )
                        }
                    }
                }
            }

            is StatisticsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(dimens.space32)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.danger
                        )
                        Spacer(Modifier.size(dimens.space16))
                        PrimaryButton(
                            text = "Retry",
                            onClick = { viewModel.loadStatistics(wheelId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FairnessScoreCard(score: Int, message: String) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val progress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(1000),
        label = "fairness"
    )

    GroupedCard {
        Column(modifier = Modifier.padding(dimens.space16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = AppIcons.Balance,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(dimens.iconSizeSmall)
                )
                Spacer(Modifier.width(dimens.space12))
                Text(
                    text = "Fairness score",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.warning
                )
            }

            Spacer(Modifier.size(dimens.space16))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.space8),
                color = colors.warning,
                trackColor = colors.divider,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Spacer(Modifier.size(dimens.space12))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectionDistributionCard(
    statistics: WheelStatistics,
    chartType: ChartType,
    onToggle: () -> Unit,
    wheel: Wheel
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    GroupedCard {
        Column(modifier = Modifier.padding(dimens.space16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selection distribution",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                ChartTypeToggle(chartType = chartType, onToggle = onToggle)
            }

            Spacer(Modifier.size(dimens.space20))

            if (chartType == ChartType.BAR) {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.space16)) {
                    val entries = statistics.selectionCounts.entries.sortedByDescending { it.value }
                    entries.forEachIndexed { index, entry ->
                        val segment = wheel.segments.find { it.name == entry.key }
                        val percentage = statistics.selectionPercentages[entry.key]?.toInt() ?: 0
                        val accent = rememberAccentOnSurface(segment?.color ?: colors.textSecondary)
                        val medal = when (index) {
                            0 -> "🥇 "
                            1 -> "🥈 "
                            2 -> "🥉 "
                            else -> ""
                        }
                        MetricBar(
                            name = "$medal${entry.key}",
                            value = "${entry.value} · $percentage%",
                            progress = percentage / 100f,
                            accent = accent
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(statistics, wheel)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = statistics.totalSpins.toString(),
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

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimens.space16),
                    horizontalArrangement = Arrangement.spacedBy(dimens.space8),
                    verticalArrangement = Arrangement.spacedBy(dimens.space8),
                    maxItemsInEachRow = 3
                ) {
                    wheel.segments.forEach { segment ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(dimens.space8)
                                    .background(segment.color, RouletteTheme.shapes.avatar)
                            )
                            Spacer(Modifier.width(dimens.space4))
                            Text(
                                text = segment.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartTypeToggle(chartType: ChartType, onToggle: () -> Unit) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Surface(
        modifier = Modifier.height(dimens.buttonHeightSmall),
        shape = RouletteTheme.shapes.chip,
        color = colors.background,
        border = BorderStroke(dimens.borderWidth, colors.divider)
    ) {
        Row(modifier = Modifier.padding(2.dp)) {
            ChartType.entries.forEach { type ->
                val selected = chartType == type
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RouletteTheme.shapes.chip)
                        .background(if (selected) colors.primary else Color.Transparent)
                        .clickable(enabled = !selected, onClick = onToggle)
                        .padding(horizontal = dimens.space16),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (type == ChartType.BAR) "Bar" else "Ring",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) colors.onPrimary else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(statistics: WheelStatistics, wheel: Wheel) {
    val fallback = RouletteTheme.colors.textSecondary
    Canvas(modifier = Modifier.size(160.dp)) {
        var startAngle = -90f
        val strokeWidth = 24.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        statistics.selectionCounts.forEach { (name, count) ->
            val segment = wheel.segments.find { it.name == name }
            val sweepAngle = if (statistics.totalSpins > 0) {
                (count.toFloat() / statistics.totalSpins) * 360f
            } else {
                0f
            }

            drawArc(
                color = segment?.color ?: fallback,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LuckyStreaksSection(streaks: List<StreakInfo>) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    Column {
        SectionHeader("Lucky streaks")
        Column(verticalArrangement = Arrangement.spacedBy(dimens.space8)) {
            streaks.forEach { streak ->
                val accent = rememberAccentOnSurface(streak.color)
                GroupedCard(
                    border = BorderStroke(dimens.borderWidth, accent.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.space16),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = AppIcons.Whatshot,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(dimens.iconSize)
                        )
                        Spacer(Modifier.width(dimens.rowIconGap))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = streak.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Picked ${streak.count} times in a row",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Pill(text = "${streak.count}x", accent = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpinTimelineCard(timeline: List<TimelineItem>) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    GroupedCard {
        Column(modifier = Modifier.padding(dimens.space16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = AppIcons.TrendingUp,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(dimens.iconSizeSmall)
                )
                Spacer(Modifier.width(dimens.space12))
                Text(
                    text = "Spin timeline",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            }

            Spacer(Modifier.size(dimens.space20))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(dimens.space4),
                verticalAlignment = Alignment.Bottom
            ) {
                timeline.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(0.4f + (item.index % 5) * 0.1f)
                            .clip(RouletteTheme.shapes.barTop)
                            .background(item.color)
                    )
                }
            }

            Spacer(Modifier.size(dimens.space8))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spin 1",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Text(
                    text = "Spin ${timeline.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}
