package com.project.roulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.project.roulette.util.BannerAd
import com.project.roulette.presentation.navigation.RouletteNavHost
import com.project.roulette.presentation.navigation.RouletteScreen
import com.project.roulette.ui.theme.DeepNavyBlack
import com.project.roulette.ui.theme.PrimaryPurple
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Roulette app.
 * Uses Hilt for dependency injection and Compose for UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            // For force update, if user cancels or it fails, we check again
            checkForUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdates()

        enableEdgeToEdge()

        MobileAds.initialize(this) {}

        setContent {
            RouletteTheme(darkTheme = true) {
                RouletteApp()
            }
        }
    }

    private fun checkForUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(updateType)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
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

            Column {
                // Show Banner Ad only on Home, Wheels, Favourites, and Notifications screens
                val showAd = when (currentDestination?.route) {
                    RouletteScreen.Home.route,
                    RouletteScreen.Wheels.route,
                    RouletteScreen.Favourites.route,
                    RouletteScreen.Notifications.route -> true
                    else -> false
                }

                if (showAd) {
                    BannerAd(modifier = Modifier.fillMaxWidth())
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