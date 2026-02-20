package com.project.roulette.presentation.screen.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Splash screen removed: this no-op composable immediately calls onTimeout so
 * navigation proceeds and the splash UI is effectively gone.
 */
@Composable
fun SplashScreen(
    timeoutMs: Long = 0L,
    onTimeout: () -> Unit
) {
    // Immediately move on; keep parameter for compatibility with callers.
    LaunchedEffect(Unit) {
        onTimeout()
    }
}
