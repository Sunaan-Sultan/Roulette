package com.project.roulette.presentation.screen.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.viewmodel.SettingsViewModel
import com.project.roulette.presentation.viewmodel.WheelViewModel
import com.project.roulette.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val paletteIndex by viewModel.paletteIndex.collectAsStateWithLifecycle()
    val defaultAlgo by viewModel.defaultAlgorithm.collectAsStateWithLifecycle()
    val defaultSpeed by viewModel.defaultSpinSpeed.collectAsStateWithLifecycle()
    val spinSound by viewModel.spinSoundEnabled.collectAsStateWithLifecycle()
    val confetti by viewModel.confettiEnabled.collectAsStateWithLifecycle()
    val removeAfterPick by viewModel.removeAfterPickEnabled.collectAsStateWithLifecycle()

    var showClearDataDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val colors = RouletteTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("APP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                        Text("Settings", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavyBlack)
            )
        },
        containerColor = DeepNavyBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. App Color Section
            item {
                SectionHeader("APP COLOR")
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Color palette", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text("Changes the app's accent color everywhere", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            maxItemsInEachRow = 4
                        ) {
                            Palettes.forEachIndexed { index, palette ->
                                PaletteItem(
                                    palette = palette,
                                    isSelected = paletteIndex == index,
                                    onClick = { viewModel.updatePaletteIndex(index) }
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        
                        // Palette preview bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.1f))
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colors.primary.copy(alpha = 0.4f)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colors.primary.copy(alpha = 0.7f)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colors.primary))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(colors.primary.copy(alpha = 0.4f)))
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = Palettes[paletteIndex].name,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 2. Spin Defaults
            item {
                SectionHeader("SPIN DEFAULTS")
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column {
                        SettingsOptionRow(
                            icon = Icons.Filled.Memory,
                            title = "Default algorithm",
                            description = "Applied to all new wheels",
                            color = colors.primary,
                            trailing = {
                                Badge(
                                    containerColor = colors.primary.copy(alpha = 0.15f),
                                    contentColor = colors.primary
                                ) {
                                    Text(defaultAlgo.name.lowercase().replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            },
                            onClick = {
                                // Toggle logic or show dialog
                                val next = SelectionAlgorithmFactory.AlgorithmType.entries[(defaultAlgo.ordinal + 1) % SelectionAlgorithmFactory.AlgorithmType.entries.size]
                                viewModel.updateDefaultAlgorithm(next)
                            }
                        )
                        Divider()
                        SettingsOptionRow(
                            icon = Icons.Filled.Speed,
                            title = "Default spin speed",
                            description = "Starting speed for new wheels",
                            color = Color(0xFF00B894),
                            trailing = {
                                Badge(
                                    containerColor = Color(0xFF00B894).copy(alpha = 0.15f),
                                    contentColor = Color(0xFF00B894)
                                ) {
                                    Text(defaultSpeed.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            },
                            onClick = {
                                val next = WheelViewModel.SpinSpeed.entries[(defaultSpeed.ordinal + 1) % WheelViewModel.SpinSpeed.entries.size]
                                viewModel.updateDefaultSpinSpeed(next)
                            }
                        )
                        Divider()
                        SettingsToggleRow(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            title = "Spin sound",
                            description = "Tick sound while spinning",
                            checked = spinSound,
                            onCheckedChange = { viewModel.updateSpinSoundEnabled(it) },
                            color = Color(0xFFFFA000)
                        )
                        Divider()
                        SettingsToggleRow(
                            icon = Icons.Filled.Celebration,
                            title = "Confetti on win",
                            description = "Celebrate every result",
                            checked = confetti,
                            onCheckedChange = { viewModel.updateConfettiEnabled(it) },
                            color = Color(0xFFE84393)
                        )
                        Divider()
                        SettingsToggleRow(
                            icon = Icons.Filled.PersonRemove,
                            title = "Remove after pick",
                            description = "Default for all new wheels",
                            checked = removeAfterPick,
                            onCheckedChange = { viewModel.updateRemoveAfterPickEnabled(it) },
                            color = Color.Gray
                        )
                    }
                }
            }

            // 3. About Section
            item {
                SectionHeader("ABOUT")
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Filled.StarOutline,
                            title = "Rate the app",
                            description = "Leave a review on Play Store",
                            color = colors.primary,
                            onClick = { 
                                val packageName = context.packageName
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "market://details?id=$packageName".toUri()
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = "https://play.google.com/store/apps/details?id=$packageName".toUri()
                                    }
                                    context.startActivity(webIntent)
                                }
                            }
                        )
                        Divider()
                        SettingsClickableRow(
                            icon = Icons.Filled.ChatBubbleOutline,
                            title = "Send feedback",
                            description = "Report bugs or suggest features",
                            color = Color(0xFF0984E3),
                            onClick = { 
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:support@roulette.com".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "Roulette App Feedback")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                                } catch (_: Exception) {
                                    // Handle cases where no email app is installed
                                }
                            }
                        )
                        Divider()
                        SettingsOptionRow(
                            icon = Icons.Filled.Info,
                            title = "App version",
                            description = "Up to date",
                            color = Color.Gray,
                            trailing = {
                                Text("v2.0.0", color = Color(0xFF00B894), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        )
                    }
                }
            }

            // 4. Danger Zone
            item {
                SectionHeader("DANGER ZONE")
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.clickable { showClearDataDialog = true },
                    color = Color(0xFFEF5350).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFEF5350).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.DeleteOutline, null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clear all data", fontWeight = FontWeight.Bold, color = Color(0xFFEF5350), fontSize = 16.sp)
                            Text("Deletes all wheels & history", color = Color(0xFFEF5350).copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFEF5350).copy(alpha = 0.3f))
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }

        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                containerColor = SurfaceDarker,
                title = { Text("Clear All Data?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently delete all your wheels and spin history. This action cannot be undone.", color = Color.Gray) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllData()
                            showClearDataDialog = false
                        }
                    ) {
                        Text("CLEAR", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) {
                        Text("CANCEL", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.padding(start = 8.dp))
}

@Composable
private fun PaletteItem(
    palette: RoulettePalette,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(16.dp))
                else Modifier
            )
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(palette.primary)
        )
        Spacer(Modifier.height(8.dp))
        Text(palette.name, color = if (isSelected) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsOptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
        trailing()
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color
) {
    SettingsOptionRow(
        icon = icon,
        title = title,
        description = description,
        color = color,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RouletteTheme.colors.primary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = SurfaceDarker,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    SettingsOptionRow(
        icon = icon,
        title = title,
        description = description,
        color = color,
        onClick = onClick,
        trailing = {
            Icon(Icons.Filled.ChevronRight, null, tint = Color.DarkGray)
        }
    )
}

@Composable
private fun Divider() {
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
}
