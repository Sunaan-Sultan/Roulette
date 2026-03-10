// RouletteApplication.kt
package com.project.roulette

import android.app.Application
import com.project.roulette.util.RemoteConfigUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RouletteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RemoteConfigUtil.init()
    }
}