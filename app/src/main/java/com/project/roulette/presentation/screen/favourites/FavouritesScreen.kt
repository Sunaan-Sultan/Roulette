package com.project.roulette.presentation.screen.favourites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Wheel
import com.project.roulette.presentation.model.HomeFilter
import com.project.roulette.presentation.model.HomeUiState
import com.project.roulette.presentation.viewmodel.HomeViewModel
import com.project.roulette.ui.theme.*
import com.project.roulette.presentation.screen.home.BannerAd
import com.project.roulette.presentation.screen.wheels.WheelsFilterChipItem
import com.project.roulette.util.loadSwitchInterstitial
import com.project.roulette.util.showSwitchInterstitial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    viewModel: HomeViewModel,
    onNavigateToWheel: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setFilter(HomeFilter.FAVOURITES)
        loadSwitchInterstitial(context)
    }

    Scaffold(
        bottomBar = {
            BannerAd(modifier = Modifier.fillMaxWidth())
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
            
            Text(
                text = "SAVED",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            
            Text(
                text = "Favourites",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WheelsFilterChipItem(
                    label = "All",
                    isSelected = currentFilter == HomeFilter.FAVOURITES,
                    onClick = { viewModel.setFilter(HomeFilter.FAVOURITES) }
                )
                WheelsFilterChipItem(
                    label = "Recent",
                    isSelected = currentFilter == HomeFilter.FAVOURITES_RECENT,
                    onClick = { viewModel.setFilter(HomeFilter.FAVOURITES_RECENT) }
                )
                WheelsFilterChipItem(
                    label = "Most used",
                    isSelected = currentFilter == HomeFilter.FAVOURITES_MOST_USED,
                    onClick = { viewModel.setFilter(HomeFilter.FAVOURITES_MOST_USED) }
                )
            }

            Spacer(Modifier.height(24.dp))

            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                }

                is HomeUiState.Success -> {
                    // Filter to only favorites if not already done by VM (VM does it, but let's be safe)
                    val favoriteWheels = state.wheels.filter { it.isFavorite }

                    if (favoriteWheels.isEmpty()) {
                        EmptyFavouritesState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            itemsIndexed(favoriteWheels) { index, wheel ->
                                val spinCount = state.wheelSpinCounts[wheel.id] ?: 0
                                if (index == 0) {
                                    FeaturedFavouriteCard(
                                        wheel = wheel,
                                        spinCount = spinCount,
                                        onSelect = {
                                            showSwitchInterstitial(context) {
                                                onNavigateToWheel(wheel.id)
                                            }
                                        }
                                    )
                                } else {
                                    SmallFavouriteCard(
                                        wheel = wheel,
                                        spinCount = spinCount,
                                        onSelect = {
                                            showSwitchInterstitial(context) {
                                                onNavigateToWheel(wheel.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is HomeUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedFavouriteCard(
    wheel: Wheel,
    spinCount: Int,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(32.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = wheel.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${wheel.segments.size} segments • Used $spinCount times",
                color = TextSecondary,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(
                    "Spin now",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SmallFavouriteCard(
    wheel: Wheel,
    spinCount: Int,
    onSelect: () -> Unit
) {
    val accentColors = listOf(TilePurple, TileTeal, TileCoral, TileBlue, TilePink, TileOrange)
    val accentColor = accentColors[wheel.id.hashCode().let { if (it < 0) -it else it } % accentColors.size]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(24.dp),
        color = SurfaceDark
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
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wheel.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wheel.segments.size} segments • $spinCount spins",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EmptyFavouritesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp)
            .height(140.dp)
            .border(1.dp, TextSecondary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Star a wheel to save it here",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
