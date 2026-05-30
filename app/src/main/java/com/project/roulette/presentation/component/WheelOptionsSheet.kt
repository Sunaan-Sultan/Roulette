package com.project.roulette.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.roulette.domain.model.Wheel
import com.project.roulette.ui.theme.DeepNavyBlack
import com.project.roulette.ui.theme.SurfaceDark

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
            .background(DeepNavyBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = onClose,
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(wheel.name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                Text("Options", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                        icon = Icons.Filled.Edit,
                        title = "Edit Wheel",
                        description = "Change names & weights",
                        color = Color(0xFF6C5CE7),
                        onClick = onEdit
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = Icons.Filled.ContentCopy,
                        title = "Duplicate Wheel",
                        description = "Clone with same segments",
                        color = Color(0xFF00B894),
                        onClick = onDuplicate
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = if (wheel.isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                        title = "Add to Favourites",
                        description = "Pin to your favourites tab",
                        color = Color(0xFFFFA000),
                        trailing = {
                            Switch(
                                checked = wheel.isFavorite,
                                onCheckedChange = { onToggleFavorite() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFFA000)
                                )
                            )
                        }
                    )
                }
            }

            item {
                OptionsGroup(title = "DATA") {
                    OptionItem(
                        icon = Icons.Filled.History,
                        title = "Spin History",
                        description = "View all past spins",
                        color = Color(0xFF0984E3),
                        onClick = onHistory
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = Icons.Filled.BarChart,
                        title = "Statistics",
                        description = "Win rates & fairness score",
                        color = Color(0xFFA29BFE),
                        onClick = onStatistics
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = Icons.Filled.FileDownload,
                        title = "Export Data",
                        description = "Download history as CSV",
                        color = Color(0xFF00B894),
                        onClick = onExport
                    )
                }
            }

            item {
                OptionsGroup(title = "DANGER ZONE") {
                    OptionItem(
                        icon = Icons.Filled.Refresh,
                        title = "Reset Wheel",
                        description = "Clears spin history & counts",
                        color = Color(0xFFEF5350),
                        onClick = onReset,
                        titleColor = Color(0xFFEF5350)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionItem(
                        icon = Icons.Filled.DeleteOutline,
                        title = "Delete Wheel",
                        description = "Permanently remove this wheel",
                        color = Color(0xFFEF5350),
                        onClick = onDelete,
                        titleColor = Color(0xFFEF5350)
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
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun OptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: (() -> Unit)? = null,
    titleColor: Color = Color.White,
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
            Text(title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Filled.ChevronRight, null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
        }
    }
}
