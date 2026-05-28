package com.project.roulette.presentation.screen.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Segment
import com.project.roulette.presentation.component.AlertDialogBox
import com.project.roulette.presentation.model.EditorUiState
import com.project.roulette.presentation.viewmodel.EditorViewModel
import com.project.roulette.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    wheelId: String? = null,
    isNew: Boolean = true,
    onNavigateBack: () -> Unit,
    onPreview: (String) -> Unit,
    onSaved: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showImportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isNew) {
            viewModel.initializeNew()
        } else if (wheelId != null) {
            viewModel.loadWheel(wheelId)
        }
    }

    LaunchedEffect(uiState) {
        val current = uiState
        if (current is EditorUiState.Success && current.isSaved) {
            scope.launch {
                snackbarHostState.showSnackbar("Wheel saved!")
            }
            current.wheel?.let { onSaved(it.id) }
        }
    }

    val themeColors = listOf(
        Color(0xFF673AB7), // Purple
        Color(0xFF00796B), // Green/Teal
        Color(0xFFD84315), // Deep Orange/Rust
        Color(0xFF1976D2), // Blue
        Color(0xFFC2185B), // Pink
        Color(0xFFFFA000), // Amber/Orange
        Color(0xFF388E3C), // Green
        Color(0xFF616161)  // Gray
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Create Wheel" else "Edit Wheel", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    val state = uiState
                    if (state is EditorUiState.Success && state.wheel != null) {
                        Button(
                            onClick = { 
                                // Preview navigation might need wheel to be saved first or passed as state.
                                // For now, we use the ID.
                                onPreview(state.wheel.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Preview", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavyBlack)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepNavyBlack
    ) { padding ->
        when (val state = uiState) {
            is EditorUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            is EditorUiState.Success -> {
                if (state.wheel != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp)
                            .imePadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            EditorSectionCard(title = "WHEEL NAME") {
                                var localName by remember(state.wheel.name) { mutableStateOf(state.wheel.name.replace("\u200B", "")) }
                                TextField(
                                    value = localName,
                                    onValueChange = { 
                                        localName = it
                                        viewModel.updateWheelName(it)
                                    },
                                    placeholder = { Text("New Wheel", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = Color.White,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        item {
                            EditorSectionCard(title = "THEME PALETTE") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    themeColors.forEachIndexed { index, color ->
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (state.wheel.themePaletteIndex == index) Color.White else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable { viewModel.updateThemePalette(index) }
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Segments auto-get shades of this palette", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        item {
                            EditorToggleCard(
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                title = "Spin sound",
                                description = "Play tick sound while spinning",
                                checked = state.wheel.spinSound,
                                onCheckedChange = { viewModel.updateSpinSound(it) }
                            )
                        }

                        item {
                            EditorToggleCard(
                                icon = Icons.Filled.PersonRemove,
                                title = "Remove after pick",
                                description = "Picked names won't repeat",
                                checked = state.wheel.removeAfterPick,
                                onCheckedChange = { viewModel.updateRemoveAfterPick(it) }
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Segments", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                                    Spacer(Modifier.width(12.dp))
                                    Surface(
                                        color = PrimaryPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            state.wheel.segments.size.toString(),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    TextButton(
                                        onClick = { showImportDialog = true },
                                        colors = ButtonDefaults.textButtonColors(containerColor = SurfaceDark),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Import", fontSize = 14.sp, color = TextSecondary)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.addSegment("New Option")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Add", fontSize = 14.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        itemsIndexed(items = state.wheel.segments, key = { _, it -> it.id }) { index, segment ->
                            SegmentEditorCard(
                                segment = segment,
                                index = index,
                                canDelete = state.wheel.segments.size > 2,
                                onRemove = { viewModel.removeSegment(segment.id) },
                                onMoveUp = { viewModel.moveSegmentUp(segment.id) },
                                onUpdate = { name, color, weight ->
                                    viewModel.updateSegment(segment.id, name, color, weight)
                                }
                            )
                        }

                        item {
                            Button(
                                onClick = { viewModel.saveWheel() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .height(64.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                enabled = !state.isSaving
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        if (state.isSaving) "Saving..." else "Save Wheel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    if (showImportDialog) {
                        var importText by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showImportDialog = false },
                            containerColor = SurfaceDarker,
                            title = { Text("Import Names", color = Color.White, fontWeight = FontWeight.Bold) },
                            text = {
                                Column {
                                    Text("Enter names separated by comma or new line", color = TextSecondary, fontSize = 14.sp)
                                    Spacer(Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = importText,
                                        onValueChange = { importText = it },
                                        modifier = Modifier.fillMaxWidth().height(180.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryPurple,
                                            unfocusedBorderColor = SurfaceDark,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            unfocusedContainerColor = DeepNavyBlack,
                                            focusedContainerColor = DeepNavyBlack
                                        )
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.importNames(importText)
                                        showImportDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Import", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showImportDialog = false }) {
                                    Text("Cancel", color = TextSecondary)
                                }
                            }
                        )
                    }

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
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.initializeNew() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SurfaceDarker)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            content()
        }
    }
}

@Composable
private fun EditorToggleCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SurfaceDarker)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(TextSecondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(description, color = TextSecondary, fontSize = 13.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryPurple,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = SurfaceDarker,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun SegmentEditorCard(
    segment: Segment,
    index: Int,
    canDelete: Boolean,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onUpdate: (String, Color, Float) -> Unit
) {
    var name by remember(segment.name) { mutableStateOf(segment.name.replace("\u200B", "")) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = segment.color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, segment.color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(segment.color, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text("SEGMENT ${index + 1}", color = segment.color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Move Up Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (index > 0) segment.color.copy(alpha = 0.12f) else SurfaceDarker.copy(alpha = 0.5f))
                            .clickable(enabled = index > 0) { onMoveUp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.ExpandLess,
                            contentDescription = "Move Up",
                            tint = if (index > 0) segment.color else Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Remove Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (canDelete) Color.Red.copy(alpha = 0.1f) else SurfaceDarker.copy(alpha = 0.5f))
                            .clickable(enabled = canDelete) { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Remove",
                            tint = if (canDelete) Color(0xFFE57373) else Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                color = DeepNavyBlack.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onUpdate(it, segment.color, segment.weight)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    placeholder = { Text("Segment name", color = TextSecondary) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weight (probability)", color = segment.color, fontSize = 13.sp)
                Text("${String.format(Locale.US, "%.1f", segment.weight)}x", color = segment.color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Slider(
                value = segment.weight,
                onValueChange = { onUpdate(segment.name, segment.color, it) },
                valueRange = 1f..5f,
                steps = 7, // 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0
                colors = SliderDefaults.colors(
                    thumbColor = Color.Gray,
                    activeTrackColor = segment.color,
                    inactiveTrackColor = Color.Black.copy(alpha = 0.3f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
