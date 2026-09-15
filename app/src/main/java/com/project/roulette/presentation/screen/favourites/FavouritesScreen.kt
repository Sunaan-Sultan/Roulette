package com.project.roulette.presentation.screen.favourites

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.WheelOptionsMenu
import com.project.roulette.presentation.component.wheelAccentFor
import com.project.roulette.presentation.component.design.AppFilterChip
import com.project.roulette.presentation.component.design.AppLargeHeader
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.EmptyState
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.model.HomeFilter
import com.project.roulette.presentation.model.HomeUiState
import com.project.roulette.presentation.viewmodel.HomeViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.rememberAccentOnSurface

@Composable
fun FavouritesScreen(
    viewModel: HomeViewModel,
    onNavigateToWheel: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    LaunchedEffect(Unit) {
        viewModel.setFilter(HomeFilter.FAVOURITES)
    }

    AppScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AppLargeHeader(eyebrow = "Saved", title = "Favourites")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.space8)
            ) {
                AppFilterChip(
                    label = "All",
                    selected = currentFilter == HomeFilter.FAVOURITES,
                    onClick = { viewModel.setFilter(HomeFilter.FAVOURITES) }
                )
                AppFilterChip(
                    label = "Recent",
                    selected = currentFilter == HomeFilter.FAVOURITES_RECENT,
                    onClick = { viewModel.setFilter(HomeFilter.FAVOURITES_RECENT) }
                )
                AppFilterChip(
                    label = "Most used",
                    selected = currentFilter == HomeFilter.FAVOURITES_MOST_USED,
                    onClick = { viewModel.setFilter(HomeFilter.FAVOURITES_MOST_USED) }
                )
            }

            Spacer(Modifier.size(dimens.space16))

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }

                    is HomeUiState.Success -> {
                        val favouriteWheels = state.wheels.filter { it.isFavorite }
                        if (favouriteWheels.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                EmptyState(
                                    icon = AppIcons.Favorite,
                                    title = "No favourites yet",
                                    message = "Mark a wheel as favourite and it will show up here."
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(dimens.space12),
                                contentPadding = PaddingValues(
                                    start = dimens.screenPadding,
                                    end = dimens.screenPadding,
                                    bottom = dimens.listBottomPadding + dimens.bottomBarSpace
                                )
                            ) {
                                itemsIndexed(favouriteWheels, key = { _, it -> it.id }) { index, wheel ->
                                    val spinCount = state.wheelSpinCounts[wheel.id] ?: 0
                                    val onSelect = { onNavigateToWheel(wheel.id) }
                                    val onToggleFavorite = {
                                        viewModel.toggleFavorite(wheel.id, wheel.isFavorite)
                                    }
                                    val onDelete = { viewModel.deleteWheel(wheel.id) }
                                    if (index == 0) {
                                        FeaturedFavouriteCard(
                                            wheel = wheel,
                                            spinCount = spinCount,
                                            onSelect = onSelect,
                                            onToggleFavorite = onToggleFavorite,
                                            onDelete = onDelete
                                        )
                                    } else {
                                        SmallFavouriteCard(
                                            wheel = wheel,
                                            spinCount = spinCount,
                                            onSelect = onSelect,
                                            onToggleFavorite = onToggleFavorite,
                                            onDelete = onDelete
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is HomeUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.danger
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedFavouriteCard(
    wheel: Wheel,
    spinCount: Int,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RouletteTheme.shapes.card,
        color = colors.surface,
        border = BorderStroke(dimens.borderWidth, colors.primaryBorder)
    ) {
        Column(modifier = Modifier.padding(dimens.space20)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RouletteTheme.shapes.thumbnail)
                        .background(colors.primarySubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = AppIcons.Wheel,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                WheelOptionsMenu(
                    isFavorite = wheel.isFavorite,
                    onToggleFavorite = onToggleFavorite,
                    onDelete = onDelete
                )
            }

            Spacer(Modifier.size(dimens.space16))

            Text(
                text = wheel.name,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.size(dimens.space2))
            Text(
                text = "${wheel.segments.size} segments · used $spinCount times",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(Modifier.size(dimens.space20))

            PrimaryButton(
                text = "Spin now",
                icon = AppIcons.Casino,
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SmallFavouriteCard(
    wheel: Wheel,
    spinCount: Int,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val accent = rememberAccentOnSurface(wheelAccentFor(wheel.id))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RouletteTheme.shapes.card,
        color = colors.surface,
        border = BorderStroke(dimens.borderWidth, colors.divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimens.space16,
                    top = dimens.space12,
                    bottom = dimens.space12
                ),
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
                    text = "${wheel.segments.size} segments · $spinCount spins",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            WheelOptionsMenu(
                isFavorite = wheel.isFavorite,
                onToggleFavorite = onToggleFavorite,
                onDelete = onDelete
            )
        }
    }
}
