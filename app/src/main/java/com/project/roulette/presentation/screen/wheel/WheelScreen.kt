package com.project.roulette.presentation.screen.wheel

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.HistoryItem
import com.project.roulette.presentation.component.WheelCanvas
import com.project.roulette.presentation.component.WheelOptionsSheet
import com.project.roulette.presentation.component.WinnerDialog
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppTopBar
import com.project.roulette.presentation.component.design.EmptyState
import com.project.roulette.presentation.component.design.GroupedCard
import com.project.roulette.presentation.component.design.Pill
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.SectionHeader
import com.project.roulette.presentation.component.design.appTextFieldColors
import com.project.roulette.presentation.model.WheelUiState
import com.project.roulette.presentation.viewmodel.WheelViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.ThemePalette
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.util.loadInterstitial
import com.project.roulette.util.showInterstitial
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen(
    wheelId: String,
    viewModel: WheelViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: (String) -> Unit,
    onNavigateToStatistics: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAlgorithm by viewModel.selectedAlgorithm.collectAsStateWithLifecycle()
    val spinSpeed by viewModel.spinSpeed.collectAsStateWithLifecycle()
    val spinsToday by viewModel.spinsToday.collectAsStateWithLifecycle()
    val seed by viewModel.seed.collectAsStateWithLifecycle()
    val pendingOutcome by viewModel.pendingSpinOutcome.collectAsStateWithLifecycle()

    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    val themeColor = remember(uiState) {
        val state = uiState
        if (state is WheelUiState.Success) {
            ThemePalette.getOrNull(state.wheel.themePaletteIndex) ?: ThemePalette[0]
        } else {
            ThemePalette[0]
        }
    }
    val lighterThemeColor = rememberAccentOnSurface(themeColor)

    var showOptions by remember { mutableStateOf(false) }
    var showAlgoInfo by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(wheelId) {
        viewModel.loadWheel(wheelId)
    }

    LaunchedEffect(Unit) {
        loadInterstitial(context)
    }

    BackHandler {
        showInterstitial(context = context) { onNavigateBack() }
    }

    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(pendingOutcome) {
        val outcome = pendingOutcome
        if (outcome != null) {
            val state = uiState
            if (state is WheelUiState.Success) {
                val current = rotationAnim.value
                val finalAngle = outcome.spinResult.finalAngle
                val sweep = 360f / state.wheel.getActiveSegments().size
                val desiredMod = ((sweep / 2f) - finalAngle + 360f) % 360f
                val currentMod = ((current % 360f) + 360f) % 360f
                val deltaMod = ((desiredMod - currentMod) % 360f + 360f) % 360f
                val target = current + spinSpeed.rotations * 360f + deltaMod

                scope.launch {
                    rotationAnim.animateTo(
                        targetValue = target,
                        animationSpec = tween(
                            durationMillis = spinSpeed.durationMs.toInt(),
                            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)
                        )
                    )
                    viewModel.onAnimationComplete()
                }
            }
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

    LaunchedEffect(spinsToday) {
        if (selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN) {
            val state = uiState
            if (state is WheelUiState.Success &&
                state.rrRemaining == state.wheel.getActiveSegments().size
            ) {
                snackbarHostState.showSnackbar("New round started")
            }
        }
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = (uiState as? WheelUiState.Success)?.wheel?.name ?: "Wheel",
                eyebrow = "Spin",
                onNavigateBack = { showInterstitial(context = context) { onNavigateBack() } },
                actions = {
                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            painter = AppIcons.MoreVert,
                            contentDescription = "Wheel options",
                            tint = colors.textPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        val state = uiState

        if (state is WheelUiState.Success && showOptions) {
            ModalBottomSheet(
                onDismissRequest = { showOptions = false },
                sheetState = optionsSheetState,
                containerColor = colors.background,
                dragHandle = null,
                modifier = Modifier.fillMaxSize()
            ) {
                WheelOptionsSheet(
                    wheel = state.wheel,
                    onClose = { showOptions = false },
                    onEdit = { onNavigateToEdit(wheelId); showOptions = false },
                    onDuplicate = {
                        viewModel.duplicateWheel(onDuplicated = { newId -> onNavigateToEdit(newId) })
                        showOptions = false
                    },
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onHistory = { onNavigateToHistory(wheelId); showOptions = false },
                    onStatistics = { onNavigateToStatistics(wheelId); showOptions = false },
                    onExport = { showOptions = false },
                    onReset = { viewModel.resetWheel(); showOptions = false },
                    onDelete = { viewModel.deleteWheel { onNavigateBack() }; showOptions = false }
                )
            }
        }

        when (state) {
            is WheelUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            is WheelUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimens.screenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.space8)
                    ) {
                        Pill(
                            text = "${state.wheel.getActiveSegments().size} names",
                            accent = lighterThemeColor,
                            showDot = true
                        )
                        Pill(
                            text = "$spinsToday spun today",
                            accent = colors.textSecondary,
                            tinted = false
                        )
                        state.rrRemaining?.let {
                            Pill(text = "$it left this round", accent = lighterThemeColor)
                        }
                    }

                    Spacer(Modifier.size(dimens.space20))

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

                    WheelCanvas(
                        wheel = state.wheel,
                        rotation = rotationAnim.value,
                        themeColor = themeColor,
                        modifier = Modifier
                            .size(280.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!state.isSpinning) viewModel.spinWheel()
                            }
                    )

                    Spacer(Modifier.size(dimens.space32))

                    GroupedCard {
                        Column(modifier = Modifier.padding(dimens.space16)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = AppIcons.Speed,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(dimens.iconSizeSmall)
                                )
                                Spacer(Modifier.width(dimens.space12))
                                Text(
                                    text = "Spin speed",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = spinSpeed.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = lighterThemeColor
                                )
                            }

                            Slider(
                                value = WheelViewModel.SpinSpeed.entries.indexOf(spinSpeed).toFloat(),
                                onValueChange = {
                                    viewModel.setSpinSpeed(WheelViewModel.SpinSpeed.entries[it.toInt()])
                                },
                                valueRange = 0f..(WheelViewModel.SpinSpeed.entries.size - 1).toFloat(),
                                steps = WheelViewModel.SpinSpeed.entries.size - 2,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColor,
                                    activeTrackColor = themeColor,
                                    inactiveTrackColor = colors.divider,
                                    activeTickColor = Color.Transparent,
                                    inactiveTickColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Slow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary
                                )
                                Text(
                                    text = "Blazing",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.size(dimens.space24))

                    SectionHeader(
                        text = "Algorithm",
                        trailing = {
                            TextButton(onClick = { showAlgoInfo = true }) {
                                Text(
                                    text = "What's this?",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = lighterThemeColor
                                )
                            }
                        }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(dimens.space8)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(dimens.space8)) {
                            AlgorithmCard(
                                title = "Uniform",
                                description = "Equal odds for all",
                                icon = AppIcons.Casino,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.UNIFORM,
                                onClick = {
                                    viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.UNIFORM)
                                },
                                modifier = Modifier.weight(1f),
                                accent = lighterThemeColor
                            )
                            AlgorithmCard(
                                title = "Weighted",
                                description = "Custom odds per name",
                                icon = AppIcons.Balance,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.WEIGHTED,
                                onClick = {
                                    viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.WEIGHTED)
                                },
                                modifier = Modifier.weight(1f),
                                accent = lighterThemeColor
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(dimens.space8)) {
                            AlgorithmCard(
                                title = "Seeded",
                                description = "Reproducible result",
                                icon = AppIcons.Tag,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.SEEDED,
                                onClick = {
                                    viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.SEEDED)
                                },
                                modifier = Modifier.weight(1f),
                                accent = lighterThemeColor
                            )
                            AlgorithmCard(
                                title = "Round robin",
                                description = "Everyone gets a turn",
                                icon = AppIcons.Autorenew,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN,
                                onClick = {
                                    viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN)
                                },
                                modifier = Modifier.weight(1f),
                                accent = lighterThemeColor
                            )
                        }
                    }

                    when (selectedAlgorithm) {
                        SelectionAlgorithmFactory.AlgorithmType.SEEDED -> {
                            Spacer(Modifier.size(dimens.space16))
                            OutlinedTextField(
                                value = seed?.toString() ?: "",
                                onValueChange = { viewModel.setSeed(it.toLongOrNull()) },
                                label = { Text("Seed number") },
                                supportingText = {
                                    Text("The same seed always gives the same sequence of results.")
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        viewModel.setSeed((100..999999).random().toLong())
                                    }) {
                                        Icon(
                                            painter = AppIcons.Casino,
                                            contentDescription = "Random seed",
                                            tint = lighterThemeColor
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RouletteTheme.shapes.textField,
                                colors = appTextFieldColors(lighterThemeColor),
                                singleLine = true
                            )
                        }

                        SelectionAlgorithmFactory.AlgorithmType.WEIGHTED -> {
                            Spacer(Modifier.size(dimens.space16))
                            GroupedCard {
                                Column(modifier = Modifier.padding(dimens.space16)) {
                                    Text(
                                        text = "Custom weights",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )
                                    Spacer(Modifier.size(dimens.space8))
                                    state.wheel.getActiveSegments().forEach { segment ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = segment.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.textSecondary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.1f", segment.weight),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = colors.textPrimary
                                            )
                                            Slider(
                                                value = segment.weight,
                                                onValueChange = {
                                                    viewModel.updateSegmentWeight(segment.id, it)
                                                },
                                                valueRange = 1f..5f,
                                                steps = 3,
                                                modifier = Modifier.width(120.dp),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = themeColor,
                                                    activeTrackColor = themeColor,
                                                    inactiveTrackColor = colors.divider,
                                                    activeTickColor = Color.Transparent,
                                                    inactiveTickColor = Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }

                    Spacer(Modifier.size(dimens.space24))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionHeader("Spin history")

                        if (state.recentSpins.isEmpty()) {
                            EmptyState(
                                icon = AppIcons.History,
                                title = "No spins yet",
                                message = "Tap the wheel above to get started."
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(dimens.space8)) {
                                state.recentSpins.forEachIndexed { index, result ->
                                    val segment = state.wheel.segments
                                        .find { it.id == result.selectedSegmentId }
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

                state.lastSpinResult?.let { result ->
                    WinnerDialog(
                        result = result,
                        wheel = state.wheel,
                        spinsToday = spinsToday,
                        themeColor = themeColor,
                        lighterThemeColor = lighterThemeColor,
                        onDismiss = { viewModel.clearResult() },
                        onSpinAgain = {
                            viewModel.clearResult()
                            viewModel.spinWheel()
                        },
                        onRemoveFromWheel = {
                            viewModel.deactivateSegment(result.selectedSegmentId)
                        }
                    )
                }

                if (showAlgoInfo) {
                    ModalBottomSheet(
                        onDismissRequest = { showAlgoInfo = false },
                        sheetState = sheetState,
                        containerColor = colors.surface,
                        shape = RouletteTheme.shapes.sheet,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textTertiary) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.85f)
                                .padding(horizontal = dimens.space24)
                                .padding(bottom = dimens.space32)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(dimens.space20)
                        ) {
                            Text(
                                text = "About algorithms",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textPrimary
                            )

                            AlgoInfoDetail(
                                icon = AppIcons.Casino,
                                title = "Uniform random",
                                description = "The classic fair choice. Every name on the wheel has a mathematically identical chance of being picked.",
                                accent = lighterThemeColor
                            )
                            AlgoInfoDetail(
                                icon = AppIcons.Balance,
                                title = "Weighted random",
                                description = "Bias the results. A name with a weight of 5 is five times more likely to win than one with a weight of 1.",
                                accent = lighterThemeColor
                            )
                            AlgoInfoDetail(
                                icon = AppIcons.Tag,
                                title = "Seeded sequence",
                                description = "Deterministic. The same seed always produces the same sequence of winners, so anyone can verify a result.",
                                accent = lighterThemeColor
                            )
                            AlgoInfoDetail(
                                icon = AppIcons.Autorenew,
                                title = "Round robin",
                                description = "Everyone gets a turn. Names are shuffled into a hidden queue and nobody wins twice until everybody has won once.",
                                accent = lighterThemeColor
                            )
                        }
                    }
                }
            }

            is WheelUiState.Error -> {
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
                            onClick = { viewModel.loadWheel(wheelId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlgoInfoDetail(
    icon: Painter,
    title: String,
    description: String,
    accent: Color
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RouletteTheme.shapes.iconTile,
            color = accent.copy(alpha = if (colors.isLight) 0.10f else 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(dimens.iconSizeSmall)
                )
            }
        }
        Spacer(Modifier.width(dimens.rowIconGap))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Spacer(Modifier.size(dimens.space2))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun AlgorithmCard(
    title: String,
    description: String,
    icon: Painter,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RouletteTheme.shapes.cardSmall,
        color = if (selected) {
            accent.copy(alpha = if (colors.isLight) 0.08f else 0.14f)
        } else {
            colors.surface
        },
        border = BorderStroke(
            dimens.borderWidth,
            if (selected) accent.copy(alpha = 0.5f) else colors.divider
        )
    ) {
        Column(modifier = Modifier.padding(dimens.space12)) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (selected) accent else colors.textSecondary,
                modifier = Modifier.size(dimens.iconSize)
            )
            Spacer(Modifier.size(dimens.space8))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }
}
