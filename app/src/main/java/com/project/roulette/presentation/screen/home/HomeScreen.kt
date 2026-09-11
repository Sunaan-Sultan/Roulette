package com.project.roulette.presentation.screen.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.design.AppLargeHeader
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.GroupedCard
import com.project.roulette.presentation.component.design.NotificationBellButton
import com.project.roulette.presentation.component.design.PrimaryButton
import com.project.roulette.presentation.component.design.SectionHeader
import com.project.roulette.presentation.component.design.StatCard
import com.project.roulette.presentation.component.design.StatCardStyle
import com.project.roulette.presentation.model.DashboardUiState
import com.project.roulette.presentation.viewmodel.DashboardViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.TileBlue
import com.project.roulette.ui.theme.TileTeal
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.util.loadInterstitial
import com.project.roulette.util.loadSwitchInterstitial
import com.project.roulette.util.showInterstitial
import com.project.roulette.util.showSwitchInterstitial
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: DashboardViewModel,
    onNavigateToWheel: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToHistory: (String) -> Unit,
    onNavigateToAllWheels: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartType by viewModel.chartType.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    val weekAccent = rememberAccentOnSurface(TileTeal)
    val bestDayAccent = rememberAccentOnSurface(TileBlue)

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    LaunchedEffect(Unit) {
        loadInterstitial(context)
        loadSwitchInterstitial(context)
    }

    val openWheel: (String) -> Unit = { wheelId ->
        showSwitchInterstitial(context) { onNavigateToWheel(wheelId) }
    }
    val createWheel: () -> Unit = {
        showInterstitial(context = context) { onNavigateToCreate() }
    }

    AppScaffold { padding ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
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
                        PrimaryButton(text = "Create wheel", onClick = createWheel)
                    }
                }
            }

            is DashboardUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        bottom = dimens.listBottomPadding + dimens.bottomBarSpace
                    ),
                    verticalArrangement = Arrangement.spacedBy(dimens.space16)
                ) {
                    item {
                        AppLargeHeader(
                            eyebrow = greeting,
                            title = "Wheel of Names",
                            trailing = {
                                NotificationBellButton(
                                    unreadCount = state.unreadNotificationCount,
                                    onClick = onNavigateToNotifications
                                )
                            }
                        )
                    }

                    if (!state.hasWheels) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = dimens.screenPadding)) {
                                WelcomeCard(onCreate = createWheel)
                            }
                        }
                    } else {
                        item {
                            Column {
                                SectionHeader(
                                    text = "Your wheels",
                                    trailing = {
                                        TextButton(onClick = onNavigateToAllWheels) {
                                            Text(
                                                text = "See all",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = colors.primary
                                            )
                                        }
                                    }
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(
                                        horizontal = dimens.screenPadding
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(dimens.space12)
                                ) {
                                    item {
                                        CreateWheelTile(onClick = createWheel)
                                    }
                                    items(state.quickWheels, key = { it.id }) { wheel ->
                                        QuickWheelCard(
                                            wheel = wheel,
                                            onClick = { openWheel(wheel.id) }
                                        )
                                    }
                                }
                            }
                        }

                        if (!state.hasSpins) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = dimens.screenPadding)) {
                                    FirstSpinCard(
                                        onSpin = state.quickWheels.firstOrNull()?.let { wheel ->
                                            { openWheel(wheel.id) }
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            Box(modifier = Modifier.padding(horizontal = dimens.screenPadding)) {
                                ActivityHeroCard(state)
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = dimens.screenPadding),
                                horizontalArrangement = Arrangement.spacedBy(dimens.space12)
                            ) {
                                StatCard(
                                    value = state.totalWheels.toString(),
                                    label = if (state.totalWheels == 1) "Wheel" else "Wheels",
                                    accent = colors.primary,
                                    style = StatCardStyle.Tinted,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    value = String.format(
                                        Locale.US,
                                        "%.1f",
                                        state.spinsThisWeek / 7f
                                    ),
                                    label = "Avg / day",
                                    accent = weekAccent,
                                    style = StatCardStyle.Tinted,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    value = state.bestDayCount.toString(),
                                    label = "Best day",
                                    accent = bestDayAccent,
                                    style = StatCardStyle.Tinted,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (state.wheelShare.isNotEmpty()) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = dimens.screenPadding)) {
                                    SpinShareCard(
                                        slices = state.wheelShare,
                                        totalSpins = state.totalSpins,
                                        chartType = chartType,
                                        onToggle = { viewModel.toggleChartType() },
                                        onSliceClick = openWheel
                                    )
                                }
                            }
                        }

                        if (state.topPicks.isNotEmpty()) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = dimens.screenPadding)) {
                                    TopPicksCard(state.topPicks)
                                }
                            }
                        }

                        if (state.recentSpins.isNotEmpty()) {
                            item {
                                val feed = state.recentSpins.take(8)
                                Column {
                                    SectionHeader(text = "Recent spins")
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = dimens.screenPadding)
                                    ) {
                                        GroupedCard {
                                            feed.forEachIndexed { index, item ->
                                                RecentSpinRow(
                                                    item = item,
                                                    onClick = { onNavigateToHistory(item.wheelId) },
                                                    showDivider = index != feed.lastIndex
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
