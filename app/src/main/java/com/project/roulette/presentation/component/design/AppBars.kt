package com.project.roulette.presentation.component.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.ui.theme.RouletteTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = RouletteTheme.colors
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                if (eyebrow != null) {
                    Text(
                        text = eyebrow.uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            }
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = AppIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.background,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
            actionIconContentColor = colors.textPrimary
        )
    )
}

@Composable
fun AppLargeHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.screenPadding, vertical = dimens.space12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        containerColor = RouletteTheme.colors.background,
        contentColor = RouletteTheme.colors.textPrimary,
        content = content
    )
}
