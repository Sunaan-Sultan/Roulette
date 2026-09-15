package com.project.roulette.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.roulette.presentation.screen.home.HomeScreen
import com.project.roulette.presentation.screen.wheels.WheelsScreen
import com.project.roulette.presentation.screen.favourites.FavouritesScreen
import com.project.roulette.presentation.screen.notifications.NotificationScreen
import com.project.roulette.presentation.screen.wheel.WheelScreen
import com.project.roulette.presentation.screen.editor.EditorScreen
import com.project.roulette.presentation.screen.templates.TemplatesScreen
import com.project.roulette.presentation.screen.history.HistoryScreen
import com.project.roulette.presentation.screen.statistics.StatisticsScreen
import com.project.roulette.presentation.viewmodel.DashboardViewModel
import com.project.roulette.presentation.viewmodel.HomeViewModel
import com.project.roulette.presentation.viewmodel.WheelViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.navigation
import androidx.compose.runtime.remember
import com.project.roulette.presentation.viewmodel.EditorViewModel
import com.project.roulette.presentation.viewmodel.HistoryViewModel
import com.project.roulette.presentation.viewmodel.StatisticsViewModel
import com.project.roulette.presentation.viewmodel.NotificationViewModel
import com.project.roulette.presentation.viewmodel.SettingsViewModel
import com.project.roulette.presentation.screen.settings.SettingsScreen
import com.project.roulette.presentation.screen.editor.WheelPreviewScreen
import com.project.roulette.presentation.viewmodel.PreviewViewModel
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.model.Segment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.presentation.model.EditorUiState
import com.project.roulette.util.AdManager
import com.project.roulette.util.AdPlacement
import com.project.roulette.util.PreviewData
import androidx.compose.ui.platform.LocalContext

@Composable
fun RouletteNavHost(navController: NavHostController, paddingValues: PaddingValues = PaddingValues()) {
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current

    // After a wheel is saved, always land on Home with a clean back stack.
    val onWheelSaved: () -> Unit = {
        AdManager.show(context, AdPlacement.WHEEL_SAVED) {
            navController.navigate(RouletteScreen.Home.route) {
                popUpTo(RouletteScreen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = RouletteScreen.Splash.route,
        modifier = Modifier.padding(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding()
        ),
        enterTransition = {
            fadeIn(animationSpec = tween(250))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(250))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(250))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(250))
        }
    ) {
        composable(RouletteScreen.Splash.route) {
            com.project.roulette.presentation.screen.splash.SplashScreen(onTimeout = {
                navController.navigate(RouletteScreen.Home.route) {
                    popUpTo(RouletteScreen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(RouletteScreen.Home.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToWheel = { wheelId ->
                    navController.navigate(RouletteScreen.Wheel.forId(wheelId))
                },
                onNavigateToCreate = {
                    navController.navigate(RouletteScreen.Templates.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(RouletteScreen.Notifications.route)
                },
                onNavigateToHistory = { wheelId ->
                    navController.navigate(RouletteScreen.History.forId(wheelId))
                },
                onNavigateToAllWheels = {
                    navController.navigate(RouletteScreen.Wheels.route) {
                        popUpTo(RouletteScreen.Home.route)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RouletteScreen.Wheels.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            WheelsScreen(
                viewModel = viewModel,
                onNavigateToWheel = { wheelId ->
                    navController.navigate(RouletteScreen.Wheel.forId(wheelId))
                },
                onNavigateToCreate = {
                    navController.navigate(RouletteScreen.Templates.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(RouletteScreen.Notifications.route)
                }
            )
        }

        composable(RouletteScreen.Favourites.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            FavouritesScreen(
                viewModel = viewModel,
                onNavigateToWheel = { wheelId ->
                    navController.navigate(RouletteScreen.Wheel.forId(wheelId))
                }
            )
        }

        composable(RouletteScreen.Notifications.route) {
            val viewModel: NotificationViewModel = hiltViewModel()
            NotificationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(RouletteScreen.Wheel.route) { backStackEntry ->
            val wheelId = backStackEntry.arguments?.getString("wheelId") ?: return@composable
            val viewModel: WheelViewModel = hiltViewModel()

            WheelScreen(
                wheelId = wheelId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(RouletteScreen.History.forId(wheelId)) },
                onNavigateToStatistics = { navController.navigate(RouletteScreen.Statistics.forId(wheelId)) },
                onNavigateToEdit = { navController.navigate(RouletteScreen.EditWheel.forId(wheelId)) }
            )
        }

        composable(RouletteScreen.Templates.route) {
            TemplatesScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectTemplate = { templateId ->
                    navController.navigate(RouletteScreen.CreateWheel.forTemplate(templateId))
                },
                onStartFromScratch = {
                    navController.navigate(RouletteScreen.CreateWheel.blank())
                }
            )
        }

        composable(
            route = RouletteScreen.CreateWheel.route,
            arguments = listOf(navArgument("templateId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            val viewModel: EditorViewModel = hiltViewModel()
            EditorScreen(
                viewModel = viewModel,
                wheelId = null,
                isNew = true,
                templateId = templateId,
                onNavigateBack = { navController.popBackStack() },
                onPreview = { wheel ->
                    PreviewData.previewWheel = wheel
                    PreviewData.onSave = { 
                        viewModel.saveWheel()
                    }
                    navController.navigate(RouletteScreen.Preview.route)
                },
                onSaved = { _ ->
                    onWheelSaved()
                }
            )
        }

        composable(RouletteScreen.EditWheel.route) { backStackEntry ->
            val wheelId = backStackEntry.arguments?.getString("wheelId") ?: return@composable
            val viewModel: EditorViewModel = hiltViewModel()

            EditorScreen(
                viewModel = viewModel,
                wheelId = wheelId,
                isNew = false,
                onNavigateBack = { navController.popBackStack() },
                onPreview = { wheel ->
                    PreviewData.previewWheel = wheel
                    PreviewData.onSave = { 
                        viewModel.saveWheel()
                    }
                    navController.navigate(RouletteScreen.Preview.route)
                },
                onSaved = { _ ->
                    onWheelSaved()
                }
            )
        }

        composable(RouletteScreen.Preview.route) {
            val previewViewModel: PreviewViewModel = hiltViewModel()
            val wheel = PreviewData.previewWheel
            
            if (wheel != null) {
                WheelPreviewScreen(
                    wheel = wheel,
                    viewModel = previewViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSave = {
                        PreviewData.onSave?.invoke()
                        navController.popBackStack() // Go back to editor (which will then go back if saved)
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(RouletteScreen.History.route) { backStackEntry ->
            val wheelId = backStackEntry.arguments?.getString("wheelId") ?: return@composable
            val viewModel: HistoryViewModel = hiltViewModel()

            HistoryScreen(
                wheelId = wheelId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(RouletteScreen.Statistics.route) { backStackEntry ->
            val wheelId = backStackEntry.arguments?.getString("wheelId") ?: return@composable
            val viewModel: StatisticsViewModel = hiltViewModel()

            StatisticsScreen(
                wheelId = wheelId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(RouletteScreen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
