package com.project.roulette.presentation.screen.history

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.design.AppFilterChip
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppTopBar
import com.project.roulette.presentation.component.design.EmptyState
import com.project.roulette.presentation.component.design.GroupedCard
import com.project.roulette.presentation.component.design.MetricBar
import com.project.roulette.presentation.component.design.Pill
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.SectionHeader
import com.project.roulette.presentation.component.design.StatCard
import com.project.roulette.presentation.component.design.StatCardStyle
import com.project.roulette.presentation.model.DistributionItem
import com.project.roulette.presentation.model.HistoryUiState
import com.project.roulette.presentation.viewmodel.HistoryEffect
import com.project.roulette.presentation.viewmodel.HistoryViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.util.PdfExporter
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

@Composable
fun HistoryScreen(
    wheelId: String,
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    LaunchedEffect(wheelId) {
        viewModel.loadHistory(wheelId)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistoryEffect.ShareCsv -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_SUBJECT, "${effect.wheelName} Spin History")
                        putExtra(Intent.EXTRA_TEXT, effect.csvData)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Spin History"))
                }
                is HistoryEffect.SharePdf -> {
                    val uri = PdfExporter.generateAndSharePdf(
                        context,
                        effect.wheelName,
                        effect.results,
                        effect.stats
                    )
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_SUBJECT, "${effect.wheelName} Spin History")
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share PDF History"))
                    }
                }
            }
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Spin history",
                eyebrow = (uiState as? HistoryUiState.Success)?.wheel?.name ?: "Wheel",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.exportHistory() }) {
                        Icon(
                            painter = AppIcons.Download,
                            contentDescription = "Export",
                            tint = colors.textPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.clearHistory(wheelId) }) {
                        Icon(
                            painter = AppIcons.Delete,
                            contentDescription = "Clear history",
                            tint = colors.danger
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            is HistoryUiState.Success -> {
                val mostPickedColor = state.wheel.segments
                    .find { it.name == state.mostPickedName }?.color ?: colors.warning
                val mostPickedAccent = rememberAccentOnSurface(mostPickedColor)

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
                                value = state.totalSpins.toString(),
                                label = "Total spins",
                                accent = colors.primary,
                                style = StatCardStyle.Tinted,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = String.format(Locale.US, "%.0fs", state.avgDuration),
                                label = "Avg duration",
                                accent = colors.success,
                                style = StatCardStyle.Tinted,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = state.mostPickedName ?: "—",
                                label = "Most picked",
                                accent = mostPickedAccent,
                                style = StatCardStyle.Tinted,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (state.winDistribution.isNotEmpty()) {
                        item {
                            WinDistributionCard(state.winDistribution, state.totalSpins)
                        }
                    }

                    item {
                        SegmentFilterRow(
                            names = state.wheel.segments.map { it.name },
                            selectedName = state.selectedFilter,
                            onSelect = { viewModel.setFilter(it) }
                        )
                    }

                    item {
                        SectionHeader(
                            text = "Recent spins",
                            trailing = {
                                TextButton(onClick = { viewModel.toggleSort() }) {
                                    Text(
                                        text = if (state.isDescending) "Newest first" else "Oldest first",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.primary
                                    )
                                }
                            }
                        )
                    }

                    if (state.filteredResults.isEmpty()) {
                        item {
                            EmptyState(
                                icon = AppIcons.History,
                                title = "No spins to show",
                                message = "Nothing matches this filter yet."
                            )
                        }
                    } else {
                        items(state.filteredResults, key = { it.id }) { spinResult ->
                            val segment = state.wheel.segments
                                .find { it.name == spinResult.selectedSegmentName }
                            val spinNumber = state.spinResults.size -
                                state.spinResults.indexOf(spinResult)
                            HistoryCard(
                                spinResult = spinResult,
                                color = segment?.color ?: colors.textSecondary,
                                spinNumber = spinNumber,
                                isLatest = state.isDescending &&
                                    spinResult == state.spinResults.firstOrNull()
                            )
                        }
                    }
                }
            }

            is HistoryUiState.Error -> {
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
                            onClick = { viewModel.loadHistory(wheelId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WinDistributionCard(distribution: List<DistributionItem>, totalSpins: Int) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    GroupedCard {
        Column(modifier = Modifier.padding(dimens.space16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Win distribution",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Pill(text = "$totalSpins spins", accent = colors.primary)
            }

            Spacer(Modifier.size(dimens.space16))

            Column(verticalArrangement = Arrangement.spacedBy(dimens.space12)) {
                distribution.take(4).forEach { item ->
                    val accent = rememberAccentOnSurface(item.color)
                    MetricBar(
                        name = item.name,
                        value = "${item.count} · ${item.percentage}%",
                        progress = item.percentage / 100f,
                        accent = accent
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentFilterRow(
    names: List<String>,
    selectedName: String?,
    onSelect: (String?) -> Unit
) {
    val dimens = RouletteTheme.dimens
    LazyRow(horizontalArrangement = Arrangement.spacedBy(dimens.space8)) {
        item {
            AppFilterChip(
                label = "All",
                selected = selectedName == null,
                onClick = { onSelect(null) }
            )
        }
        items(names) { name ->
            AppFilterChip(
                label = name,
                selected = selectedName == name,
                onClick = { onSelect(name) }
            )
        }
    }
}

@Composable
private fun HistoryCard(
    spinResult: SpinResult,
    color: Color,
    spinNumber: Int,
    isLatest: Boolean
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val accent = rememberAccentOnSurface(color)

    GroupedCard(
        border = androidx.compose.foundation.BorderStroke(
            dimens.borderWidth,
            if (isLatest) colors.primaryBorder else colors.divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.space16),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RouletteTheme.shapes.thumbnail)
                    .background(accent.copy(alpha = if (colors.isLight) 0.12f else 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = AppIcons.RadioButtonChecked,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(dimens.iconSize)
                )
            }

            Spacer(Modifier.width(dimens.rowIconGap))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = spinResult.selectedSegmentName,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(dimens.space8))
                    Pill(
                        text = if (isLatest) "Latest" else "#$spinNumber",
                        accent = if (isLatest) colors.primary else colors.textSecondary,
                        tinted = isLatest
                    )
                }

                Spacer(Modifier.size(dimens.space8))

                Row(horizontalArrangement = Arrangement.spacedBy(dimens.space16)) {
                    InfoItem(
                        icon = AppIcons.Timer,
                        text = "${spinResult.spinDuration / 1000f}s"
                    )
                    InfoItem(
                        icon = AppIcons.Explore,
                        text = "${String.format(Locale.US, "%.1f", spinResult.finalAngle)}°"
                    )
                }

                Spacer(Modifier.size(dimens.space4))

                val dt = spinResult.spinTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                InfoItem(
                    icon = AppIcons.CalendarMonth,
                    text = String.format(
                        Locale.US,
                        "%04d-%02d-%02d · %02d:%02d",
                        dt.year, dt.monthNumber, dt.dayOfMonth, dt.hour, dt.minute
                    )
                )
            }
        }
    }
}

@Composable
private fun InfoItem(icon: Painter, text: String) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(dimens.space4))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
    }
}
