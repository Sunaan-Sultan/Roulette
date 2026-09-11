package com.project.roulette.presentation.component.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.ui.theme.RouletteTheme
import java.util.Locale

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val dimens = RouletteTheme.dimens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = dimens.rowHorizontalPadding, end = dimens.rowHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelMedium,
            color = RouletteTheme.colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
    Spacer(Modifier.size(dimens.headerToCard))
}

@Composable
fun GroupedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RouletteTheme.shapes.card,
    color: Color = RouletteTheme.colors.surface,
    border: BorderStroke? = BorderStroke(
        RouletteTheme.dimens.borderWidth,
        RouletteTheme.colors.divider
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = color,
        border = border
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    footnote: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimens = RouletteTheme.dimens
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) SectionHeader(title)
        GroupedCard(content = content)
        if (footnote != null) {
            Spacer(Modifier.size(dimens.space8))
            Text(
                text = footnote,
                style = MaterialTheme.typography.bodySmall,
                color = RouletteTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = dimens.rowHorizontalPadding)
            )
        }
    }
}

@Composable
fun InsetDivider(startInset: Dp = RouletteTheme.dimens.dividerInset) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startInset),
        thickness = RouletteTheme.dimens.dividerThickness,
        color = RouletteTheme.colors.divider
    )
}

@Composable
fun Chevron(tint: Color = RouletteTheme.colors.textTertiary) {
    Icon(
        painter = AppIcons.ChevronRight,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(RouletteTheme.dimens.chevronSize)
    )
}

@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: Painter? = null,
    leadingIconTint: Color = RouletteTheme.colors.textSecondary,
    leadingIconContainer: Color? = null,
    value: String? = null,
    valueColor: Color = RouletteTheme.colors.textSecondary,
    titleColor: Color = RouletteTheme.colors.textPrimary,
    showChevron: Boolean? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    minHeight: Dp = RouletteTheme.dimens.rowHeight,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val isToggle = checked != null
    val chevron = showChevron ?: (onClick != null && !isToggle && trailing == null)

    val rowClick: (() -> Unit)? = when {
        isToggle && enabled && onCheckedChange != null -> {
            { onCheckedChange(!checked!!) }
        }
        enabled -> onClick
        else -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (rowClick != null) Modifier.clickable(onClick = rowClick) else Modifier)
            .defaultMinSize(minHeight = minHeight)
            .padding(
                horizontal = dimens.rowHorizontalPadding,
                vertical = dimens.rowVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            if (leadingIconContainer != null) {
                Box(
                    modifier = Modifier
                        .size(dimens.iconTileSize)
                        .clip(RouletteTheme.shapes.iconTile)
                        .background(leadingIconContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = leadingIcon,
                        contentDescription = null,
                        tint = leadingIconTint,
                        modifier = Modifier.size(dimens.iconSizeSmall)
                    )
                }
            } else {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    tint = leadingIconTint,
                    modifier = Modifier.size(dimens.iconSize)
                )
            }
            Spacer(Modifier.width(dimens.rowIconGap))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) titleColor else colors.textTertiary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        when {
            trailing != null -> {
                Spacer(Modifier.width(dimens.space12))
                trailing()
            }
            isToggle -> {
                Spacer(Modifier.width(dimens.space12))
                Switch(
                    checked = checked!!,
                    onCheckedChange = if (enabled) onCheckedChange else null,
                    enabled = enabled
                )
            }
            else -> {
                if (value != null) {
                    Spacer(Modifier.width(dimens.space12))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelLarge,
                        color = valueColor
                    )
                }
                if (chevron) {
                    Spacer(Modifier.width(dimens.space4))
                    Chevron()
                }
            }
        }
    }
}
