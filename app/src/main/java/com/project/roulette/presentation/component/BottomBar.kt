package com.project.roulette.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.project.roulette.presentation.navigation.RouletteScreen
import com.project.roulette.ui.theme.RouletteTheme

private data class NavTab(
    val screen: RouletteScreen,
    val label: String,
    @DrawableRes val icon: Int
)

private val navTabs = listOf(
    NavTab(RouletteScreen.Home, "Home", AppIcons.Res.Home),
    NavTab(RouletteScreen.Wheels, "Wheels", AppIcons.Res.Wheel),
    NavTab(RouletteScreen.Favourites, "Favourites", AppIcons.Res.Favorite),
    NavTab(RouletteScreen.Settings, "Settings", AppIcons.Res.Settings)
)

@Composable
fun BottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    val colors = RouletteTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = if (colors.isLight) 8.dp else 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(32.dp),
            color = colors.surfaceElevated,
            border = BorderStroke(RouletteTheme.dimens.borderWidth, colors.divider)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navTabs.forEach { tab ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) colors.primary else Color.Transparent,
                        animationSpec = tween(300),
                        label = "nav_item_bg"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) colors.onPrimary else colors.textSecondary,
                        animationSpec = tween(300),
                        label = "nav_item_content"
                    )

                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(backgroundColor)
                            .clickable {
                                if (currentDestination?.route != tab.screen.route) {
                                    navController.navigate(tab.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .animateContentSize(animationSpec = tween(300)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = if (isSelected) 16.dp else 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(tab.icon),
                                contentDescription = tab.label,
                                tint = contentColor,
                                modifier = Modifier.size(RouletteTheme.dimens.iconSize)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tab.label,
                                    color = contentColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
