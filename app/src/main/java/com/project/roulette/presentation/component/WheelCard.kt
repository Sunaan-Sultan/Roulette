package com.project.roulette.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.component.design.Pill
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.TileBlue
import com.project.roulette.ui.theme.TileAmber
import com.project.roulette.ui.theme.TileGreen
import com.project.roulette.ui.theme.TilePink
import com.project.roulette.ui.theme.TilePurple
import com.project.roulette.ui.theme.TileTeal
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.util.TimeUtils

val WheelAccentPalette = listOf(TilePurple, TileTeal, TileBlue, TilePink, TileAmber, TileGreen)

fun wheelAccentFor(id: String) =
    WheelAccentPalette[(id.hashCode().let { if (it < 0) -it else it }) % WheelAccentPalette.size]

@Composable
fun WheelCard(
    wheel: Wheel,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val accent = rememberAccentOnSurface(wheelAccentFor(wheel.id))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RouletteTheme.shapes.card,
        color = colors.surface,
        border = BorderStroke(
            dimens.borderWidth,
            if (isActive) colors.primaryBorder else colors.divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimens.space16, top = dimens.space12, bottom = dimens.space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RouletteTheme.shapes.thumbnail)
                    .background(accent.copy(alpha = if (colors.isLight) 0.12f else 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = AppIcons.Wheel,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(dimens.iconSize)
                )
            }

            Spacer(Modifier.width(dimens.rowIconGap))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wheel.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wheel.segments.size} segments · ${TimeUtils.getRelativeTime(wheel.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isActive) {
                Spacer(Modifier.width(dimens.space8))
                Pill(text = "Active", accent = colors.primary)
            }

            WheelOptionsMenu(
                isFavorite = wheel.isFavorite,
                onToggleFavorite = onToggleFavorite,
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun WheelOptionsMenu(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RouletteTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = AppIcons.MoreVert,
                contentDescription = "Wheel options",
                tint = colors.textSecondary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.surfaceElevated,
            shape = RouletteTheme.shapes.cardSmall
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (isFavorite) "Unfavourite" else "Favourite",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                },
                onClick = {
                    onToggleFavorite()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        painter = if (isFavorite) AppIcons.FavoriteFilled else AppIcons.Favorite,
                        contentDescription = null,
                        tint = if (isFavorite) colors.danger else colors.textSecondary
                    )
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.danger
                    )
                },
                onClick = {
                    onDelete()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        painter = AppIcons.Delete,
                        contentDescription = null,
                        tint = colors.danger
                    )
                }
            )
        }
    }
}
