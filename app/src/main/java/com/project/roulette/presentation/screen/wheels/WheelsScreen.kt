package com.project.roulette.presentation.screen.wheels

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.WheelCard
import com.project.roulette.presentation.component.design.AppFilterChip
import com.project.roulette.presentation.component.design.AppLargeHeader
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.AppSearchField
import com.project.roulette.presentation.component.design.EmptyState
import com.project.roulette.presentation.component.design.NotificationBellButton
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.PrimaryFab
import com.project.roulette.presentation.component.design.StatCard
import com.project.roulette.presentation.component.design.StatCardStyle
import com.project.roulette.presentation.model.HomeFilter
import com.project.roulette.presentation.model.HomeUiState
import com.project.roulette.presentation.viewmodel.HomeViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.ui.theme.TileTeal
import com.project.roulette.util.loadInterstitial
import com.project.roulette.util.loadSwitchInterstitial
import com.project.roulette.util.showInterstitial
import com.project.roulette.util.showSwitchInterstitial

@Composable
fun WheelsScreen(
    viewModel: HomeViewModel,
    onNavigateToWheel: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens
    val spinsAccent = rememberAccentOnSurface(TileTeal)

    LaunchedEffect(Unit) {
        loadInterstitial(context)
        loadSwitchInterstitial(context)
    }

    AppScaffold(
        floatingActionButton = {
            PrimaryFab(
                text = "Create wheel",
                icon = AppIcons.Add,
                onClick = { showInterstitial(context = context) { onNavigateToCreate() } },
                modifier = Modifier.padding(bottom = dimens.bottomBarSpace)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AppLargeHeader(
                eyebrow = "Explore",
                title = "Wheels",
                trailing = {
                    NotificationBellButton(
                        unreadCount = (uiState as? HomeUiState.Success)?.unreadNotificationCount ?: 0,
                        onClick = onNavigateToNotifications
                    )
                }
            )

            Column(modifier = Modifier.padding(horizontal = dimens.screenPadding)) {
                AppSearchField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchWheels(it) },
                    placeholder = "Search wheels"
                )

                (uiState as? HomeUiState.Success)?.let { state ->
                    Spacer(Modifier.size(dimens.space16))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.space12)
                    ) {
                        StatCard(
                            value = state.totalWheels.toString(),
                            label = "Total",
                            accent = colors.primary,
                            style = StatCardStyle.Tinted,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = state.totalSpins.toString(),
                            label = "Spins",
                            accent = spinsAccent,
                            style = StatCardStyle.Tinted,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = state.spinsToday.toString(),
                            label = "Today",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.size(dimens.space16))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.space8)
                ) {
                    AppFilterChip(
                        label = "All",
                        selected = currentFilter == HomeFilter.ALL,
                        onClick = { viewModel.setFilter(HomeFilter.ALL) }
                    )
                    AppFilterChip(
                        label = "Recent",
                        selected = currentFilter == HomeFilter.RECENT,
                        onClick = { viewModel.setFilter(HomeFilter.RECENT) }
                    )
                    AppFilterChip(
                        label = "Most used",
                        selected = currentFilter == HomeFilter.MOST_USED,
                        onClick = { viewModel.setFilter(HomeFilter.MOST_USED) }
                    )
                }
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
                        if (state.wheels.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (searchQuery.isNotEmpty()) {
                                    EmptyState(
                                        icon = AppIcons.Search,
                                        title = "No results",
                                        message = "No wheels match that search. Try another term."
                                    )
                                } else {
                                    EmptyState(
                                        icon = AppIcons.Wheel,
                                        title = "No wheels yet",
                                        message = "Create your first wheel and start spinning.",
                                        actionLabel = "Create wheel",
                                        onAction = {
                                            showInterstitial(context = context) { onNavigateToCreate() }
                                        }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(dimens.space12),
                                contentPadding = PaddingValues(
                                    start = dimens.screenPadding,
                                    end = dimens.screenPadding,
                                    bottom = dimens.listBottomPadding + dimens.fabSize +
                                        dimens.bottomBarSpace
                                )
                            ) {
                                items(state.wheels, key = { it.id }) { wheel ->
                                    WheelCard(
                                        wheel = wheel,
                                        isActive = wheel.id == state.selectedWheelId,
                                        onSelect = {
                                            showSwitchInterstitial(context) {
                                                viewModel.selectWheel(wheel.id)
                                                onNavigateToWheel(wheel.id)
                                            }
                                        },
                                        onToggleFavorite = {
                                            viewModel.toggleFavorite(wheel.id, wheel.isFavorite)
                                        },
                                        onDelete = { viewModel.deleteWheel(wheel.id) }
                                    )
                                }
                            }
                        }
                    }

                    is HomeUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(dimens.space32)
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.danger
                                )
                                Spacer(Modifier.size(dimens.space16))
                                PrimaryButton(
                                    text = "Retry",
                                    onClick = { viewModel.loadAllWheels() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
