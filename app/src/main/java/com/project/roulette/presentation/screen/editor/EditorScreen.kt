package com.project.roulette.presentation.screen.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Segment
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.component.AlertDialogBox
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppTopBar
import com.project.roulette.presentation.component.design.GroupedCard
import com.project.roulette.presentation.component.design.InsetDivider
import com.project.roulette.presentation.component.design.Pill
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.SecondaryButton
import com.project.roulette.presentation.component.design.SectionHeader
import com.project.roulette.presentation.component.design.SettingsGroup
import com.project.roulette.presentation.component.design.SettingsRow
import com.project.roulette.presentation.component.design.appTextFieldColors
import com.project.roulette.presentation.model.EditorUiState
import com.project.roulette.presentation.viewmodel.EditorViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.ThemePalette
import com.project.roulette.ui.theme.rememberAccentOnSurface
import kotlinx.coroutines.launch
import java.util.Locale

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

    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

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
            scope.launch { snackbarHostState.showSnackbar("Wheel saved") }
            current.wheel?.let { onSaved(it.id) }
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = if (isNew) "Create wheel" else "Edit wheel",
                eyebrow = "Editor",
                onNavigateBack = onNavigateBack,
                actions = {
                    val state = uiState
                    if (state is EditorUiState.Success && state.wheel != null) {
                        SecondaryButton(
                            text = "Preview",
                            onClick = { onPreview(state.wheel) },
                            contentColor = lighterThemeColor,
                            modifier = Modifier.padding(end = dimens.space8)
                        )
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
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            is EditorUiState.Success -> {
                if (state.wheel != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .imePadding(),
                        contentPadding = PaddingValues(
                            start = dimens.screenPadding,
                            end = dimens.screenPadding,
                            top = dimens.listTopPadding,
                            bottom = dimens.listBottomPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(dimens.space24)
                    ) {
                        item {
                            var localName by remember(state.wheel.name) {
                                mutableStateOf(state.wheel.name.replace("​", ""))
                            }
                            Column {
                                SectionHeader("Wheel name")
                                OutlinedTextField(
                                    value = localName,
                                    onValueChange = {
                                        localName = it
                                        viewModel.updateWheelName(it)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = "New wheel",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = colors.textTertiary
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    shape = RouletteTheme.shapes.textField,
                                    colors = appTextFieldColors(lighterThemeColor),
                                    singleLine = true
                                )
                            }
                        }

                        item {
                            SettingsGroup(
                                title = "Theme palette",
                                footnote = "Segments get shades of the palette you pick."
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimens.space16),
                                    horizontalArrangement = Arrangement.spacedBy(dimens.space12)
                                ) {
                                    ThemePalette.forEachIndexed { index, color ->
                                        PaletteSwatch(
                                            color = color,
                                            selected = state.wheel.themePaletteIndex == index,
                                            onClick = { viewModel.updateThemePalette(index) }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            SettingsGroup(title = "Spin behaviour") {
                                SettingsRow(
                                    leadingIcon = AppIcons.VolumeUp,
                                    title = "Spin sound",
                                    subtitle = "Play a tick sound while spinning",
                                    checked = state.wheel.spinSound,
                                    onCheckedChange = { viewModel.updateSpinSound(it) }
                                )
                                InsetDivider(dimens.dividerInsetWithIcon)
                                SettingsRow(
                                    leadingIcon = AppIcons.PersonRemove,
                                    title = "Remove after pick",
                                    subtitle = "Picked names will not repeat",
                                    checked = state.wheel.removeAfterPick,
                                    onCheckedChange = { viewModel.updateRemoveAfterPick(it) }
                                )
                            }
                        }

                        item {
                            Column {
                                SectionHeader(
                                    text = "Segments",
                                    trailing = {
                                        Pill(
                                            text = state.wheel.segments.size.toString(),
                                            accent = lighterThemeColor
                                        )
                                    }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(dimens.space8)
                                ) {
                                    SecondaryButton(
                                        text = "Import",
                                        icon = AppIcons.Upload,
                                        onClick = { showImportDialog = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PrimaryButton(
                                        text = "Add segment",
                                        icon = AppIcons.Add,
                                        onClick = { viewModel.addSegment("New Option") },
                                        containerColor = themeColor,
                                        height = dimens.buttonHeightSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        itemsIndexed(
                            items = state.wheel.segments,
                            key = { _, it -> it.id }
                        ) { index, segment ->
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
                            PrimaryButton(
                                text = if (state.isSaving) "Saving…" else "Save wheel",
                                icon = AppIcons.Save,
                                onClick = { viewModel.saveWheel() },
                                enabled = !state.isSaving,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (showImportDialog) {
                        ImportNamesDialog(
                            accent = themeColor,
                            onImport = {
                                viewModel.importNames(it)
                                showImportDialog = false
                            },
                            onDismiss = { showImportDialog = false }
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
                            color = colors.danger,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.size(dimens.space16))
                        PrimaryButton(
                            text = "Retry",
                            onClick = { viewModel.initializeNew() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RouletteTheme.shapes.avatar)
            .background(color)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) RouletteTheme.colors.textPrimary else Color.Transparent,
                shape = RouletteTheme.shapes.avatar
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ImportNamesDialog(
    accent: Color,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    var importText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        shape = RouletteTheme.shapes.dialog,
        title = {
            Text(
                text = "Import names",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter names separated by a comma or a new line.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(Modifier.size(dimens.space16))
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RouletteTheme.shapes.textField,
                    colors = appTextFieldColors(accent)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(importText) }) {
                Text(
                    text = "IMPORT",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCEL",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
            }
        }
    )
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
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val accent = rememberAccentOnSurface(segment.color)
    var name by remember(segment.name) {
        mutableStateOf(segment.name.replace("​", ""))
    }

    GroupedCard(border = BorderStroke(dimens.borderWidth, accent.copy(alpha = 0.32f))) {
        Column(modifier = Modifier.padding(dimens.space16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Pill(
                    text = "Segment ${index + 1}",
                    accent = accent,
                    showDot = true
                )
                Spacer(Modifier.weight(1f))
                SquareIconButton(
                    icon = AppIcons.ExpandLess,
                    contentDescription = "Move up",
                    tint = accent,
                    enabled = index > 0,
                    onClick = onMoveUp
                )
                Spacer(Modifier.width(dimens.space8))
                SquareIconButton(
                    icon = AppIcons.Delete,
                    contentDescription = "Remove segment",
                    tint = colors.danger,
                    enabled = canDelete,
                    onClick = onRemove
                )
            }

            Spacer(Modifier.size(dimens.space12))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onUpdate(it, segment.weight)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Segment name",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textTertiary
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RouletteTheme.shapes.textField,
                colors = appTextFieldColors(accent),
                singleLine = true
            )

            Spacer(Modifier.size(dimens.space12))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", segment.weight)}x",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent
                )
            }

            Slider(
                value = segment.weight,
                onValueChange = { onUpdate(segment.name, it) },
                valueRange = 1f..5f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = colors.divider,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SquareIconButton(
    icon: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Surface(
        modifier = Modifier
            .size(dimens.buttonHeightSmall)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RouletteTheme.shapes.iconTile,
        color = if (enabled) tint.copy(alpha = 0.12f) else colors.surfacePressed,
        border = BorderStroke(
            dimens.borderWidth,
            if (enabled) tint.copy(alpha = 0.28f) else colors.divider
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = if (enabled) tint else colors.textTertiary,
                modifier = Modifier.size(dimens.iconSizeSmall)
            )
        }
    }
}
