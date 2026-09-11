package com.project.roulette.presentation.screen.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.roulette.ui.theme.ThemePalette
import com.project.roulette.util.WheelTemplate
import com.project.roulette.util.WheelTemplates
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.ui.theme.RouletteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onStartFromScratch: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back", tint = RouletteTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RouletteTheme.colors.background)
            )
        },
        containerColor = RouletteTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Start with a ready-made wheel — you can customize everything before saving.",
                color = RouletteTheme.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    StartFromScratchCard(onClick = onStartFromScratch)
                }

                itemsIndexed(WheelTemplates.all) { _, template ->
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RouletteTheme.shapes.card)
            .background(RouletteTheme.colors.surface)
            .border(1.dp, RouletteTheme.colors.divider, RouletteTheme.shapes.card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(RouletteTheme.colors.surfacePressed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(AppIcons.Add, contentDescription = null, tint = RouletteTheme.colors.textPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text("Start from scratch", color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text("Build your own wheel", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TemplateCard(
    template: WheelTemplate,
    onClick: () -> Unit
) {
    val accent = ThemePalette.getOrElse(template.paletteIndex) { ThemePalette[0] }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clip(RouletteTheme.shapes.card)
            .background(RouletteTheme.colors.surface)
            .border(1.dp, accent.copy(alpha = 0.25f), RouletteTheme.shapes.card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(accent.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(template.emoji, style = MaterialTheme.typography.headlineMedium)
        }
        Column {
            Text(template.name, color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(template.description, color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            Spacer(Modifier.height(6.dp))
            Text(
                "${template.segmentNames.size} options",
                color = accent,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
