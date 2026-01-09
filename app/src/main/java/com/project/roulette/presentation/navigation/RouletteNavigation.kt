package com.project.roulette.presentation.navigation

/**
 * Type-safe navigation destinations expressed as route strings.
 * Each destination exposes a route pattern and helper to create a concrete route.
 */
sealed class RouletteScreen(val route: String) {
    object Splash : RouletteScreen("splash")

    object Home : RouletteScreen("home")

    object CreateWheel : RouletteScreen("create_wheel")

    object Wheel : RouletteScreen("wheel/{wheelId}") {
        fun forId(id: String) = "wheel/$id"
    }

    object EditWheel : RouletteScreen("edit/{wheelId}") {
        fun forId(id: String) = "edit/$id"
    }

    object History : RouletteScreen("history/{wheelId}") {
        fun forId(id: String) = "history/$id"
    }

    object Statistics : RouletteScreen("statistics/{wheelId}") {
        fun forId(id: String) = "statistics/$id"
    }
}
