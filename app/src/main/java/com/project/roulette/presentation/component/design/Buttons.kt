package com.project.roulette.presentation.component.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.contentColorOn

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    containerColor: Color = RouletteTheme.colors.primary,
    contentColor: Color = contentColorOn(containerColor),
    height: Dp = RouletteTheme.dimens.buttonHeight
) {
    val dimens = RouletteTheme.dimens
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RouletteTheme.shapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = dimens.space20)
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(dimens.iconSizeSmall)
            )
            Spacer(Modifier.width(dimens.space8))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    contentColor: Color = RouletteTheme.colors.textSecondary,
    height: Dp = RouletteTheme.dimens.buttonHeightSmall
) {
    val dimens = RouletteTheme.dimens
    TextButton(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RouletteTheme.shapes.button,
        colors = ButtonDefaults.textButtonColors(
            containerColor = RouletteTheme.colors.surface,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = dimens.space16)
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(dimens.iconSizeSmall)
            )
            Spacer(Modifier.width(dimens.space8))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun PrimaryFab(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Button(
        onClick = onClick,
        modifier = modifier.height(dimens.fabSize),
        shape = RouletteTheme.shapes.chip,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = dimens.space24)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(dimens.iconSize)
            )
            Spacer(Modifier.width(dimens.space8))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
