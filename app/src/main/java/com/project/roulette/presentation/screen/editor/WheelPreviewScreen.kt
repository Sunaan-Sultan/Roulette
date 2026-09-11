package com.project.roulette.presentation.screen.editor

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.HistoryItem
import com.project.roulette.presentation.component.WheelCanvas
import com.project.roulette.presentation.component.WinnerDialog
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppTopBar
import com.project.roulette.presentation.component.design.EmptyState
import com.project.roulette.presentation.component.design.Pill
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.SectionHeader
import com.project.roulette.presentation.component.design.StatCard
import com.project.roulette.presentation.model.WheelUiState
import com.project.roulette.presentation.viewmodel.PreviewViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.ThemePalette
import com.project.roulette.ui.theme.rememberAccentOnSurface
import java.util.Locale

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

    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

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
            val target = current + 7 * 360f + (360f - finalAngle)

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

    AppScaffold(
        topBar = {
            AppTopBar(
                title = wheel.name,
                eyebrow = "Preview",
                onNavigateBack = onNavigateBack,
                actions = {
                    PrimaryButton(
                        text = "Save",
                        icon = AppIcons.Save,
                        onClick = onSave,
                        containerColor = themeColor,
                        height = dimens.buttonHeightSmall,
                        modifier = Modifier.padding(end = dimens.space8)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = dimens.screenPadding,
                    vertical = dimens.space12
                ),
                horizontalArrangement = Arrangement.spacedBy(dimens.space8)
            ) {
                items(wheel.segments, key = { it.id }) { segment ->
                    SegmentPill(
                        name = segment.name,
                        weight = segment.weight,
                        color = segment.color
                    )
                }
            }

            Spacer(Modifier.size(dimens.space8))

            val infiniteTransition = rememberInfiniteTransition(label = "tap_hint")
            val hintAlpha by infiniteTransition.animateFloat(
                initialValue = 0.35f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Text(
                text = "Tap the wheel to spin",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary.copy(alpha = hintAlpha)
            )

            Spacer(Modifier.size(dimens.space16))

            val isSpinning = (uiState as? WheelUiState.Success)?.isSpinning == true
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RouletteTheme.shapes.avatar)
                    .clickable(enabled = !isSpinning) { viewModel.spinWheel() }
            ) {
                Surface(
                    modifier = Modifier.size(296.dp),
                    shape = RouletteTheme.shapes.avatar,
                    color = themeColor.copy(alpha = 0.05f),
                    border = BorderStroke(dimens.borderWidth, themeColor.copy(alpha = 0.22f))
                ) {}

                WheelCanvas(
                    wheel = wheel,
                    rotation = rotationAnim.value,
                    themeColor = themeColor,
                    modifier = Modifier.size(280.dp)
                )

                Icon(
                    painter = AppIcons.ArrowDropDown,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-12).dp)
                )
            }

            Spacer(Modifier.size(dimens.space32))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.space12)
            ) {
                StatCard(
                    value = wheel.segments.size.toString(),
                    label = "Segments",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = testSpins.toString(),
                    label = "Test spins",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = lastPick ?: "—",
                    label = "Last pick",
                    accent = lighterThemeColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.size(dimens.space24))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.screenPadding)
            ) {
                SectionHeader("Spin history")

                if (history.isEmpty()) {
                    EmptyState(
                        icon = AppIcons.History,
                        title = "No test spins yet",
                        message = "Tap the wheel above to try it out."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(dimens.space8)) {
                        history.forEachIndexed { index, result ->
                            val segment = wheel.segments.find { it.id == result.selectedSegmentId }
                            HistoryItem(
                                name = result.selectedSegmentName,
                                color = segment?.color ?: colors.textSecondary,
                                isLatest = index == 0
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(dimens.listBottomPadding))
        }

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
                onRemoveFromWheel = {}
            )
        }
    }
}

@Composable
private fun SegmentPill(
    name: String,
    weight: Float,
    color: androidx.compose.ui.graphics.Color
) {
    val accent = rememberAccentOnSurface(color)
    Pill(
        text = "$name · ${String.format(Locale.US, "%.1f", weight)}x",
        accent = accent,
        showDot = true
    )
}
