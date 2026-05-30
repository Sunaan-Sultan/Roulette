package com.project.roulette.presentation.screen.wheel

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.component.WheelCanvas
import com.project.roulette.presentation.component.HistoryItem
import com.project.roulette.presentation.component.WinnerDialog
import com.project.roulette.presentation.component.WheelOptionsSheet
import com.project.roulette.presentation.model.WheelUiState
import com.project.roulette.presentation.viewmodel.WheelViewModel
import kotlinx.coroutines.launch
import com.project.roulette.util.loadInterstitial
import com.project.roulette.util.showInterstitial
import com.project.roulette.ui.theme.ThemePalette
import com.project.roulette.ui.theme.DeepNavyBlack
import com.project.roulette.ui.theme.SurfaceDark
import androidx.activity.compose.BackHandler

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
    val recentSpins by viewModel.recentSpins.collectAsStateWithLifecycle()

    val themeColor = remember(uiState) {
        val state = uiState
        if (state is WheelUiState.Success) {
            ThemePalette.getOrNull(state.wheel.themePaletteIndex) ?: ThemePalette[0]
        } else {
            ThemePalette[0]
        }
    }
    
    val lighterThemeColor = remember(themeColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(themeColor.toArgb(), hsv)
        hsv[1] *= 0.6f // Less saturated
        hsv[2] = (hsv[2] + 1f) / 2f // Brighter
        Color(android.graphics.Color.HSVToColor(hsv))
    }

    var showOptions by remember { mutableStateOf(false) }
    var showAlgoInfo by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(wheelId) {
        viewModel.loadWheel(wheelId)
    }

    LaunchedEffect(Unit) {
        loadInterstitial(context)
    }

    BackHandler {
        showInterstitial(context = context) {
            onNavigateBack()
        }
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
                
                val rotations = spinSpeed.rotations
                val currentMod = ((current % 360f) + 360f) % 360f
                val deltaMod = ((desiredMod - currentMod) % 360f + 360f) % 360f
                val target = current + rotations * 360f + deltaMod
                val duration = spinSpeed.durationMs.toInt()

                scope.launch {
                    rotationAnim.animateTo(
                        targetValue = target,
                        animationSpec = tween(
                            durationMillis = duration,
                            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)
                        )
                    )
                    viewModel.onAnimationComplete()
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(spinsToday) {
        if (selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN) {
            val state = uiState
            if (state is WheelUiState.Success) {
                if (state.rrRemaining == state.wheel.getActiveSegments().size) {
                    snackbarHostState.showSnackbar("New Round Started!")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is WheelUiState.Success -> Text(state.wheel.name, fontWeight = FontWeight.Bold)
                        else -> Text("Wheel")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showInterstitial(context = context) { onNavigateBack() } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showOptions = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
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
                containerColor = DeepNavyBlack,
                dragHandle = null,
                modifier = Modifier.fillMaxSize()
            ) {
                WheelOptionsSheet(
                    wheel = state.wheel,
                    onClose = { showOptions = false },
                    onEdit = { onNavigateToEdit(wheelId); showOptions = false },
                    onDuplicate = { viewModel.duplicateWheel(onDuplicated = { newId -> onNavigateToEdit(newId) }); showOptions = false },
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onHistory = { onNavigateToHistory(wheelId); showOptions = false },
                    onStatistics = { onNavigateToStatistics(wheelId); showOptions = false },
                    onExport = { 
                        // Shared logic for export
                        showOptions = false 
                    },
                    onReset = { viewModel.resetWheel(); showOptions = false },
                    onDelete = { viewModel.deleteWheel { onNavigateBack() }; showOptions = false }
                )
            }
        }

        when (state) {
            is WheelUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WheelUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFF121212))
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pills Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = themeColor.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(lighterThemeColor, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text("${state.wheel.getActiveSegments().size} names", color = lighterThemeColor, fontSize = 12.sp)
                            }
                        }
                        Surface(
                            color = Color(0xFF212121),
                            shape = CircleShape,
                        ) {
                            Text(
                                "$spinsToday spun today",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        if (state.rrRemaining != null) {
                            Surface(
                                color = themeColor.copy(alpha = 0.2f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, themeColor)
                            ) {
                                Text(
                                    "RR: ${state.rrRemaining} left",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = lighterThemeColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Tap to Spin Hint
                    val infiniteTransition = rememberInfiniteTransition(label = "tap_hint")
                    val hintAlpha by infiniteTransition.animateFloat(
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
                        color = Color.White.copy(alpha = hintAlpha),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Wheel
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

                    Spacer(Modifier.height(32.dp))

                    // Spin Speed Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Spin speed", color = Color.Gray)
                        }
                        Text(spinSpeed.label, color = lighterThemeColor, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = WheelViewModel.SpinSpeed.entries.indexOf(spinSpeed).toFloat(),
                        onValueChange = { viewModel.setSpinSpeed(WheelViewModel.SpinSpeed.entries[it.toInt()]) },
                        valueRange = 0f..4f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColor,
                            activeTrackColor = themeColor,
                            inactiveTrackColor = Color(0xFF212121),
                            activeTickColor = themeColor.copy(alpha = 0.5f),
                            inactiveTickColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Slow", color = Color.DarkGray, fontSize = 10.sp)
                        Text("Blazing", color = Color.DarkGray, fontSize = 10.sp)
                    }

                    // Algorithm Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Memory, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Algorithm", color = Color.Gray)
                        }
                        TextButton(
                            onClick = { showAlgoInfo = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = lighterThemeColor)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("What's this?", fontSize = 12.sp)
                        }
                    }

                    // Algorithm Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AlgorithmCard(
                                title = "Uniform",
                                description = "Equal odds for all",
                                icon = Icons.Filled.Casino,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.UNIFORM,
                                onClick = { viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.UNIFORM) },
                                modifier = Modifier.weight(1f),
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                            AlgorithmCard(
                                title = "Weighted",
                                description = "Custom odds per name",
                                icon = Icons.Filled.Balance,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.WEIGHTED,
                                onClick = { viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.WEIGHTED) },
                                modifier = Modifier.weight(1f),
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AlgorithmCard(
                                title = "Seeded",
                                description = "Reproducible result",
                                icon = Icons.Filled.Tag,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.SEEDED,
                                onClick = { viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.SEEDED) },
                                modifier = Modifier.weight(1f),
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                            AlgorithmCard(
                                title = "Round Robin",
                                description = "Everyone gets a turn",
                                icon = Icons.Filled.Autorenew,
                                selected = selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN,
                                onClick = { viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN) },
                                modifier = Modifier.weight(1f),
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                        }
                    }

                    // Algorithm specific settings
                    if (selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.SEEDED) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = seed?.toString() ?: "",
                            onValueChange = { viewModel.setSeed(it.toLongOrNull()) },
                            label = { Text("Seed Number") },
                            supportingText = {
                                Text("Any number works! Using the same seed gives the same results every time.")
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.setSeed((100..999999).random().toLong()) }) {
                                    Icon(Icons.Filled.Casino, contentDescription = "Random Seed", tint = lighterThemeColor)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeColor,
                                unfocusedBorderColor = Color(0xFF212121),
                                focusedSupportingTextColor = Color.Gray,
                                unfocusedSupportingTextColor = Color.Gray
                            )
                        )
                    } else if (selectedAlgorithm == SelectionAlgorithmFactory.AlgorithmType.WEIGHTED) {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Custom Weights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            state.wheel.getActiveSegments().forEach { segment ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(segment.name, color = Color.Gray, modifier = Modifier.weight(1f))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(String.format(java.util.Locale.US, "%.1f", segment.weight), color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                                        Slider(
                                            value = segment.weight,
                                            onValueChange = { viewModel.updateSegmentWeight(segment.id, it) },
                                            valueRange = 1f..5f,
                                            steps = 3,
                                            modifier = Modifier.width(120.dp),
                                            colors = SliderDefaults.colors(
                                                thumbColor = themeColor,
                                                activeTrackColor = themeColor,
                                                inactiveTrackColor = Color(0xFF212121),
                                                activeTickColor = themeColor.copy(alpha = 0.5f),
                                                inactiveTickColor = Color.DarkGray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Spin History Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            "SPIN HISTORY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        if (recentSpins.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E1E1E).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No spins yet — tap wheel to start",
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                recentSpins.forEachIndexed { index, result ->
                                    val segment = state.wheel.segments.find { it.id == result.selectedSegmentId }
                                    HistoryItem(
                                        name = result.selectedSegmentName,
                                        color = segment?.color ?: Color.Gray,
                                        isLatest = index == 0
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }

                // Result Dialog
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

                // Algo Info Bottom Sheet
                if (showAlgoInfo) {
                    ModalBottomSheet(
                        onDismissRequest = { showAlgoInfo = false },
                        sheetState = sheetState,
                        containerColor = Color(0xFF1E1E1E),
                        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.85f)
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 48.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "About Algorithms",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(24.dp))
                            
                            AlgoInfoDetail(
                                icon = Icons.Filled.Casino,
                                title = "Uniform Random",
                                description = "The classic fair choice. Every name on the wheel has an mathematically identical chance of being picked. It's like flipping a perfectly balanced coin or rolling a fair dice.",
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                            Spacer(Modifier.height(20.dp))
                            
                            AlgoInfoDetail(
                                icon = Icons.Filled.Balance,
                                title = "Weighted Random",
                                description = "Allows you to bias the results. If one name has a weight of 5 and another has 1, the first name is 5 times more likely to win. Perfect for 'Luck-based' games where some entries are more valuable than others.",
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                            Spacer(Modifier.height(20.dp))
                            
                            AlgoInfoDetail(
                                icon = Icons.Filled.Tag,
                                title = "Seeded Sequence",
                                description = "A deterministic approach. Using the same seed number will always produce the exact same sequence of winners. This is useful for running fair competitions where everyone can verify the result by using the same seed.",
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                            Spacer(Modifier.height(20.dp))
                            
                            AlgoInfoDetail(
                                icon = Icons.Filled.Autorenew,
                                title = "Round Robin",
                                description = "Ensures everyone gets a turn. It shuffles all names into a hidden queue. Each spin picks the next person until the queue is empty, then it reshuffles for a new round. No one wins twice until everyone has won once.",
                                themeColor = themeColor,
                                lighterThemeColor = lighterThemeColor
                            )
                        }
                    }
                }
            }
            is WheelUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = Color.Red)
                        Button(onClick = { viewModel.loadWheel(wheelId) }) { Text("Retry") }
                    }
                }
            }
        }
    }
}

@Composable
fun AlgoInfoDetail(icon: ImageVector, title: String, description: String, themeColor: Color, lighterThemeColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            color = themeColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = lighterThemeColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun AlgorithmCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    themeColor: Color,
    lighterThemeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (selected) themeColor.copy(alpha = 0.1f) else Color(0xFF1E1E1E),
        border = BorderStroke(1.dp, if (selected) themeColor else Color(0xFF212121))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) lighterThemeColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(description, color = Color.Gray, fontSize = 10.sp)
        }
    }
}
