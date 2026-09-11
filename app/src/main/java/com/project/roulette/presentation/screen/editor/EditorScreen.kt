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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.toArgb
import com.project.roulette.domain.model.Segment
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.component.AlertDialogBox
import com.project.roulette.presentation.model.EditorUiState
import com.project.roulette.presentation.viewmodel.EditorViewModel
import com.project.roulette.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.graphics.painter.Painter
import com.project.roulette.presentation.component.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    wheelId: String? = null,
    isNew: Boolean = true,
    templateId: String? = null,
    onNavigateBack: () -> Unit,
    onPreview: (Wheel) -> Unit,
    onSaved: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showImportDialog by remember { mutableStateOf(false) }

    val themeColor = remember(uiState) {
        val state = uiState
        if (state is EditorUiState.Success && state.wheel != null) {
            ThemePalette.getOrNull(state.wheel.themePaletteIndex) ?: ThemePalette[0]
        } else {
            ThemePalette[0]
        }
    }

    val lighterThemeColor = rememberAccentOnSurface(themeColor)

    LaunchedEffect(Unit) {
        if (isNew) {
            if (templateId != null) {
                viewModel.initializeFromTemplate(templateId)
            } else {
                viewModel.initializeNew()
            }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Create Wheel" else "Edit Wheel", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back", tint = RouletteTheme.colors.textPrimary)
                    }
                },
                actions = {
                    val state = uiState
                    if (state is EditorUiState.Success && state.wheel != null) {
                        Button(
                            onClick = { 
                                onPreview(state.wheel)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RouletteTheme.colors.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Preview", color = RouletteTheme.colors.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RouletteTheme.colors.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = RouletteTheme.colors.background
    ) { padding ->
        when (val state = uiState) {
            is EditorUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RouletteTheme.colors.primary)
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
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    color = RouletteTheme.colors.background.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.4f))
                                ) {
                                    TextField(
                                        value = localName,
                                        onValueChange = { 
                                            localName = it
                                            viewModel.updateWheelName(it)
                                        },
                                        placeholder = { Text("New Wheel", color = RouletteTheme.colors.textSecondary.copy(alpha = 0.3f)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = lighterThemeColor,
                                            focusedTextColor = RouletteTheme.colors.textPrimary,
                                            unfocusedTextColor = RouletteTheme.colors.textPrimary
                                        ),
                                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        item {
                            EditorSectionCard(title = "THEME PALETTE") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ThemePalette.forEachIndexed { index, color ->
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (state.wheel.themePaletteIndex == index) RouletteTheme.colors.textPrimary else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable { viewModel.updateThemePalette(index) }
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(AppIcons.Info, contentDescription = null, tint = RouletteTheme.colors.textSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Segments auto-get shades of this palette", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        item {
                            EditorToggleCard(
                                icon = AppIcons.VolumeUp,
                                title = "Spin sound",
                                description = "Play tick sound while spinning",
                                checked = state.wheel.spinSound,
                                onCheckedChange = { viewModel.updateSpinSound(it) },
                                themeColor = themeColor
                            )
                        }

                        item {
                            EditorToggleCard(
                                icon = AppIcons.PersonRemove,
                                title = "Remove after pick",
                                description = "Picked names won't repeat",
                                checked = state.wheel.removeAfterPick,
                                onCheckedChange = { viewModel.updateRemoveAfterPick(it) },
                                themeColor = themeColor
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Segments", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = RouletteTheme.colors.textPrimary)
                                    Spacer(Modifier.width(12.dp))
                                    Surface(
                                        color = themeColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            state.wheel.segments.size.toString(),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = lighterThemeColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    TextButton(
                                        onClick = { showImportDialog = true },
                                        colors = ButtonDefaults.textButtonColors(containerColor = RouletteTheme.colors.surface),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(AppIcons.Upload, contentDescription = null, modifier = Modifier.size(18.dp), tint = RouletteTheme.colors.textSecondary)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Import", style = MaterialTheme.typography.bodySmall, color = RouletteTheme.colors.textSecondary)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.addSegment("New Option")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(AppIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColorOn(themeColor))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Add", style = MaterialTheme.typography.bodySmall, color = contentColorOn(themeColor))
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
                                onUpdate = { name, weight ->
                                    viewModel.updateSegment(segment.id, name, weight)
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
                                shape = RouletteTheme.shapes.card,
                                colors = ButtonDefaults.buttonColors(containerColor = RouletteTheme.colors.primary),
                                enabled = !state.isSaving
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(AppIcons.Save, contentDescription = null, tint = RouletteTheme.colors.onPrimary)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        if (state.isSaving) "Saving..." else "Save Wheel",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = RouletteTheme.colors.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    if (showImportDialog) {
                        var importText by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showImportDialog = false },
                            containerColor = RouletteTheme.colors.surfaceElevated,
                            title = { Text("Import Names", color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold) },
                            text = {
                                Column {
                                    Text("Enter names separated by comma or new line", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = importText,
                                        onValueChange = { importText = it },
                                        modifier = Modifier.fillMaxWidth().height(180.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = themeColor,
                                            unfocusedBorderColor = RouletteTheme.colors.surface,
                                            focusedTextColor = RouletteTheme.colors.textPrimary,
                                            unfocusedTextColor = RouletteTheme.colors.textPrimary,
                                            unfocusedContainerColor = RouletteTheme.colors.background,
                                            focusedContainerColor = RouletteTheme.colors.background
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
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Import", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showImportDialog = false }) {
                                    Text("Cancel", color = RouletteTheme.colors.textSecondary)
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
                        Text("Error: ${state.message}", color = RouletteTheme.colors.danger, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.initializeNew() }, colors = ButtonDefaults.buttonColors(containerColor = themeColor)) {
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
        color = RouletteTheme.colors.surface,
        shape = RouletteTheme.shapes.card,
        border = BorderStroke(1.dp, RouletteTheme.colors.divider)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = RouletteTheme.colors.textSecondary.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            content()
        }
    }
}

@Composable
private fun EditorToggleCard(
    icon: Painter,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RouletteTheme.colors.surface,
        shape = RouletteTheme.shapes.card,
        border = BorderStroke(1.dp, RouletteTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(RouletteTheme.colors.textSecondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = RouletteTheme.colors.textSecondary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(title, color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(description, color = RouletteTheme.colors.textSecondary.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = RouletteTheme.colors.textPrimary,
                    checkedTrackColor = RouletteTheme.colors.primary,
                    uncheckedThumbColor = RouletteTheme.colors.textSecondary,
                    uncheckedTrackColor = RouletteTheme.colors.surfaceElevated,
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
    onUpdate: (String, Float) -> Unit
) {
    var name by remember(segment.name) { mutableStateOf(segment.name.replace("\u200B", "")) }
    
    // Calculate a brighter color for UI accents and text to ensure visibility
    val accentColor = remember(segment.color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(segment.color.toArgb(), hsv)
        hsv[1] = (hsv[1] * 0.8f).coerceIn(0.3f, 0.7f) // Reduce saturation
        hsv[2] = 0.95f // High brightness
        Color(android.graphics.Color.HSVToColor(hsv))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RouletteTheme.colors.surface,
        shape = RouletteTheme.shapes.card,
        border = BorderStroke(1.dp, segment.color.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.background(segment.color.copy(alpha = 0.08f))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "SEGMENT ${index + 1}", 
                            color = accentColor.copy(alpha = 0.9f), 
                            style = MaterialTheme.typography.bodySmall, 
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 1.2.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Move Up Button
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = index > 0) { onMoveUp() },
                            color = if (index > 0) segment.color.copy(alpha = 0.15f) else RouletteTheme.colors.surfaceElevated.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (index > 0) BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    AppIcons.ExpandLess,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) accentColor else RouletteTheme.colors.textTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Remove Button
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = canDelete) { onRemove() },
                            color = if (canDelete) RouletteTheme.colors.danger.copy(alpha = 0.1f) else RouletteTheme.colors.surfaceElevated.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (canDelete) BorderStroke(1.dp, RouletteTheme.colors.danger.copy(alpha = 0.2f)) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    AppIcons.Delete,
                                    contentDescription = "Remove",
                                    tint = if (canDelete) RouletteTheme.colors.danger else RouletteTheme.colors.textTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = RouletteTheme.colors.background.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, segment.color.copy(alpha = 0.4f))
                ) {
                    TextField(
                        value = name,
                        onValueChange = {
                            name = it
                            onUpdate(it, segment.weight)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = accentColor,
                            focusedTextColor = RouletteTheme.colors.textPrimary,
                            unfocusedTextColor = RouletteTheme.colors.textPrimary
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        placeholder = { Text("Segment name", color = RouletteTheme.colors.textSecondary.copy(alpha = 0.3f)) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Weight (probability)", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("${String.format(Locale.US, "%.1f", segment.weight)}x", color = accentColor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = segment.weight,
                    onValueChange = { onUpdate(segment.name, it) },
                    valueRange = 1f..5f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = RouletteTheme.colors.textPrimary,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = RouletteTheme.colors.divider,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
