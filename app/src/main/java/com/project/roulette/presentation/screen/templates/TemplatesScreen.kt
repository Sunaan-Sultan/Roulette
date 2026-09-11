package com.project.roulette.presentation.screen.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppTopBar
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.ThemePalette
import com.project.roulette.util.WheelTemplate
import com.project.roulette.util.WheelTemplates

@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onStartFromScratch: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Templates",
                eyebrow = "New wheel",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Start with a ready-made wheel — you can customize everything before saving.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(
                    horizontal = dimens.screenPadding,
                    vertical = dimens.space12
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(dimens.space12),
                verticalArrangement = Arrangement.spacedBy(dimens.space12),
                contentPadding = PaddingValues(
                    start = dimens.screenPadding,
                    end = dimens.screenPadding,
                    bottom = dimens.listBottomPadding
                )
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    StartFromScratchCard(onClick = onStartFromScratch)
                }

                items(WheelTemplates.all, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { onSelectTemplate(template.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StartFromScratchCard(onClick: () -> Unit) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RouletteTheme.shapes.card,
        color = colors.surface,
        border = BorderStroke(dimens.borderWidth, colors.divider)
    ) {
        Row(
            modifier = Modifier.padding(dimens.space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.primarySubtle, RouletteTheme.shapes.avatar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = AppIcons.Add,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(dimens.iconSize)
                )
            }
            Spacer(Modifier.width(dimens.rowIconGap))
            Column {
                Text(
                    text = "Start from scratch",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "Build your own wheel",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: WheelTemplate,
    onClick: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val accent = ThemePalette.getOrElse(template.paletteIndex) { ThemePalette[0] }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clickable(onClick = onClick),
        shape = RouletteTheme.shapes.card,
        color = colors.surface,
        border = BorderStroke(dimens.borderWidth, accent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(dimens.space16),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accent.copy(alpha = 0.15f), RouletteTheme.shapes.avatar),
                contentAlignment = Alignment.Center
            ) {
                Text(template.emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.size(dimens.space16))
            Column {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.size(dimens.space2))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.size(dimens.space8))
                Text(
                    text = "${template.segmentNames.size} options",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
        }
    }
}
