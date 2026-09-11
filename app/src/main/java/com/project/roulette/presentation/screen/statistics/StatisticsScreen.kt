package com.project.roulette.presentation.screen.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.model.WheelStatistics
import com.project.roulette.presentation.model.ChartType
import com.project.roulette.presentation.model.StatisticsUiState
import com.project.roulette.presentation.model.StreakInfo
import com.project.roulette.presentation.model.TimelineItem
import com.project.roulette.presentation.viewmodel.StatisticsEffect
import com.project.roulette.presentation.viewmodel.StatisticsViewModel
import com.project.roulette.util.PdfExporter
import com.project.roulette.ui.theme.RouletteTheme
import android.content.Intent
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalContext
import com.project.roulette.presentation.component.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    wheelId: String,
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(wheelId) {
        viewModel.loadStatistics(wheelId)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StatisticsEffect.SharePdf -> {
                    val uri = PdfExporter.generateAndSharePdf(context, effect.wheelName, effect.results, effect.stats)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val subtitle = (uiState as? StatisticsUiState.Success)?.wheel?.name ?: "Wheel"
                        Text(subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textSecondary, letterSpacing = 1.sp)
                        Text("Statistics", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, "Back", tint = RouletteTheme.colors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportHistory() }) {
                        Icon(AppIcons.Download, "Export", tint = RouletteTheme.colors.textPrimary)
                    }
                    IconButton(onClick = { viewModel.exportHistory() }) {
                        Icon(AppIcons.Share, "Share", tint = RouletteTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RouletteTheme.colors.background)
            )
        },
        containerColor = RouletteTheme.colors.background
    ) { padding ->
        when (val state = uiState) {
            is StatisticsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RouletteTheme.colors.primary)
                }
            }

            is StatisticsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Top 3 Stats Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                value = state.statistics.totalSpins.toString(),
                                label = "Total spins",
                                color = RouletteTheme.colors.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = state.statistics.mostFrequent ?: "—",
                                label = "Most picked",
                                color = RouletteTheme.colors.success,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = state.statistics.leastFrequent ?: "—",
                                label = "Least picked",
                                color = RouletteTheme.colors.warning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 2. Fairness Score
                    item {
                        FairnessScoreCard(state.fairnessScore, state.fairnessMessage)
                    }

                    // 3. Selection Distribution
                    item {
                        SelectionDistributionCard(
                            statistics = state.statistics,
                            chartType = state.chartType,
                            onToggle = { viewModel.toggleChartType() },
                            wheel = state.wheel
                        )
                    }

                    // 4. Lucky Streaks
                    if (state.streaks.isNotEmpty()) {
                        item {
                            LuckyStreaksSection(state.streaks)
                        }
                    }

                    // 5. Spin Timeline
                    if (state.timelineData.isNotEmpty()) {
                        item {
                            SpinTimelineCard(state.timelineData)
                        }
                    }

                    // 6. Reset Button
                    item {
                        ResetStatsCard(onClick = { viewModel.clearStatistics(wheelId) })
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }

            is StatisticsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = RouletteTheme.colors.danger)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadStatistics(wheelId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = RouletteTheme.colors.textSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FairnessScoreCard(score: Int, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RouletteTheme.colors.surface,
        shape = RouletteTheme.shapes.card,
        border = BorderStroke(1.dp, RouletteTheme.colors.divider)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(AppIcons.Balance, null, tint = RouletteTheme.colors.warning, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Fairness score", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                }
                Text("$score%", fontWeight = FontWeight.ExtraBold, color = RouletteTheme.colors.warning, style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(Modifier.height(16.dp))
            
            val progress by animateFloatAsState(targetValue = score / 100f, animationSpec = tween(1000))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                color = RouletteTheme.colors.warning,
                trackColor = RouletteTheme.colors.divider
            )
            
            Spacer(Modifier.height(16.dp))
            Text(message, color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun SelectionDistributionCard(
    statistics: WheelStatistics,
    chartType: ChartType,
    onToggle: () -> Unit,
    wheel: Wheel
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RouletteTheme.colors.surface,
        shape = RouletteTheme.shapes.card,
        border = BorderStroke(1.dp, RouletteTheme.colors.divider)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selection distribution", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                
                Surface(
                    color = RouletteTheme.colors.surfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(36.dp).width(100.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (chartType == ChartType.BAR) RouletteTheme.colors.primary else Color.Transparent)
                                .clickable { if (chartType != ChartType.BAR) onToggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Bar", color = if (chartType == ChartType.BAR) RouletteTheme.colors.textPrimary else RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (chartType == ChartType.RING) RouletteTheme.colors.primary else Color.Transparent)
                                .clickable { if (chartType != ChartType.RING) onToggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ring", color = if (chartType == ChartType.RING) RouletteTheme.colors.textPrimary else RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (chartType == ChartType.BAR) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val entries = statistics.selectionCounts.entries.sortedByDescending { it.value }
                    entries.forEachIndexed { index, entry ->
                        val segment = wheel.segments.find { it.name == entry.key }
                        val percentage = statistics.selectionPercentages[entry.key]?.toInt() ?: 0
                        DistributionBarItem(
                            name = entry.key,
                            count = entry.value,
                            percentage = percentage,
                            color = segment?.color ?: RouletteTheme.colors.textSecondary,
                            rank = index + 1
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    DonutChart(statistics, wheel)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(statistics.totalSpins.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary)
                        Text("spins", style = MaterialTheme.typography.labelSmall, color = RouletteTheme.colors.textSecondary)
                    }
                }
                
                // Legend
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 3
                ) {
                    wheel.segments.forEach { segment ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(segment.color, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(segment.name, color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DistributionBarItem(name: String, count: Int, percentage: Int, color: Color, rank: Int) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
            val medal = when(rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> ""
            }
            Text("$medal $name", color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(count.toString(), color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(12.dp))
            Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                Text("$percentage%", color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        val progress by animateFloatAsState(targetValue = percentage / 100f, animationSpec = tween(800))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = RouletteTheme.colors.divider,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun DonutChart(statistics: WheelStatistics, wheel: Wheel) {
    val fallbackSegmentColor = RouletteTheme.colors.textSecondary
    Canvas(modifier = Modifier.size(160.dp)) {
        var startAngle = -90f
        val strokeWidth = 24.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        
        statistics.selectionCounts.forEach { (name, count) ->
            val segment = wheel.segments.find { it.name == name }
            val sweepAngle = if (statistics.totalSpins > 0) (count.toFloat() / statistics.totalSpins) * 360f else 0f
            
            drawArc(
                color = segment?.color ?: fallbackSegmentColor,
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
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(AppIcons.Whatshot, null, tint = RouletteTheme.colors.warning, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text("Lucky streaks", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            streaks.forEach { streak ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = streak.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, streak.color.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(streak.name, color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Picked ${streak.count}× in a row", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Surface(color = RouletteTheme.colors.surfacePressed, shape = CircleShape) {
                            Text("${streak.count}x", color = RouletteTheme.colors.textSecondary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpinTimelineCard(timeline: List<TimelineItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RouletteTheme.colors.surface,
        shape = RouletteTheme.shapes.card,
        border = BorderStroke(1.dp, RouletteTheme.colors.divider)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.TrendingUp, null, tint = RouletteTheme.colors.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Spin timeline", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                timeline.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(0.4f + (item.index % 5) * 0.1f) // Randomish height for visual effect
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(item.color)
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spin 1", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.labelSmall)
                Text("Spin ${timeline.size}", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ResetStatsCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = RouletteTheme.colors.danger.copy(alpha = 0.1f),
        shape = RouletteTheme.shapes.card,
        border = BorderStroke(1.dp, RouletteTheme.colors.danger.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(RouletteTheme.colors.danger.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(AppIcons.Autorenew, null, tint = RouletteTheme.colors.danger, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Reset statistics", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.danger, style = MaterialTheme.typography.bodyMedium)
                Text("Clears all spin history for this wheel", color = RouletteTheme.colors.danger.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
