package com.project.roulette

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.project.roulette.presentation.navigation.RouletteNavHost
import com.project.roulette.presentation.navigation.RouletteScreen
import com.project.roulette.ui.theme.RouletteTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.project.roulette.ui.theme.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Main Activity for Roulette app.
 * Uses Hilt for dependency injection and Compose for UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MobileAds.initialize(this) {}

        setContent {
            RouletteTheme(darkTheme = true) {
                RouletteApp()
            }
        }
    }
}

@Composable
fun RouletteApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Triple(RouletteScreen.Home, "Home", Icons.Filled.Home),
        Triple(RouletteScreen.Wheels, "Wheels", Icons.Filled.Refresh),
        Triple(RouletteScreen.Favourites, "Favourites", Icons.Filled.Favorite),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val showBottomBar = when (currentDestination?.route) {
                RouletteScreen.Home.route,
                RouletteScreen.Wheels.route,
                RouletteScreen.Favourites.route,
                RouletteScreen.Profile.route -> true
                else -> false
            }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = DeepNavyBlack,
                    contentColor = Color.White
                ) {
                    items.forEach { (screen, label, icon) ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 12.sp) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryPurple,
                                selectedTextColor = PrimaryPurple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                if (screen.route != RouletteScreen.Profile.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        RouletteNavHost(navController, paddingValues)
    }
}

@Preview(showBackground = true)
@Composable
fun RouletteAppPreview() {
    RouletteTheme {
        RouletteApp()
    }
}