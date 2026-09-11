package com.project.roulette.presentation.component.design

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.ui.theme.RouletteTheme

@Composable
fun appTextFieldColors(accent: Color = RouletteTheme.colors.primary): TextFieldColors {
    val colors = RouletteTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent,
        unfocusedBorderColor = colors.divider,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        cursorColor = accent,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedPlaceholderColor = colors.textTertiary,
        unfocusedPlaceholderColor = colors.textTertiary,
        focusedLabelColor = accent,
        unfocusedLabelColor = colors.textSecondary,
        focusedSupportingTextColor = colors.textSecondary,
        unfocusedSupportingTextColor = colors.textSecondary
    )
}

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search"
) {
    val colors = RouletteTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(RouletteTheme.dimens.textFieldHeight),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary
            )
        },
        leadingIcon = {
            Icon(
                painter = AppIcons.Search,
                contentDescription = null,
                tint = colors.textSecondary
            )
        },
        textStyle = TextStyle(color = colors.textPrimary).merge(MaterialTheme.typography.bodyMedium),
        shape = RouletteTheme.shapes.textField,
        colors = appTextFieldColors(),
        singleLine = true
    )
}
