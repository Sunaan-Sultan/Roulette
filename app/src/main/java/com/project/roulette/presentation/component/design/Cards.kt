package com.project.roulette.presentation.component.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.roulette.ui.theme.RouletteTheme

enum class StatCardStyle { Plain, Tinted }

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = RouletteTheme.colors.primary,
    style: StatCardStyle = StatCardStyle.Plain,
    icon: Painter? = null,
    height: Dp? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val tintAlpha = if (colors.isLight) 0.08f else 0.12f

    Surface(
        modifier = modifier
            .then(if (height != null) Modifier.height(height) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RouletteTheme.shapes.card,
        color = when (style) {
            StatCardStyle.Plain -> colors.surface
            StatCardStyle.Tinted -> accent.copy(alpha = tintAlpha)
        },
        border = BorderStroke(
            dimens.borderWidth,
            when (style) {
                StatCardStyle.Plain -> colors.divider
                StatCardStyle.Tinted -> accent.copy(alpha = 0.22f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(dimens.space16),
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(dimens.iconSizeSmall)
                )
                Spacer(Modifier.size(dimens.space8))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = if (style == StatCardStyle.Tinted) accent else colors.textPrimary
            )
            Spacer(Modifier.size(dimens.space2))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RouletteTheme.shapes.chip,
        color = if (selected) colors.primary else colors.surface,
        border = BorderStroke(
            dimens.borderWidth,
            if (selected) colors.primary else colors.divider
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimens.space16, vertical = dimens.space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) colors.onPrimary else colors.textSecondary
            )
            if (count != null) {
                Spacer(Modifier.width(dimens.space4))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) colors.onPrimary.copy(alpha = 0.7f) else colors.textTertiary
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: Painter,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimens.space32),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(colors.primarySubtle, RouletteTheme.shapes.card),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.size(dimens.space16))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(dimens.space4))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.size(dimens.space20))
            Button(
                onClick = onAction,
                shape = RouletteTheme.shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = dimens.space24, vertical = dimens.space12)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
