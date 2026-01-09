package com.project.roulette.presentation.screen.wheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.component.WheelCanvas
import com.project.roulette.presentation.model.WheelUiState
import com.project.roulette.presentation.viewmodel.WheelViewModel
import kotlinx.coroutines.launch

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
    val spinDuration by viewModel.spinDuration.collectAsStateWithLifecycle()
    val pendingOutcome by viewModel.pendingSpinOutcome.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(wheelId) {
        viewModel.loadWheel(wheelId)
    }

    // Animatable for rotation so we can await animation end
    val rotationAnim = remember { Animatable(0f) }

    // When a pending outcome appears, compute a visually pleasing target rotation
    LaunchedEffect(pendingOutcome) {
        val outcome = pendingOutcome
        if (outcome != null) {
            // Compute a target rotation that lands the selected segment under the pointer.
            // outcome.spinResult.finalAngle is the angle (0-360) of the segment midpoint relative to 0 at top.
            // We'll rotate so that the wheel ends with the pointer at 0 degrees matching (360 - finalAngle).
            val current = rotationAnim.value % 360f
            val rotations = 5 // number of full spins for effect
            val target = current + rotations * 360f + (360f - outcome.spinResult.finalAngle)

            // Animate and await completion
            scope.launch {
                rotationAnim.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = spinDuration.toInt())
                )
                // Notify ViewModel that animation finished
                viewModel.onAnimationComplete()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is WheelUiState.Success -> Text(state.wheel.name)
                        else -> Text("Wheel")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onNavigateToEdit(wheelId)
                                menuExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Filled.Edit, "Edit") }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = {
                                onNavigateToHistory(wheelId)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Statistics") },
                            onClick = {
                                onNavigateToStatistics(wheelId)
                                menuExpanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is WheelUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is WheelUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WheelCanvas(
                        wheel = state.wheel,
                        rotation = rotationAnim.value,
                        modifier = Modifier.weight(1f)
                    )

                    if (state.lastSpinResult != null) {
                        Text("Last: ${state.lastSpinResult.selectedSegmentName}")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Algorithm:")
                        SelectionAlgorithmFactory.getAllAlgorithmTypes().forEach { type ->
                            val isSelectedType = (type == selectedAlgorithm)
                            Button(
                                onClick = { viewModel.setSelectionAlgorithm(type) },
                                modifier = Modifier,
                                enabled = !isSelectedType
                            ) {
                                val label = when (type) {
                                    SelectionAlgorithmFactory.AlgorithmType.UNIFORM -> "UNI"
                                    SelectionAlgorithmFactory.AlgorithmType.WEIGHTED -> "WGT"
                                    SelectionAlgorithmFactory.AlgorithmType.SEEDED -> "SEED"
                                    SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN -> "RR"
                                }
                                Text(label)
                            }
                        }
                    }

                    // Show selected algorithm full name
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Selected: ${SelectionAlgorithmFactory.getAlgorithmName(selectedAlgorithm)}")
                    }

                    Button(
                        onClick = {
                            if (!state.isSpinning) {
                                viewModel.spinWheel()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        enabled = !state.isSpinning
                    ) {
                        Text(if (state.isSpinning) "Spinning..." else "SPIN WHEEL")
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}")
                        Button(onClick = { viewModel.loadWheel(wheelId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
