package com.project.roulette.presentation.screen.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.presentation.model.DistributionItem
import com.project.roulette.presentation.model.HistoryUiState
import com.project.roulette.presentation.viewmodel.HistoryEffect
import com.project.roulette.presentation.viewmodel.HistoryViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.util.PdfExporter
import com.project.roulette.ui.theme.DeepNavyBlack
import com.project.roulette.ui.theme.SurfaceDark
import com.project.roulette.ui.theme.SurfaceDarker
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    wheelId: String,
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                    val uri = PdfExporter.generateAndSharePdf(context, effect.wheelName, effect.results, effect.stats)
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

    Scaffold(
        topBar = {
            val titleColor = Color.White
            val subtitleColor = Color.Gray
            
            TopAppBar(
                title = {
                    Column {
                        val subtitle = (uiState as? HistoryUiState.Success)?.wheel?.name ?: "Wheel"
                        Text(subtitle.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor, letterSpacing = 1.sp)
                        Text("Spin History", fontWeight = FontWeight.Bold, color = titleColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportHistory() }) {
                        Icon(Icons.Filled.FileDownload, "Export", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.clearHistory(wheelId) },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                Icon(Icons.Filled.DeleteOutline, "Clear", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavyBlack)
            )
        },
        containerColor = DeepNavyBlack
    ) { padding ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RouletteTheme.colors.primary)
                }
            }

            is HistoryUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Stats Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                value = state.totalSpins.toString(),
                                label = "Total spins",
                                color = RouletteTheme.colors.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = String.format(Locale.US, "%.0fs", state.avgDuration),
                                label = "Avg duration",
                                color = Color(0xFF00B894),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = state.mostPickedName ?: "—",
                                label = "Most picked",
                                color = state.wheel.segments.find { it.name == state.mostPickedName }?.color ?: Color(0xFFE17055),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Win Distribution
                    item {
                        WinDistributionCard(state.winDistribution, state.totalSpins)
                    }

                    // Filter Chips
                    item {
                        FilterChipsRow(
                            names = state.wheel.segments.map { it.name },
                            selectedName = state.selectedFilter,
                            onSelect = { viewModel.setFilter(it) }
                        )
                    }

                    // Recent Spins Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "RECENT SPINS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.2.sp
                            )
                            TextButton(onClick = { viewModel.toggleSort() }) {
                                Text(
                                    if (state.isDescending) "Sort ↓" else "Sort ↑",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RouletteTheme.colors.primary
                                )
                            }
                        }
                    }

                    if (state.filteredResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No matching spins found", color = Color.Gray)
                            }
                        }
                    } else {
                        items(state.filteredResults) { spinResult ->
                            val segment = state.wheel.segments.find { it.name == spinResult.selectedSegmentName }
                            val spinIndex = state.spinResults.size - state.spinResults.indexOf(spinResult)
                            val isLatest = spinResult == state.spinResults.firstOrNull() && state.isDescending
                            
                            HistoryCard(
                                spinResult = spinResult,
                                color = segment?.color ?: Color.Gray,
                                spinNumber = spinIndex,
                                isLatest = isLatest
                            )
                        }
                    }
                    
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }

            is HistoryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = Color.Red)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadHistory(wheelId) }) {
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
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
            Text(label, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WinDistributionCard(distribution: List<DistributionItem>, totalSpins: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Win distribution", fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    color = RouletteTheme.colors.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        "$totalSpins spins",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = RouletteTheme.colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                distribution.take(4).forEach { item ->
                    DistributionRow(item)
                }
            }
        }
    }
}

@Composable
private fun DistributionRow(item: DistributionItem) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("${item.count} picks · ${item.percentage}%", color = item.color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { item.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = item.color,
            trackColor = Color.White.copy(alpha = 0.05f)
        )
    }
}

@Composable
private fun FilterChipsRow(names: List<String>, selectedName: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            FilterChip(
                selected = selectedName == null,
                onClick = { onSelect(null) },
                label = { Text("All") },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RouletteTheme.colors.primary,
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceDark,
                    labelColor = Color.Gray
                ),
                border = null
            )
        }
        items(names) { name ->
            FilterChip(
                selected = selectedName == name,
                onClick = { onSelect(name) },
                label = { Text(name) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RouletteTheme.colors.primary.copy(alpha = 0.2f),
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceDark,
                    labelColor = Color.Gray
                ),
                border = BorderStroke(1.dp, if (selectedName == name) RouletteTheme.colors.primary else Color.Transparent)
            )
        }
    }
}

@Composable
private fun HistoryCard(spinResult: SpinResult, color: Color, spinNumber: Int, isLatest: Boolean) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[1] *= 0.5f // Less saturation for background
    hsv[2] *= 0.3f // Darker background
    val bgColor = Color(android.graphics.Color.HSVToColor(hsv))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Box {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Target Icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.RadioButtonChecked, null, tint = color, modifier = Modifier.size(24.dp))
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            spinResult.selectedSegmentName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                "Spin #$spinNumber",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoItem(Icons.Filled.Timer, "${spinResult.spinDuration / 1000f}s spin", color)
                        InfoItem(Icons.Filled.Explore, "${String.format(Locale.US, "%.1f", spinResult.finalAngle)}°", color)
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    val dt = spinResult.spinTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateStr = String.format(Locale.US, "%04d-%02d-%02d · %02d:%02d:%02d", 
                        dt.year, dt.monthNumber, dt.dayOfMonth, 
                        dt.hour, dt.minute, dt.second)
                    
                    InfoItem(Icons.Filled.CalendarMonth, dateStr, color)
                }
            }
            
            if (isLatest) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    color = RouletteTheme.colors.primary,
                    shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 24.dp)
                ) {
                    Text(
                        "LATEST",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

