package com.project.roulette.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.roulette.domain.model.Wheel
import com.project.roulette.ui.theme.RouletteTheme
import androidx.compose.ui.graphics.painter.Painter

/**
 * Full-screen modal content for wheel options.
 */
@Composable
fun WheelOptionsSheet(
    wheel: Wheel,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHistory: () -> Unit,
    onStatistics: () -> Unit,
    onExport: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouletteTheme.colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = onClose,
                color = RouletteTheme.colors.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(AppIcons.Close, contentDescription = "Close", tint = RouletteTheme.colors.textPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(wheel.name.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textSecondary, letterSpacing = 1.sp)
                Text("Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary)
            }
        }

        Spacer(Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                OptionsGroup(title = "WHEEL") {
                    OptionItem(
                        icon = AppIcons.Edit,
                        title = "Edit Wheel",
                        description = "Change names & weights",
                        color = RouletteTheme.colors.primary,
                        onClick = onEdit
                    )
                    HorizontalDivider(color = RouletteTheme.colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = AppIcons.ContentCopy,
                        title = "Duplicate Wheel",
                        description = "Clone with same segments",
                        color = RouletteTheme.colors.success,
                        onClick = onDuplicate
                    )
                    HorizontalDivider(color = RouletteTheme.colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = if (wheel.isFavorite) AppIcons.StarFilled else AppIcons.Star,
                        title = "Add to Favourites",
                        description = "Pin to your favourites tab",
                        color = RouletteTheme.colors.warning,
                        trailing = {
                            Switch(
                                checked = wheel.isFavorite,
                                onCheckedChange = { onToggleFavorite() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RouletteTheme.colors.textPrimary,
                                    checkedTrackColor = RouletteTheme.colors.primary
                                )
                            )
                        }
                    )
                }
            }

            item {
                OptionsGroup(title = "DATA") {
                    OptionItem(
                        icon = AppIcons.History,
                        title = "Spin History",
                        description = "View all past spins",
                        color = RouletteTheme.colors.info,
                        onClick = onHistory
                    )
                    HorizontalDivider(color = RouletteTheme.colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = AppIcons.BarChart,
                        title = "Statistics",
                        description = "Win rates & fairness score",
                        color = RouletteTheme.colors.primary,
                        onClick = onStatistics
                    )
                    HorizontalDivider(color = RouletteTheme.colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = AppIcons.Download,
                        title = "Export Data",
                        description = "Download history as CSV",
                        color = RouletteTheme.colors.success,
                        onClick = onExport
                    )
                }
            }

            item {
                OptionsGroup(title = "DANGER ZONE") {
                    OptionItem(
                        icon = AppIcons.Refresh,
                        title = "Reset Wheel",
                        description = "Clears spin history & counts",
                        color = RouletteTheme.colors.danger,
                        onClick = onReset,
                        titleColor = RouletteTheme.colors.danger
                    )
                    HorizontalDivider(color = RouletteTheme.colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = AppIcons.Delete,
                        title = "Delete Wheel",
                        description = "Permanently remove this wheel",
                        color = RouletteTheme.colors.danger,
                        onClick = onDelete,
                        titleColor = RouletteTheme.colors.danger
                    )
                }
            }
        }
    }
}

@Composable
fun OptionsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = RouletteTheme.colors.textSecondary,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Surface(
            color = RouletteTheme.colors.surface,
            shape = RouletteTheme.shapes.card,
            border = BorderStroke(1.dp, RouletteTheme.colors.divider)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun OptionItem(
    icon: Painter,
    title: String,
    description: String,
    color: Color,
    onClick: (() -> Unit)? = null,
    titleColor: Color = RouletteTheme.colors.textPrimary,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
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
            Text(title, color = titleColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(description, color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(AppIcons.ChevronRight, null, tint = RouletteTheme.colors.textTertiary, modifier = Modifier.size(20.dp))
        }
    }
}
