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
import com.project.roulette.presentation.screen.history.HistoryScreen
import com.project.roulette.presentation.screen.statistics.StatisticsScreen
import com.project.roulette.presentation.viewmodel.HomeViewModel
import com.project.roulette.presentation.viewmodel.WheelViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.platform.LocalLayoutDirection
import com.project.roulette.presentation.viewmodel.EditorViewModel
import com.project.roulette.presentation.viewmodel.HistoryViewModel
import com.project.roulette.presentation.viewmodel.StatisticsViewModel
import com.project.roulette.presentation.viewmodel.NotificationViewModel

@Composable
fun RouletteNavHost(navController: NavHostController, paddingValues: PaddingValues = PaddingValues()) {
    val layoutDirection = LocalLayoutDirection.current
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
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToWheel = { wheelId ->
                    navController.navigate(RouletteScreen.Wheel.forId(wheelId))
                },
                onNavigateToCreate = {
                    navController.navigate(RouletteScreen.CreateWheel.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(RouletteScreen.Notifications.route)
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
                    navController.navigate(RouletteScreen.CreateWheel.route)
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

        composable(RouletteScreen.CreateWheel.route) {
            val viewModel: EditorViewModel = hiltViewModel()
            EditorScreen(
                viewModel = viewModel,
                wheelId = null,
                isNew = true,
                onNavigateBack = { navController.popBackStack() },
                onPreview = { id ->
                    navController.navigate(RouletteScreen.Wheel.forId(id))
                },
                onSaved = { _ ->
                    navController.popBackStack()
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
                onPreview = { id ->
                    navController.navigate(RouletteScreen.Wheel.forId(id))
                },
                onSaved = { _ ->
                    navController.popBackStack()
                }
            )
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
            // Placeholder for Settings screen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Settings Screen", color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}
