package com.project.roulette.presentation.screen.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.ThemeMode
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.WhatsNewDialog
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppTopBar
import com.project.roulette.presentation.component.design.InsetDivider
import com.project.roulette.presentation.component.design.SettingsGroup
import com.project.roulette.presentation.component.design.SettingsRow
import com.project.roulette.presentation.viewmodel.SettingsViewModel
import com.project.roulette.presentation.viewmodel.WheelViewModel
import com.project.roulette.ui.theme.Palettes
import com.project.roulette.ui.theme.RoulettePalette
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.util.AppChangelog
import com.project.roulette.util.getCurrentVersionName

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val paletteIndex by viewModel.paletteIndex.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val defaultAlgo by viewModel.defaultAlgorithm.collectAsStateWithLifecycle()
    val defaultSpeed by viewModel.defaultSpinSpeed.collectAsStateWithLifecycle()
    val spinSound by viewModel.spinSoundEnabled.collectAsStateWithLifecycle()
    val confetti by viewModel.confettiEnabled.collectAsStateWithLifecycle()
    val removeAfterPick by viewModel.removeAfterPickEnabled.collectAsStateWithLifecycle()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                eyebrow = "App",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = dimens.screenPadding,
                end = dimens.screenPadding,
                top = dimens.listTopPadding,
                bottom = dimens.listBottomPadding + dimens.bottomBarSpace
            ),
            verticalArrangement = Arrangement.spacedBy(dimens.space24)
        ) {
            item {
                SettingsGroup(
                    title = "Appearance",
                    footnote = "System follows your device's light or dark setting."
                ) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        if (index > 0) InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                        SettingsRow(
                            title = mode.label,
                            leadingIcon = when (mode) {
                                ThemeMode.SYSTEM -> AppIcons.Contrast
                                ThemeMode.LIGHT -> AppIcons.LightMode
                                ThemeMode.DARK -> AppIcons.DarkMode
                            },
                            leadingIconTint = if (themeMode == mode) colors.primary else colors.textSecondary,
                            onClick = { viewModel.updateThemeMode(mode) },
                            showChevron = false,
                            trailing = if (themeMode == mode) {
                                {
                                    Icon(
                                        painter = AppIcons.Check,
                                        contentDescription = "Selected",
                                        tint = colors.primary,
                                        modifier = Modifier.size(dimens.iconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            item {
                SettingsGroup(title = "App color") {
                    Column(modifier = Modifier.padding(dimens.space16)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = AppIcons.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(dimens.iconSizeSmall)
                            )
                            Spacer(Modifier.width(dimens.space12))
                            Column {
                                Text(
                                    "Color palette",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    "Changes the app's accent color everywhere",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(Modifier.height(dimens.space24))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.space8),
                            verticalArrangement = Arrangement.spacedBy(dimens.space16),
                            maxItemsInEachRow = 4
                        ) {
                            Palettes.forEachIndexed { index, palette ->
                                PaletteItem(
                                    palette = palette,
                                    isSelected = paletteIndex == index,
                                    onClick = { viewModel.updatePaletteIndex(index) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(dimens.space24))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimens.space8)
                                .clip(CircleShape)
                                .background(colors.primarySubtle)
                        ) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(colors.primary.copy(alpha = 0.4f)))
                            Box(Modifier.weight(1f).fillMaxHeight().background(colors.primary.copy(alpha = 0.7f)))
                            Box(Modifier.weight(1f).fillMaxHeight().background(colors.primary))
                            Box(Modifier.weight(1f).fillMaxHeight().background(colors.primary.copy(alpha = 0.4f)))
                        }

                        Spacer(Modifier.height(dimens.space12))
                        Text(
                            text = Palettes.getOrElse(paletteIndex) { Palettes[0] }.name,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.primary
                        )
                    }
                }
            }

            item {
                SettingsGroup(title = "Spin defaults") {
                    SettingsRow(
                        leadingIcon = AppIcons.Memory,
                        title = "Default algorithm",
                        subtitle = "Applied to all new wheels",
                        value = defaultAlgo.name.lowercase().replaceFirstChar { it.uppercase() },
                        valueColor = colors.primary,
                        onClick = {
                            val entries = SelectionAlgorithmFactory.AlgorithmType.entries
                            viewModel.updateDefaultAlgorithm(entries[(defaultAlgo.ordinal + 1) % entries.size])
                        }
                    )
                    InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                    SettingsRow(
                        leadingIcon = AppIcons.Speed,
                        title = "Default spin speed",
                        subtitle = "Starting speed for new wheels",
                        value = defaultSpeed.label,
                        valueColor = colors.primary,
                        onClick = {
                            val entries = WheelViewModel.SpinSpeed.entries
                            viewModel.updateDefaultSpinSpeed(entries[(defaultSpeed.ordinal + 1) % entries.size])
                        }
                    )
                    InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                    SettingsRow(
                        leadingIcon = AppIcons.VolumeUp,
                        title = "Spin sound",
                        subtitle = "Tick sound while spinning",
                        checked = spinSound,
                        onCheckedChange = { viewModel.updateSpinSoundEnabled(it) }
                    )
                    InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                    SettingsRow(
                        leadingIcon = AppIcons.Celebration,
                        title = "Confetti on win",
                        subtitle = "Celebrate every result",
                        checked = confetti,
                        onCheckedChange = { viewModel.updateConfettiEnabled(it) }
                    )
                    InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                    SettingsRow(
                        leadingIcon = AppIcons.PersonRemove,
                        title = "Remove after pick",
                        subtitle = "Default for all new wheels",
                        checked = removeAfterPick,
                        onCheckedChange = { viewModel.updateRemoveAfterPickEnabled(it) }
                    )
                }
            }

            item {
                SettingsGroup(title = "About") {
                    SettingsRow(
                        leadingIcon = AppIcons.Star,
                        title = "Rate the app",
                        subtitle = "Leave a review on Play Store",
                        onClick = {
                            val packageName = context.packageName
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = "market://details?id=$packageName".toUri()
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            "https://play.google.com/store/apps/details?id=$packageName".toUri()
                                    }
                                )
                            }
                        }
                    )
                    InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                    SettingsRow(
                        leadingIcon = AppIcons.ChatBubble,
                        title = "Send feedback",
                        subtitle = "Report bugs or suggest features",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:support@roulette.com".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Roulette App Feedback")
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                            } catch (_: Exception) {
                            }
                        }
                    )
                    InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                    SettingsRow(
                        leadingIcon = AppIcons.NewReleases,
                        title = "What's new",
                        subtitle = "See the latest changes",
                        onClick = { showWhatsNewDialog = true }
                    )
                    InsetDivider(RouletteTheme.dimens.dividerInsetWithIcon)
                    SettingsRow(
                        leadingIcon = AppIcons.Info,
                        title = "App version",
                        subtitle = "Up to date",
                        value = "v${getCurrentVersionName(context)}",
                        showChevron = false
                    )
                }
            }

            item {
                SettingsGroup(title = "Danger zone") {
                    SettingsRow(
                        leadingIcon = AppIcons.Delete,
                        leadingIconTint = colors.danger,
                        title = "Clear all data",
                        subtitle = "Deletes all wheels & history",
                        titleColor = colors.danger,
                        onClick = { showClearDataDialog = true }
                    )
                }
            }
        }

        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                containerColor = colors.surfaceElevated,
                shape = RouletteTheme.shapes.dialog,
                title = {
                    Text(
                        "Clear All Data?",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        "This will permanently delete all your wheels and spin history. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }) {
                        Text(
                            "CLEAR",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.danger
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) {
                        Text(
                            "CANCEL",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary
                        )
                    }
                }
            )
        }

        if (showWhatsNewDialog) {
            AppChangelog.latest?.let { entry ->
                WhatsNewDialog(
                    entry = entry,
                    themeColor = colors.primary,
                    onDismiss = { showWhatsNewDialog = false }
                )
            }
        }
    }
}

@Composable
private fun PaletteItem(
    palette: RoulettePalette,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RouletteTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(palette.primary)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) colors.textPrimary else Color.Transparent,
                    shape = CircleShape
                )
        )
        Spacer(Modifier.height(RouletteTheme.dimens.space8))
        Text(
            palette.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colors.textPrimary else colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
