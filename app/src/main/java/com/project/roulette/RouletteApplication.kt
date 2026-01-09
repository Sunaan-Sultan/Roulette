package com.project.roulette

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for dependency injection setup.
 * Hilt annotation enables dependency graph generation.
 */
@HiltAndroidApp
class RouletteApplication : Application()

