package com.project.roulette.presentation.screen.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.presentation.component.AlertDialogBox
import com.project.roulette.presentation.model.EditorUiState
import com.project.roulette.presentation.viewmodel.EditorViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    wheelId: String? = null,
    isNew: Boolean = true,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (isNew) {
            viewModel.initializeNew()
        } else if (wheelId != null) {
            viewModel.loadWheel(wheelId)
        }
    }

    // When save completes, show snackbar and call onSaved to navigate back
    LaunchedEffect(uiState) {
        val current = uiState
        if (current is EditorUiState.Success && current.isSaved) {
            // show snackbar asynchronously (do not block navigation)
            scope.launch {
                snackbarHostState.showSnackbar("Wheel saved!")
            }
            // Navigate back immediately when saved flag is set
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Create Wheel" else "Edit Wheel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is EditorUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EditorUiState.Success -> {
                if (state.wheel != null) {
                    val canDelete = state.wheel.segments.size > 2
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                            .imePadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            var localName by remember(state.wheel.name) { mutableStateOf(state.wheel.name) }

                            OutlinedTextField(
                                value = localName,
                                onValueChange = { 
                                    localName = it
                                    viewModel.updateWheelName(it)
                                },
                                label = { Text("Wheel Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.isFocused) {
                                            val safeName = localName.ifBlank { "New Wheel" }
                                            localName = safeName
                                            viewModel.updateWheelName(safeName)
                                        }
                                    }
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = state.wheel.description,
                                onValueChange = { viewModel.updateWheelDescription(it) },
                                label = { Text("Description") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                maxLines = 3
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Segments (${state.wheel.segments.size})")
                                IconButton(onClick = {
                                    viewModel.addSegment(
                                        "New Segment",
                                        Color(
                                            (Math.random() * 0xFFFFFF).toLong() or 0xFF000000L
                                        )
                                    )
                                }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add")
                                }
                            }
                        }

                        // Use stable keys for segments to ensure correct removal and stable state
                        items(items = state.wheel.segments, key = { it.id }) { segment ->
                            SegmentEditorCard(
                                segment = segment,
                                canDelete = canDelete,
                                onRemove = { viewModel.removeSegment(segment.id) },
                                onUpdate = { name, color, weight ->
                                    viewModel.updateSegment(
                                        segment.id,
                                        name,
                                        color,
                                        weight
                                    )
                                }
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    // Trigger save. Navigation is handled by LaunchedEffect(uiState)
                                    viewModel.saveWheel()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                enabled = !state.isSaving
                            ) {
                                Text(if (state.isSaving) "Saving..." else "Save Wheel")
                            }
                        }
                    }

                    // Show alert dialog when there's a saveError
                    if (state.saveError != null) {
                        AlertDialogBox(
                            visible = true,
                            title = "Error",
                            message = state.saveError,
                            confirmText = "OK",
                            onConfirm = { viewModel.clearSaveError() },
                            onDismiss = { viewModel.clearSaveError() }
                        )
                    }
                }
            }

            is EditorUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}")
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentEditorCard(
    segment: com.project.roulette.domain.model.Segment,
    canDelete: Boolean,
    onRemove: () -> Unit,
    onUpdate: (String, Color, Float) -> Unit
) {
    val (name, setName) = remember { mutableStateOf(segment.name) }
    val (weight, setWeight) = remember { mutableStateOf(segment.weight.toString()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        setName(it)
                        // Sync with ViewModel on every change so Save works correctly
                        val parsedWeight = weight.toFloatOrNull() ?: 1f
                        onUpdate(it, segment.color, parsedWeight)
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                // when focus lost: if name is blank, restore previous usable name and persist it
                                if (name.isBlank()) {
                                    val restored = segment.name.ifBlank { "Option" }
                                    setName(restored)
                                    val parsedWeight = weight.toFloatOrNull() ?: 1f
                                    onUpdate(restored, segment.color, parsedWeight)
                                }
                            }
                        }
                )
                IconButton(
                    onClick = onRemove,
                    enabled = canDelete
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                }
            }

            OutlinedTextField(
                value = weight,
                singleLine = true,
                onValueChange = {
                    // Only allow digits and a single decimal point
                    val filtered = it.filter { c -> c.isDigit() || c == '.' }
                        .let { s ->
                            val dotIndex = s.indexOf('.')
                            if (dotIndex != -1) {
                                // Only allow up to 2 digits after decimal point
                                val beforeDot = s.substring(0, dotIndex + 1)
                                val afterDot = s.substring(dotIndex + 1).replace(".", "").take(2)
                                beforeDot + afterDot
                            } else s
                        }
                    setWeight(filtered)
                    val parsed = filtered.toFloatOrNull() ?: 1f
                    val nameToUse = if (name.isNotBlank()) name else segment.name.ifBlank { "Option" }
                    onUpdate(nameToUse, segment.color, parsed)
                },
                label = { Text("Weight") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}
