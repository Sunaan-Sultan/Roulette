package com.project.roulette.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.model.HomeFilter
import com.project.roulette.presentation.model.HomeUiState
import com.project.roulette.presentation.viewmodel.HomeViewModel
import com.project.roulette.ui.theme.*
import com.project.roulette.util.TimeUtils
import com.project.roulette.util.loadInterstitial
import com.project.roulette.util.showInterstitial
import com.project.roulette.util.loadSwitchInterstitial
import com.project.roulette.util.showSwitchInterstitial
import com.project.roulette.presentation.component.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
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
        // Reset filter to ALL when coming to Home
        viewModel.setFilter(HomeFilter.ALL)
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
                    Icon(AppIcons.Add, contentDescription = null, tint = RouletteTheme.colors.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Wheel", fontWeight = FontWeight.Bold, color = RouletteTheme.colors.onPrimary)
                }
            }
        },
        containerColor = RouletteTheme.colors.background
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
                text = "MY COLLECTION",
                style = MaterialTheme.typography.labelLarge,
                color = RouletteTheme.colors.textSecondary,
                letterSpacing = 1.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wheel of Names",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = RouletteTheme.colors.textPrimary
                )
                
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RouletteTheme.colors.surface)
                ) {
                    BadgedBox(
                        badge = {
                            if (uiState is HomeUiState.Success && (uiState as HomeUiState.Success).unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = RouletteTheme.colors.danger,
                                    contentColor = RouletteTheme.colors.textPrimary
                                ) {
                                    Text((uiState as HomeUiState.Success).unreadNotificationCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            AppIcons.Notifications,
                            contentDescription = "Notifications",
                            tint = RouletteTheme.colors.textPrimary
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
                placeholder = { Text("Search wheels...", color = RouletteTheme.colors.textSecondary) },
                leadingIcon = { Icon(AppIcons.Search, contentDescription = null, tint = RouletteTheme.colors.textSecondary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = RouletteTheme.colors.primary,
                    unfocusedContainerColor = RouletteTheme.colors.surface,
                    focusedContainerColor = RouletteTheme.colors.surface,
                    cursorColor = RouletteTheme.colors.primary,
                    focusedTextColor = RouletteTheme.colors.textPrimary,
                    unfocusedTextColor = RouletteTheme.colors.textPrimary
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChipItem(
                    label = "All",
                    isSelected = currentFilter == HomeFilter.ALL,
                    onClick = { viewModel.setFilter(HomeFilter.ALL) }
                )
                FilterChipItem(
                    label = "Recent",
                    isSelected = currentFilter == HomeFilter.RECENT,
                    onClick = { viewModel.setFilter(HomeFilter.RECENT) }
                )
                FilterChipItem(
                    label = "Favourites",
                    isSelected = currentFilter == HomeFilter.FAVOURITES,
                    onClick = { viewModel.setFilter(HomeFilter.FAVOURITES) }
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
                                Text(if (searchQuery.isNotEmpty()) "No results found" else "No wheels yet. Create one!", color = RouletteTheme.colors.textSecondary)
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
                                Text("Error: ${state.message}", color = RouletteTheme.colors.danger)
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
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RouletteTheme.shapes.chip,
        color = if (isSelected) RouletteTheme.colors.primary else RouletteTheme.colors.surface,
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) RouletteTheme.colors.textPrimary else RouletteTheme.colors.textSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun WheelCard(
    wheel: Wheel,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    isActive: Boolean = false
) {
    val accentColors = listOf(TilePurple, TileTeal, TileCoral, TileBlue, TilePink, TileOrange)
    val accentColor = accentColors[wheel.id.hashCode().let { if (it < 0) -it else it } % accentColors.size]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RouletteTheme.shapes.card,
        color = RouletteTheme.colors.surface,
        border = if (isActive) BorderStroke(1.dp, RouletteTheme.colors.primary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    AppIcons.Wheel,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wheel.name,
                    color = RouletteTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wheel.segments.size} segments • ${TimeUtils.getRelativeTime(wheel.updatedAt)}",
                    color = RouletteTheme.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RouletteTheme.colors.primary.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Active",
                        color = RouletteTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = wheel.segments.size.toString(),
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(AppIcons.MoreVert, contentDescription = "Menu", tint = RouletteTheme.colors.textSecondary)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(RouletteTheme.colors.surfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (wheel.isFavorite) "Unfavourite" else "Favourite", color = RouletteTheme.colors.textPrimary) },
                        onClick = {
                            onToggleFavorite()
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                if (wheel.isFavorite) AppIcons.FavoriteFilled else AppIcons.Favorite,
                                contentDescription = null,
                                tint = if (wheel.isFavorite) RouletteTheme.colors.danger else RouletteTheme.colors.textPrimary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = RouletteTheme.colors.danger) },
                        onClick = {
                            onDelete()
                            expanded = false
                        },
                        leadingIcon = { Icon(AppIcons.Delete, contentDescription = null, tint = RouletteTheme.colors.danger) }
                    )
                }
            }
        }
    }
}
