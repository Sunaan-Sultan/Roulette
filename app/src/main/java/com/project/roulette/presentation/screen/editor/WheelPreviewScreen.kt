package com.project.roulette.presentation.screen.editor

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.component.WheelCanvas
import com.project.roulette.presentation.component.WinnerDialog
import com.project.roulette.presentation.component.HistoryItem
import com.project.roulette.presentation.model.WheelUiState
import com.project.roulette.presentation.viewmodel.PreviewViewModel
import com.project.roulette.ui.theme.ThemePalette
import java.util.Locale
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.ui.theme.contentColorOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelPreviewScreen(
    wheel: Wheel,
    viewModel: PreviewViewModel,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val testSpins by viewModel.testSpinsCount.collectAsStateWithLifecycle()
    val lastPick by viewModel.lastPick.collectAsStateWithLifecycle()
    val history by viewModel.spinHistory.collectAsStateWithLifecycle()
    val pendingOutcome by viewModel.pendingSpinOutcome.collectAsStateWithLifecycle()

    val themeColor = remember(wheel) {
        ThemePalette.getOrNull(wheel.themePaletteIndex) ?: ThemePalette[0]
    }

    val lighterThemeColor = rememberAccentOnSurface(themeColor)

    LaunchedEffect(wheel) {
        viewModel.initialize(wheel)
    }

    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(pendingOutcome) {
        val outcome = pendingOutcome
        if (outcome != null) {
            val current = rotationAnim.value
            val finalAngle = outcome.spinResult.finalAngle
            
            // For preview, we use Weighted algorithm but if weights are equal, 
            // the segments on canvas are equal size. 
            // In WheelCanvas, it seems it divides 360 by segment count equally?
            // Let's check WheelCanvas.
            
            val rotations = 7
            val target = current + rotations * 360f + (360f - finalAngle)
            
            rotationAnim.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)
                )
            )
            viewModel.onAnimationComplete()
        }
    }

    // Tick sound logic
    var lastBoundaryIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(rotationAnim.value) {
        val state = uiState
        if (state is WheelUiState.Success && state.isSpinning && state.wheel.spinSound) {
            val segmentCount = state.wheel.getActiveSegments().size
            if (segmentCount > 0) {
                val sweep = 360f / segmentCount
                val currentBoundaryIndex = ((rotationAnim.value + (sweep / 2f)) / sweep).toInt()
                if (lastBoundaryIndex != -1 && currentBoundaryIndex != lastBoundaryIndex) {
                    viewModel.playTickSound()
                }
                lastBoundaryIndex = currentBoundaryIndex
            }
        } else {
            lastBoundaryIndex = -1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PREVIEW", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textSecondary)
                        Text(wheel.name, fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back", tint = RouletteTheme.colors.textPrimary)
                    }
                },
                actions = {
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            contentColor = contentColorOn(themeColor)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            AppIcons.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RouletteTheme.colors.background)
            )
        },
        containerColor = RouletteTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Segment pills row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wheel.segments) { segment ->
                    Surface(
                        color = segment.color.copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, segment.color.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(segment.color, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(segment.name, color = RouletteTheme.colors.textPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Text("${String.format(Locale.US, "%.1f", segment.weight)}x", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "tap_hint")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Text(
                "Tap Wheel to Spin",
                color = RouletteTheme.colors.textPrimary.copy(alpha = alpha),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(Modifier.height(16.dp))

            // Wheel
            val isSpinning = (uiState as? WheelUiState.Success)?.isSpinning == true
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isSpinning) { viewModel.spinWheel() }
            ) {
                // Glow effect
                Surface(
                    modifier = Modifier.size(290.dp),
                    shape = CircleShape,
                    color = themeColor.copy(alpha = 0.05f),
                    border = BorderStroke(2.dp, themeColor.copy(alpha = 0.2f))
                ) {}

                WheelCanvas(
                    wheel = wheel,
                    rotation = rotationAnim.value,
                    themeColor = themeColor,
                    modifier = Modifier.size(280.dp)
                )
                
                // Pointer arrow (Top)
                Icon(
                    painter = AppIcons.ArrowDropDown,
                    contentDescription = null,
                    tint = RouletteTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-15).dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Segments",
                    value = wheel.segments.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Test spins",
                    value = testSpins.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Last pick",
                    value = lastPick ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Spin History Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    "SPIN HISTORY",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = RouletteTheme.colors.textSecondary,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(12.dp))
                
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .border(1.dp, RouletteTheme.colors.divider, RoundedCornerShape(16.dp))
                            .background(RouletteTheme.colors.surface.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No spins yet — tap wheel to test",
                            color = RouletteTheme.colors.textSecondary.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.forEachIndexed { index, result ->
                            val segment = wheel.segments.find { it.id == result.selectedSegmentId }
                            HistoryItem(
                                name = result.selectedSegmentName,
                                color = segment?.color ?: RouletteTheme.colors.textSecondary,
                                isLatest = index == 0
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        // Result Dialog
        (uiState as? WheelUiState.Success)?.lastSpinResult?.let { result ->
            WinnerDialog(
                result = result,
                wheel = wheel,
                spinsToday = testSpins,
                themeColor = themeColor,
                lighterThemeColor = lighterThemeColor,
                onDismiss = { viewModel.clearResult() },
                onSpinAgain = {
                    viewModel.clearResult()
                    viewModel.spinWheel()
                },
                onRemoveFromWheel = {
                    // Removal not supported in preview for now as it's a test environment
                }
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = RouletteTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RouletteTheme.colors.divider)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RouletteTheme.colors.textPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = RouletteTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
