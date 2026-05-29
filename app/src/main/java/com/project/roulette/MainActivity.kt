package com.project.roulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    val showBottomBar by remember(currentDestination) {
        mutableStateOf(
            when (currentDestination?.route) {
                RouletteScreen.Home.route,
                RouletteScreen.Wheels.route,
                RouletteScreen.Favourites.route,
                RouletteScreen.Settings.route -> true
                else -> false
            }
        )
    }

    val showAd by remember(currentDestination) {
        mutableStateOf(
            when (currentDestination?.route) {
                RouletteScreen.Home.route,
                RouletteScreen.Wheels.route,
                RouletteScreen.Favourites.route,
                RouletteScreen.Notifications.route -> true
                else -> false
            }
        )
    }

    val items = remember {
        listOf(
            Triple(RouletteScreen.Home, "Home", Icons.Outlined.Home),
            Triple(RouletteScreen.Wheels, "Wheels", Icons.Outlined.Refresh),
            Triple(RouletteScreen.Favourites, "Favourites", Icons.Outlined.FavoriteBorder),
            Triple(RouletteScreen.Settings, "Settings", Icons.Outlined.Settings),
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepNavyBlack,
        bottomBar = {
            // Bottom UI Layer (Ad + Nav Bar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(400)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Banner Ad
                if (showAd) {
                    BannerAd(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Floating Navigation Bar
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(32.dp),
                                    clip = false
                                ),
                            shape = RoundedCornerShape(32.dp),
                            color = Color.White.copy(alpha = 0.15f) // Transparent frosted effect
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items.forEach { (screen, label, icon) ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    
                                    val backgroundColor by animateColorAsState(
                                        targetValue = if (isSelected) PrimaryPurple else Color.Transparent,
                                        animationSpec = tween(300)
                                    )
                                    
                                    val contentColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else Color.Gray,
                                        animationSpec = tween(300)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(backgroundColor)
                                            .clickable {
                                                if (currentDestination?.route != screen.route) {
                                                    navController.navigate(screen.route) {
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
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = contentColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = label,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
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
            }
        }
    ) { paddingValues ->
        // Adjust padding to bring the FAB closer to the Ad/Nav bar
        val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
        val adjustedPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            top = paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = (paddingValues.calculateBottomPadding() - 12.dp).coerceAtLeast(0.dp)
        )

        // Main Content
        RouletteNavHost(
            navController = navController,
            paddingValues = adjustedPadding
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RouletteAppPreview() {
    RouletteTheme {
        RouletteApp()
    }
}