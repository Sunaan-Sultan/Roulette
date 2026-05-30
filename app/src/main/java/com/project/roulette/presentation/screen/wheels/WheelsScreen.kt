package com.project.roulette.presentation.screen.wheels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.presentation.model.HomeFilter
import com.project.roulette.presentation.model.HomeUiState
import com.project.roulette.presentation.viewmodel.HomeViewModel
import com.project.roulette.ui.theme.*
import com.project.roulette.util.loadInterstitial
import com.project.roulette.util.showInterstitial
import com.project.roulette.util.loadSwitchInterstitial
import com.project.roulette.util.showSwitchInterstitial
import com.project.roulette.presentation.screen.home.WheelCard

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(Unit) {
        loadInterstitial(context)
        loadSwitchInterstitial(context)
    }

    Scaffold(
        floatingActionButton = {
            Button(
                onClick = {
                    showInterstitial(context = context) {
                        onNavigateToCreate()
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RouletteTheme.colors.primary),
                modifier = Modifier
                    .height(56.dp)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Wheel", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = DeepNavyBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            // Header
            Text(
                text = "EXPLORE",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wheels",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    BadgedBox(
                        badge = {
                            if (uiState is HomeUiState.Success && (uiState as HomeUiState.Success).unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text((uiState as HomeUiState.Success).unreadNotificationCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchWheels(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Search wheels...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = RouletteTheme.colors.primary,
                    unfocusedContainerColor = SurfaceDark,
                    focusedContainerColor = SurfaceDark,
                    cursorColor = RouletteTheme.colors.primary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            // Statistics Row
            if (uiState is HomeUiState.Success) {
                val state = uiState as HomeUiState.Success
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = state.totalWheels.toString(),
                        label = "Total",
                        modifier = Modifier.weight(1f),
                        accentColor = TilePurple
                    )
                    StatCard(
                        value = state.totalSpins.toString(),
                        label = "Spins",
                        modifier = Modifier.weight(1f),
                        accentColor = TileTeal
                    )
                    StatCard(
                        value = state.spinsToday.toString(),
                        label = "Today",
                        modifier = Modifier.weight(1f),
                        accentColor = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WheelsFilterChipItem(
                    label = "All",
                    isSelected = currentFilter == HomeFilter.ALL,
                    onClick = { viewModel.setFilter(HomeFilter.ALL) }
                )
                WheelsFilterChipItem(
                    label = "Recent",
                    isSelected = currentFilter == HomeFilter.RECENT,
                    onClick = { viewModel.setFilter(HomeFilter.RECENT) }
                )
                WheelsFilterChipItem(
                    label = "Most used",
                    isSelected = currentFilter == HomeFilter.MOST_USED,
                    onClick = { viewModel.setFilter(HomeFilter.MOST_USED) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // List Content
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = RouletteTheme.colors.primary)
                        }
                    }

                    is HomeUiState.Success -> {
                        if (state.wheels.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(if (searchQuery.isNotEmpty()) "No results found" else "No wheels yet. Create one!", color = TextSecondary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(state.wheels) { wheel ->
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Error: ${state.message}", color = Color.Red)
                                Button(onClick = { viewModel.loadAllWheels() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = if (accentColor == TextSecondary) Color.White else accentColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun WheelsFilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) RouletteTheme.colors.primary else SurfaceDark,
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}
