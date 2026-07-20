package com.project.roulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.project.roulette.util.BannerAd
import com.project.roulette.presentation.component.BottomBar
import com.project.roulette.presentation.component.WhatsNewDialog
import com.project.roulette.presentation.navigation.RouletteNavHost
import com.project.roulette.presentation.navigation.RouletteScreen
import com.project.roulette.ui.theme.DeepNavyBlack
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.util.AppChangelog
import com.project.roulette.util.getCurrentVersionCode
import com.project.roulette.util.isFreshInstall
import dagger.hilt.android.AndroidEntryPoint

import com.project.roulette.presentation.viewmodel.SettingsViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val paletteIndex by settingsViewModel.paletteIndex.collectAsStateWithLifecycle()

            RouletteTheme(darkTheme = true, paletteIndex = paletteIndex) {
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

    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val lastSeenChangelogVersion by settingsViewModel.lastSeenChangelogVersion.collectAsStateWithLifecycle()
    val latestChangelogEntry = AppChangelog.latest
    var showWhatsNew by remember { mutableStateOf(false) }

    LaunchedEffect(lastSeenChangelogVersion, latestChangelogEntry) {
        val latest = latestChangelogEntry ?: return@LaunchedEffect
        when {
            // Already caught up to the latest announcement.
            lastSeenChangelogVersion >= latest.versionCode -> Unit
            // Genuine fresh install: nothing to announce, just record the current version as seen.
            lastSeenChangelogVersion == 0 && isFreshInstall(context) ->
                settingsViewModel.updateLastSeenChangelogVersion(getCurrentVersionCode(context))
            // Existing user who hasn't seen this release's changelog yet.
            else -> showWhatsNew = true
        }
    }

    if (showWhatsNew && latestChangelogEntry != null) {
        WhatsNewDialog(
            entry = latestChangelogEntry,
            themeColor = RouletteTheme.colors.primary,
            onDismiss = {
                showWhatsNew = false
                settingsViewModel.updateLastSeenChangelogVersion(latestChangelogEntry.versionCode)
            }
        )
    }

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
                    BottomBar(
                        navController = navController,
                        currentDestination = currentDestination
                    )
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